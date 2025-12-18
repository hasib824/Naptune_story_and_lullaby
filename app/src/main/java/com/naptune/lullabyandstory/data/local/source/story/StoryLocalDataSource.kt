package com.naptune.lullabyandstory.data.local.source.story

import com.naptune.lullabyandstory.data.local.dao.StoryWithFullLocalization
import com.naptune.lullabyandstory.data.local.dao.StoryWithLocalizedName
import com.naptune.lullabyandstory.data.local.dao.StoryWithNameTranslation
import com.naptune.lullabyandstory.data.local.entity.StoryAudioLanguageLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryDescriptionTranslationLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryNameTranslationLocalEntity
import kotlinx.coroutines.flow.Flow

interface StoryLocalDataSource {

    // =====================================================
    // STORY OPERATIONS
    // =====================================================

    suspend fun insertAllStories(stories: List<StoryLocalEntity>): Int

    fun getAllStories(): Flow<List<StoryLocalEntity>>

    suspend fun getStoriesCount(): Int

    suspend fun toggleStoryFavourite(documentId: String): Int

    fun checkIfItemIsFavourite(documentId: String): Flow<Boolean>

    suspend fun deleteAllStories(): Int

    fun getFavouriteStories(): Flow<List<StoryLocalEntity>>

    fun getFavouriteStoriesWithLocalizedNames(languageCode: String): Flow<List<StoryWithLocalizedName>>


    // =====================================================
    // STORY NAME TRANSLATION OPERATIONS
    // =====================================================

    fun getAllStoryNameTranslations(): Flow<List<StoryNameTranslationLocalEntity>>

    suspend fun getStoryNameTranslationByDocumentId(storyDocumentId: String): StoryNameTranslationLocalEntity?

    suspend fun getStoryNameTranslationById(storyId: String): StoryNameTranslationLocalEntity?

    suspend fun insertStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity): Int

    suspend fun insertAllStoryNameTranslations(storyNameTranslations: List<StoryNameTranslationLocalEntity>): Int

    suspend fun updateStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity): Int

    suspend fun deleteStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity): Int

    suspend fun deleteAllStoryNameTranslations(): Int

    suspend fun getStoryNameTranslationCount(): Int

    // ✅ OPTIMIZED: Single method for language-specific stories
    fun getAllStoriesWithLocalizedNames(languageCode: String): Flow<List<StoryWithLocalizedName>>

    // 🚀 ULTRA OPTIMIZED: Single method for FULL story localization (Name + Description)
    fun getAllStoriesWithFullLocalization(languageCode: String): Flow<List<StoryWithFullLocalization>>

    // 🚀 ULTRA OPTIMIZED: Single method for FULL favourite story localization (Name + Description)
    fun getFavouriteStoriesWithFullLocalization(languageCode: String): Flow<List<StoryWithFullLocalization>>

    // =====================================================
    // STORY DESCRIPTION TRANSLATION OPERATIONS
    // =====================================================

    suspend fun insertAllStoryDescriptionTranslations(storyDescriptionTranslations: List<StoryDescriptionTranslationLocalEntity>): Int

    suspend fun deleteAllStoryDescriptionTranslations(): Int

    // =====================================================
    // STORY AUDIO LANGUAGE OPERATIONS
    // =====================================================

    suspend fun insertAllStoryAudioLanguages(storyAudioLanguages: List<StoryAudioLanguageLocalEntity>): Int

    suspend fun getStoryAudioLanguageByStoryDocumentId(storyDocumentId: String): StoryAudioLanguageLocalEntity?

    suspend fun getStoryAudioLanguageCount(): Int

    suspend fun deleteAllStoryAudioLanguages(): Int

    // =====================================================
    // JOIN OPERATIONS (Story + StoryNameTranslation)
    // =====================================================

    suspend fun getStoryWithNameTranslation(documentId: String): StoryWithNameTranslation?

    suspend fun insertFavouriteMetadata(itemId: String, itemType: String): Int

    /**
     * Delete favourite metadata when user unfavourites an item
     */
    suspend fun deleteFavouriteMetadata(itemId: String, itemType: String): Int


}