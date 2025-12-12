package com.naptune.lullabyandstory.domain.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.android.parcel.Parcelize

@Immutable
@Parcelize
data class StoryDomainModel(
    val documentId: String = "",
    val id: String = "",
    val storyName: String = "",
    val storyDescription: String = "",
    val storyAudioPath: String = "",
    val imagePath: String = "",
    val story_reading_time: String = "",
    val story_listen_time_in_millis: Long = 0,
    val popularity_count: Long = 0,
    val isFavourite: Boolean = false,
    val isFree: Boolean = false
) : Parcelable