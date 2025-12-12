package com.naptune.lullabyandstory.presentation.main

import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.presentation.lullaby.LullabyUiState

sealed class MainUiState {
    object Loading : MainUiState()
    data class Content(
        val todaysPickLullabies: List<LullabyDomainModel> = emptyList(),
        val todaysPickStory: StoryDomainModel? = null,
        val popularLullabies: List<LullabyDomainModel> = emptyList(),
        val popularStories: List<StoryDomainModel> = emptyList(),
        val favouriteLullabies: List<LullabyDomainModel> = emptyList(),
        val favouriteStories: List<StoryDomainModel> = emptyList(),
        val downloadingItems : Set<String> = emptySet(),
        val downloadedItems : Set<String> = emptySet(),
        val downloadProgress : Map<String, Int> = emptyMap(),
        val downloadError : Map<String, Throwable> = emptyMap(),
        val currentTodaysPickPage: Int = 0,
        // ✅ NEW: Session-only unlocked items via rewarded ads
        val adUnlockedIds: Set<String> = emptySet(),
        // ✅ Centralized Ad state
        val adState: AdUiState = AdUiState(),
        // ✅ MVI FIX: Premium status as part of UI state (single source of truth)
        val isPremium: Boolean = false  // Default to free user until billing initializes
    ) : MainUiState()


    data class Error(val message: String) : MainUiState()
}
