package com.naptune.lullabyandstory.domain.repository

import com.naptune.lullabyandstory.data.local.dao.LullabyWithTranslation
import com.naptune.lullabyandstory.domain.data.DownloadLullabyResult
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.TranslationDomainModel
import kotlinx.coroutines.flow.Flow

interface LullabyRepository {

    suspend fun syncLullabiesFromRemote(): Flow<List<LullabyDomainModel>>

    suspend fun downloadLullaby(lullabyItem: LullabyDomainModel): Flow<DownloadLullabyResult>

    fun getAllLullabies(): Flow<List<LullabyDomainModel>>
    suspend fun getLullabyById(documentId: String): LullabyDomainModel?
    suspend fun refreshLullabies(): Result<List<LullabyDomainModel>>
    fun searchLullabies(query: String): Flow<List<LullabyDomainModel>>
    suspend fun saveLullaby(lullabyDomainModel: LullabyDomainModel): Result<Unit>
    suspend fun deleteLullaby(lullabyDomainModel: LullabyDomainModel): Result<Unit>

    // ✅ Lullaby favourite methods
    suspend fun toggleLullabyFavourite(lullabyId: String)
    fun checkIfLullabyIsFavourite(lullabyId: String): Flow<Boolean>

    // ✅ Favourite lullabies
    fun getFavouriteLullabies(): Flow<List<LullabyDomainModel>>

    // ✅ NEW: Translation methods
    suspend fun getTranslationByLullabyDocumentId(lullabyDocumentId: String): TranslationDomainModel?
    suspend fun getLullabyWithTranslation(documentId: String): LullabyWithTranslation?
    suspend fun getTranslationCount(): Int

}