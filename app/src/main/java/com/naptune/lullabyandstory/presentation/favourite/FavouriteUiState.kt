package com.naptune.lullabyandstory.presentation.favourite

import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.domain.model.BannerAdDomainModel
import com.naptune.lullabyandstory.domain.model.RewardedAdDomainModel
import com.naptune.lullabyandstory.presentation.main.AdUiState

sealed class FavouriteUiState {
    object isLoading : FavouriteUiState()

    data class Content(
        val currentCategory: FavouriteCategory = FavouriteCategory.LULLABY,
        val favouriteLullabies: List<LullabyDomainModel> = emptyList(),
        val favouriteStories: List<StoryDomainModel> = emptyList(),
        // ✅ NEW: Use AdUiState wrapper like MainViewModel for consistency
        val adState: AdUiState = AdUiState(),
        // ✅ NEW: Session-only unlocked items via rewarded ads
        val adUnlockedIds: Set<String> = emptySet(),
        // ✅ DEPRECATED: Keep for backward compatibility during transition
        @Deprecated("Use adState.bannerAd instead")
        val bannerAd: BannerAdDomainModel? = adState.bannerAd,
        @Deprecated("Use adState.isAdInitialized instead")
        val isAdInitialized: Boolean = adState.isAdInitialized,
        @Deprecated("Use adState.rewardedAd instead")
        val rewardedAd: RewardedAdDomainModel? = adState.rewardedAd,
        @Deprecated("Use adState.isLoadingRewardedAd instead")
        val isLoadingRewardedAd: Boolean = adState.isLoadingRewardedAd,
        @Deprecated("Use adState.rewardedAdError instead")
        val rewardedAdError: String? = adState.rewardedAdError
    ) : FavouriteUiState()

    data class Error(val message: String): FavouriteUiState()
}
