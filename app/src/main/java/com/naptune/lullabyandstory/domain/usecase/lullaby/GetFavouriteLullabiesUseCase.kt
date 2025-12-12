package com.naptune.lullabyandstory.domain.usecase.lullaby

import android.util.Log
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.repository.LullabyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavouriteLullabiesUseCase @Inject constructor(
    private val repository: LullabyRepository
) {
    operator fun invoke(): Flow<List<LullabyDomainModel>> {
        Log.d(
            "MainViewModel update",
            "🔄 Data updated in use case "
        )


        return repository.getFavouriteLullabies()
    }
}
