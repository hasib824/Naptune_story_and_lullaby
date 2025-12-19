package com.naptune.lullabyandstory.data.local.source.lullaby

import com.naptune.lullabyandstory.data.local.dao.LullabyWithLocalizedName
import com.naptune.lullabyandstory.data.local.dao.LullabyWithTranslation
import com.naptune.lullabyandstory.data.local.entity.TranslationLocalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data source for Lullaby translation operations
 *
 * SOLID Principles:
 * - Single Responsibility: Only handles lullaby translation operations
 * - Interface Segregation: Clients only depend on translation operations
 * - Dependency Inversion: Repository depends on abstraction, not implementation
 *
 * Responsibilities:
 * - Manage lullaby translations (multi-language support)
 * - Provide localized lullaby queries
 * - Handle lullaby join operations with translations
 */
interface LullabyTranslationDataSource {

    // =====================================================
    // TRANSLATION CRUD OPERATIONS
    // =====================================================

    /**
     * Get all lullaby translations
     * @return Flow emitting list of all translations
     */
    fun getAllTranslations(): Flow<List<TranslationLocalEntity>>

    /**
     * Get translation by lullaby document ID
     * @param lullabyDocumentId The lullaby document ID
     * @return TranslationLocalEntity or null if not found
     */
    suspend fun getTranslationByLullabyDocumentId(lullabyDocumentId: String): TranslationLocalEntity?

    /**
     * Get translation by lullaby ID
     * @param lullabyId The lullaby ID
     * @return TranslationLocalEntity or null if not found
     */
    suspend fun getTranslationByLullabyId(lullabyId: String): TranslationLocalEntity?

    /**
     * Insert a single translation
     * @param translation The translation to insert
     * @return Number of rows inserted
     */
    suspend fun insertTranslation(translation: TranslationLocalEntity): Int

    /**
     * Insert multiple translations
     * @param translations List of translations to insert
     * @return Number of rows inserted
     */
    suspend fun insertAllTranslations(translations: List<TranslationLocalEntity>): Int

    /**
     * Update a translation
     * @param translation The translation to update
     * @return Number of rows updated
     */
    suspend fun updateTranslation(translation: TranslationLocalEntity): Int

    /**
     * Delete a translation
     * @param translation The translation to delete
     * @return Number of rows deleted
     */
    suspend fun deleteTranslation(translation: TranslationLocalEntity): Int

    /**
     * Delete all translations
     * @return Number of rows deleted
     */
    suspend fun deleteAllTranslations(): Int

    /**
     * Get total count of translations
     * @return Number of translations
     */
    suspend fun getTranslationCount(): Int

    // =====================================================
    // LOCALIZED LULLABY QUERIES (JOIN OPERATIONS)
    // =====================================================

    /**
     * Get a single lullaby with its translation
     * @param documentId The lullaby document ID
     * @return LullabyWithTranslation or null if not found
     */
    suspend fun getLullabyWithTranslation(documentId: String): LullabyWithTranslation?

    /**
     * Get all lullabies with localized names for a specific language
     * @param languageCode The language code (e.g., "en", "es", "fr")
     * @return Flow emitting list of lullabies with localized names
     */
    fun getAllLullabiesWithLocalizedNames(languageCode: String): Flow<List<LullabyWithLocalizedName>>

    /**
     * Get favourite lullabies with localized names for a specific language
     * @param languageCode The language code (e.g., "en", "es", "fr")
     * @return Flow emitting list of favourite lullabies with localized names
     */
    fun getFavouriteLullabiesWithLocalizedNames(languageCode: String): Flow<List<LullabyWithLocalizedName>>
}
