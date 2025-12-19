package com.naptune.lullabyandstory.data.local.source.story

import android.util.Log
import com.naptune.lullabyandstory.data.local.dao.StoryDescriptionTranslationDao
import com.naptune.lullabyandstory.data.local.dao.StoryNameTranslationDao
import com.naptune.lullabyandstory.data.local.dao.StoryWithFullLocalization
import com.naptune.lullabyandstory.data.local.dao.StoryWithLocalizedName
import com.naptune.lullabyandstory.data.local.dao.StoryWithNameTranslation
import com.naptune.lullabyandstory.data.local.entity.StoryDescriptionTranslationLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryNameTranslationLocalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of StoryTranslationDataSource
 *
 * SOLID Principles Applied:
 * - Single Responsibility: Only handles story translation operations
 * - Dependency Inversion: Depends on DAO abstractions (interfaces)
 *
 * @param storyNameTranslationDao DAO for story name translations
 * @param storyDescriptionTranslationDao DAO for story description translations
 */
@Singleton
class StoryTranslationDataSourceImpl @Inject constructor(
    private val storyNameTranslationDao: StoryNameTranslationDao,
    private val storyDescriptionTranslationDao: StoryDescriptionTranslationDao
) : StoryTranslationDataSource {

    // =====================================================
    // STORY NAME TRANSLATION OPERATIONS
    // =====================================================

    override fun getAllStoryNameTranslations(): Flow<List<StoryNameTranslationLocalEntity>> {
        Log.d(TAG, "🌍 Getting all story name translations")
        return storyNameTranslationDao.getAllStoryNameTranslations()
    }

    override suspend fun getStoryNameTranslationByDocumentId(storyDocumentId: String): StoryNameTranslationLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔍 Getting story name translation for document ID: $storyDocumentId")
                storyNameTranslationDao.getStoryNameTranslationByDocumentId(storyDocumentId)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting story name translation: ${e.message}")
                null
            }
        }

    override suspend fun getStoryNameTranslationById(storyId: String): StoryNameTranslationLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔍 Getting story name translation for story ID: $storyId")
                storyNameTranslationDao.getStoryNameTranslationById(storyId)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting story name translation by story ID: ${e.message}")
                null
            }
        }

    override suspend fun insertStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "💾 Inserting story name translation: ${storyNameTranslation.storyId}")
                storyNameTranslationDao.insertStoryNameTranslation(storyNameTranslation)
                Log.d(TAG, "✅ Story name translation inserted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inserting story name translation: ${e.message}")
                throw e
            }
        }

    override suspend fun insertAllStoryNameTranslations(storyNameTranslations: List<StoryNameTranslationLocalEntity>): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "💾 Inserting ${storyNameTranslations.size} story name translations...")
                storyNameTranslationDao.insertAllStoryNameTranslations(storyNameTranslations)
                Log.d(TAG, "✅ Story name translations inserted successfully")
                Log.d(TAG, "🔗 Story name translation relationships: ${storyNameTranslations.map { "${it.storyId}->${it.storyDocumentId}" }}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inserting story name translations: ${e.message}")
                throw e
            }
        }

    override suspend fun updateStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Updating story name translation: ${storyNameTranslation.storyId}")
                storyNameTranslationDao.updateStoryNameTranslation(storyNameTranslation)
                Log.d(TAG, "✅ Story name translation updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error updating story name translation: ${e.message}")
                throw e
            }
        }

    override suspend fun deleteStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🗑️ Deleting story name translation: ${storyNameTranslation.storyId}")
                storyNameTranslationDao.deleteStoryNameTranslation(storyNameTranslation)
                Log.d(TAG, "✅ Story name translation deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting story name translation: ${e.message}")
                throw e
            }
        }

    override suspend fun deleteAllStoryNameTranslations(): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🗑️ Deleting all story name translations...")
                storyNameTranslationDao.deleteAllStoryNameTranslations()
                Log.d(TAG, "✅ All story name translations deleted")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting all story name translations: ${e.message}")
                throw e
            }
        }

    override suspend fun getStoryNameTranslationCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                val count = storyNameTranslationDao.getStoryNameTranslationCount()
                Log.d(TAG, "📊 Story name translation count: $count")
                count
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting story name translation count: ${e.message}")
                0
            }
        }

    // =====================================================
    // STORY DESCRIPTION TRANSLATION OPERATIONS
    // =====================================================

    override suspend fun insertAllStoryDescriptionTranslations(storyDescriptionTranslations: List<StoryDescriptionTranslationLocalEntity>): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "💾 Inserting ${storyDescriptionTranslations.size} story description translations...")
                storyDescriptionTranslationDao.insertAllStoryDescriptionTranslations(storyDescriptionTranslations)
                Log.d(TAG, "✅ Story description translations inserted successfully")
                Log.d(TAG, "🔗 Story description translation relationships: ${storyDescriptionTranslations.map { "${it.storyId}->${it.storyDocumentId}" }}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inserting story description translations: ${e.message}")
                throw e
            }
        }

    override suspend fun deleteAllStoryDescriptionTranslations(): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🗑️ Deleting all story description translations...")
                storyDescriptionTranslationDao.deleteAllStoryDescriptionTranslations()
                Log.d(TAG, "✅ All story description translations deleted")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting all story description translations: ${e.message}")
                throw e
            }
        }

    // =====================================================
    // LOCALIZED STORY QUERIES (JOIN OPERATIONS)
    // =====================================================

    override fun getAllStoriesWithLocalizedNames(languageCode: String): Flow<List<StoryWithLocalizedName>> {
        Log.d(TAG, "🚀 Getting all stories with localized names for language: $languageCode")
        return storyNameTranslationDao.getAllStoriesWithNameTranslations(languageCode)
    }

    override fun getFavouriteStoriesWithLocalizedNames(languageCode: String): Flow<List<StoryWithLocalizedName>> {
        Log.d(TAG, "❤️ Getting favourite stories with localized names for language: $languageCode")
        return storyNameTranslationDao.getFavouriteStoriesWithNameTranslations(languageCode)
    }

    override fun getAllStoriesWithFullLocalization(languageCode: String): Flow<List<StoryWithFullLocalization>> {
        Log.d(TAG, "🚀 ULTRA OPTIMIZED: Getting all stories with FULL localization for language: $languageCode")
        return storyNameTranslationDao.getAllStoriesWithFullLocalization(languageCode)
    }

    override fun getFavouriteStoriesWithFullLocalization(languageCode: String): Flow<List<StoryWithFullLocalization>> {
        Log.d(TAG, "❤️ ULTRA OPTIMIZED: Getting favourite stories with FULL localization for language: $languageCode")
        return storyNameTranslationDao.getFavouriteStoriesWithFullLocalization(languageCode)
    }

    override suspend fun getStoryWithNameTranslation(documentId: String): StoryWithNameTranslation? =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔗 Getting story with name translation for ID: $documentId")
                storyNameTranslationDao.getStoryWithNameTranslation(documentId)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting story with name translation: ${e.message}")
                null
            }
        }

    companion object {
        private const val TAG = "StoryTranslationDataSource"
    }
}
