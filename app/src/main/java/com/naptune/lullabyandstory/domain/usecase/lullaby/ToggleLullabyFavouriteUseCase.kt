package com.naptune.lullabyandstory.domain.usecase.lullaby

import com.naptune.lullabyandstory.domain.repository.LullabyRepository
import javax.inject.Inject

class ToggleLullabyFavouriteUseCase @Inject constructor(
    private val repository: LullabyRepository
) {
    suspend operator fun invoke(lullabyId: String) {
        repository.toggleLullabyFavourite(lullabyId)
    }
}
