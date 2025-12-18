package com.naptune.lullabyandstory.data.repository

import android.content.Context
import android.util.Log
import com.naptune.lullabyandstory.data.mapper.localToDomainModelList
import com.naptune.lullabyandstory.data.mapper.toDomainModel
import com.naptune.lullabyandstory.data.mapper.toDomainModelList
import com.naptune.lullabyandstory.data.mapper.toEntity
import com.naptune.lullabyandstory.data.mapper.toEntityList
import com.naptune.lullabyandstory.data.mapper.toEntityList as translationToEntityList
import com.naptune.lullabyandstory.domain.manager.LanguageStateManager
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.repository.LullabyRepository
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import com.naptune.lullabyandstory.data.local.source.LocalDataSource
import com.naptune.lullabyandstory.data.network.source.lullaby.LullabyRemoteDataSource
import com.naptune.lullabyandstory.data.network.prdownloader.PRDownloadManager
import com.naptune.lullabyandstory.domain.data.DownloadLullabyResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LullabyRepositoryImpl @Inject constructor(
    private val lullabyRemoteDataSourceImpl: LullabyRemoteDataSource,
    private val localDataSourceImpl: LocalDataSource,
    private val prDownloadManager: PRDownloadManager,
    private val appPreferences: AppPreferences,
    private val languageStateManager: LanguageStateManager,
    @ApplicationContext private val applicatonContext: Context
) : LullabyRepository {

    override suspend fun syncLullabiesFromRemote(): Flow<List<LullabyDomainModel>> {
        Log.d("LullabyRepositoryImpl", "🚀 Starting reactive sync process...")
        Log.d("isinparallel", "📞 Calling LullabyRepository")

        return flow {
            try {
                // Step 1: Check if sync is needed
                val syncNeeded = appPreferences.isSyncNeeded(false)
                val localCount = localDataSourceImpl.getLullabyCount()
                
                Log.d("LullabyRepositoryImpl", "🔄 Sync needed: $syncNeeded, Local count: $localCount")
                
                // Step 2: If we need fresh data (first time or 24+ hours passed)
               // if (syncNeeded || localCount == 0)
                // ✅ DEBUG: Force sync for testing translation data
                 if (syncNeeded || localCount == 0) { // Temporarily force sync for testing
                    Log.d("LullabyRepositoryImpl", "🌐 Fetching from remote...")

                    try {
                        // 🚀 REVOLUTIONARY PARALLEL FETCHING: Fetch both data sources simultaneously
                        coroutineScope {
                        val lullabiesDeferred = async { lullabyRemoteDataSourceImpl.fetchLullabyData() }
                        val translationsDeferred = async { lullabyRemoteDataSourceImpl.fetchTranslationData() }

                        // ⚡ PARALLEL WAITING: Get both results when they complete
                        val lullabyListResult = lullabiesDeferred.await()
                        val translationsListResult = translationsDeferred.await()

                        // ✅ Process both results together for proper relationship
                        if (lullabyListResult.isSuccess && translationsListResult.isSuccess) {
                            val remoteLullabies = lullabyListResult.getOrNull() ?: emptyList()
                            val remoteTranslations = translationsListResult.getOrNull() ?: emptyList()

                            Log.d("LullabyRepositoryImpl", "🚀 PARALLEL SYNC COMPLETE: ${remoteLullabies.size} lullabies, ${remoteTranslations.size} translations fetched simultaneously!")

                            // ✅ Create mapping: lullaby.id -> lullaby.documentId for foreign key relationship
                            val lullabyIdToDocumentMap = remoteLullabies.associate {
                                it.id to it.documentId
                            }

                            Log.d("LullabyRepositoryImpl", "🔗 Foreign key relationship map: $lullabyIdToDocumentMap")

                            // 🚀 PARALLEL DATABASE OPERATIONS: Prepare entities concurrently
                            val lullabyEntitiesDeferred = async { remoteLullabies.toEntityList() }
                            val translationEntitiesDeferred = async { remoteTranslations.translationToEntityList(lullabyIdToDocumentMap) }

                            // Wait for entity preparations
                            val lullabyEntities = lullabyEntitiesDeferred.await()
                            val translationEntities = translationEntitiesDeferred.await()

                            // 🚀 PARALLEL DATABASE INSERTS: Execute both inserts concurrently
                            val insertJobs = listOf(
                                async { localDataSourceImpl.insertAllLullabies(lullabyEntities) },
                                async { localDataSourceImpl.insertAllTranslations(translationEntities) }
                            )


                            // Wait for all database operations to complete
                            insertJobs.awaitAll()

                            Log.d("isinparallel", "📞 Calling LullabyRepository end")
                            Log.d("LullabyRepositoryImpl", "✅ PARALLEL INSERTS COMPLETE: ${remoteLullabies.size} lullabies, ${translationEntities.size} translations stored simultaneously!")
                            Log.d("LullabyRepositoryImpl", "🔗 Translation relationships: ${translationEntities.map { "${it.lullabyId}->${it.lullabyDocumentId}" }}")

                            // Update sync timestamp AFTER successful parallel sync
                            appPreferences.updateLastSyncTime(false)
                        } else {
                            // Handle individual failures
                            lullabyListResult.onFailure { error ->
                                Log.e("LullabyRepositoryImpl", "❌ Lullaby sync failed: ${error.message}")
                            }
                            translationsListResult.onFailure { error ->
                                Log.e("LullabyRepositoryImpl", "❌ Translation sync failed: ${error.message}")
                            }
                        }
                    } // End coroutineScope
                    } catch (e: Exception) {
                        Log.e("LullabyRepositoryImpl", "💥 Exception during remote sync: ${e.message}")
                        // Continue to emit local data below
                    }
                }
                
                // Step 3: Always emit the reactive local data stream
                // This will automatically update when favorites, downloads, etc. change
                Log.d("LullabyRepositoryImpl", "🔄 Starting reactive local data stream...")
                Log.d("isinparallel", "📞 Calling LullabyRepository end")
                emitAll(getReactiveLullabies())
                
            } catch (e: Exception) {
                Log.e("LullabyRepositoryImpl", "💥 Critical error in sync: ${e.message}")
                // Fallback to just local data
                emitAll(getReactiveLullabies())
            }
        }
    }

    /**
     * ✅ ULTRA OPTIMIZED: Database-level language-specific reactive flow
     * No ViewModel processing needed - translation handled at database level
     */
    private fun getReactiveLullabies(): Flow<List<LullabyDomainModel>> {
        return languageStateManager.currentLanguage.flatMapLatest { currentLanguage ->
            Log.d("LullabyRepositoryImpl", "🚀 ULTRA OPTIMIZED: Getting reactive lullabies for language: $currentLanguage")

            localDataSourceImpl.getAllLullabiesWithLocalizedNames(currentLanguage)
                .map { lullabiesWithLocalizedNames ->
                    Log.d("LullabyRepositoryImpl", "🔄 ULTRA OPTIMIZED: Database returned ${lullabiesWithLocalizedNames.size} items for language: $currentLanguage")

                    lullabiesWithLocalizedNames.map { lullabyWithLocalizedName ->
                        val lullaby = lullabyWithLocalizedName.lullaby
                        val localizedName = lullabyWithLocalizedName.localizedMusicName

                        // ✅ DEBUG: Log WhatsApp-style optimized translation
                        Log.d("TranslationDebug", "🚀 ULTRA OPTIMIZED [$currentLanguage]: ${lullaby.musicName} → $localizedName")

                        // Convert to domain model with pre-computed name
                        com.naptune.lullabyandstory.domain.model.LullabyDomainModel(
                            documentId = lullaby.documentId,
                            id = lullaby.id,
                            musicName = localizedName, // ✅ Database-computed name
                            musicPath = lullaby.musicPath,
                            musicLocalPath = lullaby.musicLocalPath,
                            musicSize = lullaby.musicSize,
                            imagePath = lullaby.imagePath,
                            musicLength = lullaby.musicLength,
                            isDownloaded = lullaby.isDownloaded,
                            isFavourite = lullaby.isFavourite,
                            popularity_count = lullaby.popularity_count,
                            isFree = lullaby.isFree,
                            translation = null // No need for translation object
                        )
                    }
                }
        }
    }

    override suspend fun downloadLullaby(
        lullabyItem: LullabyDomainModel
    ): Flow<DownloadLullabyResult> {
        Log.e("LullabyRepositoryImpl", "📁 Starting download for: $lullabyItem.fileName")
        Log.e("LullabyRepositoryImpl", "URL: $lullabyItem.url")
        Log.e("LullabyRepositoryImpl", "ID: $lullabyItem.id")



        return prDownloadManager.downloadFile(
            lullabyItem = lullabyItem
        ).map { downloadResult ->

            if (downloadResult is DownloadLullabyResult.Completed) {
                Log.d("RoomUpdate", "✅ Entry : ${downloadResult}")
                val rowsUpdated = localDataSourceImpl.markAsDownloaded(lullabyItem.documentId)
                localDataSourceImpl.updateLocalPath(downloadResult.muusciLocalPath, downloadResult.documentId)
            }
            downloadResult
        }


    }

    override fun getAllLullabies(): Flow<List<LullabyDomainModel>> {
        Log.d("LullabyRepositoryImpl", "🚀 Getting all lullabies with database-optimized translations")

        // ✅ ULTRA OPTIMIZED: Direct reactive flow - no combining needed
        // getReactiveLullabies() already handles language changes internally
        return getReactiveLullabies()
    }

    override suspend fun getLullabyById(documentId: String): LullabyDomainModel? {
        return localDataSourceImpl.getLullabyById(documentId)?.toDomainModel()
    }


    override suspend fun refreshLullabies(): Result<List<LullabyDomainModel>> {
        return try {
            // Clear local cache
            localDataSourceImpl.deleteAllLullabies()

            // Fetch fresh data from remote

            return lullabyRemoteDataSourceImpl.fetchLullabyData()
                .onSuccess { remoteLullabies ->
                    localDataSourceImpl.insertAllLullabies(remoteLullabies.toEntityList())
                }
                .map { remoteLullabies ->
                    remoteLullabies.toDomainModelList()
                }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchLullabies(query: String): Flow<List<LullabyDomainModel>> {
        return localDataSourceImpl.searchLullabies(query).map { entityList ->
            entityList.localToDomainModelList()
        }
    }

    override suspend fun saveLullaby(lullaby: LullabyDomainModel): Result<Unit> {
        return try {
            localDataSourceImpl.insertLullaby(lullaby.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteLullaby(lullaby: LullabyDomainModel): Result<Unit> {
        return try {
            localDataSourceImpl.deleteLullaby(lullaby.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Lullaby favourite methods implementation with LIFO metadata support
    override suspend fun toggleLullabyFavourite(lullabyId: String) {
        try {
            Log.d("LullabyRepositoryImpl", "❤️ Toggling lullaby favourite for ID: $lullabyId")

            // Check current favourite status BEFORE toggling
            val lullaby = localDataSourceImpl.getLullabyById(lullabyId)
            val wasAlreadyFavourite = lullaby?.isFavourite ?: false

            Log.d("LullabyRepositoryImpl", "📊 Current favourite status: $wasAlreadyFavourite")

            // Toggle the favourite boolean in lullaby table
            localDataSourceImpl.toggleLullabyFavourite(lullabyId)

            // Update metadata for LIFO ordering
            if (wasAlreadyFavourite) {
                // Was favourite, now unfavouriting -> DELETE metadata
                Log.d("LullabyRepositoryImpl", "💔 Removing from favourites, deleting metadata")
                localDataSourceImpl.deleteFavouriteMetadata(lullabyId, "lullaby")
            } else {
                // Was not favourite, now favouriting -> INSERT metadata
                Log.d("LullabyRepositoryImpl", "❤️ Adding to favourites, inserting metadata")
                localDataSourceImpl.insertFavouriteMetadata(lullabyId, "lullaby")
            }

            Log.d("LullabyRepositoryImpl", "✅ Lullaby favourite toggled successfully with LIFO metadata")
        } catch (e: Exception) {
            Log.e("LullabyRepositoryImpl", "❌ Error toggling lullaby favourite: ${e.message}")
            throw e
        }
    }

    override fun checkIfLullabyIsFavourite(lullabyId: String): Flow<Boolean> {
        Log.d("LullabyRepositoryImpl", "🔍 Checking if lullaby is favourite for ID: $lullabyId")
        return localDataSourceImpl.isLullabyFavourite(lullabyId)
    }
    
    // ✅ UPDATED: Language-aware favourite lullabies
    override fun getFavouriteLullabies(): Flow<List<LullabyDomainModel>> {
        Log.d("LullabyRepositoryImpl", "❤️ Getting language-aware favourite lullabies")
        Log.d("isinparallel", "📞 Calling GetFavouriteLullabiesUseCase")

        return languageStateManager.currentLanguage.flatMapLatest { currentLanguage ->
            Log.d("LullabyRepositoryImpl", "🔄 Getting favourite lullabies for language: $currentLanguage")

            localDataSourceImpl.getFavouriteLullabiesWithLocalizedNames(currentLanguage)
                .map { favouriteLullabiesWithLocalizedNames ->
                    Log.d("LullabyRepositoryImpl", "❤️ Favourite lullabies updated: ${favouriteLullabiesWithLocalizedNames.size} items")

                    favouriteLullabiesWithLocalizedNames.map { lullabyWithLocalizedName ->
                        val lullaby = lullabyWithLocalizedName.lullaby
                        val localizedName = lullabyWithLocalizedName.localizedMusicName

                        // ✅ DEBUG: Log RAW database entity values
                        Log.d("FavouriteDatabaseDebug", "❤️ RAW ENTITY - ${lullaby.musicName}: documentId=${lullaby.documentId}, isDownloaded=${lullaby.isDownloaded}, isFree=${lullaby.isFree}, musicLocalPath=${lullaby.musicLocalPath}")

                        // ✅ DEBUG: Log favourite lullaby translation
                        Log.d("FavouriteTranslationDebug", "❤️ Favourite: ${lullaby.musicName} → $localizedName")

                        Log.d("isinparallel", "📞 Calling GetFavouriteLullabiesUseCase end")
                        // Convert to domain model with pre-computed name
                        com.naptune.lullabyandstory.domain.model.LullabyDomainModel(
                            documentId = lullaby.documentId,
                            id = lullaby.id,
                            musicName = localizedName, // ✅ Database-computed name
                            musicPath = lullaby.musicPath,
                            musicLocalPath = lullaby.musicLocalPath,
                            musicSize = lullaby.musicSize,
                            imagePath = lullaby.imagePath,
                            musicLength = lullaby.musicLength,
                            isDownloaded = lullaby.isDownloaded,
                            isFavourite = lullaby.isFavourite,
                            popularity_count = lullaby.popularity_count,
                            isFree = lullaby.isFree,
                            translation = null // No need for translation object
                        )
                    }

                }

        }

    }

    // ✅ NEW: Translation-related methods via LocalDataSourceImpl
    override suspend fun getTranslationByLullabyDocumentId(lullabyDocumentId: String): com.naptune.lullabyandstory.domain.model.TranslationDomainModel? {
        return try {
            Log.d("LullabyRepositoryImpl", "🔍 Getting translation for lullaby document ID: $lullabyDocumentId")
            val translationEntity = localDataSourceImpl.getTranslationByLullabyDocumentId(lullabyDocumentId)
            translationEntity?.let { entity ->
                entity.toDomainModel()
            }
        } catch (e: Exception) {
            Log.e("LullabyRepositoryImpl", "❌ Error getting translation: ${e.message}")
            null
        }
    }

    override suspend fun getLullabyWithTranslation(documentId: String): com.naptune.lullabyandstory.data.local.dao.LullabyWithTranslation? {
        return try {
            Log.d("LullabyRepositoryImpl", "🔗 Getting lullaby with translation for ID: $documentId")
            localDataSourceImpl.getLullabyWithTranslation(documentId)
        } catch (e: Exception) {
            Log.e("LullabyRepositoryImpl", "❌ Error getting lullaby with translation: ${e.message}")
            null
        }
    }


    // ✅ OPTIMIZED: Database-level language-specific query
    fun getAllLullabiesOptimized(): Flow<List<LullabyDomainModel>> {
        Log.d("LullabyRepositoryImpl", "🚀 Getting optimized lullabies with database-level translation")

        return languageStateManager.currentLanguage.flatMapLatest { currentLanguage ->
            Log.d("LullabyRepositoryImpl", "🔄 Database-optimized query for language: $currentLanguage")

            // Get pre-computed localized names from database
            localDataSourceImpl.getAllLullabiesWithLocalizedNames(currentLanguage)
                .map { lullabiesWithLocalizedNames ->
                    lullabiesWithLocalizedNames.map { lullabyWithLocalizedName ->
                        val lullaby = lullabyWithLocalizedName.lullaby
                        val localizedName = lullabyWithLocalizedName.localizedMusicName

                        // Create domain model with pre-computed name
                        com.naptune.lullabyandstory.domain.model.LullabyDomainModel(
                            documentId = lullaby.documentId,
                            id = lullaby.id,
                            musicName = localizedName, // ✅ Direct from database
                            musicPath = lullaby.musicPath,
                            musicLocalPath = lullaby.musicLocalPath,
                            musicSize = lullaby.musicSize,
                            imagePath = lullaby.imagePath,
                            musicLength = lullaby.musicLength,
                            isDownloaded = lullaby.isDownloaded,
                            isFavourite = lullaby.isFavourite,
                            popularity_count = lullaby.popularity_count,
                            isFree = lullaby.isFree,
                            translation = null // No need for translation object
                        )
                    }
                }
        }
    }

    override suspend fun getTranslationCount(): Int {
        return try {
            val count = localDataSourceImpl.getTranslationCount()
            Log.d("LullabyRepositoryImpl", "📊 Translation count: $count")
            count
        } catch (e: Exception) {
            Log.e("LullabyRepositoryImpl", "❌ Error getting translation count: ${e.message}")
            0
        }
    }

}

