package com.naptune.lullabyandstory.domain.repository

import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for lullaby favourite operations.
 * Follows Interface Segregation Principle (ISP) - focused on favourites only.
 */
interface LullabyFavouriteRepository {

    // =====================================================
    // FAVOURITE OPERATIONS
    // =====================================================

    suspend fun toggleLullabyFavourite(lullabyId: String)

    fun checkIfLullabyIsFavourite(lullabyId: String): Flow<Boolean>

    fun getFavouriteLullabies(): Flow<List<LullabyDomainModel>>
}
