package com.naptune.lullabyandstory.data.network.source.story

import com.naptune.lullabyandstory.data.model.StoryAudioLanguageRemoteModel
import com.naptune.lullabyandstory.data.model.StoryRemoteModel
import javax.inject.Singleton

@Singleton
interface StoryRemoteDataSource  {


    // ✅ StoryAudioLanguage Collection Configuration

    suspend fun fetchStoryData(): Result<List<StoryRemoteModel>>

    /**
     * ✅ Fetch Story Audio Languages from Appwrite
     * Collection: story_audio_path_collection_id
     * Direct document_id foreign key relationship - no mapping needed! 🎯
     */
    suspend fun fetchStoryAudioLocalizations(): Result<List<StoryAudioLanguageRemoteModel>>
}