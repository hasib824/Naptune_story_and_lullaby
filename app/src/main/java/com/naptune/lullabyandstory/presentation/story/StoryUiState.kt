package com.naptune.lullabyandstory.presentation.story

import com.naptune.lullabyandstory.data.model.StoryRemoteModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.domain.model.BannerAdDomainModel
import com.naptune.lullabyandstory.domain.model.RewardedAdDomainModel
import com.naptune.lullabyandstory.presentation.main.AdUiState

sealed class StoryUiState {

    object IsLoading : StoryUiState()

    data class Content(
        val storyList: List<StoryDomainModel>, // Original all data
        val filteredStories: List<StoryDomainModel> = storyList, // Current category data
        // ✅ Pre-filtered lists for instant switching
        val popularStories: List<StoryDomainModel> = emptyList(),
        val freeStories: List<StoryDomainModel> = emptyList(),
        val currentCategory: StoryCategory = StoryCategory.ALL,
        // ✅ NEW: Session-only unlocked items via rewarded ads
        val adUnlockedIds: Set<String> = emptySet(),
        // ✅ NEW: Use AdUiState wrapper like MainViewModel for consistency
        val adState: AdUiState = AdUiState(),
        // ✅ MVI FIX: Premium status as part of UI state (single source of truth)
        val isPremium: Boolean = false,  // Default to free user until billing initializes
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
        val rewardedAdError: String? = adState.rewardedAdError,
        val lastRewardedStory: StoryDomainModel? = null
    ) : StoryUiState()
    
    data class Error(val message: String) : StoryUiState()

}
