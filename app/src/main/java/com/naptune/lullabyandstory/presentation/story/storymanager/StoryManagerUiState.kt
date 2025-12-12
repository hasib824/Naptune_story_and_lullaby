package com.naptune.lullabyandstory.presentation.story.storymanager

import androidx.compose.runtime.Immutable
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.domain.model.BannerAdDomainModel
import com.naptune.lullabyandstory.presentation.main.AdUiState

@Immutable
data class StoryManagerUiState(
    val story: StoryDomainModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    // ✅ NEW: Use AdUiState wrapper like other ViewModels for consistency
    val adState: AdUiState = AdUiState(),
    // ✅ DEPRECATED: Keep for backward compatibility during transition
    @Deprecated("Use adState.bannerAd instead") 
    val bannerAd: BannerAdDomainModel? = adState.bannerAd,
    @Deprecated("Use adState.isAdInitialized instead")
    val isAdInitialized: Boolean = adState.isAdInitialized
)