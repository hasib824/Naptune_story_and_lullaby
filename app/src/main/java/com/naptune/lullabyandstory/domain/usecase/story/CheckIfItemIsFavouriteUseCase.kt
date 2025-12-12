package com.naptune.lullabyandstory.domain.usecase.story

import com.naptune.lullabyandstory.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckIfItemIsFavouriteUseCase @Inject constructor(private val storyRepository: StoryRepository){

    suspend operator fun invoke(documentId: String): Flow<Boolean> {
        return storyRepository.checkIfItemIsFavourite(documentId)
    }
}