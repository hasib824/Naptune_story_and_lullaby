package com.naptune.lullabyandstory.domain.repository

import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import kotlinx.coroutines.flow.Flow

interface StoryRepository {

    suspend fun fetchStories(): Flow<List<StoryDomainModel>>

    suspend fun downloadStory(): Result<Int>

    suspend fun toggleStoryFavourite(documentid : String): Int

    suspend fun checkIfItemIsFavourite(documentid : String) : Flow<Boolean>
    
    // ✅ New method to get favourite stories
    fun getFavouriteStories(): Flow<List<StoryDomainModel>>

}