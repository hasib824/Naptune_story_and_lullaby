package com.naptune.lullabyandstory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story_table")
data class StoryLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "document_id")
    val documentId: String,

    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "story_name")
    val storyName: String,

    @ColumnInfo(name = "story_description")
    val storyDescription: String,

    @ColumnInfo(name = "story_audio_path")
    val storyAudioPath: String,

    @ColumnInfo(name = "image_path")
    val imagePath: String,

    @ColumnInfo(name = "story_length")
    val story_reading_time: String,

    @ColumnInfo(name = "story_listen_time_in_millis")
    val story_listen_time_in_millis: Long ,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_favourite")
    val isFavourite: Boolean = false,

    @ColumnInfo(name = "popularity_count")
    val popularity_count: Long,

    @ColumnInfo(name = "is_free")
    val isFree: Boolean,

    )