package com.naptune.lullabyandstory.presentation.profile

import com.naptune.lullabyandstory.domain.model.AdSizeType

/**
 * Sealed class representing all possible user intents/actions in the Profile screen
 * Following MVI pattern for clean state management
 */
sealed class ProfileIntent {

    /**
     * User wants to share the Naptune app with others
     */
    object ShareApp : ProfileIntent()

    /**
     * User wants to send feedback via email
     */
    object SendFeedback : ProfileIntent()

    /**
     * User wants to rate the app on Play Store
     */
    object RateApp : ProfileIntent()

    /**
     * Clear any error or success messages
     */
    object ClearMessage : ProfileIntent()

    // ✅ NEW: AdMob related intents
    /**
     * Initialize AdMob SDK
     */
    object InitializeAds : ProfileIntent()

    /**
     * Load banner ad
     * @param adUnitId The ad unit ID for the banner
     * @param adSizeType The size type of the banner ad
     */
    data class LoadBannerAd(
        val adUnitId: String,
        val adSizeType: AdSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
    ) : ProfileIntent()

    /**
     * Destroy banner ad when leaving screen
     * @param adUnitId The ad unit ID to destroy
     */
    data class DestroyBannerAd(val adUnitId: String) : ProfileIntent()
}