package com.naptune.lullabyandstory.data.network.appwrite

import android.util.Log
import com.naptune.lullabyandstory.data.appwrite.AppwriteBaseClient
import com.naptune.lullabyandstory.data.model.LullabyRemoteModel
import com.naptune.lullabyandstory.data.model.TranslationRemoteModel
import io.appwrite.models.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LullabyRemoteDataSource @Inject constructor(private val appwriteBaseClient: AppwriteBaseClient) {

    private val databaseId = "671e1589003e4219336b"
    private val collectionId = "671e159b000a0bd19137"


    fun getIntegerFromDocument(document: Document<Map<String, Any>>, columnName: String): Int {
        return try {
            when (val value = document.data[columnName]) {
                is Int -> { Log.e("AppwriteInt","int"); value }
                is Double -> { Log.e("AppwriteInt","Double"); value.toInt()} // যদি double আসে
                is String -> { Log.e("AppwriteInt","String"); value.toInt()} // যদি string আসে
                is Long -> { Log.e("AppwriteInt","Long"); value.toInt()} // যদি string আসে
                else -> 0
            }
        } catch (e: Exception) {
           0
        }
    }

    suspend fun fetchLullabyData(): Result<List<LullabyRemoteModel>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyRemoteDataSource", "🌐 Starting API call...")
                Log.d("LullabyRemoteDataSource", "🔗 Database ID: $databaseId")
                Log.d("LullabyRemoteDataSource", "🔗 Collection ID: $collectionId")

                val result = appwriteBaseClient.databases.listDocuments(
                    databaseId = databaseId,
                    collectionId = collectionId,
                    queries = listOf(), // (optional)
                )

                val lullabyList = result.documents.map { document ->

                    Log.e("Response 1 lull", document.data.toString())

                    LullabyRemoteModel(
                        documentId = document.id,
                        id = document.data["id"]?.toString() ?: "",
                        musicName = document.data["music_name"]?.toString() ?: "",
                        musicPath = document.data["music_path"]?.toString() ?: "",
                        musicSize = document.data["music_size"]?.toString() ?: "",
                        imagePath = document.data["image_path"]?.toString() ?: "",
                        musicLength = document.data["music_length"]?.toString() ?: "",
                       // popularity_count = document.data["popularity_count"] as? Int ?: 0,
                        popularity_count = document.data["popularity_count"] as? Long ?: 0L,
                        isFree =  (document.data["is_free"] as? Boolean) ?: false
                    )
                }

                val documents = result.documents
                
                Log.d("LullabyRemoteDataSource", "✅ API Response received")
                Log.d("LullabyRemoteDataSource", "📄 Total documents: ${documents.size}")
                Log.d("LullabyRemoteDataSource", "🎵 Mapped lullabies: ${lullabyList.size}")

             //   fetchTranslationData()

                Result.success(lullabyList)




            } catch (e: Exception) {
                Log.e("AppwriteAPI", "💥 EXCEPTION occurred: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun fetchTranslationData(): Result<List<TranslationRemoteModel>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("TranslationRemoteDataSource", "🌐 Fetching translation data...")

                val result = appwriteBaseClient.databases.listDocuments(
                    databaseId = databaseId,
                    collectionId = "68cae0970030cc03e41a",
                    queries = listOf()
                )

                val translationList = result.documents.map { document ->
                    Log.e("Translation Response", document.data.toString())

                    TranslationRemoteModel(
                        documentId = document.id,
                        id = document.data["id"]?.toString() ?: "",
                        musicNameEn = document.data["music_name_en"]?.toString() ?: "",
                        musicNameEs = document.data["music_name_es"]?.toString() ?: "",
                        musicNameFr = document.data["music_name_fr"]?.toString() ?: "",
                        musicNameDe = document.data["music_name_de"]?.toString() ?: "",
                        musicNamePt = document.data["music_name_pt"]?.toString() ?: "",
                        musicNameHi = document.data["music_name_hi"]?.toString() ?: "",
                        musicNameAr = document.data["music_name_ar"]?.toString() ?: ""
                    )
                }

                Log.d("TranslationRemoteDataSource", "✅ Translations fetched: ${translationList}")
                Result.success(translationList)

            } catch (e: Exception) {
                Log.e("TranslationFetch", "💥 Failed to fetch translations: ${e.message}", e)
                Result.failure(e)
            }
        }
    }


}