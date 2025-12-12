package com.naptune.lullabyandstory.data.mapper

import com.naptune.lullabyandstory.data.local.entity.StoryAudioLanguageLocalEntity
import com.naptune.lullabyandstory.data.model.StoryAudioLanguageRemoteModel

/**
 * ✅ StoryAudioLanguage Mapper
 * Transforms StoryAudioLanguage data between remote and local entities
 *
 * Clean mapping with direct document_id foreign key relationship
 * No complex mapping logic needed! 🎯
 */

/**
 * ✅ Convert StoryAudioLanguageRemoteModel to StoryAudioLanguageLocalEntity
 * Direct document_id mapping - no additional processing needed
 */
fun StoryAudioLanguageRemoteModel.toStoryAudioLanguageEntity(): StoryAudioLanguageLocalEntity {
    return StoryAudioLanguageLocalEntity(
        storyAudioLanguageId = documentId,      // Use documentId as primary key
        storyDocumentId = documentId,           // Same as story's documentId - direct foreign key!
        audioPathEn = audioPathEn,
        audioPathEs = audioPathEs,
        audioPathFr = audioPathFr,
        audioPathDe = audioPathDe,
        audioPathPt = audioPathPt,
        audioPathHi = audioPathHi,
        audioPathAr = audioPathAr
    )
}

/**
 * ✅ Convert List<StoryAudioLanguageRemoteModel> to List<StoryAudioLanguageLocalEntity>
 * Simple list transformation - no complex mapping needed
 */
fun List<StoryAudioLanguageRemoteModel>.toStoryAudioLocalizationEntityList(): List<StoryAudioLanguageLocalEntity> {
    return map { it.toStoryAudioLanguageEntity() }
}