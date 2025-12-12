package com.naptune.lullabyandstory.data.mapper

import com.naptune.lullabyandstory.data.local.entity.StoryDescriptionTranslationLocalEntity
import com.naptune.lullabyandstory.data.model.StoryDescriptionTranslationRemoteModel

// ✅ Remote to Local Entity Mapping
fun StoryDescriptionTranslationRemoteModel.toStoryDescriptionTranslationEntity(): StoryDescriptionTranslationLocalEntity {
    return StoryDescriptionTranslationLocalEntity(
        storyDescriptionTranslationId = documentId,
        storyDocumentId = "", // Will be set during insertion with proper mapping
        storyId = id,
        storyDescriptionEn = storyDescriptionEn,
        storyDescriptionEs = storyDescriptionEs,
        storyDescriptionFr = storyDescriptionFr,
        storyDescriptionDe = storyDescriptionDe,
        storyDescriptionPt = storyDescriptionPt,
        storyDescriptionHi = storyDescriptionHi,
        storyDescriptionAr = storyDescriptionAr
    )
}

// ✅ Remote List to Local Entity List Mapping
fun List<StoryDescriptionTranslationRemoteModel>.toStoryDescriptionTranslationEntityList(): List<StoryDescriptionTranslationLocalEntity> {
    return map { it.toStoryDescriptionTranslationEntity() }
}

// ✅ Remote List to Local Entity List with Document ID Mapping (for foreign keys)
fun List<StoryDescriptionTranslationRemoteModel>.toStoryDescriptionTranslationEntityList(
    storyIdToDocumentMap: Map<String, String>
): List<StoryDescriptionTranslationLocalEntity> {
    return map { remoteTranslation ->
        val storyDocumentId = storyIdToDocumentMap[remoteTranslation.id] ?: ""
        StoryDescriptionTranslationLocalEntity(
            storyDescriptionTranslationId = remoteTranslation.documentId,
            storyDocumentId = storyDocumentId, // ✅ Proper foreign key relationship
            storyId = remoteTranslation.id,
            storyDescriptionEn = remoteTranslation.storyDescriptionEn,
            storyDescriptionEs = remoteTranslation.storyDescriptionEs,
            storyDescriptionFr = remoteTranslation.storyDescriptionFr,
            storyDescriptionDe = remoteTranslation.storyDescriptionDe,
            storyDescriptionPt = remoteTranslation.storyDescriptionPt,
            storyDescriptionHi = remoteTranslation.storyDescriptionHi,
            storyDescriptionAr = remoteTranslation.storyDescriptionAr
        )
    }
}