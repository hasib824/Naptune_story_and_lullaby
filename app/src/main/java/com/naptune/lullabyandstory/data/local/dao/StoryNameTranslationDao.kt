package com.naptune.lullabyandstory.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.naptune.lullabyandstory.data.local.entity.StoryLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryNameTranslationLocalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryNameTranslationDao {

    @Query("SELECT * FROM story_name_translation_table")
    fun getAllStoryNameTranslations(): Flow<List<StoryNameTranslationLocalEntity>>

    @Query("SELECT * FROM story_name_translation_table WHERE story_document_id = :storyDocumentId LIMIT 1")
    suspend fun getStoryNameTranslationByDocumentId(storyDocumentId: String): StoryNameTranslationLocalEntity?

    @Query("SELECT * FROM story_name_translation_table WHERE story_id = :storyId LIMIT 1")
    suspend fun getStoryNameTranslationById(storyId: String): StoryNameTranslationLocalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStoryNameTranslations(storyNameTranslations: List<StoryNameTranslationLocalEntity>)

    @Update
    suspend fun updateStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity)

    @Delete
    suspend fun deleteStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity)

    @Query("DELETE FROM story_name_translation_table")
    suspend fun deleteAllStoryNameTranslations()

    @Query("SELECT COUNT(*) FROM story_name_translation_table")
    suspend fun getStoryNameTranslationCount(): Int

    // Join query for getting story with name translation
    @Query("""
        SELECT s.*, t.story_name_en, t.story_name_es, t.story_name_fr,
               t.story_name_de, t.story_name_pt, t.story_name_hi, t.story_name_ar
        FROM story_table s
        LEFT JOIN story_name_translation_table t ON s.document_id = t.story_document_id
        WHERE s.document_id = :documentId
        LIMIT 1
    """)
    suspend fun getStoryWithNameTranslation(documentId: String): StoryWithNameTranslation?

    // ✅ ULTRA OPTIMIZED: Get all stories with language-specific name translations
    @Query("""
        SELECT s.*,
        COALESCE(
            CASE :languageCode
                WHEN 'en' THEN t.story_name_en
                WHEN 'es' THEN t.story_name_es
                WHEN 'fr' THEN t.story_name_fr
                WHEN 'de' THEN t.story_name_de
                WHEN 'pt' THEN t.story_name_pt
                WHEN 'hi' THEN t.story_name_hi
                WHEN 'ar' THEN t.story_name_ar
                ELSE t.story_name_en
            END,
            s.story_name
        ) as localized_story_name
        FROM story_table s
        LEFT JOIN story_name_translation_table t ON s.document_id = t.story_document_id
    """)
    fun getAllStoriesWithNameTranslations(languageCode: String): Flow<List<StoryWithLocalizedName>>

    // ✅ LIFO-ordered favourite stories with language-specific name translations
    @Query("""
        SELECT s.*,
        COALESCE(
            CASE :languageCode
                WHEN 'en' THEN t.story_name_en
                WHEN 'es' THEN t.story_name_es
                WHEN 'fr' THEN t.story_name_fr
                WHEN 'de' THEN t.story_name_de
                WHEN 'pt' THEN t.story_name_pt
                WHEN 'hi' THEN t.story_name_hi
                WHEN 'ar' THEN t.story_name_ar
                ELSE t.story_name_en
            END,
            s.story_name
        ) as localized_story_name
        FROM story_table s
        LEFT JOIN story_name_translation_table t ON s.document_id = t.story_document_id
        INNER JOIN favourite_metadata_table fm ON s.document_id = fm.item_id
        WHERE s.is_favourite = 1 AND fm.item_type = 'story'
        ORDER BY fm.favourited_at DESC
    """)
    fun getFavouriteStoriesWithNameTranslations(languageCode: String): Flow<List<StoryWithLocalizedName>>



    // 🚀 ULTRA OPTIMIZED: Get all stories with BOTH name + description translations +  audio_path  (O(1) performance)
    @Query("""
    SELECT s.*,
    COALESCE(
        NULLIF(
            CASE :languageCode
                WHEN 'en' THEN snt.story_name_en
                WHEN 'es' THEN snt.story_name_es
                WHEN 'fr' THEN snt.story_name_fr
                WHEN 'de' THEN snt.story_name_de
                WHEN 'pt' THEN snt.story_name_pt
                WHEN 'hi' THEN snt.story_name_hi
                WHEN 'ar' THEN snt.story_name_ar
                ELSE snt.story_name_en
            END, ''
        ),
        s.story_name
    ) as localized_story_name,
    COALESCE(
        NULLIF(
            CASE :languageCode
                WHEN 'en' THEN sdt.story_description_en
                WHEN 'es' THEN sdt.story_description_es
                WHEN 'fr' THEN sdt.story_description_fr
                WHEN 'de' THEN sdt.story_description_de
                WHEN 'pt' THEN sdt.story_description_pt
                WHEN 'hi' THEN sdt.story_description_hi
                WHEN 'ar' THEN sdt.story_description_ar
                ELSE sdt.story_description_en
            END, ''
        ),
        s.story_description
    ) as localized_story_description,
    COALESCE(
        NULLIF(
            CASE :languageCode
                WHEN 'en' THEN sal.audio_path_en
                WHEN 'es' THEN sal.audio_path_es
                WHEN 'fr' THEN sal.audio_path_fr
                WHEN 'de' THEN sal.audio_path_de
                WHEN 'pt' THEN sal.audio_path_pt
                WHEN 'hi' THEN sal.audio_path_hi
                WHEN 'ar' THEN sal.audio_path_ar
                ELSE sal.audio_path_en
            END, ''
        ),
        s.story_audio_path
    ) as localized_audio_path
    FROM story_table s
    LEFT JOIN story_name_translation_table snt ON s.document_id = snt.story_document_id
    LEFT JOIN story_description_translation_table sdt ON s.document_id = sdt.story_document_id
    LEFT JOIN story_audio_language_table sal ON s.document_id = sal.story_document_id
""")
    fun getAllStoriesWithFullLocalization(languageCode: String): Flow<List<StoryWithFullLocalization>>
    // 🚀 ULTRA OPTIMIZED: Get all stories with BOTH name + description translations +  audio_path  (O(1) performance)
