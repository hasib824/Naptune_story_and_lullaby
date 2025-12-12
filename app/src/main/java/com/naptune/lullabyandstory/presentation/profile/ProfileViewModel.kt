package com.naptune.lullabyandstory.presentation.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.data.billing.BillingManager
import com.naptune.lullabyandstory.domain.usecase.RateAppUseCase
import com.naptune.lullabyandstory.domain.usecase.SendFeedbackUseCase
import com.naptune.lullabyandstory.domain.usecase.ShareAppUseCase
import com.naptune.lullabyandstory.utils.LanguageManager
import com.naptune.lullabyandstory.data.model.Language
import com.naptune.lullabyandstory.data.model.getSupportedLanguages
import com.naptune.lullabyandstory.domain.model.AdLoadResult
import com.naptune.lullabyandstory.domain.model.BannerAdDomainModel
import com.naptune.lullabyandstory.domain.usecase.admob.InitializeAdMobUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.LoadBannerAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.DestroyBannerAdUseCase
import com.naptune.lullabyandstory.utils.InternetConnectionManager
import com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Profile Screen following MVI pattern
 * Handles all business logic and state management for profile operations
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val shareAppUseCase: ShareAppUseCase,
    private val sendFeedbackUseCase: SendFeedbackUseCase,
    private val rateAppUseCase: RateAppUseCase,
    private val languageManager: LanguageManager,
    // ✅ NEW: AdMob use cases
    private val initializeAdMobUseCase: InitializeAdMobUseCase,
    private val loadBannerAdUseCase: LoadBannerAdUseCase,
    private val destroyBannerAdUseCase: DestroyBannerAdUseCase,
    // ✅ NEW: Internet connection manager
    val internetConnectionManager: InternetConnectionManager,
    // ✅ Analytics
    private val analyticsHelper: AnalyticsHelper,

    private val billingManager: BillingManager
) : ViewModel() {

    // Private mutable state
    private val _uiState = MutableStateFlow(ProfileUiState())

    // ✅ MVI FIX: Combine base state + billing status + language into single state (single source of truth)
    private val _currentLanguage = MutableStateFlow(
        Language("en", "English", "English", "🇺🇸", "https://flagcdn.com/w80/us.png")
    )

    val uiState: StateFlow<ProfileUiState> = combine(
        _uiState,
        billingManager.isPurchased,
        _currentLanguage
    ) { baseState, isPremium, language ->
        Log.d("ProfileViewModel", "🔄 State combine - isPremium: $isPremium, language: ${language.code}")
        baseState.copy(
            isPremiumUser = isPremium,
            currentLanguage = language
        )
    }.onStart {

    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ProfileUiState()
    )

    // ❌ REMOVED: Separate isPurchased StateFlow (violates MVI single source of truth)
    // ❌ REMOVED: Separate currentLanguage StateFlow (violates MVI single source of truth)
    // ✅ MVI FIX: Use uiState.isPremiumUser and uiState.currentLanguage instead

    init {
        // ✅ Track screen view
        trackScreenView()
        loadCurrentLanguage()

        // ✅ Initialize ads only for free users (reactive to premium status changes)
        viewModelScope.launch {
            var adsInitialized = false
            billingManager.isPurchased.collect { isPremium ->
                if (!isPremium && !adsInitialized) {
                    Log.d("ProfileViewModel", "🎯 User is free - initializing ads")
                    initializeAds()
                    loadBannerAd(
                        adUnitId = com.naptune.lullabyandstory.data.network.admob.AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                        adSizeType = com.naptune.lullabyandstory.domain.model.AdSizeType.ANCHORED_ADAPTIVE_BANNER
                    )
                    adsInitialized = true
                } else if (isPremium) {
                    Log.d("ProfileViewModel", "👑 User is premium - skipping ads")
                }
            }
        }
    }

    /**
     * ✅ Track screen view
     */
    private fun trackScreenView() {
        analyticsHelper.logScreenView(
            screenName = "Profile",
            screenClass = "ProfileScreen"
        )
    }

    /**
     * Handle user intents/actions from UI
     * @param intent The user action to be processed
     * @param context Android context needed for launching intents
     */
    fun handleIntent(intent: ProfileIntent, context: Context? = null) {
        when (intent) {
            is ProfileIntent.ShareApp -> {
                context?.let { shareApp(it) }
            }
            is ProfileIntent.SendFeedback -> {
                context?.let { sendFeedback(it) }
            }
            is ProfileIntent.RateApp -> {
                context?.let { rateApp(it) }
            }
            is ProfileIntent.ClearMessage -> {
                clearMessages()
            }
            // ✅ NEW: AdMob intents
            is ProfileIntent.InitializeAds -> {
                initializeAds()
            }
            is ProfileIntent.LoadBannerAd -> {
                loadBannerAd(intent.adUnitId, intent.adSizeType)
            }
            is ProfileIntent.DestroyBannerAd -> {
                destroyBannerAd(intent.adUnitId)
            }
        }
    }

    /**
     * Execute share app operation
     */
    private fun shareApp(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            
            shareAppUseCase(context).fold(
                onSuccess = { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = message
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to share app"
                    )
                }
            )
        }
    }

    /**
     * Execute send feedback operation
     */
    private fun sendFeedback(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            
            sendFeedbackUseCase(context).fold(
                onSuccess = { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = message
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to send feedback"
                    )
                }
            )
        }
    }

    /**
     * Execute rate app operation
     */
    private fun rateApp(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            
            rateAppUseCase(context).fold(
                onSuccess = { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = message
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to rate app"
                    )
                }
            )
        }
    }

    /**
     * Clear any error or success messages
     */
    private fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }

    /**
     * Load current language from preferences
     */
    private fun loadCurrentLanguage() {
        viewModelScope.launch {
            val languageCode = languageManager.getLanguage()
            val currentLang = getSupportedLanguages().find { it.code == languageCode }
                ?: Language("en", "English", "English", "🇺🇸", "https://flagcdn.com/w80/us.png")
            _currentLanguage.value = currentLang
        }
    }

    /**
     * Change app language
     */
    fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                // Mark as manual change to override automatic detection
                languageManager.setLanguage(languageCode, isManualChange = true)
                loadCurrentLanguage() // Refresh current language

                // Set success message
                _uiState.value = _uiState.value.copy(
                    successMessage = "Language changed to ${getSupportedLanguages().find { it.code == languageCode }?.nativeName ?: languageCode}. Please restart the app."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to change language: ${e.message}"
                )
            }
        }
    }

    // For testing: Force detect device language
    fun forceDetectDeviceLanguage() {
        viewModelScope.launch {
            try {
                val detectedLanguage = languageManager.forceDeviceLanguageDetection()
                loadCurrentLanguage() // Refresh current language
                _uiState.value = _uiState.value.copy(
                    successMessage = "Device language detected: ${getSupportedLanguages().find { it.code == detectedLanguage }?.nativeName ?: detectedLanguage}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to detect device language: ${e.message}"
                )
            }
        }
    }

    // ✅ NEW: AdMob Functions

    /**
     * Initialize AdMob SDK
     */
    private fun initializeAds() {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "🎯 Initializing AdMob...")
                initializeAdMobUseCase()
                _uiState.value = _uiState.value.copy(
                    adState = _uiState.value.adState.copy(isAdInitialized = true)
                )
                Log.d("ProfileViewModel", "✅ AdMob initialized successfully")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "❌ AdMob initialization failed: ${e.message}", e)
            }
        }
    }

    /**
     * Load banner ad
     */
    private fun loadBannerAd(adUnitId: String, adSizeType: com.naptune.lullabyandstory.domain.model.AdSizeType) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "📢 Loading banner ad: $adUnitId")
                loadBannerAdUseCase(adUnitId, adSizeType).collect { result ->
                    when (result) {
                        is AdLoadResult.Loading -> {
                            Log.d("ProfileViewModel", "⏳ Banner ad loading...")
                            val defaultHeight = when (adSizeType) {
                                com.naptune.lullabyandstory.domain.model.AdSizeType.ANCHORED_ADAPTIVE_BANNER -> 90
                                com.naptune.lullabyandstory.domain.model.AdSizeType.LARGE_BANNER -> 100
                                else -> 50
                            }

                            val currentState = _uiState.value
                            val loadingAd = currentState.adState.bannerAd?.copy(isLoading = true, error = null)
                                ?: BannerAdDomainModel(
                                    adUnitId = adUnitId,
                                    adSize = com.naptune.lullabyandstory.domain.model.AdSize(
                                        width = -1,
                                        height = defaultHeight,
                                        type = adSizeType
                                    ),
                                    isLoading = true
                                )
                            _uiState.value = currentState.copy(
                                adState = currentState.adState.copy(bannerAd = loadingAd)
                            )
                        }
                        is AdLoadResult.Success -> {
                            Log.d("ProfileViewModel", "✅ Banner ad loaded successfully")
                            _uiState.value = _uiState.value.copy(
                                adState = _uiState.value.adState.copy(
                                    bannerAd = result.bannerAd
                                )
                            )
                        }
                        is AdLoadResult.Error -> {
                            Log.e("ProfileViewModel", "❌ Banner ad load failed: ${result.message}")
                            val defaultHeight = when (adSizeType) {
                                com.naptune.lullabyandstory.domain.model.AdSizeType.ANCHORED_ADAPTIVE_BANNER -> 90
                                com.naptune.lullabyandstory.domain.model.AdSizeType.LARGE_BANNER -> 100
                                else -> 50
                            }

                            val currentState = _uiState.value
                            val errorAd = currentState.adState.bannerAd?.copy(
                                isLoading = false,
                                isLoaded = false,
                                error = result.message
                            ) ?: BannerAdDomainModel(
                                adUnitId = adUnitId,
                                adSize = com.naptune.lullabyandstory.domain.model.AdSize(
                                    width = -1,
                                    height = defaultHeight,
                                    type = adSizeType
                                ),
                                isLoading = false,
                                isLoaded = false,
                                error = result.message
                            )
                            _uiState.value = currentState.copy(
                                adState = currentState.adState.copy(bannerAd = errorAd)
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "❌ Exception loading banner ad: ${e.message}", e)
            }
        }
    }

    /**
     * Destroy banner ad when leaving screen
     */
    private fun destroyBannerAd(adUnitId: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "🗑️ Destroying banner ad: $adUnitId")
                destroyBannerAdUseCase(adUnitId)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "❌ Error destroying banner ad: ${e.message}", e)
            }
        }
    }

    /**
     * ✅ MEMORY FIX: Clean up ads when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        Log.d("ProfileViewModel", "🧹 Cleaning up ProfileViewModel - destroying ads")
        try {
            // Destroy all ads to prevent memory leaks
            destroyBannerAd(com.naptune.lullabyandstory.data.network.admob.AdMobDataSource.TEST_BANNER_AD_UNIT_ID)
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "❌ Error in onCleared: ${e.message}", e)
        }
    }
}