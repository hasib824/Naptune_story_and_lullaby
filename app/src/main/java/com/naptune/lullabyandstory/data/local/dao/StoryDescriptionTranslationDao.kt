package com.naptune.lullabyandstory.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.naptune.lullabyandstory.data.local.entity.StoryDescriptionTranslationLocalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDescriptionTranslationDao {

    @Query("SELECT * FROM story_description_translation_table")
    fun getAllStoryDescriptionTranslations(): Flow<List<StoryDescriptionTranslationLocalEntity>>

    @Query("SELECT * FROM story_description_translation_table WHERE story_document_id = :storyDocumentId LIMIT 1")
    suspend fun getStoryDescriptionTranslationByDocumentId(storyDocumentId: String): StoryDescriptionTranslationLocalEntity?

    @Query("SELECT * FROM story_description_translation_table WHERE story_id = :storyId LIMIT 1")
    suspend fun getStoryDescriptionTranslationById(storyId: String): StoryDescriptionTranslationLocalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryDescriptionTranslation(storyDescriptionTranslation: StoryDescriptionTranslationLocalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStoryDescriptionTranslations(storyDescriptionTranslations: List<StoryDescriptionTranslationLocalEntity>)

    @Update
    suspend fun updateStoryDescriptionTranslation(storyDescriptionTranslation: StoryDescriptionTranslationLocalEntity)

    @Delete
    suspend fun deleteStoryDescriptionTranslation(storyDescriptionTranslation: StoryDescriptionTranslationLocalEntity)

    @Query("DELETE FROM story_description_translation_table")
    suspend fun deleteAllStoryDescriptionTranslations()

    @Query("SELECT COUNT(*) FROM story_description_translation_table")
    suspend fun getStoryDescriptionTranslationCount(): Int
}