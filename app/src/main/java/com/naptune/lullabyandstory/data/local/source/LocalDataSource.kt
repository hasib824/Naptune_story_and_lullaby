package com.naptune.lullabyandstory.data.local.source

import com.naptune.lullabyandstory.data.local.dao.LullabyWithLocalizedName
import com.naptune.lullabyandstory.data.local.dao.LullabyWithTranslation
import com.naptune.lullabyandstory.data.local.dao.StoryWithFullLocalization
import com.naptune.lullabyandstory.data.local.dao.StoryWithLocalizedName
import com.naptune.lullabyandstory.data.local.dao.StoryWithNameTranslation
import com.naptune.lullabyandstory.data.local.entity.LullabyLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryAudioLanguageLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryDescriptionTranslationLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryNameTranslationLocalEntity
import com.naptune.lullabyandstory.data.local.entity.TranslationLocalEntity
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {


    fun getAllLullabies(): Flow<List<LullabyLocalEntity>>

    suspend fun getLullabyById(documentId: String): LullabyLocalEntity?

    fun searchLullabies(query: String): Flow<List<LullabyLocalEntity>>

    suspend fun insertLullaby(lullaby: LullabyLocalEntity)

    suspend fun insertAllLullabies(lullabies: List<LullabyLocalEntity>): Int

    suspend fun updateLullaby(lullaby: LullabyLocalEntity)

    suspend fun updateLocalPath(musicLocalPath: String, documentId: String)

    suspend fun markAsDownloaded(documentId: String): Int

    suspend fun deleteLullaby(lullaby: LullabyLocalEntity)

    suspend fun deleteAllLullabies(): Int

    suspend fun getLullabyCount(): Int

    suspend fun getLullabiesPaginated(limit: Int, offset: Int): List<LullabyLocalEntity>

    // =====================================================
    // STORY OPERATIONS
    // =====================================================

    suspend fun insertAllStories(stories: List<StoryLocalEntity>): Int

    fun getAllStories(): Flow<List<StoryLocalEntity>>

    suspend fun getStoriesCount(): Int

    suspend fun toggleStoryFavourite(documentId: String): Int

    fun checkIfItemIsFavourite(documentId: String): Flow<Boolean>

    suspend fun deleteAllStories(): Int

    // =====================================================
    // FAVOURITE OPERATIONS
    // =====================================================

    suspend fun toggleLullabyFavourite(lullabyId: String): Int

    fun isLullabyFavourite(lullabyId: String): Flow<Boolean>

    fun getFavouriteLullabies(): Flow<List<LullabyLocalEntity>>
    // ✅ NEW: Language-aware favourite lullabies

    fun getFavouriteLullabiesWithLocalizedNames(languageCode: String): Flow<List<LullabyWithLocalizedName>>

    fun getFavouriteStories(): Flow<List<StoryLocalEntity>>

    // ✅ NEW: Language-aware favourite stories
    fun getFavouriteStoriesWithLocalizedNames(languageCode: String): Flow<List<StoryWithLocalizedName>>

    // =====================================================
    // TRANSLATION OPERATIONS
    // =====================================================

    fun getAllTranslations(): Flow<List<TranslationLocalEntity>>

    suspend fun getTranslationByLullabyDocumentId(lullabyDocumentId: String): TranslationLocalEntity?

    suspend fun getTranslationByLullabyId(lullabyId: String): TranslationLocalEntity?

    suspend fun insertTranslation(translation: TranslationLocalEntity): Int

    suspend fun insertAllTranslations(translations: List<TranslationLocalEntity>): Int

    suspend fun updateTranslation(translation: TranslationLocalEntity): Int

    suspend fun deleteTranslation(translation: TranslationLocalEntity): Int

    suspend fun deleteAllTranslations(): Int

    suspend fun getTranslationCount(): Int

    // =====================================================
    // JOIN OPERATIONS (Lullaby + Translation)
    // =====================================================

    suspend fun getLullabyWithTranslation(documentId: String): LullabyWithTranslation?

    // ✅ OPTIMIZED: Single method for language-specific lullabies
    fun getAllLullabiesWithLocalizedNames(languageCode: String): Flow<List<LullabyWithLocalizedName>>

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

    // =====================================================
    // FAVOURITE METADATA OPERATIONS (LIFO ordering support)
    // =====================================================

    /**
     * Insert favourite metadata when user favourites an item
     * This enables LIFO ordering for favourites
     */
    suspend fun insertFavouriteMetadata(itemId: String, itemType: String): Int

    /**
     * Delete favourite metadata when user unfavourites an item
     */
    suspend fun deleteFavouriteMetadata(itemId: String, itemType: String): Int

    /**
     * Check if favourite metadata exists for an item
     */
    suspend fun hasFavouriteMetadata(itemId: String, itemType: String): Boolean

    /**
     * Get favourite count
     */
    suspend fun getFavouriteCount(): Int
}