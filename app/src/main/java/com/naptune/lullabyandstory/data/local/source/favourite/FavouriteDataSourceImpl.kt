package com.naptune.lullabyandstory.data.local.source.favourite

import android.util.Log
import com.naptune.lullabyandstory.data.local.dao.FavouriteMetadataDao
import com.naptune.lullabyandstory.data.local.dao.LullabyDao
import com.naptune.lullabyandstory.data.local.dao.StoryDao
import com.naptune.lullabyandstory.data.local.entity.FavouriteMetadataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of FavouriteDataSource
 *
 * SOLID Principles Applied:
 * - Single Responsibility: Only handles favourite operations
 * - Dependency Inversion: Depends on DAO abstractions (interfaces)
 *
 * @param lullabyDao DAO for lullaby favourite operations
 * @param storyDao DAO for story favourite operations
 * @param favouriteMetadataDao DAO for favourite metadata (LIFO ordering)
 */
@Singleton
class FavouriteDataSourceImpl @Inject constructor(
    private val lullabyDao: LullabyDao,
    private val storyDao: StoryDao,
    private val favouriteMetadataDao: FavouriteMetadataDao
) : FavouriteDataSource {

    // =====================================================
    // FAVOURITE TOGGLE OPERATIONS
    // =====================================================

    override suspend fun toggleFavourite(documentId: String, itemType: String): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "❤️ Toggling favourite: $documentId ($itemType)")

                when (itemType) {
                    "lullaby" -> lullabyDao.toggleLullabyFavourite(documentId)
                    "story" -> storyDao.toggleStoryFavourite(documentId)
                    else -> {
                        Log.e(TAG, "❌ Unknown item type: $itemType")
                        throw IllegalArgumentException("Unknown item type: $itemType")
                    }
                }

                Log.d(TAG, "✅ Favourite toggled successfully")
                1 // Return success
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error toggling favourite: ${e.message}")
                throw e
            }
        }

    override fun checkIfItemIsFavourite(documentId: String): Flow<Boolean> {
        Log.d(TAG, "🔍 Checking if item is favourite: $documentId")
        // Try lullaby first, then story (same documentId could exist in both)
        return lullabyDao.isLullabyFavourite(documentId)
    }

    // =====================================================
    // FAVOURITE METADATA OPERATIONS (LIFO ordering support)
    // =====================================================

    override suspend fun insertFavouriteMetadata(itemId: String, itemType: String): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "❤️ Inserting favourite metadata: $itemId ($itemType)")
                val metadata = FavouriteMetadataEntity(
                    itemId = itemId,
                    itemType = itemType,
                    favouritedAt = System.currentTimeMillis()
                )
                favouriteMetadataDao.insertFavouriteMetadata(metadata)
                Log.d(TAG, "✅ Favourite metadata inserted successfully")
                1 // Return success
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inserting favourite metadata: ${e.message}")
                throw e
            }
        }

    override suspend fun deleteFavouriteMetadata(itemId: String, itemType: String): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "💔 Deleting favourite metadata: $itemId ($itemType)")
                favouriteMetadataDao.deleteFavouriteMetadata(itemId, itemType)
                Log.d(TAG, "✅ Favourite metadata deleted successfully")
                1 // Return success
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting favourite metadata: ${e.message}")
                throw e
            }
        }

    override suspend fun hasFavouriteMetadata(itemId: String, itemType: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                favouriteMetadataDao.hasFavouriteMetadata(itemId, itemType)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error checking favourite metadata: ${e.message}")
                false
            }
        }

    override suspend fun getFavouriteCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                favouriteMetadataDao.getFavouriteCount()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting favourite count: ${e.message}")
                0
            }
        }

    companion object {
        private const val TAG = "FavouriteDataSource"
    }
}
