package com.naptune.lullabyandstory.data.network.appwrite

import android.util.Log
import com.naptune.lullabyandstory.data.appwrite.AppwriteBaseClient
import com.naptune.lullabyandstory.data.model.StoryDescriptionTranslationRemoteModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryDescriptionTranslationRemoteDataSource @Inject constructor(
    private val appwriteBaseClient: AppwriteBaseClient
) {

    private val databaseId = "67cc6c770003e94b7ec3"
    private val storyDescriptionTranslationCollectionId = "story_description_collection_id"

    suspend fun fetchStoryDescriptionTranslations(): Result<List<StoryDescriptionTranslationRemoteModel>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("StoryDescriptionTranslationRemoteDataSource", "🌐 Fetching story description translations...")

                val result = appwriteBaseClient.databases.listDocuments(
                    databaseId = databaseId,
                    collectionId = storyDescriptionTranslationCollectionId,
                    queries = listOf()
                )

                val storyDescriptionTranslationList = result.documents.map { document ->
                    Log.e("StoryDescriptionTranslation Response", document.data.toString())

                    StoryDescriptionTranslationRemoteModel(
                        documentId = document.id,
                        id = document.data["id"]?.toString() ?: "",
                        storyDescriptionEn = document.data["story_description_en"]?.toString() ?: "",
                        storyDescriptionEs = document.data["story_description_es"]?.toString() ?: "",
                        storyDescriptionFr = document.data["story_description_fr"]?.toString() ?: "",
                        storyDescriptionDe = document.data["story_description_de"]?.toString() ?: "",
                        storyDescriptionPt = document.data["story_description_pt"]?.toString() ?: "",
                        storyDescriptionHi = document.data["story_description_hi"]?.toString() ?: "",
                        storyDescriptionAr = document.data["story_description_ar"]?.toString() ?: ""
                    )
                }

                Log.d("StoryDescriptionTranslationRemoteDataSource", "✅ Story description translations fetched: ${storyDescriptionTranslationList.size}")
                Result.success(storyDescriptionTranslationList)

            } catch (e: Exception) {
                Log.e("StoryDescriptionTranslationFetch", "💥 Failed to fetch story description translations: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}