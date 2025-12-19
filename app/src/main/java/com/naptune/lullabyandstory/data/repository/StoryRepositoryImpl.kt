package com.naptune.lullabyandstory.data.repository

import android.content.Context
import android.util.Log
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import com.naptune.lullabyandstory.data.local.source.story.StoryLocalDataSource
import com.naptune.lullabyandstory.data.local.source.story.StoryTranslationDataSource
import com.naptune.lullabyandstory.data.local.source.story.StoryAudioLanguageDataSource
import com.naptune.lullabyandstory.data.local.source.favourite.FavouriteDataSource
import com.naptune.lullabyandstory.domain.manager.LanguageStateManager
import com.naptune.lullabyandstory.data.mapper.remoteToLocalModelList
import com.naptune.lullabyandstory.data.mapper.toStoryDescriptionTranslationEntityList
import com.naptune.lullabyandstory.data.mapper.toStoryNameTranslationEntityList
import com.naptune.lullabyandstory.data.mapper.toStoryAudioLocalizationEntityList
import com.naptune.lullabyandstory.data.network.source.story.StoryRemoteDataSource
import com.naptune.lullabyandstory.data.network.appwrite.StoryDescriptionTranslationRemoteDataSource
import com.naptune.lullabyandstory.data.network.appwrite.StoryNameTranslationRemoteDataSource
import com.naptune.lullabyandstory.data.network.prdownloader.PRDownloadManager
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.domain.repository.StoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton
/**
 * Implementation of StoryRepository
 *
 * SOLID Principles Applied:
 * - Dependency Inversion: Depends on data source abstractions (interfaces)
 * - Single Responsibility: Coordinates story data operations between remote, local, and cache
 *
 * @param storyLocalDataSource Handles core story CRUD operations
 * @param storyTranslationDataSource Handles story translation operations
 * @param storyAudioLanguageDataSource Handles story audio language operations
 * @param favouriteDataSource Handles favourite operations (shared with lullaby)
 */
