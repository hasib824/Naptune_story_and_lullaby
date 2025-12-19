package com.naptune.lullabyandstory.data.local.source.lullaby

import android.util.Log
import com.naptune.lullabyandstory.data.local.dao.LullabyDao
import com.naptune.lullabyandstory.data.local.entity.LullabyLocalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LullabyLocalDataSource
 *
 * SOLID Principles Applied:
 * - Single Responsibility: Only handles lullaby entity CRUD operations
 * - Dependency Inversion: Depends on DAO abstraction (interface)
 *
 * @param lullabyDao DAO for lullaby database operations
 */
@Singleton
class LullabyLocalDataSourceImpl @Inject constructor(
    private val lullabyDao: LullabyDao
) : LullabyLocalDataSource {

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
                Log.d("LullabyDataSourceImpl", "💾 Inserting ${lullabies.size} lullabies...")
                lullabyDao.insertAllLullabies(lullabies)
                Log.d("LullabyDataSourceImpl", "✅ Lullabies inserted successfully")
            } catch (e: Exception) {
                Log.e("LullabyDataSourceImpl", "❌ Error inserting lullabies: ${e.message}")
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
            Log.d("LullabyDataSourceImpl", "💾 Marking lullaby as downloaded: $documentId")
            lullabyDao.updateIsDownloaded(documentId, true)
        }

    override suspend fun deleteLullaby(lullaby: LullabyLocalEntity) =
        withContext(Dispatchers.IO) {
            lullabyDao.deleteLullaby(lullaby)
        }

    override suspend fun deleteAllLullabies() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyDataSourceImpl", "🗑️ Deleting all lullabies...")
                lullabyDao.deleteAllLullabies()
                Log.d("LullabyDataSourceImpl", "✅ All lullabies deleted")
            } catch (e: Exception) {
                Log.e("LullabyDataSourceImpl", "❌ Error deleting lullabies: ${e.message}")
                throw e
            }
        }

    override suspend fun getLullabyCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                val count = lullabyDao.getLullabyCount()
                Log.d("LullabyDataSourceImpl", "📊 Lullaby count: $count")
                count
            } catch (e: Exception) {
                Log.e("LullabyDataSourceImpl", "❌ Error getting lullaby count: ${e.message}")
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

    override suspend fun toggleLullabyFavourite(lullabyId: String): Int =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "❤️ Toggling lullaby favourite: $lullabyId")
                lullabyDao.toggleLullabyFavourite(lullabyId)
                Log.d(TAG, "✅ Lullaby favourite toggled successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error toggling lullaby favourite: ${e.message}")
                throw e
            }
        }

    override fun isLullabyFavourite(lullabyId: String): Flow<Boolean> {
        Log.d(TAG, "🔍 Checking if lullaby is favourite: $lullabyId")
        return lullabyDao.isLullabyFavourite(lullabyId)
    }

    override fun getFavouriteLullabies(): Flow<List<LullabyLocalEntity>> {
        Log.d(TAG, "❤️ Getting favourite lullabies")
        return lullabyDao.getFavouriteLullabies()
    }

    companion object {
        private const val TAG = "LullabyLocalDataSource"
    }
}
