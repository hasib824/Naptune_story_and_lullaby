package com.naptune.lullabyandstory.data.local.source.story

import com.naptune.lullabyandstory.data.local.dao.StoryWithFullLocalization
import com.naptune.lullabyandstory.data.local.dao.StoryWithLocalizedName
import com.naptune.lullabyandstory.data.local.dao.StoryWithNameTranslation
import com.naptune.lullabyandstory.data.local.entity.StoryDescriptionTranslationLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryNameTranslationLocalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data source for Story translation operations
 *
 * SOLID Principles:
 * - Single Responsibility: Only handles story translation operations
 * - Interface Segregation: Clients only depend on translation operations
 * - Dependency Inversion: Repository depends on abstraction, not implementation
 *
 * Responsibilities:
 * - Manage story name translations (multi-language support)
 * - Manage story description translations
 * - Provide localized story queries
 * - Handle story join operations with translations
 */
interface StoryTranslationDataSource {

    // =====================================================
    // STORY NAME TRANSLATION OPERATIONS
    // =====================================================

    /**
     * Get all story name translations
     * @return Flow emitting list of all name translations
     */
    fun getAllStoryNameTranslations(): Flow<List<StoryNameTranslationLocalEntity>>

    /**
     * Get story name translation by document ID
     * @param storyDocumentId The story document ID
     * @return StoryNameTranslationLocalEntity or null if not found
     */
    suspend fun getStoryNameTranslationByDocumentId(storyDocumentId: String): StoryNameTranslationLocalEntity?

    /**
     * Get story name translation by story ID
     * @param storyId The story ID
     * @return StoryNameTranslationLocalEntity or null if not found
     */
    suspend fun getStoryNameTranslationById(storyId: String): StoryNameTranslationLocalEntity?

    /**
     * Insert a single story name translation
     * @param storyNameTranslation The translation to insert
     * @return Number of rows inserted
     */
    suspend fun insertStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity): Int

    /**
     * Insert multiple story name translations
     * @param storyNameTranslations List of translations to insert
     * @return Number of rows inserted
     */
    suspend fun insertAllStoryNameTranslations(storyNameTranslations: List<StoryNameTranslationLocalEntity>): Int

    /**
     * Update a story name translation
     * @param storyNameTranslation The translation to update
     * @return Number of rows updated
     */
    suspend fun updateStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity): Int

    /**
     * Delete a story name translation
     * @param storyNameTranslation The translation to delete
     * @return Number of rows deleted
     */
    suspend fun deleteStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity): Int

    /**
     * Delete all story name translations
     * @return Number of rows deleted
     */
    suspend fun deleteAllStoryNameTranslations(): Int

    /**
     * Get total count of story name translations
     * @return Number of translations
     */
    suspend fun getStoryNameTranslationCount(): Int

    // =====================================================
    // STORY DESCRIPTION TRANSLATION OPERATIONS
    // =====================================================

    /**
     * Insert multiple story description translations
     * @param storyDescriptionTranslations List of description translations to insert
     * @return Number of rows inserted
     */
    suspend fun insertAllStoryDescriptionTranslations(storyDescriptionTranslations: List<StoryDescriptionTranslationLocalEntity>): Int

    /**
     * Delete all story description translations
     * @return Number of rows deleted
     */
    suspend fun deleteAllStoryDescriptionTranslations(): Int

    // =====================================================
    // LOCALIZED STORY QUERIES (JOIN OPERATIONS)
    // =====================================================

    /**
     * Get all stories with localized names for a specific language
     * @param languageCode The language code (e.g., "en", "es", "fr")
     * @return Flow emitting list of stories with localized names
     */
    fun getAllStoriesWithLocalizedNames(languageCode: String): Flow<List<StoryWithLocalizedName>>

    /**
     * Get favourite stories with localized names for a specific language
     * @param languageCode The language code (e.g., "en", "es", "fr")
     * @return Flow emitting list of favourite stories with localized names
     */
    fun getFavouriteStoriesWithLocalizedNames(languageCode: String): Flow<List<StoryWithLocalizedName>>

    /**
     * Get all stories with FULL localization (name + description) for a specific language
     * @param languageCode The language code (e.g., "en", "es", "fr")
     * @return Flow emitting list of stories with full localization
     */
    fun getAllStoriesWithFullLocalization(languageCode: String): Flow<List<StoryWithFullLocalization>>

    /**
     * Get favourite stories with FULL localization (name + description) for a specific language
     * @param languageCode The language code (e.g., "en", "es", "fr")
     * @return Flow emitting list of favourite stories with full localization
     */
    fun getFavouriteStoriesWithFullLocalization(languageCode: String): Flow<List<StoryWithFullLocalization>>

    /**
     * Get a single story with its name translation
     * @param documentId The story document ID
     * @return StoryWithNameTranslation or null if not found
     */
    suspend fun getStoryWithNameTranslation(documentId: String): StoryWithNameTranslation?
}