/*    @Query("""
        SELECT s.*,
        COALESCE(
            CASE :languageCode
                WHEN 'en' THEN snt.story_name_en
                WHEN 'es' THEN snt.story_name_es
                WHEN 'fr' THEN snt.story_name_fr
                WHEN 'de' THEN snt.story_name_de
                WHEN 'pt' THEN snt.story_name_pt
                WHEN 'hi' THEN snt.story_name_hi
                WHEN 'ar' THEN snt.story_name_ar
                ELSE snt.story_name_en
            END,
            s.story_name
        ) as localized_story_name,
        COALESCE(
            CASE :languageCode
                WHEN 'en' THEN sdt.story_description_en
                WHEN 'es' THEN sdt.story_description_es
                WHEN 'fr' THEN sdt.story_description_fr
                WHEN 'de' THEN sdt.story_description_de
                WHEN 'pt' THEN sdt.story_description_pt
                WHEN 'hi' THEN sdt.story_description_hi
                WHEN 'ar' THEN sdt.story_description_ar
                ELSE sdt.story_description_en
            END,
            s.story_description
        ) as localized_story_description,
        COALESCE(
            CASE :languageCode
                WHEN 'en' THEN sal.audio_path_en
                WHEN 'es' THEN sal.audio_path_es
                WHEN 'fr' THEN sal.audio_path_fr
                WHEN 'de' THEN sal.audio_path_de
                WHEN 'pt' THEN sal.audio_path_pt
                WHEN 'hi' THEN sal.audio_path_hi
                WHEN 'ar' THEN sal.audio_path_ar
                ELSE sal.audio_path_en
            END,
            s.story_audio_path
        ) as localized_audio_path
        FROM story_table s
        LEFT JOIN story_name_translation_table snt ON s.document_id = snt.story_document_id
        LEFT JOIN story_description_translation_table sdt ON s.document_id = sdt.story_document_id
        LEFT JOIN story_audio_language_table sal ON s.document_id = sal.story_document_id
    """)
    fun getAllStoriesWithFullLocalization(languageCode: String): Flow<List<StoryWithFullLocalization>>*/

    // 🚀 LIFO-ordered favourite stories with name + description + audio translations
    @Query("""
    SELECT s.*,
    COALESCE(
        NULLIF(
            CASE :languageCode
                WHEN 'en' THEN snt.story_name_en
                WHEN 'es' THEN snt.story_name_es
                WHEN 'fr' THEN snt.story_name_fr
                WHEN 'de' THEN snt.story_name_de
                WHEN 'pt' THEN snt.story_name_pt
                WHEN 'hi' THEN snt.story_name_hi
                WHEN 'ar' THEN snt.story_name_ar
                ELSE snt.story_name_en
            END, ''
        ),
        s.story_name
    ) as localized_story_name,
    COALESCE(
        NULLIF(
            CASE :languageCode
                WHEN 'en' THEN sdt.story_description_en
                WHEN 'es' THEN sdt.story_description_es
                WHEN 'fr' THEN sdt.story_description_fr
                WHEN 'de' THEN sdt.story_description_de
                WHEN 'pt' THEN sdt.story_description_pt
                WHEN 'hi' THEN sdt.story_description_hi
                WHEN 'ar' THEN sdt.story_description_ar
                ELSE sdt.story_description_en
            END, ''
        ),
        s.story_description
    ) as localized_story_description,
    COALESCE(
        NULLIF(
            CASE :languageCode
                WHEN 'en' THEN sal.audio_path_en
                WHEN 'es' THEN sal.audio_path_es
                WHEN 'fr' THEN sal.audio_path_fr
                WHEN 'de' THEN sal.audio_path_de
                WHEN 'pt' THEN sal.audio_path_pt
                WHEN 'hi' THEN sal.audio_path_hi
                WHEN 'ar' THEN sal.audio_path_ar
                ELSE sal.audio_path_en
            END, ''
        ),
        s.story_audio_path
    ) as localized_audio_path
        FROM story_table s
        LEFT JOIN story_name_translation_table snt ON s.document_id = snt.story_document_id
        LEFT JOIN story_description_translation_table sdt ON s.document_id = sdt.story_document_id
        LEFT JOIN story_audio_language_table sal ON s.document_id = sal.story_document_id
        INNER JOIN favourite_metadata_table fm ON s.document_id = fm.item_id
        WHERE s.is_favourite = 1 AND fm.item_type = 'story'
        ORDER BY fm.favourited_at DESC
    """)
    fun getFavouriteStoriesWithFullLocalization(languageCode: String): Flow<List<StoryWithFullLocalization>>

}

