package com.naptune.lullabyandstory.data.local.dao

import androidx.room.*
import com.naptune.lullabyandstory.data.local.entity.LullabyLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryLocalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LullabyDao {

    @Query("SELECT * FROM lullaby_table ORDER BY created_at DESC")
    fun getAllLullabies(): Flow<List<LullabyLocalEntity>>

    @Query("SELECT * FROM lullaby_table WHERE document_id = :documentId")
    suspend fun getLullabyById(documentId: String): LullabyLocalEntity?

    @Query("SELECT * FROM lullaby_table WHERE music_name LIKE '%' || :query || '%'")
    fun searchLullabies(query: String): Flow<List<LullabyLocalEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLullaby(lullaby: LullabyLocalEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllLullabies(lullabies: List<LullabyLocalEntity>)

    @Update
    suspend fun updateLullaby(lullaby: LullabyLocalEntity)

    @Query("UPDATE lullaby_table SET is_downloaded = :isDownloaded WHERE document_id = :documentId")
    suspend fun updateIsDownloaded(documentId: String, isDownloaded: Boolean): Int

    @Delete
    suspend fun deleteLullaby(lullaby: LullabyLocalEntity)

    @Query("DELETE FROM lullaby_table")
    suspend fun deleteAllLullabies()

    @Query("SELECT COUNT(*) FROM lullaby_table")
    suspend fun getLullabyCount(): Int

    @Query("SELECT * FROM lullaby_table LIMIT :limit OFFSET :offset")
    suspend fun getLullabiesPaginated(limit: Int, offset: Int): List<LullabyLocalEntity>

    // ✅ Lullaby favourite methods
    @Query("UPDATE lullaby_table SET is_favourite = NOT is_favourite WHERE document_id = :lullabyId")
    suspend fun toggleLullabyFavourite(lullabyId: String)

    @Query("SELECT is_favourite FROM lullaby_table WHERE document_id = :lullabyId")
    fun isLullabyFavourite(lullabyId: String): Flow<Boolean>

    // ✅ LIFO-ordered favourite lullabies using JOIN with metadata table
    @Query("""
        SELECT l.* FROM lullaby_table l
        INNER JOIN favourite_metadata_table fm ON l.document_id = fm.item_id
        WHERE l.is_favourite = 1 AND fm.item_type = 'lullaby'
        ORDER BY fm.favourited_at DESC
    """)
    fun getFavouriteLullabies(): Flow<List<LullabyLocalEntity>>

    @Query("UPDATE lullaby_table  SET music_local_path = :musicLocalPath WHERE document_id = :documentId")
    suspend fun updateLocalPath(musicLocalPath: String, documentId: String)

}