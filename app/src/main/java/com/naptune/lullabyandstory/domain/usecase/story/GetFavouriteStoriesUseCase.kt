package com.naptune.lullabyandstory.domain.usecase.story

import android.util.Log
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavouriteStoriesUseCase @Inject constructor(
    private val repository: StoryRepository
) {
    operator fun invoke(): Flow<List<StoryDomainModel>> {

        return repository.getFavouriteStories()
    }
}
