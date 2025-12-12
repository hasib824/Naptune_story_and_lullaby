package com.naptune.lullabyandstory.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.naptune.lullabyandstory.data.local.entity.StoryLocalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllStories(stories: List<StoryLocalEntity>)

    @Query("SELECT * FROM story_table")
    fun getAllStories(): Flow<List<StoryLocalEntity>>

    @Query("SELECT COUNT(*) FROM story_table")
    suspend fun getStoriesCount(): Int

    @Query("UPDATE story_table SET is_favourite = NOT is_favourite WHERE document_id = :documentId")
    suspend fun toggleStoryFavourite(documentId: String)

    @Query("SELECT is_favourite FROM story_table WHERE document_id = :documentId")
    fun checkIfItemIsFavourite(documentId: String): Flow<Boolean>
    
    // ✅ LIFO-ordered favourite stories using JOIN with metadata table
    @Query("""
        SELECT s.* FROM story_table s
        INNER JOIN favourite_metadata_table fm ON s.document_id = fm.item_id
        WHERE s.is_favourite = 1 AND fm.item_type = 'story'
        ORDER BY fm.favourited_at DESC
    """)
    fun getFavouriteStories(): Flow<List<StoryLocalEntity>>

    @Query("DELETE FROM story_table")
    suspend fun deleteAllStories()
}
