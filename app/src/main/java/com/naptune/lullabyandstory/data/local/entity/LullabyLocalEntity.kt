package com.naptune.lullabyandstory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lullaby_table")
data class LullabyLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "document_id")
    val documentId: String,

    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "music_name")
    val musicName: String,

    @ColumnInfo(name = "music_path")
    val musicPath: String,

    @ColumnInfo(name = "music_local_path")
    val musicLocalPath: String?,

    @ColumnInfo(name = "music_size")
    val musicSize: String,

    @ColumnInfo(name = "image_path")
    val imagePath: String,

    @ColumnInfo(name = "music_length")
    val musicLength: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_downloaded")
    val isDownloaded: Boolean = false,

    @ColumnInfo(name = "is_favourite")
    val isFavourite: Boolean = false,

    @ColumnInfo(name = "popularity_count")
    val popularity_count: Long ,

    @ColumnInfo(name = "is_free")
    val isFree: Boolean,

)