package com.naptune.lullabyandstory.data.local.dao

import androidx.room.*
import com.naptune.lullabyandstory.data.local.entity.FavouriteMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteMetadataDao {

    /**
     * Insert favourite metadata when user favourites an item
     * Uses REPLACE strategy to handle cases where metadata already exists
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavouriteMetadata(metadata: FavouriteMetadataEntity)

    /**
     * Delete favourite metadata when user unfavourites an item
     */
    @Query("DELETE FROM favourite_metadata_table WHERE item_id = :itemId AND item_type = :itemType")
    suspend fun deleteFavouriteMetadata(itemId: String, itemType: String)

    /**
     * Check if an item has favourite metadata (for verification)
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favourite_metadata_table WHERE item_id = :itemId AND item_type = :itemType)")
    suspend fun hasFavouriteMetadata(itemId: String, itemType: String): Boolean

    /**
     * Get favourite metadata for a specific item
     */
    @Query("SELECT * FROM favourite_metadata_table WHERE item_id = :itemId AND item_type = :itemType")
    suspend fun getFavouriteMetadata(itemId: String, itemType: String): FavouriteMetadataEntity?

    /**
     * Get all favourite lullaby IDs ordered by favourited_at DESC (LIFO)
     * This is used for ordering in JOIN queries
     */
    @Query("SELECT item_id FROM favourite_metadata_table WHERE item_type = 'lullaby' ORDER BY favourited_at DESC")
    fun getFavouriteLullabyIdsOrdered(): Flow<List<String>>

    /**
     * Get all favourite story IDs ordered by favourited_at DESC (LIFO)
     * This is used for ordering in JOIN queries
     */
    @Query("SELECT item_id FROM favourite_metadata_table WHERE item_type = 'story' ORDER BY favourited_at DESC")
    fun getFavouriteStoryIdsOrdered(): Flow<List<String>>

    /**
     * Delete all favourite metadata (for testing/reset)
     */
    @Query("DELETE FROM favourite_metadata_table")
    suspend fun deleteAllFavouriteMetadata()

    /**
     * Get count of all favourites
     */
    @Query("SELECT COUNT(*) FROM favourite_metadata_table")
    suspend fun getFavouriteCount(): Int

    /**
     * Get count of favourite lullabies
     */
    @Query("SELECT COUNT(*) FROM favourite_metadata_table WHERE item_type = 'lullaby'")
    suspend fun getFavouriteLullabyCount(): Int

    /**
     * Get count of favourite stories
     */
    @Query("SELECT COUNT(*) FROM favourite_metadata_table WHERE item_type = 'story'")
    suspend fun getFavouriteStoryCount(): Int
}
