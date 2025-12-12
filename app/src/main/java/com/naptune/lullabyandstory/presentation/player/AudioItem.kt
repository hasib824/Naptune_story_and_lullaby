package com.naptune.lullabyandstory.presentation.player

import com.naptune.lullabyandstory.domain.model.StoryDomainModel

data class AudioItem(
    val id: String = "",
    val musicName: String = "",
    val storyName: String = "",
    val imagePath: String = "",
    val musicPath: String = "",
    val story_listen_time_in_millis: Long = 0L
)
{

}
