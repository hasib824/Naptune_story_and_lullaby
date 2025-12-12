package com.naptune.lullabyandstory.data.network.appwrite

import android.util.Log
import com.naptune.lullabyandstory.data.appwrite.AppwriteBaseClient
import com.naptune.lullabyandstory.data.model.StoryNameTranslationRemoteModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryNameTranslationRemoteDataSource @Inject constructor(
    private val appwriteBaseClient: AppwriteBaseClient
) {

    private val databaseId = "67cc6c770003e94b7ec3"
    private val storyNameTranslationCollectionId = "story_name_collection_id" // TODO: Replace with actual collection ID

    suspend fun fetchStoryNameTranslations(): Result<List<StoryNameTranslationRemoteModel>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("StoryNameTranslationRemoteDataSource", "🌐 Fetching story name translations...")

                val result = appwriteBaseClient.databases.listDocuments(
                    databaseId = databaseId,
                    collectionId = storyNameTranslationCollectionId,
                    queries = listOf()
                )

                val storyNameTranslationList = result.documents.map { document ->
                    Log.e("StoryNameTranslation Response", document.data.toString())

                    StoryNameTranslationRemoteModel(
                        documentId = document.id,
                        id = document.data["id"]?.toString() ?: "",
                        storyNameEn = document.data["story_name_en"]?.toString() ?: "",
                        storyNameEs = document.data["story_name_es"]?.toString() ?: "",
                        storyNameFr = document.data["story_name_fr"]?.toString() ?: "",
                        storyNameDe = document.data["story_name_de"]?.toString() ?: "",
                        storyNamePt = document.data["story_name_pt"]?.toString() ?: "",
                        storyNameHi = document.data["story_name_hi"]?.toString() ?: "",
                        storyNameAr = document.data["story_name_ar"]?.toString() ?: ""
                    )
                }

                Log.d("StoryNameTranslationRemoteDataSource", "✅ Story name translations fetched: ${storyNameTranslationList.size}")
                Result.success(storyNameTranslationList)

            } catch (e: Exception) {
                Log.e("StoryNameTranslationFetch", "💥 Failed to fetch story name translations: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}