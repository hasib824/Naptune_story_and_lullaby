package com.naptune.lullabyandstory.data.local.source.story

import android.util.Log
import com.naptune.lullabyandstory.data.local.dao.StoryAudioLanguageDao
import com.naptune.lullabyandstory.data.local.entity.StoryAudioLanguageLocalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of StoryAudioLanguageDataSource
 *
 * SOLID Principles Applied:
 * - Single Responsibility: Only handles story audio language operations
 * - Dependency Inversion: Depends on DAO abstraction (interface)
 *
 * @param storyAudioLanguageDao DAO for story audio language operations
 */
@Singleton
class StoryAudioLanguageDataSourceImpl @Inject constructor(
    private val storyAudioLanguageDao: StoryAudioLanguageDao
) : StoryAudioLanguageDataSource {

    // =====================================================
    // STORY AUDIO LANGUAGE OPERATIONS
    // =====================================================

    override suspend fun insertAllStoryAudioLanguages(storyAudioLanguages: List<StoryAudioLanguageLocalEntity>): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🎵 Inserting ${storyAudioLanguages.size} story audio languages...")
                storyAudioLanguageDao.insertAllStoryAudioLanguages(storyAudioLanguages)
                Log.d(TAG, "✅ Story audio languages inserted successfully")
                Log.d(TAG, "🔗 Story audio language relationships: ${storyAudioLanguages.map { it.storyDocumentId }}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inserting story audio languages: ${e.message}")
                throw e
            }
        }

    override suspend fun getStoryAudioLanguageByStoryDocumentId(storyDocumentId: String): StoryAudioLanguageLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔍 Getting story audio language for story document ID: $storyDocumentId")
                storyAudioLanguageDao.getStoryAudioLanguageByStoryDocumentId(storyDocumentId)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting story audio language: ${e.message}")
                null
            }
        }

    override suspend fun getStoryAudioLanguageCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                storyAudioLanguageDao.getStoryAudioLanguageCount()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting story audio language count: ${e.message}")
                0
            }
        }

    override suspend fun deleteAllStoryAudioLanguages(): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🗑️ Deleting all story audio languages...")
                storyAudioLanguageDao.deleteAllStoryAudioLanguages()
                Log.d(TAG, "✅ All story audio languages deleted")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting all story audio languages: ${e.message}")
                throw e
            }
        }

    companion object {
        private const val TAG = "StoryAudioLanguageDataSource"
    }
}
