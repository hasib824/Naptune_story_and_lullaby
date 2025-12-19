package com.naptune.lullabyandstory.data.manager

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.naptune.lullabyandstory.data.billing.BillingManager
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.domain.model.AdLoadResult
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.model.ContentInfo
import com.naptune.lullabyandstory.domain.model.RewardedAdLoadResult
import com.naptune.lullabyandstory.domain.model.RewardedAdShowResult
import com.naptune.lullabyandstory.domain.usecase.admob.CheckRewardedAdAvailabilityUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.DestroyBannerAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.InitializeAdMobUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.LoadBannerAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.LoadRewardedAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.ShowRewardedAdUseCase
import com.naptune.lullabyandstory.presentation.main.AdUiState
import com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified AdManager for handling all AdMob-related operations across the entire app.
 * Follows SOLID principles - Single Responsibility, Open/Closed, and Dependency Inversion.
 *
 * This manager consolidates ad logic from multiple ViewModels into a single, reusable component.
 * Works with any content type (lullaby, story, etc.) through the ContentInfo abstraction.
 *
 * @see ContentInfo for content type abstraction
 */
@Singleton
class AdManager @Inject constructor(
    private val initializeAdMobUseCase: InitializeAdMobUseCase,
    private val loadBannerAdUseCase: LoadBannerAdUseCase,
    private val destroyBannerAdUseCase: DestroyBannerAdUseCase,
    private val loadRewardedAdUseCase: LoadRewardedAdUseCase,
    private val showRewardedAdUseCase: ShowRewardedAdUseCase,
    private val checkRewardedAdAvailabilityUseCase: CheckRewardedAdAvailabilityUseCase,
    private val sessionUnlockManager: SessionUnlockManager,
    private val billingManager: BillingManager,
    private val analyticsHelper: AnalyticsHelper
) {

    // ✅ Use SupervisorJob to prevent cancellation of entire scope if one coroutine fails
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _adState = MutableStateFlow(AdUiState())
    val adState: StateFlow<AdUiState> = _adState.asStateFlow()

    private val _isLoadingRewardedAd = MutableStateFlow(false)
    val isLoadingRewardedAd: StateFlow<Boolean> = _isLoadingRewardedAd.asStateFlow()

    private val _rewardedAdError = MutableStateFlow<String?>(null)
    val rewardedAdError: StateFlow<String?> = _rewardedAdError.asStateFlow()

    // =====================================================
    // AD INITIALIZATION
    // =====================================================

    fun initializeAds() {
        Log.d(TAG, "🚀 Initializing AdMob SDK...")
        managerScope.launch {
            try {
                initializeAdMobUseCase()
                _adState.value = _adState.value.copy(isAdInitialized = true)
                Log.d(TAG, "✅ AdMob initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ AdMob initialization failed: ${e.message}", e)
            }
        }
    }

    // =====================================================
    // BANNER AD OPERATIONS
    // =====================================================

    /**
     * Load a banner ad for the specified placement.
     * Automatically skips loading for premium users.
     *
     * @param adUnitId AdMob banner ad unit ID
     * @param adSizeType Banner size type (e.g., ANCHORED_ADAPTIVE_BANNER)
     * @param placement Screen/placement identifier for analytics (e.g., "lullaby_screen")
     */
    fun loadBannerAd(
        adUnitId: String,
        adSizeType: AdSizeType,
        placement: String = "unknown"
    ) {
        // 🏆 Skip ad loading for premium users
        if (billingManager.isPurchased.value) {
            Log.d(TAG, "🏆 Premium user - Skipping banner ad load")
            return
        }

        Log.d(TAG, "📢 Loading banner ad - Unit: $adUnitId, Size: $adSizeType, Placement: $placement")
        managerScope.launch {
            try {
                loadBannerAdUseCase(adUnitId, adSizeType).collect { result ->
                    Log.d(TAG, "📢 Banner ad result: $result")

                    when (result) {
                        is AdLoadResult.Loading -> {
                            Log.d(TAG, "⏳ Banner ad loading...")
                            // Loading state is handled by the bannerAd object itself
                        }

                        is AdLoadResult.Success -> {
                            _adState.value = _adState.value.copy(
                                bannerAd = result.bannerAd
                            )
                            Log.d(TAG, "✅ Banner ad loaded successfully")

                            // ✅ Track ad viewed
                            analyticsHelper.logAdViewed(
                                adUnitId = adUnitId,
                                adType = "banner",
                                placement = placement
                            )
                        }

                        is AdLoadResult.Error -> {
                            // Error is stored in the bannerAd object
                            Log.e(TAG, "❌ Banner ad failed: ${result.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Banner ad loading exception: ${e.message}", e)
            }
        }
    }

    /**
     * Destroy a banner ad to free resources.
     *
     * @param adUnitId AdMob banner ad unit ID to destroy
     */
    fun destroyBannerAd(adUnitId: String) {
        Log.d(TAG, "🗑️ Destroying banner ad - Unit: $adUnitId")
        managerScope.launch {
            try {
                destroyBannerAdUseCase(adUnitId)
                _adState.value = _adState.value.copy(bannerAd = null)
                Log.d(TAG, "✅ Banner ad destroyed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Banner ad destruction failed: ${e.message}", e)
            }
        }
    }

    // =====================================================
    // REWARDED AD OPERATIONS
    // =====================================================

    /**
     * Load a rewarded ad.
     * Automatically skips loading for premium users.
     *
     * @param adUnitId AdMob rewarded ad unit ID
     */
    fun loadRewardedAd(adUnitId: String) {
        // 🏆 Skip ad loading for premium users
        if (billingManager.isPurchased.value) {
            Log.d(TAG, "🏆 Premium user - Skipping rewarded ad load")
            return
        }

        Log.d(TAG, "🎁 Loading rewarded ad: $adUnitId")
        managerScope.launch {
            _isLoadingRewardedAd.value = true
            _rewardedAdError.value = null

            try {
                loadRewardedAdUseCase(adUnitId).collect { result ->
                    Log.d(TAG, "🎁 Rewarded ad result: $result")

                    when (result) {
                        is RewardedAdLoadResult.Loading -> {
                            Log.d(TAG, "⏳ Rewarded ad loading...")
                        }

                        is RewardedAdLoadResult.Success -> {
                            _isLoadingRewardedAd.value = false
                            _adState.value = _adState.value.copy(
                                rewardedAd = result.rewardedAd,
                                isLoadingRewardedAd = false,
                                rewardedAdError = null
                            )
                            Log.d(TAG, "✅ Rewarded ad loaded successfully")
                        }

                        is RewardedAdLoadResult.Error -> {
                            _isLoadingRewardedAd.value = false
                            _rewardedAdError.value = result.message
                            _adState.value = _adState.value.copy(
                                isLoadingRewardedAd = false,
                                rewardedAdError = result.message
                            )
                            Log.e(TAG, "❌ Rewarded ad failed: ${result.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Rewarded ad loading exception: ${e.message}", e)
                _isLoadingRewardedAd.value = false
                _rewardedAdError.value = e.message ?: "Unknown error"
            }
        }
    }

    /**
     * Show a rewarded ad and unlock content upon completion.
     * Works with any content type (lullaby, story, etc.) through ContentInfo abstraction.
     *
     * @param adUnitId AdMob rewarded ad unit ID
     * @param activity Activity context for showing the ad
     * @param content Content information (type, id, name, isFree)
     * @param sourceScreen Screen identifier for analytics (e.g., "lullaby_screen")
     * @param onAdWatched Callback invoked when user successfully watches the ad
     */
    fun showRewardedAd(
        adUnitId: String,
        activity: Activity,
        content: ContentInfo,
        sourceScreen: String = "unknown",
        onAdWatched: () -> Unit = {}
    ) {
        Log.d(TAG, "🎬 Showing rewarded ad: $adUnitId for ${content.type}: ${content.name}")

        // ✅ Check if ad is available
        if (!checkRewardedAdAvailabilityUseCase(adUnitId)) {
            Log.w(TAG, "❌ Rewarded ad not available, loading new ad...")
            loadRewardedAd(adUnitId)
            Toast.makeText(activity, "Loading ad, please try again in a moment", Toast.LENGTH_SHORT).show()
            return
        }

        managerScope.launch {
            try {
                // ✅ Track ad request
                analyticsHelper.logRewardedAdRequested(
                    contentType = content.type,
                    contentId = content.id,
                    contentName = content.name,
                    isPremium = !content.isFree,
                    sourceScreen = sourceScreen,
                    adUnitId = adUnitId
                )

                showRewardedAdUseCase(adUnitId, activity).collect { result ->
                    Log.d(TAG, "🎬 Rewarded ad show result: $result")

                    when (result) {
                        is RewardedAdShowResult.Loading -> {
                            Log.d(TAG, "⏳ Rewarded ad is loading...")
                        }

                        is RewardedAdShowResult.Success -> {
                            Log.d(TAG, "🎉 User earned reward: ${result.reward.amount} ${result.reward.type}")

                            // ✅ Track ad started
                            analyticsHelper.logRewardedAdStarted(
                                adUnitId = adUnitId,
                                contentId = content.id
                            )

                            // ✅ Unlock the content for this session
                            val unlockType = when (content.type) {
                                "lullaby" -> UnlockType.Lullaby
                                "story" -> UnlockType.Story
                                else -> UnlockType.Lullaby // Default fallback
                            }
                            sessionUnlockManager.unlockItem(content.id, unlockType)

                            // ✅ Track ad completion
                            analyticsHelper.logRewardedAdCompleted(
                                adUnitId = adUnitId,
                                contentType = content.type,
                                contentId = content.id,
                                contentName = content.name,
                                rewardAmount = result.reward.amount,
                                rewardType = result.reward.type,
                                watchDurationSeconds = 30 // Approximate duration
                            )

                            // ✅ Track content unlock
                            val category = if (content.isFree) {
                                "free_${content.type}"
                            } else {
                                "premium_${content.type}"
                            }
                            analyticsHelper.logContentUnlockedViaAd(
                                contentType = content.type,
                                contentId = content.id,
                                contentName = content.name,
                                category = category,
                                sessionUnlockCount = 1,
                                timeSpentBeforeUnlock = 0L
                            )

                            Toast.makeText(
                                activity,
                                "${content.type.capitalize()} unlocked for this session!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // ✅ Notify callback
                            onAdWatched()

                            // ✅ Preload next rewarded ad
                            loadRewardedAd(AdMobDataSource.TEST_REWARDED_AD_UNIT_ID)
                        }

                        is RewardedAdShowResult.Dismissed -> {
                            Log.d(TAG, "👋 Rewarded ad dismissed: ${result.reason}")

                            // ✅ Track early closure
                            analyticsHelper.logRewardedAdClosedEarly(
                                adUnitId = adUnitId,
                                contentId = content.id,
                                watchedSeconds = 0,
                                requiredSeconds = 30
                            )
                        }

                        is RewardedAdShowResult.Error -> {
                            Log.e(TAG, "❌ Rewarded ad failed to show: ${result.message}")

                            // ✅ Track load failure
                            analyticsHelper.logRewardedAdLoadFailed(
                                adUnitId = adUnitId,
                                errorCode = "SHOW_FAILED",
                                errorMessage = result.message,
                                networkType = "unknown"
                            )

                            Toast.makeText(
                                activity,
                                "Failed to show ad, please try again",
                                Toast.LENGTH_SHORT
                            ).show()

                            // ✅ Try to reload ad
                            loadRewardedAd(adUnitId)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Rewarded ad show exception: ${e.message}", e)
                Toast.makeText(activity, "Error showing ad", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // =====================================================
    // AD AVAILABILITY CHECK
    // =====================================================

    /**
     * Check if a rewarded ad is currently available to show.
     *
     * @param adUnitId AdMob rewarded ad unit ID
     * @return true if ad is loaded and ready to show
     */
    fun isRewardedAdAvailable(adUnitId: String): Boolean {
        return checkRewardedAdAvailabilityUseCase(adUnitId)
    }

    companion object {
        private const val TAG = "AdManager"
    }
}
