package com.naptune.lullabyandstory.data.network.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import android.util.DisplayMetrics
import android.view.WindowManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.naptune.lullabyandstory.domain.model.AdLoadResult
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.model.BannerAdDomainModel
import com.naptune.lullabyandstory.domain.model.RewardDomainModel
import com.naptune.lullabyandstory.domain.model.RewardedAdDomainModel
import com.naptune.lullabyandstory.domain.model.RewardedAdLoadResult
import com.naptune.lullabyandstory.domain.model.RewardedAdShowResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class AdMobDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var isInitialized = false
    private val activeAdViews = mutableMapOf<String, AdView>()
    private val activeRewardedAds = mutableMapOf<String, RewardedAd>()
    
    companion object {
        private const val TAG = "AdMobDataSource"
        
        // Test ad unit IDs from official AdMob documentation
        // Replace with your real ad unit IDs in production
        const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INLINE_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/2014213617"
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }
    
    suspend fun initializeMobileAds(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            if (isInitialized) {
                continuation.resume(true)
                return@suspendCancellableCoroutine
            }
            
            Log.d(TAG, "🚀 Initializing Mobile Ads SDK...")
            
            // Check inline adaptive banner support
            try {
                Log.d(TAG, "🔍 Testing inline adaptive banner support...")
                
                // Test if inline adaptive banner method exists
                val testSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, 320)
                Log.d(TAG, "✅ Inline adaptive banner method available: ${testSize.width}x${testSize.height}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Inline adaptive banner not supported: ${e.message}")
                Log.e(TAG, "🔧 This might be due to older AdMob SDK version or device limitation")
            }
            
            MobileAds.initialize(context) { initializationStatus ->
                isInitialized = true
                Log.d(TAG, "✅ Mobile Ads SDK initialized successfully")
                Log.d(TAG, "📊 Adapter status: ${initializationStatus.adapterStatusMap}")
                continuation.resume(true)
            }
        }
    }
    
    suspend fun loadBannerAd(
        adUnitId: String,
        adSizeType: AdSizeType
    ): Flow<AdLoadResult> = flow {
        try {
            Log.d(TAG, "📢 Loading banner ad for unit: $adUnitId")
            emit(AdLoadResult.Loading)
            
            // Ensure Mobile Ads is initialized
            if (!isInitialized) {
                initializeMobileAds()
            }
            
            val adSize = mapAdSizeType(adSizeType)
            val adView = AdView(context).apply {
                setAdSize(adSize)
                this.adUnitId = adUnitId
            }
            
            val adRequest = AdRequest.Builder().build()
            
            suspendCancellableCoroutine<Unit> { continuation ->
                adView.adListener = object : com.google.android.gms.ads.AdListener() {
                    override fun onAdLoaded() {
                        Log.d(TAG, "✅ Banner ad loaded successfully for unit: $adUnitId")
                        Log.d(TAG, "🎦 Ad type: $adSizeType")
                        Log.d(TAG, "📊 Final ad size: ${adView.adSize?.width ?: "unknown"}x${adView.adSize?.height ?: "unknown"}")
                        activeAdViews[adUnitId] = adView
                        
                        // Use actual loaded AdView size (especially for inline adaptive)
                        val actualWidth = adView.adSize?.width ?: adSize.width
                        val actualHeight = adView.adSize?.height ?: adSize.height
                        
                        Log.d(TAG, "🔄 Using actual ad size: ${actualWidth}x${actualHeight}")
                        
                        val bannerAdModel = BannerAdDomainModel(
                            adUnitId = adUnitId,
                            adSize = com.naptune.lullabyandstory.domain.model.AdSize(
                                width = actualWidth,
                                height = actualHeight, // This will be 320 for inline adaptive!
                                type = adSizeType
                            ),
                            isLoaded = true,
                            isLoading = false,
                            error = null
                        )
                        
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }
                    
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, "❌ Banner ad failed to load: ${loadAdError.message}")
                        Log.e(TAG, "💫 Error code: ${loadAdError.code}")
                        Log.e(TAG, "🌍 Error domain: ${loadAdError.domain}")
                        Log.e(TAG, "🎦 Failed ad type: $adSizeType")
                        Log.e(TAG, "📊 Requested ad size: ${adSize.width}x${adSize.height}")
                        
                        // Additional error analysis
                        when (loadAdError.code) {
                            0 -> Log.e(TAG, "🔴 ERROR: Internal error")
                            1 -> Log.e(TAG, "🔴 ERROR: Invalid request (possibly unsupported ad size)")
                            2 -> Log.e(TAG, "🔴 ERROR: Network error")
                            3 -> Log.e(TAG, "🔴 ERROR: No fill (no ads available)")
                            else -> Log.e(TAG, "🔴 ERROR: Unknown error code")
                        }
                        
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }
                }
                
                adView.loadAd(adRequest)
                
                continuation.invokeOnCancellation {
                    adView.destroy()
                }
            }
            
            // Check if ad was loaded successfully
            val loadedAdView = activeAdViews[adUnitId]
            if (loadedAdView != null) {
                // Use actual loaded AdView size
                val actualWidth = loadedAdView.adSize?.width ?: adSize.width
                val actualHeight = loadedAdView.adSize?.height ?: adSize.height
                
                Log.d(TAG, "🏁 Final emit with actual size: ${actualWidth}x${actualHeight}")
                
                val bannerAdModel = BannerAdDomainModel(
                    adUnitId = adUnitId,
                    adSize = com.naptune.lullabyandstory.domain.model.AdSize(
                        width = actualWidth,
                        height = actualHeight, // This should be 320!
                        type = adSizeType
                    ),
                    isLoaded = true,
                    isLoading = false,
                    error = null
                )
                emit(AdLoadResult.Success(bannerAdModel))
            } else {
                emit(AdLoadResult.Error("Failed to load banner ad"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Exception while loading banner ad: ${e.message}", e)
            emit(AdLoadResult.Error(e.message ?: "Unknown error occurred"))
        }
    }
    
    fun destroyBannerAd(adUnitId: String) {
        Log.d(TAG, "🗑️ Destroying banner ad for unit: $adUnitId")
        activeAdViews[adUnitId]?.let { adView ->
            try {
                // Remove from parent first to avoid "child already has parent" error
                (adView.parent as? android.view.ViewGroup)?.removeView(adView)
                Log.d(TAG, "🔄 AdView removed from parent")
                
                // Now destroy safely
                adView.destroy()
                Log.d(TAG, "💥 AdView destroyed")
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Error destroying AdView: ${e.message}")
                // Force remove from parent anyway
                try {
                    (adView.parent as? android.view.ViewGroup)?.removeView(adView)
                } catch (ex: Exception) {
                    Log.e(TAG, "🔴 Failed to remove from parent: ${ex.message}")
                }
            } finally {
                activeAdViews.remove(adUnitId)
                Log.d(TAG, "✅ Banner ad cleanup completed")
            }
        }
    }
    
    fun getAdView(adUnitId: String): AdView? {
        return activeAdViews[adUnitId]
    }
    
    fun isAdMobInitialized(): Boolean = isInitialized
    
    private fun mapAdSizeType(adSizeType: AdSizeType): AdSize {
        return when (adSizeType) {
            AdSizeType.BANNER -> AdSize.BANNER
            AdSizeType.LARGE_BANNER -> AdSize.LARGE_BANNER
            AdSizeType.SMART_BANNER -> AdSize.SMART_BANNER
            AdSizeType.ADAPTIVE_BANNER -> AdSize.BANNER
            AdSizeType.ANCHORED_ADAPTIVE_BANNER -> getAnchoredAdaptiveBannerAdSize()
            AdSizeType.INLINE_ADAPTIVE_BANNER -> getInlineAdaptiveBannerAdSize()
            AdSizeType.MEDIUM_RECTANGLE -> AdSize.MEDIUM_RECTANGLE
        }
    }
    
    /**
     * Create anchored adaptive banner ad size for full-width banner
     * This provides better performance and revenue optimization
     */
    private fun getAnchoredAdaptiveBannerAdSize(): AdSize {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        
        val screenWidthDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        
        Log.d(TAG, "📐 Screen width: ${screenWidthDp}dp")
        
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
    }
    
    /**
     * Create inline adaptive banner ad size for story content
     * Inline adaptive banners are larger and use variable heights
     * Perfect for placing within scrollable content like story text
     * Using limited height for better story reading experience
     */
    private fun getInlineAdaptiveBannerAdSize(): AdSize {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        
        val screenWidthDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        
        Log.d(TAG, "🔍 INLINE ADAPTIVE BANNER DEBUG:")
        Log.d(TAG, "📐 Screen width pixels: ${displayMetrics.widthPixels}")
        Log.d(TAG, "📐 Screen density: ${displayMetrics.density}")
        Log.d(TAG, "📐 Calculated width DP: ${screenWidthDp}")
        
        // Try multiple approaches to create inline adaptive banner
        return try {
            Log.d(TAG, "🎆 Attempt 1: getCurrentOrientationInlineAdaptiveBannerAdSize")
            val inlineAdSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, screenWidthDp)
            Log.d(TAG, "✅ Method 1 SUCCESS: ${inlineAdSize.width}x${inlineAdSize.height}")
            inlineAdSize
        } catch (e1: Exception) {
            Log.e(TAG, "❌ Method 1 FAILED: ${e1.message}")
            try {
                Log.d(TAG, "🎆 Attempt 2: getInlineAdaptiveBannerAdSize with maxHeight=250")
                val inlineAdSizeWithHeight = AdSize.getInlineAdaptiveBannerAdSize(screenWidthDp, 250)
                Log.d(TAG, "✅ Method 2 SUCCESS: ${inlineAdSizeWithHeight.width}x${inlineAdSizeWithHeight.height}")
                inlineAdSizeWithHeight
            } catch (e2: Exception) {
                Log.e(TAG, "❌ Method 2 FAILED: ${e2.message}")
                try {
                    Log.d(TAG, "🎆 Attempt 3: getInlineAdaptiveBannerAdSize with maxHeight=150")
                    val inlineAdSizeSmaller = AdSize.getInlineAdaptiveBannerAdSize(screenWidthDp, 150)
                    Log.d(TAG, "✅ Method 3 SUCCESS: ${inlineAdSizeSmaller.width}x${inlineAdSizeSmaller.height}")
                    inlineAdSizeSmaller
                } catch (e3: Exception) {
                    Log.e(TAG, "❌ Method 3 FAILED: ${e3.message}")
                    Log.e(TAG, "🔄 ALL INLINE METHODS FAILED - Falling back to anchored adaptive banner")
                    getAnchoredAdaptiveBannerAdSize()
                }
            }
        }
    }
    
    suspend fun loadRewardedAd(adUnitId: String): Flow<RewardedAdLoadResult> = flow {
        try {
            Log.d(TAG, "🎁 Loading rewarded ad for unit: $adUnitId")
            emit(RewardedAdLoadResult.Loading)
            
            // Ensure Mobile Ads is initialized
            if (!isInitialized) {
                initializeMobileAds()
            }
            
            val adRequest = AdRequest.Builder().build()
            
            suspendCancellableCoroutine<Unit> { continuation ->
                RewardedAd.load(
                    context,
                    adUnitId,
                    adRequest,
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(rewardedAd: RewardedAd) {
                            Log.d(TAG, "✅ Rewarded ad loaded successfully for unit: $adUnitId")
                            activeRewardedAds[adUnitId] = rewardedAd
                            
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }
                        
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Log.e(TAG, "❌ Rewarded ad failed to load: ${loadAdError.message}")
                            Log.e(TAG, "💫 Error code: ${loadAdError.code}")
                            Log.e(TAG, "🌍 Error domain: ${loadAdError.domain}")
                            
                            // Additional error analysis
                            when (loadAdError.code) {
                                0 -> Log.e(TAG, "🔴 ERROR: Internal error")
                                1 -> Log.e(TAG, "🔴 ERROR: Invalid request")
                                2 -> Log.e(TAG, "🔴 ERROR: Network error")
                                3 -> Log.e(TAG, "🔴 ERROR: No fill (no ads available)")
                                else -> Log.e(TAG, "🔴 ERROR: Unknown error code")
                            }
                            
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }
                    }
                )
                
                continuation.invokeOnCancellation {
                    Log.d(TAG, "🚫 Rewarded ad loading cancelled for unit: $adUnitId")
                }
            }
            
            // Check if ad was loaded successfully
            val loadedRewardedAd = activeRewardedAds[adUnitId]
            if (loadedRewardedAd != null) {
                Log.d(TAG, "🏁 Rewarded ad successfully loaded and cached")
                val rewardedAdModel = RewardedAdDomainModel(
                    adUnitId = adUnitId,
                    isLoaded = true,
                    isLoading = false,
                    error = null
                )
                emit(RewardedAdLoadResult.Success(rewardedAdModel))
            } else {
                emit(RewardedAdLoadResult.Error("Failed to load rewarded ad"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Exception while loading rewarded ad: ${e.message}", e)
            emit(RewardedAdLoadResult.Error(e.message ?: "Unknown error occurred"))
        }
    }
    
    suspend fun showRewardedAd(
        adUnitId: String,
        activity: Activity
    ): Flow<RewardedAdShowResult> = flow {
        try {
            Log.d(TAG, "🎬 Showing rewarded ad for unit: $adUnitId")
            emit(RewardedAdShowResult.Loading)
            
            val rewardedAd = activeRewardedAds[adUnitId]
            if (rewardedAd == null) {
                Log.e(TAG, "❌ No loaded rewarded ad found for unit: $adUnitId")
                emit(RewardedAdShowResult.Error("Rewarded ad not loaded"))
                return@flow
            }
            
            // ✅ Track whether reward was earned
            var rewardEarned: RewardDomainModel? = null

            suspendCancellableCoroutine<Unit> { continuation ->
                var hasCompleted = false

                // Set full screen content callback
                rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdClicked() {
                        Log.d(TAG, "🖱️ Rewarded ad clicked")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "🚪 Rewarded ad dismissed - restoring system UI")

                        // ✅ Restore system UI after ad dismissal
                        AdMobFullscreenHelper.restoreFromFullscreenAd(activity)

                        activeRewardedAds.remove(adUnitId)

                        if (!hasCompleted && continuation.isActive) {
                            hasCompleted = true
                            continuation.resume(Unit)
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        Log.e(TAG, "❌ Rewarded ad failed to show: ${adError.message}")

                        // ✅ Restore system UI on failure
                        AdMobFullscreenHelper.restoreFromFullscreenAd(activity)

                        activeRewardedAds.remove(adUnitId)

                        if (!hasCompleted && continuation.isActive) {
                            hasCompleted = true
                            continuation.resume(Unit)
                        }
                    }

                    override fun onAdImpression() {
                        Log.d(TAG, "👁️ Rewarded ad impression")
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "📺 Rewarded ad showed full screen content")
                        // ✅ NOW prepare for fullscreen when ad actually shows
                        AdMobFullscreenHelper.prepareForFullscreenAd(activity)
                    }
                }

                // Show the ad with reward listener
                rewardedAd.show(activity) { rewardItem ->
                    Log.d(TAG, "🎁 User earned reward: ${rewardItem.type} - ${rewardItem.amount}")

                    // ✅ Track that reward was earned
                    rewardEarned = RewardDomainModel(
                        type = rewardItem.type,
                        amount = rewardItem.amount
                    )

                    if (!hasCompleted && continuation.isActive) {
                        hasCompleted = true
                        continuation.resume(Unit)
                    }
                }

                continuation.invokeOnCancellation {
                    Log.d(TAG, "🚫 Rewarded ad show cancelled - restoring system UI")
                    // ✅ Restore system UI on cancellation
                    AdMobFullscreenHelper.restoreFromFullscreenAd(activity)
                    activeRewardedAds.remove(adUnitId)
                }
            }

            // ✅ FIXED: Check if reward was actually earned
            if (rewardEarned != null) {
                Log.d(TAG, "✅ Emitting Success - Reward earned: ${rewardEarned.type}")
                emit(RewardedAdShowResult.Success(rewardEarned))
            } else {
                Log.d(TAG, "🚫 Emitting Dismissed - No reward earned")
                emit(RewardedAdShowResult.Dismissed("User dismissed ad without completing"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Exception while showing rewarded ad: ${e.message}", e)
            emit(RewardedAdShowResult.Error(e.message ?: "Unknown error occurred"))
        }
    }
    
    fun isRewardedAdLoaded(adUnitId: String): Boolean {
        val isLoaded = activeRewardedAds.containsKey(adUnitId)
        Log.d(TAG, "🔍 Checking if rewarded ad is loaded for $adUnitId: $isLoaded")
        return isLoaded
    }
}