package com.naptune.lullabyandstory.data.local.source.story

import com.naptune.lullabyandstory.data.local.entity.StoryAudioLanguageLocalEntity

/**
 * Data source for Story audio language operations
 *
 * SOLID Principles:
 * - Single Responsibility: Only handles story audio language mapping operations
 * - Interface Segregation: Clients only depend on audio language operations
 * - Dependency Inversion: Repository depends on abstraction, not implementation
 *
 * Responsibilities:
 * - Manage story audio language mappings (which audio file for which language)
 * - Insert, query, and delete audio language entries
 */
interface StoryAudioLanguageDataSource {

    // =====================================================
    // STORY AUDIO LANGUAGE OPERATIONS
    // =====================================================

    /**
     * Insert multiple story audio language entries
     * @param storyAudioLanguages List of audio language entries to insert
     * @return Number of rows inserted
     */
    suspend fun insertAllStoryAudioLanguages(storyAudioLanguages: List<StoryAudioLanguageLocalEntity>): Int

    /**
     * Get story audio language entry by story document ID
     * @param storyDocumentId The story document ID
     * @return StoryAudioLanguageLocalEntity or null if not found
     */
    suspend fun getStoryAudioLanguageByStoryDocumentId(storyDocumentId: String): StoryAudioLanguageLocalEntity?

    /**
     * Get total count of story audio language entries
     * @return Number of audio language entries
     */
    suspend fun getStoryAudioLanguageCount(): Int

    /**
     * Delete all story audio language entries
     * @return Number of rows deleted
     */
    suspend fun deleteAllStoryAudioLanguages(): Int
}
