package com.naptune.lullabyandstory.data.network.source.lullaby

import com.naptune.lullabyandstory.data.model.LullabyRemoteModel
import com.naptune.lullabyandstory.data.model.TranslationRemoteModel
import io.appwrite.models.Document
import javax.inject.Singleton

@Singleton
interface LullabyRemoteDataSource {

    fun getIntegerFromDocument(document: Document<Map<String, Any>>, columnName: String): Int

    suspend fun fetchLullabyData(): Result<List<LullabyRemoteModel>>

    suspend fun fetchTranslationData(): Result<List<TranslationRemoteModel>>


}