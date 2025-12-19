package com.naptune.lullabyandstory.data.local.source.lullaby

import com.naptune.lullabyandstory.data.local.dao.LullabyWithLocalizedName
import com.naptune.lullabyandstory.data.local.entity.LullabyLocalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data source interface for lullaby favourite operations.
 * Follows Interface Segregation Principle (ISP) - focused on favourites only.
 */
interface LullabyFavouriteDataSource {

    // =====================================================
    // FAVOURITE OPERATIONS
    // =====================================================

    suspend fun toggleLullabyFavourite(lullabyId: String): Int

    fun isLullabyFavourite(lullabyId: String): Flow<Boolean>

    fun getFavouriteLullabies(): Flow<List<LullabyLocalEntity>>

    fun getFavouriteLullabiesWithLocalizedNames(languageCode: String): Flow<List<LullabyWithLocalizedName>>

    // =====================================================
    // FAVOURITE METADATA OPERATIONS (LIFO ordering support)
    // =====================================================

    /**
     * Insert favourite metadata when user favourites an item
     * This enables LIFO ordering for favourites
     */
    suspend fun insertFavouriteMetadata(itemId: String, itemType: String): Int

    /**
     * Delete favourite metadata when user unfavourites an item
     */
    suspend fun deleteFavouriteMetadata(itemId: String, itemType: String): Int

    /**
     * Check if favourite metadata exists for an item
     */
    suspend fun hasFavouriteMetadata(itemId: String, itemType: String): Boolean

    /**
     * Get favourite count
     */
    suspend fun getFavouriteCount(): Int
}