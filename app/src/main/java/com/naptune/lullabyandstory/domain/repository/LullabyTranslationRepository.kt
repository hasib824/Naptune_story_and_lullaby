package com.naptune.lullabyandstory.domain.repository

import com.naptune.lullabyandstory.data.local.dao.LullabyWithTranslation
import com.naptune.lullabyandstory.domain.model.TranslationDomainModel

/**
 * Repository interface for lullaby translation operations.
 * Follows Interface Segregation Principle (ISP) - focused on translations only.
 */
interface LullabyTranslationRepository {

    // =====================================================
    // TRANSLATION OPERATIONS
    // =====================================================

    suspend fun getTranslationByLullabyDocumentId(lullabyDocumentId: String): TranslationDomainModel?

    suspend fun getLullabyWithTranslation(documentId: String): LullabyWithTranslation?

    suspend fun getTranslationCount(): Int
}