// 🚀 ULTRA OPTIMIZED: Data class for FULL localization queries (Name + Description + Audio)
data class StoryWithFullLocalization(
    @Embedded val story: StoryLocalEntity,
    // Pre-computed localized name, description, and audio path from database query
    @ColumnInfo(name = "localized_story_name") val localizedStoryName: String,
    @ColumnInfo(name = "localized_story_description") val localizedStoryDescription: String,
    @ColumnInfo(name = "localized_audio_path") val localizedAudioPath: String
)

// Data class for joined result
data class StoryWithNameTranslation(
    @Embedded val story: StoryLocalEntity,

    // Translation fields with proper column mapping
    @ColumnInfo(name = "story_name_en") val storyNameEn: String? = null,
    @ColumnInfo(name = "story_name_es") val storyNameEs: String? = null,
    @ColumnInfo(name = "story_name_fr") val storyNameFr: String? = null,
    @ColumnInfo(name = "story_name_de") val storyNameDe: String? = null,
    @ColumnInfo(name = "story_name_pt") val storyNamePt: String? = null,
    @ColumnInfo(name = "story_name_hi") val storyNameHi: String? = null,
    @ColumnInfo(name = "story_name_ar") val storyNameAr: String? = null
)

// ✅ ULTRA OPTIMIZED: Data class for language-specific queries (reusing from previous file)
data class StoryWithLocalizedName(
    @Embedded val story: StoryLocalEntity,

    // Pre-computed localized name from database query
    @ColumnInfo(name = "localized_story_name") val localizedStoryName: String
)