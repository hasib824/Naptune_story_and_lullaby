package com.naptune.lullabyandstory.data.local.source.lullaby

import com.naptune.lullabyandstory.data.local.entity.LullabyLocalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Local data source for core Lullaby CRUD operations
 *
 * SOLID Principles:
 * - Single Responsibility: Only handles lullaby entity CRUD operations
 * - Interface Segregation: Clients only depend on lullaby CRUD, not translations/favourites
 * - Dependency Inversion: Repository depends on abstraction, not implementation
 *
 * Responsibilities:
 * - Insert, read, update, delete lullaby entities
 * - Get lullaby count, search, and pagination
 * - Handle download status for offline playback
 * - NO translation operations (delegated to LullabyTranslationDataSource)
 * - NO favourite operations (delegated to FavouriteDataSource)
 */
interface LullabyLocalDataSource {

    // =====================================================
    // LULLABY CRUD OPERATIONS
    // =====================================================

    /**
     * Get all lullabies from the database
     * @return Flow emitting list of all lullabies
     */
    fun getAllLullabies(): Flow<List<LullabyLocalEntity>>

    /**
     * Get a lullaby by its document ID
     * @param documentId The document ID of the lullaby
     * @return LullabyLocalEntity or null if not found
     */
    suspend fun getLullabyById(documentId: String): LullabyLocalEntity?

    /**
     * Search lullabies by query string
     * @param query The search query
     * @return Flow emitting list of matching lullabies
     */
    fun searchLullabies(query: String): Flow<List<LullabyLocalEntity>>

    /**
     * Insert a single lullaby into the database
     * @param lullaby The lullaby to insert
     */
    suspend fun insertLullaby(lullaby: LullabyLocalEntity)

    /**
     * Insert multiple lullabies into the database
     * @param lullabies List of lullabies to insert
     * @return Number of rows inserted
     */
    suspend fun insertAllLullabies(lullabies: List<LullabyLocalEntity>): Int

    /**
     * Update a lullaby in the database
     * @param lullaby The lullaby to update
     */
    suspend fun updateLullaby(lullaby: LullabyLocalEntity)

    /**
     * Update the local file path for a lullaby (for offline playback)
     * @param musicLocalPath The local file path
     * @param documentId The document ID of the lullaby
     */
    suspend fun updateLocalPath(musicLocalPath: String, documentId: String)

    /**
     * Mark a lullaby as downloaded (for offline playback)
     * @param documentId The document ID of the lullaby
     * @return Number of rows updated
     */
    suspend fun markAsDownloaded(documentId: String): Int

    /**
     * Delete a lullaby from the database
     * @param lullaby The lullaby to delete
     */
    suspend fun deleteLullaby(lullaby: LullabyLocalEntity)

    /**
     * Delete all lullabies from the database
     * @return Number of rows deleted
     */
    suspend fun deleteAllLullabies(): Int

    /**
     * Get total count of lullabies in the database
     * @return Number of lullabies
     */
    suspend fun getLullabyCount(): Int

    /**
     * Get lullabies with pagination support
     * @param limit Maximum number of lullabies to return
     * @param offset Number of lullabies to skip
     * @return List of lullabies
     */
    suspend fun getLullabiesPaginated(limit: Int, offset: Int): List<LullabyLocalEntity>

    // =====================================================
    // FAVOURITE OPERATIONS
    // =====================================================

    /**
     * Toggle favourite status for a lullaby
     * @param lullabyId The document ID of the lullaby
     * @return Number of rows affected
     */
    suspend fun toggleLullabyFavourite(lullabyId: String): Int

    /**
     * Check if a lullaby is marked as favourite
     * @param lullabyId The document ID of the lullaby
     * @return Flow emitting true if favourite, false otherwise
     */
    fun isLullabyFavourite(lullabyId: String): Flow<Boolean>

    /**
     * Get all favourite lullabies (without translations)
     * @return Flow emitting list of favourite lullabies
     */
    fun getFavouriteLullabies(): Flow<List<LullabyLocalEntity>>
}
