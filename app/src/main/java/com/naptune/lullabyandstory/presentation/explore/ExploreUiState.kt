package com.naptune.lullabyandstory.presentation.explore

import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.presentation.main.AdUiState

/**
 * MVI UI State for ExploreScreen
 */
sealed class ExploreUiState {
    object IsLoading : ExploreUiState()

    data class Content(
        // Current selected categories
        val contentCategory: ExploreContentCategory = ExploreContentCategory.LULLABY,
        val filterCategory: ExploreFilterCategory = ExploreFilterCategory.ALL,

        // Lullaby data
        val allLullabies: List<LullabyDomainModel> = emptyList(),
        val popularLullabies: List<LullabyDomainModel> = emptyList(),
        val freeLullabies: List<LullabyDomainModel> = emptyList(),

        // Download state (matching LullabyUiState.Content structure for LullabyGrid compatibility)
        val downloadingItems: Set<String> = emptySet(),
        val downloadedItems: Set<String> = emptySet(),
        val downloadProgress: Map<String, Int> = emptyMap(),

        // Story data
        val allStories: List<StoryDomainModel> = emptyList(),
        val popularStories: List<StoryDomainModel> = emptyList(),
        val freeStories: List<StoryDomainModel> = emptyList(),

        // AdMob state
        val adState: AdUiState = AdUiState()
    ) : ExploreUiState() {

        /**
         * Get filtered lullabies based on current filter category
         */
        val filteredLullabies: List<LullabyDomainModel>
            get() = when (filterCategory) {
                ExploreFilterCategory.ALL -> allLullabies
                ExploreFilterCategory.POPULAR -> popularLullabies
                ExploreFilterCategory.FREE -> freeLullabies
            }

        /**
         * Get filtered stories based on current filter category
         */
        val filteredStories: List<StoryDomainModel>
            get() = when (filterCategory) {
                ExploreFilterCategory.ALL -> allStories
                ExploreFilterCategory.POPULAR -> popularStories
                ExploreFilterCategory.FREE -> freeStories
            }
    }

    data class Error(val message: String) : ExploreUiState()
}
