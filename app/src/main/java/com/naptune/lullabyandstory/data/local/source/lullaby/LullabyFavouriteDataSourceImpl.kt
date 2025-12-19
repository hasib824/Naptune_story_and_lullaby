package com.naptune.lullabyandstory.data.local.source.lullaby

import android.util.Log
import com.naptune.lullabyandstory.data.local.dao.FavouriteMetadataDao
import com.naptune.lullabyandstory.data.local.dao.LullabyDao
import com.naptune.lullabyandstory.data.local.dao.LullabyTranslationDao
import com.naptune.lullabyandstory.data.local.dao.LullabyWithLocalizedName
import com.naptune.lullabyandstory.data.local.entity.FavouriteMetadataEntity
import com.naptune.lullabyandstory.data.local.entity.LullabyLocalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LullabyFavouriteDataSource for favourite operations.
 * Follows Single Responsibility Principle (SRP) - handles only favourite operations.
 */
@Singleton
class LullabyFavouriteDataSourceImpl @Inject constructor(
    private val lullabyDao: LullabyDao,
    private val lullabyTranslationDao: LullabyTranslationDao,
    private val favouriteMetadataDao: FavouriteMetadataDao
) : LullabyFavouriteDataSource {

    // =====================================================
    // FAVOURITE OPERATIONS
    // =====================================================

    override suspend fun toggleLullabyFavourite(lullabyId: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyFavouriteDataSourceImpl", "❤️ Toggling lullaby favourite: $lullabyId")
                lullabyDao.toggleLullabyFavourite(lullabyId)
                Log.d("LullabyFavouriteDataSourceImpl", "✅ Lullaby favourite toggled successfully")
            } catch (e: Exception) {
                Log.e("LullabyFavouriteDataSourceImpl", "❌ Error toggling lullaby favourite: ${e.message}")
                throw e
            }
        }

    override fun isLullabyFavourite(lullabyId: String): Flow<Boolean> {
        Log.d("LullabyFavouriteDataSourceImpl", "🔍 Checking if lullaby is favourite: $lullabyId")
        return lullabyDao.isLullabyFavourite(lullabyId)
    }

    override fun getFavouriteLullabies(): Flow<List<LullabyLocalEntity>> {
        Log.d("LullabyFavouriteDataSourceImpl", "❤️ Getting favourite lullabies")
        return lullabyDao.getFavouriteLullabies()
    }

    override fun getFavouriteLullabiesWithLocalizedNames(languageCode: String): Flow<List<LullabyWithLocalizedName>> {
        Log.d("LullabyFavouriteDataSourceImpl", "❤️ Getting favourite lullabies with localized names for language: $languageCode")
        return lullabyTranslationDao.getFavouriteLullabiesWithTranslations(languageCode)
    }

    // =====================================================
    // FAVOURITE METADATA OPERATIONS (LIFO ordering support)
    // =====================================================

    override suspend fun insertFavouriteMetadata(itemId: String, itemType: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyFavouriteDataSourceImpl", "❤️ Inserting favourite metadata: $itemId ($itemType)")
                val metadata = FavouriteMetadataEntity(
                    itemId = itemId,
                    itemType = itemType,
                    favouritedAt = System.currentTimeMillis()
                )
                favouriteMetadataDao.insertFavouriteMetadata(metadata)
                Log.d("LullabyFavouriteDataSourceImpl", "✅ Favourite metadata inserted successfully")
            } catch (e: Exception) {
                Log.e("LullabyFavouriteDataSourceImpl", "❌ Error inserting favourite metadata: ${e.message}")
                throw e
            }
        }

    override suspend fun deleteFavouriteMetadata(itemId: String, itemType: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyFavouriteDataSourceImpl", "💔 Deleting favourite metadata: $itemId ($itemType)")
                favouriteMetadataDao.deleteFavouriteMetadata(itemId, itemType)
                Log.d("LullabyFavouriteDataSourceImpl", "✅ Favourite metadata deleted successfully")
            } catch (e: Exception) {
                Log.e("LullabyFavouriteDataSourceImpl", "❌ Error deleting favourite metadata: ${e.message}")
                throw e
            }
        }

    override suspend fun hasFavouriteMetadata(itemId: String, itemType: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                favouriteMetadataDao.hasFavouriteMetadata(itemId, itemType)
            } catch (e: Exception) {
                Log.e("LullabyFavouriteDataSourceImpl", "❌ Error checking favourite metadata: ${e.message}")
                false
            }
        }

    override suspend fun getFavouriteCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                favouriteMetadataDao.getFavouriteCount()
            } catch (e: Exception) {
                Log.e("LullabyFavouriteDataSourceImpl", "❌ Error getting favourite count: ${e.message}")
                0
            }
        }
}
