package com.naptune.lullabyandstory.domain.repository

import com.naptune.lullabyandstory.domain.data.DownloadLullabyResult
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for core lullaby data operations.
 * Follows Interface Segregation Principle (ISP) - focused on lullaby data only.
 */
interface LullabyDataRepository {

    // =====================================================
    // SYNC & FETCH OPERATIONS
    // =====================================================

    suspend fun syncLullabiesFromRemote(): Flow<List<LullabyDomainModel>>

    fun getAllLullabies(): Flow<List<LullabyDomainModel>>

    suspend fun getLullabyById(documentId: String): LullabyDomainModel?

    suspend fun refreshLullabies(): Result<List<LullabyDomainModel>>

    // =====================================================
    // SEARCH & FILTER OPERATIONS
    // =====================================================

    fun searchLullabies(query: String): Flow<List<LullabyDomainModel>>

    // =====================================================
    // SAVE & DELETE OPERATIONS
    // =====================================================

    suspend fun saveLullaby(lullabyDomainModel: LullabyDomainModel): Result<Unit>

    suspend fun deleteLullaby(lullabyDomainModel: LullabyDomainModel): Result<Unit>

    // =====================================================
    // DOWNLOAD OPERATIONS
    // =====================================================

    suspend fun downloadLullaby(lullabyItem: LullabyDomainModel): Flow<DownloadLullabyResult>
}
