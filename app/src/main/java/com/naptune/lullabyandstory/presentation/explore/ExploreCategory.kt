package com.naptune.lullabyandstory.presentation.explore

/**
 * Represents the main content type category in Explore screen
 */
enum class ExploreContentCategory {
    LULLABY,  // Shows lullabies with All/Popular/Free sub-tabs
    STORY     // Shows stories with All/Popular/Free sub-tabs
}

/**
 * Represents the filter sub-category (shared by both Lullaby and Story)
 */
enum class ExploreFilterCategory {
    ALL,      // Show all items
    POPULAR,  // Show popular items only
    FREE      // Show free items only
}
