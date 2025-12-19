package com.naptune.lullabyandstory.data.local.source.story

import android.util.Log
import com.naptune.lullabyandstory.data.local.dao.StoryDao
import com.naptune.lullabyandstory.data.local.entity.StoryLocalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of StoryLocalDataSource
 *
 * SOLID Principles Applied:
 * - Single Responsibility: Only handles story entity CRUD operations
 * - Dependency Inversion: Depends on DAO abstraction (interface)
 *
 * @param storyDao DAO for story database operations
 */
@Singleton
class StoryLocalDataSourceImpl @Inject constructor(
    private val storyDao: StoryDao
) : StoryLocalDataSource {

    // =====================================================
    // STORY CRUD OPERATIONS
    // =====================================================

    override suspend fun insertAllStories(stories: List<StoryLocalEntity>): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "💾 Inserting ${stories.size} stories...")
                storyDao.insertAllStories(stories)
                Log.d(TAG, "✅ Stories inserted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inserting stories: ${e.message}")
                throw e
            }
        }

    override fun getAllStories(): Flow<List<StoryLocalEntity>> {
        Log.d(TAG, "📖 Getting all stories from database...")
        return storyDao.getAllStories()
    }

    override suspend fun getStoriesCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                val count = storyDao.getStoriesCount()
                Log.d(TAG, "📊 Story count: $count")
                count
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting story count: ${e.message}")
                0
            }
        }

    override suspend fun deleteAllStories(): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🗑️ Deleting all stories...")
                storyDao.deleteAllStories()
                Log.d(TAG, "✅ All stories deleted")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting stories: ${e.message}")
                throw e
            }
        }

    override fun getFavouriteStories(): Flow<List<StoryLocalEntity>> {
        Log.d(TAG, "❤️ Getting favourite stories")
        return storyDao.getFavouriteStories()
    }

    override suspend fun toggleStoryFavourite(documentId: String): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "❤️ Toggling story favourite: $documentId")
                storyDao.toggleStoryFavourite(documentId)
                Log.d(TAG, "✅ Story favourite toggled successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error toggling story favourite: ${e.message}")
                throw e
            }
        }

    override fun checkIfItemIsFavourite(documentId: String): Flow<Boolean> {
        Log.d(TAG, "🔍 Checking if story is favourite: $documentId")
        return storyDao.checkIfItemIsFavourite(documentId)
    }

    companion object {
        private const val TAG = "StoryLocalDataSource"
    }
}
