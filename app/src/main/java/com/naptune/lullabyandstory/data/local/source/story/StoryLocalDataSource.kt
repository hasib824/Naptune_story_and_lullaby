package com.naptune.lullabyandstory.data.local.source.story

import com.naptune.lullabyandstory.data.local.entity.StoryLocalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Local data source for core Story CRUD operations
 *
 * SOLID Principles:
 * - Single Responsibility: Only handles story entity CRUD operations
 * - Interface Segregation: Clients only depend on story CRUD, not translations/favourites
 * - Dependency Inversion: Repository depends on abstraction, not implementation
 *
 * Responsibilities:
 * - Insert, read, update, delete story entities
 * - Get story count and basic queries
 * - NO translation operations (delegated to StoryTranslationDataSource)
 * - NO favourite operations (delegated to FavouriteDataSource)
 * - NO audio language operations (delegated to StoryAudioLanguageDataSource)
 */
interface StoryLocalDataSource {

    // =====================================================
    // STORY CRUD OPERATIONS
    // =====================================================

    /**
     * Insert multiple stories into the database
     * @param stories List of stories to insert
     * @return Number of rows inserted
     */
    suspend fun insertAllStories(stories: List<StoryLocalEntity>): Int

    /**
     * Get all stories from the database
     * @return Flow emitting list of all stories
     */
    fun getAllStories(): Flow<List<StoryLocalEntity>>

    /**
     * Get total count of stories in the database
     * @return Number of stories
     */
    suspend fun getStoriesCount(): Int

    /**
     * Delete all stories from the database
     * @return Number of rows deleted
     */
    suspend fun deleteAllStories(): Int

    /**
     * Get all favourite stories (without translations)
     * @return Flow emitting list of favourite stories
     */
    fun getFavouriteStories(): Flow<List<StoryLocalEntity>>

    /**
     * Toggle favourite status for a story
     * @param documentId The document ID of the story
     * @return Number of rows affected
     */
    suspend fun toggleStoryFavourite(documentId: String): Int

    /**
     * Check if a story is marked as favourite
     * @param documentId The document ID of the story
     * @return Flow emitting true if favourite, false otherwise
     */
    fun checkIfItemIsFavourite(documentId: String): Flow<Boolean>
}
