package com.naptune.lullabyandstory.presentation.lullaby

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.naptune.lullabyandstory.data.billing.BillingManager
import com.naptune.lullabyandstory.data.manager.SessionUnlockManager
import com.naptune.lullabyandstory.data.manager.UnlockType
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.domain.model.AdLoadResult
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
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
 * Manager for handling all AdMob-related operations for Lullaby screen.
 * Follows Single Responsibility Principle (SRP) - handles only ad management logic.
 * Extracted from LullabyViewModel to improve separation of concerns.
 */
@Singleton
class LullabyAdManager @Inject constructor(
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
        Log.d("LullabyAdManager", "🚀 Initializing AdMob SDK...")
        managerScope.launch {
            try {
                initializeAdMobUseCase()
                _adState.value = _adState.value.copy(isAdInitialized = true)
                Log.d("LullabyAdManager", "✅ AdMob initialized successfully")
            } catch (e: Exception) {
                Log.e("LullabyAdManager", "❌ AdMob initialization failed: ${e.message}", e)
            }
        }
    }

    // =====================================================
    // BANNER AD OPERATIONS
    // =====================================================

    fun loadBannerAd(adUnitId: String, adSizeType: AdSizeType) {
        // 🏆 Skip ad loading for premium users
        if (billingManager.isPurchased.value) {
            Log.d("LullabyAdManager", "🏆 Premium user - Skipping banner ad load")
            return
        }

        Log.d("LullabyAdManager", "📢 Loading banner ad - Unit: $adUnitId, Size: $adSizeType")
        managerScope.launch {
            try {
                loadBannerAdUseCase(adUnitId, adSizeType).collect { result ->
                    Log.d("LullabyAdManager", "📢 Banner ad result: $result")

                    when (result) {
                        is AdLoadResult.Loading -> {
                            Log.d("LullabyAdManager", "⏳ Banner ad loading...")
                            // Loading state is handled by the bannerAd object itself
                        }

                        is AdLoadResult.Success -> {
                            _adState.value = _adState.value.copy(
                                bannerAd = result.bannerAd
                            )
                            Log.d("LullabyAdManager", "✅ Banner ad loaded successfully")

                            // ✅ Track ad viewed
                            analyticsHelper.logAdViewed(
                                adUnitId = adUnitId,
                                adType = "banner",
                                placement = "lullaby_screen"
                            )
                        }

                        is AdLoadResult.Error -> {
                            // Error is stored in the bannerAd object
                            Log.e("LullabyAdManager", "❌ Banner ad failed: ${result.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LullabyAdManager", "💥 Banner ad loading exception: ${e.message}", e)
            }
        }
    }

    fun destroyBannerAd(adUnitId: String) {
        Log.d("LullabyAdManager", "🗑️ Destroying banner ad - Unit: $adUnitId")
        managerScope.launch {
            try {
                destroyBannerAdUseCase(adUnitId)
                _adState.value = _adState.value.copy(bannerAd = null)
                Log.d("LullabyAdManager", "✅ Banner ad destroyed successfully")
            } catch (e: Exception) {
                Log.e("LullabyAdManager", "❌ Banner ad destruction failed: ${e.message}", e)
            }
        }
    }

    // =====================================================
    // REWARDED AD OPERATIONS
    // =====================================================

    fun loadRewardedAd(adUnitId: String) {
        // 🏆 Skip ad loading for premium users
        if (billingManager.isPurchased.value) {
            Log.d("LullabyAdManager", "🏆 Premium user - Skipping rewarded ad load")
            return
        }

        Log.d("LullabyAdManager", "🎁 Loading rewarded ad: $adUnitId")
        managerScope.launch {
            _isLoadingRewardedAd.value = true
            _rewardedAdError.value = null

            try {
                loadRewardedAdUseCase(adUnitId).collect { result ->
                    Log.d("LullabyAdManager", "🎁 Rewarded ad result: $result")

                    when (result) {
                        is RewardedAdLoadResult.Loading -> {
                            Log.d("LullabyAdManager", "⏳ Rewarded ad loading...")
                        }

                        is RewardedAdLoadResult.Success -> {
                            _isLoadingRewardedAd.value = false
                            _adState.value = _adState.value.copy(
                                rewardedAd = result.rewardedAd,
                                isLoadingRewardedAd = false,
                                rewardedAdError = null
                            )
                            Log.d("LullabyAdManager", "✅ Rewarded ad loaded successfully")
                        }

                        is RewardedAdLoadResult.Error -> {
                            _isLoadingRewardedAd.value = false
                            _rewardedAdError.value = result.message
                            _adState.value = _adState.value.copy(
                                isLoadingRewardedAd = false,
                                rewardedAdError = result.message
                            )
                            Log.e("LullabyAdManager", "❌ Rewarded ad failed: ${result.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LullabyAdManager", "💥 Rewarded ad loading exception: ${e.message}", e)
                _isLoadingRewardedAd.value = false
                _rewardedAdError.value = e.message ?: "Unknown error"
            }
        }
    }

    fun showRewardedAd(
        adUnitId: String,
        activity: Activity,
        lullaby: LullabyDomainModel,
        onAdWatched: () -> Unit = {}
    ) {
        Log.d("LullabyAdManager", "🎬 Showing rewarded ad: $adUnitId for lullaby: ${lullaby.musicName}")

        // ✅ Check if ad is available
        if (!checkRewardedAdAvailabilityUseCase(adUnitId)) {
            Log.w("LullabyAdManager", "❌ Rewarded ad not available, loading new ad...")
            loadRewardedAd(adUnitId)
            Toast.makeText(activity, "Loading ad, please try again in a moment", Toast.LENGTH_SHORT).show()
            return
        }

        managerScope.launch {
            try {
                // ✅ Track ad request
                analyticsHelper.logRewardedAdRequested(
                    contentType = "lullaby",
                    contentId = lullaby.documentId,
                    contentName = lullaby.musicName,
                    isPremium = !lullaby.isFree,
                    sourceScreen = "lullaby_screen",
                    adUnitId = adUnitId
                )

                showRewardedAdUseCase(adUnitId, activity).collect { result ->
                    Log.d("LullabyAdManager", "🎬 Rewarded ad show result: $result")

                    when (result) {
                        is RewardedAdShowResult.Loading -> {
                            Log.d("LullabyAdManager", "⏳ Rewarded ad is loading...")
                        }

                        is RewardedAdShowResult.Success -> {
                            Log.d("LullabyAdManager", "🎉 User earned reward: ${result.reward.amount} ${result.reward.type}")

                            // ✅ Track ad started
                            analyticsHelper.logRewardedAdStarted(
                                adUnitId = adUnitId,
                                contentId = lullaby.documentId
                            )

                            // ✅ Unlock the lullaby for this session
                            sessionUnlockManager.unlockItem(lullaby.documentId, UnlockType.Lullaby)

                            // ✅ Track ad completion
                            analyticsHelper.logRewardedAdCompleted(
                                adUnitId = adUnitId,
                                contentType = "lullaby",
                                contentId = lullaby.documentId,
                                contentName = lullaby.musicName,
                                rewardAmount = result.reward.amount,
                                rewardType = result.reward.type,
                                watchDurationSeconds = 30 // Approximate duration
                            )

                            // ✅ Track content unlock
                            analyticsHelper.logContentUnlockedViaAd(
                                contentType = "lullaby",
                                contentId = lullaby.documentId,
                                contentName = lullaby.musicName,
                                category = if (lullaby.isFree) "free_lullaby" else "premium_lullaby",
                                sessionUnlockCount = 1,
                                timeSpentBeforeUnlock = 0L
                            )

                            Toast.makeText(
                                activity,
                                "Lullaby unlocked for this session!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // ✅ Notify callback
                            onAdWatched()

                            // ✅ Preload next rewarded ad
                            loadRewardedAd(AdMobDataSource.TEST_REWARDED_AD_UNIT_ID)
                        }

                        is RewardedAdShowResult.Dismissed -> {
                            Log.d("LullabyAdManager", "👋 Rewarded ad dismissed: ${result.reason}")

                            // ✅ Track early closure
                            analyticsHelper.logRewardedAdClosedEarly(
                                adUnitId = adUnitId,
                                contentId = lullaby.documentId,
                                watchedSeconds = 0,
                                requiredSeconds = 30
                            )
                        }

                        is RewardedAdShowResult.Error -> {
                            Log.e("LullabyAdManager", "❌ Rewarded ad failed to show: ${result.message}")

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
                Log.e("LullabyAdManager", "💥 Rewarded ad show exception: ${e.message}", e)
                Toast.makeText(activity, "Error showing ad", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // =====================================================
    // AD AVAILABILITY CHECK
    // =====================================================

    fun isRewardedAdAvailable(adUnitId: String): Boolean {
        return checkRewardedAdAvailabilityUseCase(adUnitId)
    }
}
