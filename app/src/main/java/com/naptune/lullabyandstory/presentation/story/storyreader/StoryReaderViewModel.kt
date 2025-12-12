package com.naptune.lullabyandstory.presentation.story.storyreader

import android.util.Log
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.domain.model.AdLoadResult
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.usecase.admob.InitializeAdMobUseCase
import com.naptune.lullabyandstory.utils.InternetConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryReaderViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val adMobDataSource: AdMobDataSource,
    private val internetConnectionManager: InternetConnectionManager,
    private val initializeAdMobUseCase: InitializeAdMobUseCase,
    // ✅ Billing - Premium status management
    private val billingManager: com.naptune.lullabyandstory.data.billing.BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryReaderUiState())
    val uiState: StateFlow<StoryReaderUiState> = _uiState.asStateFlow()

    // ✅ Add network state monitoring
    val isNetworkAvailable = internetConnectionManager.isNetworkAvailable
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // ✅ Premium status from BillingManager
    val isPurchased: StateFlow<Boolean> = billingManager.isPurchased.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true  // Default to non-premium until billing initializes
    )

    init {
        // Load saved font size on initialization
        handleIntent(StoryReaderIntent.LoadSavedFontSize)

        // ✅ Monitor network state changes for ad loading
        monitorNetworkChanges()

        // ✅ Initialize ads only for free users
        viewModelScope.launch {
            var adsInitialized = false
            billingManager.isPurchased.collect { isPremium ->
                if (!isPremium && !adsInitialized) {
                    Log.d("StoryReaderViewModel", "🎯 User is free - initializing ads")
                    initializeAds()
                    // Load banner ad if network is available
                    // ✅ Use INLINE_ADAPTIVE_BANNER for story reader (ad appears within content)
                    if (internetConnectionManager.isCurrentlyConnected()) {
                        loadBannerAd(
                            adUnitId = AdMobDataSource.TEST_INLINE_BANNER_AD_UNIT_ID,
                            adSizeType = AdSizeType.INLINE_ADAPTIVE_BANNER
                        )
                    }
                    adsInitialized = true
                } else if (isPremium) {
                    Log.d("StoryReaderViewModel", "👑 User is premium - skipping ads")
                }
            }
        }
    }

    fun handleIntent(intent: StoryReaderIntent) {
        when (intent) {
            is StoryReaderIntent.IncreaseFontSize -> increaseFontSize()
            is StoryReaderIntent.DecreaseFontSize -> decreaseFontSize()
            is StoryReaderIntent.LoadSavedFontSize -> loadSavedFontSize()
            is StoryReaderIntent.InitializeAds -> initializeAds()
            is StoryReaderIntent.LoadBannerAd -> loadBannerAd(intent.adUnitId, intent.adSizeType)
        }
    }

    private fun increaseFontSize() {
        val currentState = _uiState.value
        if (currentState.canIncrease) {
            val newFontSize = (currentState.fontSize.value + 2).sp
            updateFontSize(newFontSize)
            saveFontSize(newFontSize.value)
            Log.d("StoryReaderViewModel", "📈 Font size increased to: ${newFontSize.value}sp")
        }
    }

    private fun decreaseFontSize() {
        val currentState = _uiState.value
        if (currentState.canDecrease) {
            val newFontSize = (currentState.fontSize.value - 2).sp
            updateFontSize(newFontSize)
            saveFontSize(newFontSize.value)
            Log.d("StoryReaderViewModel", "📉 Font size decreased to: ${newFontSize.value}sp")
        }
    }

    private fun loadSavedFontSize() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val savedFontSize = appPreferences.getStoryFontSizeOnce()
                val fontSize = savedFontSize.sp
                
                _uiState.value = _uiState.value.copy(
                    fontSize = fontSize,
                    isLoading = false,
                    error = null
                )
                
                Log.d("StoryReaderViewModel", "📖 Loaded saved font size: ${fontSize.value}sp")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load font size: ${e.message}"
                )
                Log.e("StoryReaderViewModel", "❌ Error loading font size: ${e.message}")
            }
        }
    }

    private fun updateFontSize(fontSize: androidx.compose.ui.unit.TextUnit) {
        _uiState.value = _uiState.value.copy(fontSize = fontSize)
    }

    private fun saveFontSize(fontSize: Float) {
        viewModelScope.launch {
            try {
                appPreferences.saveStoryFontSize(fontSize)
                Log.d("StoryReaderViewModel", "💾 Font size saved: ${fontSize}sp")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save font size: ${e.message}"
                )
                Log.e("StoryReaderViewModel", "❌ Error saving font size: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up inline banner ad when ViewModel is destroyed
        try {
            adMobDataSource.destroyBannerAd(AdMobDataSource.TEST_INLINE_BANNER_AD_UNIT_ID)
            Log.d("StoryReaderViewModel", "🧹 ViewModel cleared - AdView destroyed")
        } catch (e: Exception) {
            Log.e("StoryReaderViewModel", "⚠️ Error during ViewModel cleanup: ${e.message}")
        }
    }
    
    // ✅ NEW: AdMob initialization for AdState compatibility
    private fun initializeAds() {
        Log.d("StoryReaderViewModel", "🎯 Initializing AdMob for StoryReader...")
        viewModelScope.launch {
            try {
                initializeAdMobUseCase()
                Log.d("StoryReaderViewModel", "✅ AdMob initialized successfully for StoryReader")

                // Update UI state to reflect initialization
                _uiState.value = _uiState.value.copy(
                    adState = _uiState.value.adState.copy(isAdInitialized = true)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "AdMob init exception: ${e.message}"
                )
                Log.e("StoryReaderViewModel", "❌ AdMob initialization failed: ${e.message}", e)
            }
        }
    }
    
    // ✅ NEW: Banner ad loading for AdState compatibility
    private fun loadBannerAd(adUnitId: String, adSizeType: AdSizeType) {
        viewModelScope.launch {
            try {
                Log.d("StoryReaderViewModel", "🔍 Loading inline banner ad for StoryReader")
                adMobDataSource.loadBannerAd(
                    adUnitId = adUnitId,
                    adSizeType = adSizeType
                ).collect { result ->
                    when (result) {
                        is AdLoadResult.Loading -> {
                            // Create loading banner ad state
                            val loadingBannerAd = com.naptune.lullabyandstory.domain.model.BannerAdDomainModel(
                                adUnitId = adUnitId,
                                adSize = com.naptune.lullabyandstory.domain.model.AdSize(
                                    width = 320,
                                    height = 100,
                                    type = adSizeType
                                ),
                                isLoaded = false,
                                isLoading = true,
                                error = null
                            )
                            _uiState.value = _uiState.value.copy(
                                adState = _uiState.value.adState.copy(
                                    bannerAd = loadingBannerAd
                                )
                            )
                            Log.d("StoryReaderViewModel", "📢 Loading inline banner ad...")
                        }
                        is AdLoadResult.Success -> {
                            // bannerAd with isLoaded = true
                            _uiState.value = _uiState.value.copy(
                                adState = _uiState.value.adState.copy(
                                    bannerAd = result.bannerAd
                                )
                            )
                            Log.d("StoryReaderViewModel", "✅ Inline banner ad loaded successfully")
                        }
                        is AdLoadResult.Error -> {
                            // Create error banner ad state
                            val errorBannerAd = com.naptune.lullabyandstory.domain.model.BannerAdDomainModel(
                                adUnitId = adUnitId,
                                adSize = com.naptune.lullabyandstory.domain.model.AdSize(
                                    width = 320,
                                    height = 100,
                                    type = adSizeType
                                ),
                                isLoaded = false,
                                isLoading = false,
                                error = result.message
                            )
                            _uiState.value = _uiState.value.copy(
                                adState = _uiState.value.adState.copy(
                                    bannerAd = errorBannerAd
                                ),
                                error = "Failed to load inline ad: ${result.message}"
                            )
                            Log.e("StoryReaderViewModel", "❌ Failed to load inline banner: ${result.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Inline ad error: ${e.message}"
                )
                Log.e("StoryReaderViewModel", "💥 Exception loading inline banner: ${e.message}")
            }
        }
    }
    
    // ✅ NEW: Monitor network state changes for ad loading
    private fun monitorNetworkChanges() {
        viewModelScope.launch {
            isNetworkAvailable.collect { isConnected ->
                Log.d("StoryReaderViewModel", "📢 Network state changed: $isConnected")
                
                if (isConnected) {
                    Log.d("StoryReaderViewModel", "🌐 Network available - checking ad state")
                    handleBannerAdNetworkState(isConnected)
                } else {
                    Log.d("StoryReaderViewModel", "❌ Network not available - clearing ad state")
                    handleBannerAdNetworkState(isConnected)
                }
            }
        }
    }
    
    // ✅ NEW: Handle banner ad loading/hiding based on network connectivity
    private fun handleBannerAdNetworkState(isConnected: Boolean) {
        Log.d("StoryReaderViewModel", "📢 Handling banner ad network state: $isConnected")
        
        if (isConnected) {
            // ✅ Network available - check if ad needs to be loaded
            val bannerAd = _uiState.value.adState.bannerAd
            if (bannerAd == null || (!bannerAd.isLoaded && !bannerAd.isLoading)) {
                Log.d("StoryReaderViewModel", "🚀 Network available - Starting banner ad load")
                loadBannerAd(
                    adUnitId = AdMobDataSource.TEST_INLINE_BANNER_AD_UNIT_ID,
                    adSizeType = AdSizeType.INLINE_ADAPTIVE_BANNER
                )
            } else {
                Log.d("StoryReaderViewModel", "✅ Banner ad already loaded or loading")
            }
        } else {
            // ✅ Network not available - clear ad state
            Log.d("StoryReaderViewModel", "❌ Network not available - Clearing banner ad state")
            _uiState.value = _uiState.value.copy(
                adState = _uiState.value.adState.copy(bannerAd = null)
            )
        }
    }
}