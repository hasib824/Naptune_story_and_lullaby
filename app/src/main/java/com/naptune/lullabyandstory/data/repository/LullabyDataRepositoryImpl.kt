package com.naptune.lullabyandstory.data.repository

import android.content.Context
import android.util.Log
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import com.naptune.lullabyandstory.data.local.source.lullaby.LullabyLocalDataSource
import com.naptune.lullabyandstory.data.local.source.lullaby.LullabyTranslationDataSource
import com.naptune.lullabyandstory.data.mapper.localToDomainModelList
import com.naptune.lullabyandstory.data.mapper.toDomainModel
import com.naptune.lullabyandstory.data.mapper.toDomainModelList
import com.naptune.lullabyandstory.data.mapper.toEntity
import com.naptune.lullabyandstory.data.mapper.toEntityList
import com.naptune.lullabyandstory.data.mapper.toEntityList as translationToEntityList
import com.naptune.lullabyandstory.data.network.prdownloader.PRDownloadManager
import com.naptune.lullabyandstory.data.network.source.lullaby.LullabyRemoteDataSource
import com.naptune.lullabyandstory.domain.data.DownloadLullabyResult
import com.naptune.lullabyandstory.domain.manager.LanguageStateManager
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.repository.LullabyDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LullabyDataRepository for core lullaby data operations.
 * Follows Single Responsibility Principle (SRP) - handles only lullaby data management.
 */
