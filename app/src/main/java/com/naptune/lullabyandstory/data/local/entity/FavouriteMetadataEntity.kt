package com.naptune.lullabyandstory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Metadata table to track when items are favourited (LIFO ordering support)
 * This table is separate from lullaby_table and story_table to avoid schema changes
 *
 * When a user favourites an item:
 * - Insert a row with current timestamp
 *
 * When a user unfavourites an item:
 * - Delete the row
 *
 * JOIN this table with lullaby/story tables to order by favourited_at DESC (LIFO)
 */
@Entity(
    tableName = "favourite_metadata_table",
    indices = [
        Index(value = ["item_id", "item_type"], unique = true), // Ensure uniqueness
        Index(value = ["favourited_at"]) // Index for efficient ordering
    ]
)
data class FavouriteMetadataEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "item_id")
    val itemId: String, // documentId from lullaby_table or story_table

    @ColumnInfo(name = "item_type")
    val itemType: String, // "lullaby" or "story"

    @ColumnInfo(name = "favourited_at")
    val favouritedAt: Long = System.currentTimeMillis() // Timestamp when favourited
)
