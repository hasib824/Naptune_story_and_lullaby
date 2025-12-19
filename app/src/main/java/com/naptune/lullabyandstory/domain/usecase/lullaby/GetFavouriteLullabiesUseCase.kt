package com.naptune.lullabyandstory.domain.usecase.lullaby

import android.util.Log
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.repository.LullabyFavouriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting favourite lullabies.
 * Updated to use LullabyFavouriteRepository (ISP compliant).
 */
class GetFavouriteLullabiesUseCase @Inject constructor(
    private val lullabyFavouriteRepository: LullabyFavouriteRepository
) {
    operator fun invoke(): Flow<List<LullabyDomainModel>> {
        Log.d(
            "MainViewModel update",
            "🔄 Data updated in use case "
        )

        return lullabyFavouriteRepository.getFavouriteLullabies()
    }
}
