package com.naptune.lullabyandstory.data.model

data class StoryRemoteModel(
    val documentId: String = "",
    val id: String = "",
    val storyName: String = "",
    val storyDescription: String = "",
    val storyAudioPath: String = "",
    val imagePath: String = "",
    val story_reading_time: String = "",
    val story_listen_time_in_millis : Long = 0,
    val popularity_count: Long = 0,
    val isFree: Boolean = false,

)