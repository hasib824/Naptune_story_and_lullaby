package com.naptune.lullabyandstory.domain.usecase.story

import com.naptune.lullabyandstory.domain.repository.StoryRepository
import javax.inject.Inject

public class ToogleStoryFavouriteUseCase @Inject constructor(private val storyRepository: StoryRepository) {

    suspend operator fun invoke(documented: String): Int {

        return try {
            storyRepository.toggleStoryFavourite(documented)
        } catch (e: Exception) {

        } as Int
    }
}