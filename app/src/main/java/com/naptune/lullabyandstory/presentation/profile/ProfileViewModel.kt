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
import com.naptune.lullabyandstory.data.manager.AdManager
import com.naptune.lullabyandstory.domain.model.AdSizeType
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
 * ViewModel for Profile Screen.
 * REFACTORED: Now follows Single Responsibility Principle (SRP).
 * Ad management logic delegated to unified AdManager.
 *
 * Responsibilities:
 * - Profile settings management (share, feedback, rate)
 * - Language selection
 * - Premium status display
 *
 * Ad management delegated to: AdManager (shared across all ViewModels)
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val shareAppUseCase: ShareAppUseCase,
    private val sendFeedbackUseCase: SendFeedbackUseCase,
    private val rateAppUseCase: RateAppUseCase,
    private val languageManager: LanguageManager,
    // ✅ SRP FIX: Single unified ad manager instead of 3 ad use cases
    private val adManager: AdManager,
    val internetConnectionManager: InternetConnectionManager,
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
        _currentLanguage,
        adManager.adState  // ✅ Add ad state from manager
    ) { baseState, isPremium, language, adState ->
        Log.d("ProfileViewModel", "🔄 State combine - isPremium: $isPremium, language: ${language.code}")
        baseState.copy(
            isPremiumUser = isPremium,
            currentLanguage = language,
            adState = adState  // ✅ Sync ad state from manager
        )
    }.onStart {
        // ✅ SRP FIX: Delegate ad initialization to manager
        adManager.initializeAds()
        adManager.loadBannerAd(
            adUnitId = com.naptune.lullabyandstory.data.network.admob.AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
            adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER,
            placement = "profile_screen"
        )
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

        // ✅ Ad initialization moved to state combine onStart block
        // This ensures ads are loaded when the state flow is collected
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
        adManager.initializeAds()
    }

    /**
     * Load banner ad
     */
    private fun loadBannerAd(adUnitId: String, adSizeType: com.naptune.lullabyandstory.domain.model.AdSizeType) {
        adManager.loadBannerAd(adUnitId, adSizeType, placement = "profile_screen")
    }

    /**
     * Destroy banner ad when leaving screen
     */
    private fun destroyBannerAd(adUnitId: String) {
        adManager.destroyBannerAd(adUnitId)
    }

    /**
     * ✅ MEMORY FIX: Clean up ads when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        // ⚠️ IMPORTANT: Do NOT destroy banner ad here!
        // Banner ads use shared adUnitId stored in AdMobDataSource singleton.
        // Destroying here would also destroy other screens' banner ads,
        // causing them to vanish when user navigates between screens.
        // The ad will be recreated when user navigates to this screen again.
        Log.d("ProfileViewModel", "🧹 ViewModel cleared - Banner ad preserved (shared across screens)")
    }
}