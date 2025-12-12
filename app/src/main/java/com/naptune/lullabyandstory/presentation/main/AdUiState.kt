package com.naptune.lullabyandstory.presentation.main

import androidx.compose.runtime.Stable
import com.naptune.lullabyandstory.domain.model.BannerAdDomainModel
import com.naptune.lullabyandstory.domain.model.RewardedAdDomainModel

/**
 * Separate AdUiState for better debugging and maintenance
 */
@Stable // Using @Stable because of computed properties
data class AdUiState(
    // Banner Ad state
    val bannerAd: BannerAdDomainModel? = null,
    val isAdInitialized: Boolean = false,
    
    // Rewarded Ad state  
    val rewardedAd: RewardedAdDomainModel? = null,
    val isLoadingRewardedAd: Boolean = false,
    val rewardedAdError: String? = null,
) {
    // ✅ Convenient getters for easy debugging
    val isBannerLoading: Boolean get() = bannerAd?.isLoading == true
    val isBannerLoaded: Boolean get() = bannerAd?.isLoaded == true
    val bannerError: String? get() = bannerAd?.error
    
    val isRewardedLoaded: Boolean get() = rewardedAd?.isLoaded == true
    val rewardedError: String? get() = rewardedAd?.error
    
    fun debugString(): String = """
        AdUiState Debug:
        - Banner: loading=$isBannerLoading, loaded=$isBannerLoaded, error=$bannerError
        - Rewarded: loading=$isLoadingRewardedAd, loaded=$isRewardedLoaded, error=$rewardedError
        - AdMob initialized: $isAdInitialized
    """.trimIndent()
}