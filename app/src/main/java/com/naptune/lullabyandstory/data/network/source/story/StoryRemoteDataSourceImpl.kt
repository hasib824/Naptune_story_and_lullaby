package com.naptune.lullabyandstory.data.network.source.story

import android.content.Context
import android.util.Log
import com.naptune.lullabyandstory.data.appwrite.AppwriteBaseClient
import com.naptune.lullabyandstory.data.model.StoryAudioLanguageRemoteModel
import com.naptune.lullabyandstory.data.model.StoryRemoteModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryRemoteDataSourceImpl @Inject constructor(
    private val appwriteBaseClient: AppwriteBaseClient,
    @ApplicationContext private val context: Context
) : StoryRemoteDataSource {

    private val collectionId = "67cc6cd5000e45d69313"
    private val databaseId = "67cc6c770003e94b7ec3"

    // ✅ StoryAudioLanguage Collection Configuration
    private val storyAudioPathCollectionId = "story_audio_path_collection_id"

    override suspend fun fetchStoryData(): Result<List<StoryRemoteModel>> {
         return withContext(Dispatchers.IO) {
             try {
                 val result = appwriteBaseClient.databases.listDocuments(
                     databaseId = databaseId,
                     collectionId = collectionId,
                     queries = listOf()
                 )

                 val storyList = result.documents.map { document ->
                     Log.e("StoryResponse", document.data.toString())

                     StoryRemoteModel(
                         documentId = document.id,
                         id = document.data["id"]?.toString() ?: "",
                         storyName = document.data["story_name"]?.toString() ?: "",
                         storyDescription = document.data["story_description"]?.toString() ?: "",
                         storyAudioPath = document.data["story_audio_path"]?.toString() ?: "",
                         imagePath = document.data["image_path"]?.toString() ?: "",
                         story_reading_time = document.data["story_reading_time"]?.toString() ?: "",
                         popularity_count = document.data["popularity_count"] as? Long ?: 0L,
                         story_listen_time_in_millis = document.data["story_listen_time_in_millis"] as? Long
                             ?: 0L,
                         isFree = (document.data["is_free"] as? Boolean) ?: false
                     )
                 }
                 val models = storyList + storyList
                 // ✅ Return success result
                 Result.success(models)

             } catch (e: Exception) {
                 Log.e("StoryRemoteDataSourceImpl", "💥 Error fetching stories: ${e.message}")
                 // ✅ Return failure result
                 Result.failure(e)
             }
         }
    }

    /**
     * ✅ Fetch Story Audio Languages from Appwrite
     * Collection: story_audio_path_collection_id
     * Direct document_id foreign key relationship - no mapping needed! 🎯
     */
    override suspend fun fetchStoryAudioLocalizations(): Result<List<StoryAudioLanguageRemoteModel>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(
                    "StoryRemoteDataSourceImpl",
                    "🎵 Fetching story audio languages from Appwrite..."
                )

                val result = appwriteBaseClient.databases.listDocuments(
                    databaseId = databaseId,
                    collectionId = storyAudioPathCollectionId,
                    queries = listOf()
                )

                val storyAudioLanguageList = result.documents.map { document ->
                    Log.d("StoryAudioLanguageResponse", "📄 Document: ${document.data}")

                    StoryAudioLanguageRemoteModel(
                        documentId = document.id,  // Same as Story.documentId - direct foreign key!
                        audioPathEn = document.data["audio_path_en"]?.toString() ?: "",
                        audioPathEs = document.data["audio_path_es"]?.toString() ?: "",
                        audioPathFr = document.data["audio_path_fr"]?.toString() ?: "",
                        audioPathDe = document.data["audio_path_de"]?.toString() ?: "",
                        audioPathPt = document.data["audio_path_pt"]?.toString() ?: "",
                        audioPathHi = document.data["audio_path_hi"]?.toString() ?: "",
                        audioPathAr = document.data["audio_path_ar"]?.toString() ?: ""
                    )
                }

                Log.d(
                    "StoryRemoteDataSourceImpl",
                    "🎵 Successfully fetched ${storyAudioLanguageList} story audio language records"
                )
                Result.success(storyAudioLanguageList)

            } catch (e: Exception) {
                Log.e(
                    "StoryRemoteDataSourceImpl",
                    "💥 Error fetching story audio languages: ${e.message}"
                )
                Result.failure(e)
            }
        }
    }
}