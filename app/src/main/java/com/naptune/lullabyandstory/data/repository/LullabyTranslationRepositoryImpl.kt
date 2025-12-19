package com.naptune.lullabyandstory.data.repository

import android.util.Log
import com.naptune.lullabyandstory.data.local.dao.LullabyWithTranslation
import com.naptune.lullabyandstory.data.local.source.lullaby.LullabyTranslationDataSource
import com.naptune.lullabyandstory.data.mapper.toDomainModel
import com.naptune.lullabyandstory.domain.model.TranslationDomainModel
import com.naptune.lullabyandstory.domain.repository.LullabyTranslationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LullabyTranslationRepository for translation operations.
 * Follows Single Responsibility Principle (SRP) - handles only translation data.
 */
@Singleton
class LullabyTranslationRepositoryImpl @Inject constructor(
    private val lullabyTranslationDataSource: LullabyTranslationDataSource,
) : LullabyTranslationRepository {

    override suspend fun getTranslationByLullabyDocumentId(lullabyDocumentId: String): TranslationDomainModel? {
        return try {
            Log.d("LullabyTranslationRepositoryImpl", "🔍 Getting translation for lullaby document ID: $lullabyDocumentId")
            val translationEntity = lullabyTranslationDataSource.getTranslationByLullabyDocumentId(lullabyDocumentId)
            translationEntity?.toDomainModel()
        } catch (e: Exception) {
            Log.e("LullabyTranslationRepositoryImpl", "❌ Error getting translation: ${e.message}")
            null
        }
    }

    override suspend fun getLullabyWithTranslation(documentId: String): LullabyWithTranslation? {
        return try {
            Log.d("LullabyTranslationRepositoryImpl", "🔗 Getting lullaby with translation for ID: $documentId")
            lullabyTranslationDataSource.getLullabyWithTranslation(documentId)
        } catch (e: Exception) {
            Log.e("LullabyTranslationRepositoryImpl", "❌ Error getting lullaby with translation: ${e.message}")
            null
        }
    }

    override suspend fun getTranslationCount(): Int {
        return try {
            val count = lullabyTranslationDataSource.getTranslationCount()
            Log.d("LullabyTranslationRepositoryImpl", "📊 Translation count: $count")
            count
        } catch (e: Exception) {
            Log.e("LullabyTranslationRepositoryImpl", "❌ Error getting translation count: ${e.message}")
            0
        }
    }
}
