package com.naptune.lullabyandstory.presentation.lullaby

import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.BannerAdDomainModel
import com.naptune.lullabyandstory.domain.model.RewardedAdDomainModel
import com.naptune.lullabyandstory.presentation.main.AdUiState

sealed class LullabyUiState {

    object IsLoading : LullabyUiState()
    data class Content(
        val lullabies: List<LullabyDomainModel>, // Original all data
        val filteredLullabies: List<LullabyDomainModel> = lullabies, // Current category data
        // ✅ Pre-filtered lists for instant switching
        val popularLullabies: List<LullabyDomainModel> = emptyList(),
        val freeLullabies: List<LullabyDomainModel> = emptyList(),
        val downloadingItems : Set<String> = emptySet(),
        val downloadedItems : Set<String> = emptySet(),
        val downloadProgress : Map<String, Int> = emptyMap(),
        val downloadError : Map<String, Throwable> = emptyMap(),
        val currentCategory: LullabyCategory = LullabyCategory.ALL,
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
        val lastRewardedLullaby: LullabyDomainModel? = null
        ) : LullabyUiState()

    data class Error(val message: String) : LullabyUiState()

}