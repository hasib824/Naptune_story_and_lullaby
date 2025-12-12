package com.naptune.lullabyandstory.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.naptune.lullabyandstory.data.local.entity.StoryAudioLanguageLocalEntity
import kotlinx.coroutines.flow.Flow

/**
 * ✅ StoryAudioLanguage DAO
 * Database access object for multilingual story audio paths
 *
 * Features language-aware queries with direct foreign key relationship
 * No mapping needed - clean document_id JOIN! 🎯
 */
@Dao
interface StoryAudioLanguageDao {

    /**
     * ✅ Insert all story audio language records with REPLACE strategy
     * OnConflict.REPLACE ensures fresh data on each sync
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStoryAudioLanguages(storyAudioLanguages: List<StoryAudioLanguageLocalEntity>)

    /**
     * ✅ Get story audio language by story document ID
     * Used for individual story audio path lookup
     */
    @Query("SELECT * FROM story_audio_language_table WHERE story_document_id = :storyDocumentId")
    suspend fun getStoryAudioLanguageByStoryDocumentId(storyDocumentId: String): StoryAudioLanguageLocalEntity?

    /**
     * ✅ Get all story audio languages
     * For debugging and admin purposes
     */
    @Query("SELECT * FROM story_audio_language_table")
    fun getAllStoryAudioLanguages(): Flow<List<StoryAudioLanguageLocalEntity>>

    /**
     * ✅ Get story audio language count
     * For sync validation and debugging
     */
    @Query("SELECT COUNT(*) FROM story_audio_language_table")
    suspend fun getStoryAudioLanguageCount(): Int

    /**
     * ✅ Delete all story audio languages
     * Used during fresh sync operations
     */
    @Query("DELETE FROM story_audio_language_table")
    suspend fun deleteAllStoryAudioLanguages()
}