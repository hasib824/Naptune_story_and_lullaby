package com.naptune.lullabyandstory.presentation.story.storymanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.domain.model.BannerAdDomainModel
import com.naptune.lullabyandstory.domain.model.AdLoadResult
import com.naptune.lullabyandstory.domain.model.AdSize
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.usecase.admob.InitializeAdMobUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.LoadBannerAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.DestroyBannerAdUseCase
import com.naptune.lullabyandstory.utils.InternetConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class StoryManagerViewModel @Inject constructor(
    private val internetConnectionManager: InternetConnectionManager,
    // AdMob use cases
    private val initializeAdMobUseCase: InitializeAdMobUseCase,
    private val loadBannerAdUseCase: LoadBannerAdUseCase,
    private val destroyBannerAdUseCase: DestroyBannerAdUseCase,
    // ✅ Billing - Premium status management
    private val billingManager: com.naptune.lullabyandstory.data.billing.BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryManagerUiState())
    val uiState: StateFlow<StoryManagerUiState> = _uiState.asStateFlow()

    // ✅ Network state monitoring like other ViewModels
    val isNetworkAvailable: StateFlow<Boolean> = internetConnectionManager.isNetworkAvailable

    // ✅ Premium status from BillingManager
    val isPurchased: StateFlow<Boolean> = billingManager.isPurchased.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        true  // Default to non-premium until billing initializes
    )

    init {
        // ✅ Initialize ads only for free users
        viewModelScope.launch {
            var adsInitialized = false
            billingManager.isPurchased.collect { isPremium ->
                if (!isPremium && !adsInitialized) {
                    Log.d("StoryManagerViewModel", "🎯 User is free - initializing ads")
                    initializeAds()
                    // Load banner ad if network is available
                    if (internetConnectionManager.isCurrentlyConnected()) {
                        loadBannerAd(
                            adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                            adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
                        )
                    }
                    adsInitialized = true
                } else if (isPremium) {
                    Log.d("StoryManagerViewModel", "👑 User is premium - skipping ads")
                }
            }
        }
    }
    
    // 🔄 Call this when returning from StoryReader to refresh ad
    fun refreshBannerAd() {
        Log.d("StoryManagerViewModel", "🔄 Refreshing banner ad after navigation return")
        loadBannerAd(
            adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
            adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
        )
    }


    fun handleIntent(intent: StoryManagerIntent) {
        when (intent) {
            StoryManagerIntent.InitializeAds -> initializeAds()
            is StoryManagerIntent.LoadBannerAd -> loadBannerAd(intent.adUnitId, intent.adSizeType)
            is StoryManagerIntent.DestroyBannerAd -> destroyBannerAd(intent.adUnitId)
            is StoryManagerIntent.CheckNetworkForStoryStream -> checkNetworkForStoryStream(intent.story, intent.onSuccess)
        }
    }
    
    private fun checkNetworkForStoryStream(story: com.naptune.lullabyandstory.domain.model.StoryDomainModel, onSuccess: (com.naptune.lullabyandstory.domain.model.StoryDomainModel) -> Unit) {
        Log.d("StoryManagerViewModel", "🌐 Checking network for story streaming: ${story.storyName}")
        
        if (internetConnectionManager.checkNetworkAndShowToast()) {
            Log.d("StoryManagerViewModel", "✅ Network available - proceeding with story stream")
            onSuccess(story)
        } else {
            Log.d("StoryManagerViewModel", "❌ No internet connection - Story streaming cancelled")
        }
    }

    // AdMob functionality
    private fun initializeAds() {
        Log.d("StoryManagerViewModel", "🎯 Initializing AdMob...")
        viewModelScope.launch {
            try {
                initializeAdMobUseCase()
                Log.d("StoryManagerViewModel", "✅ AdMob initialized successfully")
                
                // ✅ Update using AdUiState pattern
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    adState = currentState.adState.copy(isAdInitialized = true)
                )
            } catch (e: Exception) {
                Log.e("StoryManagerViewModel", "❌ AdMob initialization failed: ${e.message}", e)
            }
        }
    }
    
    private fun loadBannerAd(adUnitId: String, adSizeType: AdSizeType) {
        Log.d("StoryManagerViewModel", "🎯 Loading banner ad: $adUnitId")
        viewModelScope.launch {
            try {
                loadBannerAdUseCase(adUnitId, adSizeType).collect { result ->
                    when (result) {
                        is AdLoadResult.Loading -> {
                            Log.d("StoryManagerViewModel", "⏳ Banner ad loading...")
                            val defaultHeight = when (adSizeType) {
                                AdSizeType.ANCHORED_ADAPTIVE_BANNER -> 90
                                AdSizeType.LARGE_BANNER -> 100
                                else -> 50
                            }
                            
                            val currentState = _uiState.value
                            val loadingAd = currentState.adState.bannerAd?.copy(isLoading = true, error = null)
                                ?: BannerAdDomainModel(
                                    adUnitId = adUnitId,
                                    adSize = AdSize(
                                        width = -1,
                                        height = defaultHeight,
                                        type = adSizeType
                                    ),
                                    isLoading = true
                                )
                            // ✅ Update using AdUiState pattern
                            _uiState.value = currentState.copy(
                                adState = currentState.adState.copy(bannerAd = loadingAd)
                            )
                        }
                        is AdLoadResult.Success -> {
                            Log.d("StoryManagerViewModel", "✅ Banner ad loaded successfully")
                            // ✅ Update using AdUiState pattern
                            val currentState = _uiState.value
                            _uiState.value = currentState.copy(
                                adState = currentState.adState.copy(bannerAd = result.bannerAd)
                            )
                        }
                        is AdLoadResult.Error -> {
                            Log.e("StoryManagerViewModel", "❌ Banner ad load failed: ${result.message}")
                            val defaultHeight = when (adSizeType) {
                                AdSizeType.ANCHORED_ADAPTIVE_BANNER -> 90
                                AdSizeType.LARGE_BANNER -> 100
                                else -> 50
                            }
                            
                            val currentState = _uiState.value
                            val errorAd = currentState.adState.bannerAd?.copy(
                                isLoading = false,
                                isLoaded = false,
                                error = result.message
                            ) ?: BannerAdDomainModel(
                                adUnitId = adUnitId,
                                adSize = AdSize(
                                    width = -1,
                                    height = defaultHeight,
                                    type = adSizeType
                                ),
                                isLoading = false,
                                isLoaded = false,
                                error = result.message
                            )
                            // ✅ Update using AdUiState pattern
                            _uiState.value = currentState.copy(
                                adState = currentState.adState.copy(bannerAd = errorAd)
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("StoryManagerViewModel", "❌ Banner ad loading failed: ${e.message}", e)
                
                // Update UI with error state
                val defaultHeight = when (adSizeType) {
                    AdSizeType.ANCHORED_ADAPTIVE_BANNER -> 90
                    AdSizeType.LARGE_BANNER -> 100
                    else -> 50
                }
                
                val errorAd = BannerAdDomainModel(
                    adUnitId = adUnitId,
                    adSize = AdSize(
                        width = -1,
                        height = defaultHeight,
                        type = adSizeType
                    ),
                    isLoaded = false,
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
                // ✅ Update using AdUiState pattern
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    adState = currentState.adState.copy(bannerAd = errorAd)
                )
            }
        }
    }
    
    private fun destroyBannerAd(adUnitId: String) {
        Log.d("StoryManagerViewModel", "🎯 Destroying banner ad: $adUnitId")
        viewModelScope.launch {
            try {
                destroyBannerAdUseCase(adUnitId)
                Log.d("StoryManagerViewModel", "✅ Banner ad destroyed successfully")
                
                // ✅ Clear ad using AdUiState pattern
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    adState = currentState.adState.copy(bannerAd = null)
                )
            } catch (e: Exception) {
                Log.e("StoryManagerViewModel", "❌ Banner ad destruction failed: ${e.message}", e)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        Log.d("StoryManagerViewModel", "🧹 ViewModel cleared - cleaning up AdMob resources")

        // ✅ Destroy any active banner ads using AdUiState pattern
        _uiState.value.adState.bannerAd?.let { bannerAd ->
            // ✅ Use GlobalScope for cleanup that must complete (viewModelScope is cancelled in onCleared)
            GlobalScope.launch(Dispatchers.Main.immediate) {
                try {
                    destroyBannerAdUseCase(bannerAd.adUnitId)
                } catch (e: Exception) {
                    Log.e("StoryManagerViewModel", "❌ Error during cleanup: ${e.message}")
                }
            }
        }
    }
}