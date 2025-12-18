package com.naptune.lullabyandstory.data.local.source.lullaby

import android.util.Log
import com.naptune.lullabyandstory.data.local.dao.FavouriteMetadataDao
import com.naptune.lullabyandstory.data.local.dao.LullabyDao
import com.naptune.lullabyandstory.data.local.dao.LullabyWithLocalizedName
import com.naptune.lullabyandstory.data.local.dao.LullabyTranslationDao
import com.naptune.lullabyandstory.data.local.dao.LullabyWithTranslation
import com.naptune.lullabyandstory.data.local.entity.FavouriteMetadataEntity
import com.naptune.lullabyandstory.data.local.entity.LullabyLocalEntity
import com.naptune.lullabyandstory.data.local.entity.TranslationLocalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LullabyLocalDataSourceImpl @Inject constructor(
    private val lullabyDao: LullabyDao,
    private val lullabyTranslationDao: LullabyTranslationDao,
    private val favouriteMetadataDao: FavouriteMetadataDao
): LullabyLocalDataSource {

      // =====================================================
     // LULLABY OPERATIONS
    // =====================================================

    override fun getAllLullabies(): Flow<List<LullabyLocalEntity>> = lullabyDao.getAllLullabies()

    override suspend fun getLullabyById(documentId: String): LullabyLocalEntity? =
        withContext(Dispatchers.IO) {
            lullabyDao.getLullabyById(documentId)
        }

    override fun searchLullabies(query: String): Flow<List<LullabyLocalEntity>> =
        lullabyDao.searchLullabies(query)

    override suspend fun insertLullaby(lullaby: LullabyLocalEntity) =
        withContext(Dispatchers.IO) {
            lullabyDao.insertLullaby(lullaby)
        }

    override suspend fun insertAllLullabies(lullabies: List<LullabyLocalEntity>) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "💾 Inserting ${lullabies.size} lullabies...")
                lullabyDao.insertAllLullabies(lullabies)
                Log.d("LullabyLocalDataSourceImpl", "✅ Lullabies inserted successfully")
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error inserting lullabies: ${e.message}")
                throw e
            }
        }

    override suspend fun updateLullaby(lullaby: LullabyLocalEntity) =
        withContext(Dispatchers.IO) {
            lullabyDao.updateLullaby(lullaby)
        }

    override suspend fun updateLocalPath(musicLocalPath: String, documentId: String) =
        withContext(Dispatchers.IO) {
            lullabyDao.updateLocalPath(musicLocalPath, documentId)
        }

    override suspend fun markAsDownloaded(documentId: String): Int =
        withContext(Dispatchers.IO) {
            Log.d("LullabyLocalDataSourceImpl", "💾 Marking lullaby as downloaded: $documentId")
            lullabyDao.updateIsDownloaded(documentId, true)
        }

    override suspend fun deleteLullaby(lullaby: LullabyLocalEntity) =
        withContext(Dispatchers.IO) {
            lullabyDao.deleteLullaby(lullaby)
        }

    override suspend fun deleteAllLullabies() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "🗑️ Deleting all lullabies...")
                lullabyDao.deleteAllLullabies()
                Log.d("LullabyLocalDataSourceImpl", "✅ All lullabies deleted")
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error deleting lullabies: ${e.message}")
                throw e
            }
        }

    override suspend fun getLullabyCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                val count = lullabyDao.getLullabyCount()
                Log.d("LullabyLocalDataSourceImpl", "📊 Lullaby count: $count")
                count
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error getting lullaby count: ${e.message}")
                0
            }
        }

    override suspend fun getLullabiesPaginated(limit: Int, offset: Int): List<LullabyLocalEntity> =
        withContext(Dispatchers.IO) {
            lullabyDao.getLullabiesPaginated(limit, offset)
        }


    
    // =====================================================
    // FAVOURITE OPERATIONS
    // =====================================================
    
    override suspend fun toggleLullabyFavourite(lullabyId: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "❤️ Toggling lullaby favourite: $lullabyId")
                lullabyDao.toggleLullabyFavourite(lullabyId)
                Log.d("LullabyLocalDataSourceImpl", "✅ Lullaby favourite toggled successfully")
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error toggling lullaby favourite: ${e.message}")
                throw e
            }
        }
    
    override fun isLullabyFavourite(lullabyId: String): Flow<Boolean> {
        Log.d("LullabyLocalDataSourceImpl", "🔍 Checking if lullaby is favourite: $lullabyId")
        return lullabyDao.isLullabyFavourite(lullabyId)
    }
    
    override fun getFavouriteLullabies(): Flow<List<LullabyLocalEntity>> {
        Log.d("LullabyLocalDataSourceImpl", "❤️ Getting favourite lullabies")
        return lullabyDao.getFavouriteLullabies()
    }

    // ✅ NEW: Language-aware favourite lullabies
    override fun getFavouriteLullabiesWithLocalizedNames(languageCode: String): Flow<List<LullabyWithLocalizedName>> {
        Log.d("LullabyLocalDataSourceImpl", "❤️ Getting favourite lullabies with localized names for language: $languageCode")
        return lullabyTranslationDao.getFavouriteLullabiesWithTranslations(languageCode)
    }
    




    // =====================================================
    // TRANSLATION OPERATIONS
    // =====================================================

    override fun getAllTranslations(): Flow<List<TranslationLocalEntity>> {
        Log.d("LullabyLocalDataSourceImpl", "🌍 Getting all translations")
        return lullabyTranslationDao.getAllTranslations()
    }

    override suspend fun getTranslationByLullabyDocumentId(lullabyDocumentId: String): TranslationLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "🔍 Getting translation for lullaby document ID: $lullabyDocumentId")
                lullabyTranslationDao.getTranslationByLullabyDocumentId(lullabyDocumentId)
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error getting translation: ${e.message}")
                null
            }
        }

    override suspend fun getTranslationByLullabyId(lullabyId: String): TranslationLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "🔍 Getting translation for lullaby ID: $lullabyId")
                lullabyTranslationDao.getTranslationByLullabyId(lullabyId)
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error getting translation by lullaby ID: ${e.message}")
                null
            }
        }

    override suspend fun insertTranslation(translation: TranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "💾 Inserting translation: ${translation.lullabyId}")
                lullabyTranslationDao.insertTranslation(translation)
                Log.d("LullabyLocalDataSourceImpl", "✅ Translation inserted successfully")
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error inserting translation: ${e.message}")
                throw e
            }
        }

    override suspend fun insertAllTranslations(translations: List<TranslationLocalEntity>) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "💾 Inserting ${translations.size} translations...")
                lullabyTranslationDao.insertAllTranslations(translations)
                Log.d("LullabyLocalDataSourceImpl", "✅ Translations inserted successfully")
                Log.d("LullabyLocalDataSourceImpl", "🔗 Translation relationships: ${translations.map { "${it.lullabyId}->${it.lullabyDocumentId}" }}")
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error inserting translations: ${e.message}")
                throw e
            }
        }

    override suspend fun updateTranslation(translation: TranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "🔄 Updating translation: ${translation.lullabyId}")
                lullabyTranslationDao.updateTranslation(translation)
                Log.d("LullabyLocalDataSourceImpl", "✅ Translation updated successfully")
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error updating translation: ${e.message}")
                throw e
            }
        }

    override suspend fun deleteTranslation(translation: TranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "🗑️ Deleting translation: ${translation.lullabyId}")
                lullabyTranslationDao.deleteTranslation(translation)
                Log.d("LullabyLocalDataSourceImpl", "✅ Translation deleted successfully")
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error deleting translation: ${e.message}")
                throw e
            }
        }

    override suspend fun deleteAllTranslations() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "🗑️ Deleting all translations...")
                lullabyTranslationDao.deleteAllTranslations()
                Log.d("LullabyLocalDataSourceImpl", "✅ All translations deleted")
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error deleting all translations: ${e.message}")
                throw e
            }
        }

    override suspend fun getTranslationCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                val count = lullabyTranslationDao.getTranslationCount()
                Log.d("LullabyLocalDataSourceImpl", "📊 Translation count: $count")
                count
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error getting translation count: ${e.message}")
                0
            }
        }

    // =====================================================
    // JOIN OPERATIONS (Lullaby + Translation)
    // =====================================================

    override  suspend fun getLullabyWithTranslation(documentId: String): LullabyWithTranslation? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "🔗 Getting lullaby with translation for ID: $documentId")
                lullabyTranslationDao.getLullabyWithTranslation(documentId)
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error getting lullaby with translation: ${e.message}")
                null
            }
        }

    // ✅ OPTIMIZED: Single method for language-specific lullabies
    override   fun getAllLullabiesWithLocalizedNames(languageCode: String): Flow<List<LullabyWithLocalizedName>> {
        Log.d("LullabyLocalDataSourceImpl", "🚀 Getting all lullabies with localized names for language: $languageCode")
        return lullabyTranslationDao.getAllLullabiesWithTranslations(languageCode)
    }



    // =====================================================
    // FAVOURITE METADATA OPERATIONS (LIFO ordering support)
    // =====================================================

    /**
     * Insert favourite metadata when user favourites an item
     * This enables LIFO ordering for favourites
     */
    override  suspend fun insertFavouriteMetadata(itemId: String, itemType: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "❤️ Inserting favourite metadata: $itemId ($itemType)")
                val metadata = FavouriteMetadataEntity(
                    itemId = itemId,
                    itemType = itemType,
                    favouritedAt = System.currentTimeMillis()
                )
                favouriteMetadataDao.insertFavouriteMetadata(metadata)
                Log.d("LullabyLocalDataSourceImpl", "✅ Favourite metadata inserted successfully")
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error inserting favourite metadata: ${e.message}")
                throw e
            }
        }

    /**
     * Delete favourite metadata when user unfavourites an item
     */
    override  suspend fun deleteFavouriteMetadata(itemId: String, itemType: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "💔 Deleting favourite metadata: $itemId ($itemType)")
                favouriteMetadataDao.deleteFavouriteMetadata(itemId, itemType)
                Log.d("LullabyLocalDataSourceImpl", "✅ Favourite metadata deleted successfully")
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error deleting favourite metadata: ${e.message}")
                throw e
            }
        }

    /**
     * Check if favourite metadata exists for an item
     */
    override   suspend fun hasFavouriteMetadata(itemId: String, itemType: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                favouriteMetadataDao.hasFavouriteMetadata(itemId, itemType)
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error checking favourite metadata: ${e.message}")
                false
            }
        }

    /**
     * Get favourite count
     */
    override  suspend fun getFavouriteCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                favouriteMetadataDao.getFavouriteCount()
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error getting favourite count: ${e.message}")
                0
            }
        }
}
