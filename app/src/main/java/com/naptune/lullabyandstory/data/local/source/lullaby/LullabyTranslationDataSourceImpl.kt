package com.naptune.lullabyandstory.data.local.source.lullaby

import android.util.Log
import com.naptune.lullabyandstory.data.local.dao.LullabyTranslationDao
import com.naptune.lullabyandstory.data.local.dao.LullabyWithLocalizedName
import com.naptune.lullabyandstory.data.local.dao.LullabyWithTranslation
import com.naptune.lullabyandstory.data.local.entity.TranslationLocalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LullabyTranslationDataSource
 *
 * SOLID Principles Applied:
 * - Single Responsibility: Only handles lullaby translation operations
 * - Dependency Inversion: Depends on DAO abstraction (interface)
 *
 * @param lullabyTranslationDao DAO for lullaby translation operations
 */
@Singleton
class LullabyTranslationDataSourceImpl @Inject constructor(
    private val lullabyTranslationDao: LullabyTranslationDao
) : LullabyTranslationDataSource {

    // =====================================================
    // TRANSLATION OPERATIONS
    // =====================================================

    override fun getAllTranslations(): Flow<List<TranslationLocalEntity>> {
        Log.d("LullabyTranslationDataSourceImpl", "🌍 Getting all translations")
        return lullabyTranslationDao.getAllTranslations()
    }

    override suspend fun getTranslationByLullabyDocumentId(lullabyDocumentId: String): TranslationLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyTranslationDataSourceImpl", "🔍 Getting translation for lullaby document ID: $lullabyDocumentId")
                lullabyTranslationDao.getTranslationByLullabyDocumentId(lullabyDocumentId)
            } catch (e: Exception) {
                Log.e("LullabyTranslationDataSourceImpl", "❌ Error getting translation: ${e.message}")
                null
            }
        }

    override suspend fun getTranslationByLullabyId(lullabyId: String): TranslationLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyTranslationDataSourceImpl", "🔍 Getting translation for lullaby ID: $lullabyId")
                lullabyTranslationDao.getTranslationByLullabyId(lullabyId)
            } catch (e: Exception) {
                Log.e("LullabyTranslationDataSourceImpl", "❌ Error getting translation by lullaby ID: ${e.message}")
                null
            }
        }

    override suspend fun insertTranslation(translation: TranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyTranslationDataSourceImpl", "💾 Inserting translation: ${translation.lullabyId}")
                lullabyTranslationDao.insertTranslation(translation)
                Log.d("LullabyTranslationDataSourceImpl", "✅ Translation inserted successfully")
            } catch (e: Exception) {
                Log.e("LullabyTranslationDataSourceImpl", "❌ Error inserting translation: ${e.message}")
                throw e
            }
        }

    override suspend fun insertAllTranslations(translations: List<TranslationLocalEntity>) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyTranslationDataSourceImpl", "💾 Inserting ${translations.size} translations...")
                lullabyTranslationDao.insertAllTranslations(translations)
                Log.d("LullabyTranslationDataSourceImpl", "✅ Translations inserted successfully")
                Log.d("LullabyTranslationDataSourceImpl", "🔗 Translation relationships: ${translations.map { "${it.lullabyId}->${it.lullabyDocumentId}" }}")
            } catch (e: Exception) {
                Log.e("LullabyTranslationDataSourceImpl", "❌ Error inserting translations: ${e.message}")
                throw e
            }
        }

    override suspend fun updateTranslation(translation: TranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyTranslationDataSourceImpl", "🔄 Updating translation: ${translation.lullabyId}")
                lullabyTranslationDao.updateTranslation(translation)
                Log.d("LullabyTranslationDataSourceImpl", "✅ Translation updated successfully")
            } catch (e: Exception) {
                Log.e("LullabyTranslationDataSourceImpl", "❌ Error updating translation: ${e.message}")
                throw e
            }
        }

    override suspend fun deleteTranslation(translation: TranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyTranslationDataSourceImpl", "🗑️ Deleting translation: ${translation.lullabyId}")
                lullabyTranslationDao.deleteTranslation(translation)
                Log.d("LullabyTranslationDataSourceImpl", "✅ Translation deleted successfully")
            } catch (e: Exception) {
                Log.e("LullabyTranslationDataSourceImpl", "❌ Error deleting translation: ${e.message}")
                throw e
            }
        }

    override suspend fun deleteAllTranslations() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyTranslationDataSourceImpl", "🗑️ Deleting all translations...")
                lullabyTranslationDao.deleteAllTranslations()
                Log.d("LullabyTranslationDataSourceImpl", "✅ All translations deleted")
            } catch (e: Exception) {
                Log.e("LullabyTranslationDataSourceImpl", "❌ Error deleting all translations: ${e.message}")
                throw e
            }
        }

    override suspend fun getTranslationCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                val count = lullabyTranslationDao.getTranslationCount()
                Log.d(TAG, "📊 Translation count: $count")
                count
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting translation count: ${e.message}")
                0
            }
        }

    // =====================================================
    // LOCALIZED LULLABY QUERIES (JOIN OPERATIONS)
    // =====================================================

    override suspend fun getLullabyWithTranslation(documentId: String): LullabyWithTranslation? =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔗 Getting lullaby with translation for ID: $documentId")
                lullabyTranslationDao.getLullabyWithTranslation(documentId)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting lullaby with translation: ${e.message}")
                null
            }
        }

    override fun getAllLullabiesWithLocalizedNames(languageCode: String): Flow<List<LullabyWithLocalizedName>> {
        Log.d(TAG, "🚀 Getting all lullabies with localized names for language: $languageCode")
        return lullabyTranslationDao.getAllLullabiesWithTranslations(languageCode)
    }

    override fun getFavouriteLullabiesWithLocalizedNames(languageCode: String): Flow<List<LullabyWithLocalizedName>> {
        Log.d(TAG, "❤️ Getting favourite lullabies with localized names for language: $languageCode")
        return lullabyTranslationDao.getFavouriteLullabiesWithTranslations(languageCode)
    }

    companion object {
        private const val TAG = "LullabyTranslationDataSource"
    }
}
