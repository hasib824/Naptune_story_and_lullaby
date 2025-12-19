package com.naptune.lullabyandstory.domain.usecase.lullaby

import com.naptune.lullabyandstory.domain.repository.LullabyFavouriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for checking if lullaby is favourite.
 * Updated to use LullabyFavouriteRepository (ISP compliant).
 */
class CheckIfLullabyIsFavouriteUseCase @Inject constructor(
    private val lullabyFavouriteRepository: LullabyFavouriteRepository
) {
    operator fun invoke(lullabyId: String): Flow<Boolean> {
        return lullabyFavouriteRepository.checkIfLullabyIsFavourite(lullabyId)
    }
}