@Singleton
class StoryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storyLocalDataSource: StoryLocalDataSource,
    private val storyTranslationDataSource: StoryTranslationDataSource,
    private val storyAudioLanguageDataSource: StoryAudioLanguageDataSource,
    private val favouriteDataSource: FavouriteDataSource,
    private val storyRemoteDataSourceImpl: StoryRemoteDataSource,
    private val storyNameTranslationRemoteDataSource: StoryNameTranslationRemoteDataSource,
    private val storyDescriptionTranslationRemoteDataSource: StoryDescriptionTranslationRemoteDataSource,
    private val prDownloadManager: PRDownloadManager,
    private val appPreference: AppPreferences,
    private val languageStateManager: LanguageStateManager
) : StoryRepository {


    override suspend fun fetchStories(): Flow<List<StoryDomainModel>> {

        Log.d("isinparallel", "📞 Calling FetchStoriesUsecase")
        Log.d("StoryRepositoryImpl", "🚀 Starting story sync process...")

        return flow {
            try {
                // ✅ Step 1: Check if sync is needed (wrap in withContext)
                val syncNeeded = withContext(Dispatchers.IO) {
                    appPreference.isSyncNeeded(isFromStory = true)
                }
                val localCount = withContext(Dispatchers.IO) {
                    storyLocalDataSource.getStoriesCount()
                }

                Log.d("StoryRepositoryImpl", "🔄 Story sync needed: $syncNeeded, Local count: $localCount")

                // ✅ Step 2: If we need fresh data (first time or 24+ hours passed)
                if (syncNeeded || localCount == 0) {
                    Log.d("StoryRepositoryImpl", "🌐 Fetching stories from remote...")

                    try {
                        // 🚀 PARALLEL FETCHING: Fetch all data sources concurrently
                        coroutineScope {
                            val storiesDeferred = async { storyRemoteDataSourceImpl.fetchStoryData() }
                            val nameTranslationsDeferred = async { storyNameTranslationRemoteDataSource.fetchStoryNameTranslations() }
                            val descriptionTranslationsDeferred = async { storyDescriptionTranslationRemoteDataSource.fetchStoryDescriptionTranslations() }
                            val audioLanguagesDeferred = async { storyRemoteDataSourceImpl.fetchStoryAudioLocalizations() }

                            // Wait for stories first (needed for foreign key mapping)
                            val remoteResult = storiesDeferred.await()

                            remoteResult.onSuccess { remoteStories ->
                                Log.d("StoryRepositoryImpl audio languages", "✅ Remote story sync successful: ${remoteStories.toString()} items")

                                // Create mapping for foreign key relationships
                                val storyIdToDocumentMap = remoteStories.associate { it.id to it.documentId }

                                // 🚀 PARALLEL WAITING: Get all results concurrently
                                val nameTranslationsResult = nameTranslationsDeferred.await()
                                val descriptionTranslationsResult = descriptionTranslationsDeferred.await()
                                val audioLanguagesResult = audioLanguagesDeferred.await()

                                // 🚀 PARALLEL DATABASE OPERATIONS: Prepare all entities concurrently
                                val storyEntitiesDeferred = async { remoteStories.remoteToLocalModelList() }

                                val nameTranslationEntitiesDeferred = async {
                                    nameTranslationsResult.getOrNull()?.let { translations ->
                                        translations.toStoryNameTranslationEntityList(storyIdToDocumentMap)
                                    }
                                }

                                val descriptionTranslationEntitiesDeferred = async {
                                    descriptionTranslationsResult.getOrNull()?.let { translations ->
                                        translations.toStoryDescriptionTranslationEntityList(storyIdToDocumentMap)
                                    }
                                }

                                val audioLanguageEntitiesDeferred = async {
                                    audioLanguagesResult.getOrNull()?.let { audioLanguages ->
                                        audioLanguages.toStoryAudioLocalizationEntityList()
                                    }
                                }

                                // Wait for all entity preparations to complete
                                val storyEntities = storyEntitiesDeferred.await()
                                val nameTranslationEntities = nameTranslationEntitiesDeferred.await()
                                val descriptionTranslationEntities = descriptionTranslationEntitiesDeferred.await()
                                val audioLanguageEntities = audioLanguageEntitiesDeferred.await()

                                // 🚀 PARALLEL DATABASE INSERTS: Execute all inserts concurrently
                                val insertJobs = mutableListOf<Deferred<Any>>()

                                // Always insert stories
                                insertJobs.add(async { storyLocalDataSource.insertAllStories(storyEntities) })

                                // Insert name translations if available
                                nameTranslationEntities?.let { entities ->
                                    insertJobs.add(async { storyTranslationDataSource.insertAllStoryNameTranslations(entities) })
                                }

                                // Insert description translations if available
                                descriptionTranslationEntities?.let { entities ->
                                    insertJobs.add(async { storyTranslationDataSource.insertAllStoryDescriptionTranslations(entities) })
                                }

                                // Insert audio languages if available
                                audioLanguageEntities?.let { entities ->
                                    insertJobs.add(async { storyAudioLanguageDataSource.insertAllStoryAudioLanguages(entities) })
                                }
                                // Wait for all database operations to complete
                                insertJobs.awaitAll()

                                // Log results based on what was successfully inserted
                                val successMessages = mutableListOf<String>()
                                successMessages.add("Stories")



                                if (nameTranslationsResult.isSuccess) {
                                    val count = nameTranslationsResult.getOrNull()?.size ?: 0
                                    successMessages.add("Name translations ($count)")
                                    Log.d("StoryRepositoryImpl", "✅ Story name translations processed: $count items")
                                } else {
                                    Log.w("StoryRepositoryImpl", "⚠️ Story name translations failed: ${nameTranslationsResult.exceptionOrNull()?.message}")
                                }

                                if (descriptionTranslationsResult.isSuccess) {
                                    val count = descriptionTranslationsResult.getOrNull()?.size ?: 0
                                    successMessages.add("Description translations ($count)")
                                    Log.d("StoryRepositoryImpl", "✅ Story description translations processed: $count items")
                                } else {
                                    Log.w("StoryRepositoryImpl", "⚠️ Story description translations failed: ${descriptionTranslationsResult.exceptionOrNull()?.message}")
                                }

                                Log.d("StoryRepositoryImpl", "🎉 PARALLEL SYNC COMPLETE: ${successMessages.joinToString(", ")} cached successfully!")

                                // Update sync timestamp AFTER successful sync


                                appPreference.updateLastSyncTime(isFromStory = true)

                            }.onFailure { error ->
                                Log.e("StoryRepositoryImpl", "❌ Remote story sync failed: ${error.message}")
                                // Cancel pending translation requests since stories failed
                                nameTranslationsDeferred.cancel()
                                descriptionTranslationsDeferred.cancel()
                            }
                        } // End coroutineScope

                    } catch (e: Exception) {
                        Log.e("StoryRepositoryImpl", "💥 Exception during remote story sync: ${e.message}")
                        // Continue to emit local data below
                    }
                }

                Log.d("isinparallel", "📞 Calling FetchStoriesUsecase END")

                // ✅ Step 3: Always emit the reactive local data stream
                Log.d("StoryRepositoryImpl", "🔄 Starting reactive story data stream...")
                emitAll(getReactiveStories())

            } catch (e: Exception) {
                Log.e("StoryRepositoryImpl", "💥 Critical error in story sync: ${e.message}")
                // Fallback to just local data
                emitAll(getReactiveStories())
            }
        }.flowOn(Dispatchers.IO) // ✅ Ensure entire flow runs on IO thread
    }

    /**
     * 🚀 REVOLUTIONARY: Database-level FULL localization reactive flow
     * BOTH name + description pre-computed at database level - O(1) performance
     */
    private fun getReactiveStories(): Flow<List<StoryDomainModel>> {
        return languageStateManager.currentLanguage.flatMapLatest { currentLanguage ->
            Log.d("StoryRepositoryImpl", "🚀 REVOLUTIONARY: Getting reactive stories with FULL localization for language: $currentLanguage")

            storyTranslationDataSource.getAllStoriesWithFullLocalization(currentLanguage)
                .map { storiesWithFullLocalization ->
                    Log.d("StoryRepositoryImpl", "🔄 REVOLUTIONARY: Database returned ${storiesWithFullLocalization.size} items with FULL localization for language: $currentLanguage")

                    storiesWithFullLocalization.map { storyWithFullLocalization ->
                        val story = storyWithFullLocalization.story
                        val localizedName = storyWithFullLocalization.localizedStoryName
                        val localizedDescription = storyWithFullLocalization.localizedStoryDescription
                        val localizedAudioPath = storyWithFullLocalization.localizedAudioPath

                        // ✅ DEBUG: Log WhatsApp-style optimized translation for ALL localizations
                        Log.d("TranslationDebug", "🚀 REVOLUTIONARY STORY [$currentLanguage]: ${story.storyName} → $localizedName")
                        Log.d("TranslationDebug", "🚀 REVOLUTIONARY DESCRIPTION [$currentLanguage]: ${story.storyDescription} → $localizedDescription")
                        Log.d("TranslationDebug", "🎵 REVOLUTIONARY AUDIO ${story.storyName} [$currentLanguage]: from story: ${story.storyAudioPath} → From Localized: ${localizedAudioPath.length} link : $localizedAudioPath")

                        // Convert to domain model with pre-computed name, description AND audio path
                        StoryDomainModel(
                            documentId = story.documentId,
                            id = story.id,
                            storyName = localizedName, // ✅ Database-computed name
                            storyDescription = localizedDescription, // ✅ Database-computed description
                            storyAudioPath = localizedAudioPath, //localizedAudioPath.ifEmpty { story.storyAudioPath }// If localization not available then send the English Version
                            imagePath = story.imagePath,
                            story_reading_time = story.story_reading_time,
                            story_listen_time_in_millis = story.story_listen_time_in_millis,
                            popularity_count = story.popularity_count,
                            isFavourite = story.isFavourite,
                            isFree = story.isFree
                        )
                    }
                }
                .flowOn(Dispatchers.IO)
        }
    }

    override suspend fun downloadStory(): Result<Int> {
        return Result.success(1)
    }

    override suspend fun toggleStoryFavourite(documentid: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("StoryRepositoryImpl", "❤️ Toggling story favourite for ID: $documentid")

                // Check current favourite status BEFORE toggling
                val wasAlreadyFavourite = storyLocalDataSource.checkIfItemIsFavourite(documentid)
                    .first()

                Log.d("StoryRepositoryImpl", "📊 Current favourite status: $wasAlreadyFavourite")

                // Toggle the favourite boolean in story table
                storyLocalDataSource.toggleStoryFavourite(documentid)

                // Update metadata for LIFO ordering
                if (wasAlreadyFavourite) {
                    // Was favourite, now unfavouriting -> DELETE metadata
                    Log.d("StoryRepositoryImpl", "💔 Removing from favourites, deleting metadata")
                    favouriteDataSource.deleteFavouriteMetadata(documentid, "story")
                } else {
                    // Was not favourite, now favouriting -> INSERT metadata
                    Log.d("StoryRepositoryImpl", "❤️ Adding to favourites, inserting metadata")
                    favouriteDataSource.insertFavouriteMetadata(documentid, "story")
                }

                Log.d("StoryRepositoryImpl", "✅ Story favourite toggled successfully with LIFO metadata")
            } catch (e: Exception) {
                Log.e("StoryRepositoryImpl", "❌ Error toggling story favourite: ${e.message}")
                throw e
            }
        }

    override suspend fun checkIfItemIsFavourite(documentid: String): Flow<Boolean> {
        Log.d("StoryRepositoryImpl", "🔍 Checking if story is favourite for ID: $documentid")
        return storyLocalDataSource.checkIfItemIsFavourite(documentid)
            .flowOn(Dispatchers.IO)
    }
    
    override fun getFavouriteStories(): Flow<List<StoryDomainModel>> {
        return languageStateManager.currentLanguage.flatMapLatest { currentLanguage ->

            Log.d("isinparallel", "📞 Calling GetFavouriteStoriesUseCase")

            Log.d("StoryRepositoryImpl", "❤️ REVOLUTIONARY: Getting favourite stories with FULL localization for language: $currentLanguage")

            storyTranslationDataSource.getFavouriteStoriesWithFullLocalization(currentLanguage)
                .map { storiesWithFullLocalization ->
                    Log.d("StoryRepositoryImpl", "❤️ REVOLUTIONARY: Database returned ${storiesWithFullLocalization.size} favourite items with FULL localization for language: $currentLanguage")

                    storiesWithFullLocalization.map { storyWithFullLocalization ->
                        val story = storyWithFullLocalization.story
                        val localizedName = storyWithFullLocalization.localizedStoryName
                        val localizedDescription = storyWithFullLocalization.localizedStoryDescription
                        val localizedAudioPath = storyWithFullLocalization.localizedAudioPath

                        // ✅ DEBUG: Log favourite story localization
                        Log.d("FavouriteTranslationDebug", "❤️ REVOLUTIONARY FAVOURITE [$currentLanguage]: ${story.storyName} → $localizedName")
                        Log.d("FavouriteTranslationDebug", "❤️ REVOLUTIONARY AUDIO [$currentLanguage]: $localizedAudioPath")

                        Log.d("isinparallel", "📞 Calling GetFavouriteStoriesUseCase end")
                        // Convert to domain model with pre-computed FULL localization
                        StoryDomainModel(
                            documentId = story.documentId,
                            id = story.id,
                            storyName = localizedName, // ✅ Database-computed name
                            storyDescription = localizedDescription, // ✅ Database-computed description
                            storyAudioPath = localizedAudioPath, // ✅ Database-computed audio path
                            imagePath = story.imagePath,
                            story_reading_time = story.story_reading_time,
                            story_listen_time_in_millis = story.story_listen_time_in_millis,
                            popularity_count = story.popularity_count,
                            isFavourite = story.isFavourite,
                            isFree = story.isFree
                        )
                    }
                }
                .flowOn(Dispatchers.IO)
        }
    }

}
