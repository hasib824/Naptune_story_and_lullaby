package com.naptune.lullabyandstory.data.local.source.favourite

import kotlinx.coroutines.flow.Flow

/**
 * Data source for favourite operations (shared between Lullaby and Story)
 *
 * SOLID Principles:
 * - Single Responsibility: Only handles favourite-related operations
 * - Interface Segregation: Clients only depend on favourite operations
 * - Dependency Inversion: Repository depends on abstraction, not implementation
 *
 * Responsibilities:
 * - Toggle favourite status for items (lullaby or story)
 * - Check if an item is marked as favourite
 * - Manage favourite metadata (LIFO ordering support)
 */
interface FavouriteDataSource {

    // =====================================================
    // FAVOURITE TOGGLE OPERATIONS
    // =====================================================

    /**
     * Toggle favourite status for an item (lullaby or story)
     * @param documentId The document ID of the item
     * @param itemType The type of item ("lullaby" or "story")
     * @return Number of rows affected
     */
    suspend fun toggleFavourite(documentId: String, itemType: String): Int

    /**
     * Check if an item is marked as favourite
     * @param documentId The document ID of the item
     * @return Flow emitting true if favourite, false otherwise
     */
    fun checkIfItemIsFavourite(documentId: String): Flow<Boolean>

    // =====================================================
    // FAVOURITE METADATA OPERATIONS (LIFO ordering support)
    // =====================================================

    /**
     * Insert favourite metadata when user favourites an item
     * This enables LIFO ordering for favourites
     * @param itemId The ID of the item
     * @param itemType The type of item ("lullaby" or "story")
     * @return Number of rows inserted
     */
    suspend fun insertFavouriteMetadata(itemId: String, itemType: String): Int

    /**
     * Delete favourite metadata when user unfavourites an item
     * @param itemId The ID of the item
     * @param itemType The type of item ("lullaby" or "story")
     * @return Number of rows deleted
     */
    suspend fun deleteFavouriteMetadata(itemId: String, itemType: String): Int

    /**
     * Check if favourite metadata exists for an item
     * @param itemId The ID of the item
     * @param itemType The type of item ("lullaby" or "story")
     * @return True if metadata exists, false otherwise
     */
    suspend fun hasFavouriteMetadata(itemId: String, itemType: String): Boolean

    /**
     * Get total favourite count (both lullabies and stories)
     * @return Total number of favourites
     */
    suspend fun getFavouriteCount(): Int
}
