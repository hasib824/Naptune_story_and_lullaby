package com.naptune.lullabyandstory.domain.usecase.story

import android.util.Log
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FetchStoriesUsecase @Inject constructor(private val storyRepository: StoryRepository){

    suspend operator fun invoke(): Flow<List<StoryDomainModel>> = flow {

        emitAll(storyRepository.fetchStories())
    }


}