@Singleton
class LullabyDataRepositoryImpl @Inject constructor(
    private val lullabyRemoteDataSource: LullabyRemoteDataSource,
    private val lullabyLocalDataSource: LullabyLocalDataSource,
    private val lullabyTranslationDataSource: LullabyTranslationDataSource,
    private val prDownloadManager: PRDownloadManager,
    private val appPreferences: AppPreferences,
    private val languageStateManager: LanguageStateManager,
    @ApplicationContext private val applicationContext: Context
) : LullabyDataRepository {

    override suspend fun syncLullabiesFromRemote(): Flow<List<LullabyDomainModel>> {
        Log.d("LullabyDataRepositoryImpl", "🚀 Starting reactive sync process...")

        return flow {
            try {
                // Step 1: Check if sync is needed
                val syncNeeded = appPreferences.isSyncNeeded(false)
                val localCount = lullabyLocalDataSource.getLullabyCount()

                Log.d("LullabyDataRepositoryImpl", "🔄 Sync needed: $syncNeeded, Local count: $localCount")

                // Step 2: If we need fresh data (first time or 24+ hours passed)
                if (syncNeeded || localCount == 0) {
                    Log.d("LullabyDataRepositoryImpl", "🌐 Fetching from remote...")

                    try {
                        // 🚀 PARALLEL FETCHING: Fetch both data sources simultaneously
                        coroutineScope {
                            val lullabiesDeferred = async { lullabyRemoteDataSource.fetchLullabyData() }
                            val translationsDeferred = async { lullabyRemoteDataSource.fetchTranslationData() }

                            // ⚡ PARALLEL WAITING: Get both results when they complete
                            val lullabyListResult = lullabiesDeferred.await()
                            val translationsListResult = translationsDeferred.await()

                            // ✅ Process both results together for proper relationship
                            if (lullabyListResult.isSuccess && translationsListResult.isSuccess) {
                                val remoteLullabies = lullabyListResult.getOrNull() ?: emptyList()
                                val remoteTranslations = translationsListResult.getOrNull() ?: emptyList()

                                Log.d("LullabyDataRepositoryImpl", "🚀 PARALLEL SYNC COMPLETE: ${remoteLullabies.size} lullabies, ${remoteTranslations.size} translations fetched simultaneously!")

                                // ✅ Create mapping: lullaby.id -> lullaby.documentId for foreign key relationship
                                val lullabyIdToDocumentMap = remoteLullabies.associate {
                                    it.id to it.documentId
                                }

                                // 🚀 PARALLEL DATABASE OPERATIONS: Prepare entities concurrently
                                val lullabyEntitiesDeferred = async { remoteLullabies.toEntityList() }
                                val translationEntitiesDeferred = async { remoteTranslations.translationToEntityList(lullabyIdToDocumentMap) }

                                // Wait for entity preparations
                                val lullabyEntities = lullabyEntitiesDeferred.await()
                                val translationEntities = translationEntitiesDeferred.await()

                                // 🚀 PARALLEL DATABASE INSERTS: Execute both inserts concurrently
                                val insertJobs = listOf(
                                    async { lullabyLocalDataSource.insertAllLullabies(lullabyEntities) },
                                    async { lullabyTranslationDataSource.insertAllTranslations(translationEntities) }
                                )

                                // Wait for all database operations to complete
                                insertJobs.awaitAll()

                                Log.d("LullabyDataRepositoryImpl", "✅ PARALLEL INSERTS COMPLETE: ${remoteLullabies.size} lullabies, ${translationEntities.size} translations stored simultaneously!")

                                // Update sync timestamp AFTER successful parallel sync
                                appPreferences.updateLastSyncTime(false)
                            } else {
                                // Handle individual failures
                                lullabyListResult.onFailure { error ->
                                    Log.e("LullabyDataRepositoryImpl", "❌ Lullaby sync failed: ${error.message}")
                                }
                                translationsListResult.onFailure { error ->
                                    Log.e("LullabyDataRepositoryImpl", "❌ Translation sync failed: ${error.message}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LullabyDataRepositoryImpl", "💥 Exception during remote sync: ${e.message}")
                        // Continue to emit local data below
                    }
                }

                // Step 3: Always emit the reactive local data stream
                Log.d("LullabyDataRepositoryImpl", "🔄 Starting reactive local data stream...")
                emitAll(getReactiveLullabies())

            } catch (e: Exception) {
                Log.e("LullabyDataRepositoryImpl", "💥 Critical error in sync: ${e.message}")
                // Fallback to just local data
                emitAll(getReactiveLullabies())
            }
        }
    }

    /**
     * ULTRA OPTIMIZED: Database-level language-specific reactive flow
     */
    private fun getReactiveLullabies(): Flow<List<LullabyDomainModel>> {
        return languageStateManager.currentLanguage.flatMapLatest { currentLanguage ->
            Log.d("LullabyDataRepositoryImpl", "🚀 Getting reactive lullabies for language: $currentLanguage")

            lullabyTranslationDataSource.getAllLullabiesWithLocalizedNames(currentLanguage)
                .map { lullabiesWithLocalizedNames ->
                    lullabiesWithLocalizedNames.map { lullabyWithLocalizedName ->
                        val lullaby = lullabyWithLocalizedName.lullaby
                        val localizedName = lullabyWithLocalizedName.localizedMusicName

                        LullabyDomainModel(
                            documentId = lullaby.documentId,
                            id = lullaby.id,
                            musicName = localizedName,
                            musicPath = lullaby.musicPath,
                            musicLocalPath = lullaby.musicLocalPath,
                            musicSize = lullaby.musicSize,
                            imagePath = lullaby.imagePath,
                            musicLength = lullaby.musicLength,
                            isDownloaded = lullaby.isDownloaded,
                            isFavourite = lullaby.isFavourite,
                            popularity_count = lullaby.popularity_count,
                            isFree = lullaby.isFree,
                            translation = null
                        )
                    }
                }
        }
    }

    override fun getAllLullabies(): Flow<List<LullabyDomainModel>> {
        Log.d("LullabyDataRepositoryImpl", "🚀 Getting all lullabies with database-optimized translations")
        return getReactiveLullabies()
    }

    override suspend fun getLullabyById(documentId: String): LullabyDomainModel? {
        return lullabyLocalDataSource.getLullabyById(documentId)?.toDomainModel()
    }

    override suspend fun refreshLullabies(): Result<List<LullabyDomainModel>> {
        return try {
            // Clear local cache
            lullabyLocalDataSource.deleteAllLullabies()

            // Fetch fresh data from remote
            lullabyRemoteDataSource.fetchLullabyData()
                .onSuccess { remoteLullabies ->
                    lullabyLocalDataSource.insertAllLullabies(remoteLullabies.toEntityList())
                }
                .map { remoteLullabies ->
                    remoteLullabies.toDomainModelList()
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchLullabies(query: String): Flow<List<LullabyDomainModel>> {
        return lullabyLocalDataSource.searchLullabies(query).map { entityList ->
            entityList.localToDomainModelList()
        }
    }

    override suspend fun saveLullaby(lullaby: LullabyDomainModel): Result<Unit> {
        return try {
            lullabyLocalDataSource.insertLullaby(lullaby.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteLullaby(lullaby: LullabyDomainModel): Result<Unit> {
        return try {
            lullabyLocalDataSource.deleteLullaby(lullaby.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadLullaby(
        lullabyItem: LullabyDomainModel
    ): Flow<DownloadLullabyResult> {
        Log.d("LullabyDataRepositoryImpl", "📁 Starting download for: ${lullabyItem.musicName}")

        return prDownloadManager.downloadFile(lullabyItem).map { downloadResult ->
            if (downloadResult is DownloadLullabyResult.Completed) {
                Log.d("LullabyDataRepositoryImpl", "✅ Download completed: ${downloadResult}")
                lullabyLocalDataSource.markAsDownloaded(lullabyItem.documentId)
                lullabyLocalDataSource.updateLocalPath(downloadResult.muusciLocalPath, downloadResult.documentId)
            }
            downloadResult
        }
    }
}
