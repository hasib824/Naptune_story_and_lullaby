package com.naptune.lullabyandstory.domain.model

/**
 * ✅ Common interface for all audio types (Lullaby, Story)
 * This allows centralized audio state management
 */
interface AudioItem {
    val documentId: String
    val id: String
    val title: String
    val audioUrl: String
    val imageUrl: String
    val isFromStory: Boolean
}

/**
 * ✅ Extension functions to make existing models implement AudioItem
 */

// For LullabyDomainModel
fun LullabyDomainModel.asAudioItem(): AudioItem = object : AudioItem {
    override val documentId: String = this@asAudioItem.documentId
    override val id: String = this@asAudioItem.id
    override val title: String = this@asAudioItem.musicName
    override val audioUrl: String = this@asAudioItem.musicPath
    override val imageUrl: String = this@asAudioItem.imagePath
    override val isFromStory: Boolean = false
}

// For StoryDomainModel  
fun StoryDomainModel.asAudioItem(): AudioItem = object : AudioItem {
    override val documentId: String = this@asAudioItem.documentId
    override val id: String = this@asAudioItem.id
    override val title: String = this@asAudioItem.storyName
    override val audioUrl: String = this@asAudioItem.storyAudioPath
    override val imageUrl: String = this@asAudioItem.imagePath
    override val isFromStory: Boolean = true
}