package com.naptune.lullabyandstory.presentation.player

import androidx.compose.runtime.Immutable
import com.naptune.lullabyandstory.domain.model.BannerAdDomainModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel

@Immutable
data class AudioPlayerUiState(
    val isLoading: Boolean = true, // ✅ Default true for initial loading state
    val audioItem: AudioItem? = null,
    val isFromStory: Boolean = false,
    val playerState: PlayerState = PlayerState(),
    val error: String? = null,
    // ✅ NEW: Story navigation state
    val storyNavigationState: StoryNavigationState = StoryNavigationState(),
    // ✅ NEW: AdMob state
    val bannerAd: BannerAdDomainModel? = null,
    // ❌ REMOVED: Navigation events - using callbacks now
    // val navigationEvent: AudioPlayerNavigationEvent? = null
)

@Immutable
data class PlayerState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true, // ✅ Default true for loading indicator
    val isLooping: Boolean = false, // ✅ New state for loop mode
    val isFavourite: Boolean = false,
    val progress: Float = 0f,
    val currentTime: String = "0:00m",
    val totalTime: String = "0:00m",
    val volume: Float = 1.0f // ✅ Volume control state
)

// ✅ NEW: Story navigation state
@Immutable
data class StoryNavigationState(
    val storyList: List<StoryDomainModel> = emptyList(),
    val currentStoryIndex: Int = -1,
    val isLoadingStories: Boolean = false,
    val storyListError: String? = null,
    val canGoToPrevious: Boolean = false,
    val canGoToNext: Boolean = false
)

// ❌ REMOVED: Navigation events - using callbacks now
// sealed class AudioPlayerNavigationEvent {
//     data class NavigateToStoryReaderWithUrl(...) : AudioPlayerNavigationEvent()
// }
