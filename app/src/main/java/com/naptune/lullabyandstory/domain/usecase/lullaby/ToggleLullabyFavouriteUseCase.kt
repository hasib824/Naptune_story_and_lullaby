package com.naptune.lullabyandstory.domain.usecase.lullaby

import com.naptune.lullabyandstory.domain.repository.LullabyFavouriteRepository
import javax.inject.Inject

/**
 * Use case for toggling lullaby favourite status.
 * Updated to use LullabyFavouriteRepository (ISP compliant).
 */
class ToggleLullabyFavouriteUseCase @Inject constructor(
    private val lullabyFavouriteRepository: LullabyFavouriteRepository
) {
    suspend operator fun invoke(lullabyId: String) {
        lullabyFavouriteRepository.toggleLullabyFavourite(lullabyId)
    }
}
