package com.naptune.lullabyandstory.domain.usecase.lullaby

import com.naptune.lullabyandstory.domain.repository.LullabyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckIfLullabyIsFavouriteUseCase @Inject constructor(
    private val repository: LullabyRepository
) {
    operator fun invoke(lullabyId: String): Flow<Boolean> {
        return repository.checkIfLullabyIsFavourite(lullabyId)
    }
}
