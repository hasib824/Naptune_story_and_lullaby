package com.naptune.lullabyandstory.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.naptune.lullabyandstory.data.local.entity.LullabyLocalEntity
import com.naptune.lullabyandstory.data.local.entity.TranslationLocalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {

    @Query("SELECT * FROM translation_table")
    fun getAllTranslations(): Flow<List<TranslationLocalEntity>>

    @Query("SELECT * FROM translation_table WHERE lullaby_document_id = :lullabyDocumentId LIMIT 1")
    suspend fun getTranslationByLullabyDocumentId(lullabyDocumentId: String): TranslationLocalEntity?

    @Query("SELECT * FROM translation_table WHERE lullaby_id = :lullabyId LIMIT 1")
    suspend fun getTranslationByLullabyId(lullabyId: String): TranslationLocalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(translation: TranslationLocalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTranslations(translations: List<TranslationLocalEntity>)

    @Update
    suspend fun updateTranslation(translation: TranslationLocalEntity)

    @Delete
    suspend fun deleteTranslation(translation: TranslationLocalEntity)

    @Query("DELETE FROM translation_table")
    suspend fun deleteAllTranslations()

    @Query("SELECT COUNT(*) FROM translation_table")
    suspend fun getTranslationCount(): Int

    // Join query for getting lullaby with translation
    @Query("""
        SELECT l.*, t.music_name_en, t.music_name_es, t.music_name_fr,
               t.music_name_de, t.music_name_pt, t.music_name_hi, t.music_name_ar
        FROM lullaby_table l
        LEFT JOIN translation_table t ON l.document_id = t.lullaby_document_id
        WHERE l.document_id = :documentId
        LIMIT 1
    """)
    suspend fun getLullabyWithTranslation(documentId: String): LullabyWithTranslation?

    // ✅ OPTIMIZED: Get all lullabies with language-specific translations
    @Query("""
        SELECT l.*,
        COALESCE(
            CASE :languageCode
                WHEN 'en' THEN t.music_name_en
                WHEN 'es' THEN t.music_name_es
                WHEN 'fr' THEN t.music_name_fr
                WHEN 'de' THEN t.music_name_de
                WHEN 'pt' THEN t.music_name_pt
                WHEN 'hi' THEN t.music_name_hi
                WHEN 'ar' THEN t.music_name_ar
                ELSE t.music_name_en
            END,
            l.music_name
        ) as localized_music_name
        FROM lullaby_table l
        LEFT JOIN translation_table t ON l.document_id = t.lullaby_document_id
    """)
    fun getAllLullabiesWithTranslations(languageCode: String): Flow<List<LullabyWithLocalizedName>>

    // ✅ LIFO-ordered favourite lullabies with language-specific translations
    @Query("""
        SELECT l.*,
        COALESCE(
            CASE :languageCode
                WHEN 'en' THEN t.music_name_en
                WHEN 'es' THEN t.music_name_es
                WHEN 'fr' THEN t.music_name_fr
                WHEN 'de' THEN t.music_name_de
                WHEN 'pt' THEN t.music_name_pt
                WHEN 'hi' THEN t.music_name_hi
                WHEN 'ar' THEN t.music_name_ar
                ELSE t.music_name_en
            END,
            l.music_name
        ) as localized_music_name
        FROM lullaby_table l
        LEFT JOIN translation_table t ON l.document_id = t.lullaby_document_id
        INNER JOIN favourite_metadata_table fm ON l.document_id = fm.item_id
        WHERE l.is_favourite = 1 AND fm.item_type = 'lullaby'
        ORDER BY fm.favourited_at DESC
    """)
    fun getFavouriteLullabiesWithTranslations(languageCode: String): Flow<List<LullabyWithLocalizedName>>

}

// Data class for joined result
data class LullabyWithTranslation(
    @Embedded val lullaby: LullabyLocalEntity,

    // Translation fields with proper column mapping
    @ColumnInfo(name = "music_name_en") val musicNameEn: String? = null,
    @ColumnInfo(name = "music_name_es") val musicNameEs: String? = null,
    @ColumnInfo(name = "music_name_fr") val musicNameFr: String? = null,
    @ColumnInfo(name = "music_name_de") val musicNameDe: String? = null,
    @ColumnInfo(name = "music_name_pt") val musicNamePt: String? = null,
    @ColumnInfo(name = "music_name_hi") val musicNameHi: String? = null,
    @ColumnInfo(name = "music_name_ar") val musicNameAr: String? = null
)

// ✅ OPTIMIZED: Data class for language-specific queries
data class LullabyWithLocalizedName(
    @Embedded val lullaby: LullabyLocalEntity,

    // Pre-computed localized name from database query
    @ColumnInfo(name = "localized_music_name") val localizedMusicName: String
)