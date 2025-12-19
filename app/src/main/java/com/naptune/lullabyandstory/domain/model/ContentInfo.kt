package com.naptune.lullabyandstory.domain.model

import androidx.compose.runtime.Immutable

/**
 * Generic content information for AdManager operations.
 * This allows AdManager to work with any content type (lullaby, story, etc.)
 * while maintaining clean architecture and SOLID principles.
 *
 * @param type Content type identifier (e.g., "lullaby", "story")
 * @param id Unique content identifier (documentId)
 * @param name Display name of the content
 * @param isFree Whether the content is free or premium
 */
@Immutable
data class ContentInfo(
    val type: String,
    val id: String,
    val name: String,
    val isFree: Boolean
) {
    companion object {
        /**
         * Create ContentInfo from LullabyDomainModel
         */
        fun fromLullaby(lullaby: LullabyDomainModel): ContentInfo {
            return ContentInfo(
                type = "lullaby",
                id = lullaby.documentId,
                name = lullaby.musicName,
                isFree = lullaby.isFree
            )
        }

        /**
         * Create ContentInfo from StoryDomainModel
         */
        fun fromStory(story: StoryDomainModel): ContentInfo {
            return ContentInfo(
                type = "story",
                id = story.documentId,
                name = story.storyName,
                isFree = story.isFree
            )
        }
    }
}
