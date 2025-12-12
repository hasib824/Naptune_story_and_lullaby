package com.naptune.lullabyandstory.data.mapper

import com.naptune.lullabyandstory.data.local.entity.StoryNameTranslationLocalEntity
import com.naptune.lullabyandstory.data.model.StoryNameTranslationRemoteModel
import com.naptune.lullabyandstory.domain.model.StoryNameTranslationDomainModel

// ✅ Remote to Local Entity Mapping
fun StoryNameTranslationRemoteModel.toStoryNameTranslationEntity(): StoryNameTranslationLocalEntity {
    return StoryNameTranslationLocalEntity(
        storyNameTranslationId = documentId,
        storyDocumentId = "", // Will be set during insertion with proper mapping
        storyId = id,
        storyNameEn = storyNameEn,
        storyNameEs = storyNameEs,
        storyNameFr = storyNameFr,
        storyNameDe = storyNameDe,
        storyNamePt = storyNamePt,
        storyNameHi = storyNameHi,
        storyNameAr = storyNameAr
    )
}

// ✅ Remote List to Local Entity List Mapping
fun List<StoryNameTranslationRemoteModel>.toStoryNameTranslationEntityList(): List<StoryNameTranslationLocalEntity> {
    return map { it.toStoryNameTranslationEntity() }
}

// ✅ Remote List to Local Entity List with Document ID Mapping (for foreign keys)
fun List<StoryNameTranslationRemoteModel>.toStoryNameTranslationEntityList(
    storyIdToDocumentMap: Map<String, String>
): List<StoryNameTranslationLocalEntity> {
    return map { remoteTranslation ->
        val storyDocumentId = storyIdToDocumentMap[remoteTranslation.id] ?: ""
        StoryNameTranslationLocalEntity(
            storyNameTranslationId = remoteTranslation.documentId,
            storyDocumentId = storyDocumentId, // ✅ Proper foreign key relationship
            storyId = remoteTranslation.id,
            storyNameEn = remoteTranslation.storyNameEn,
            storyNameEs = remoteTranslation.storyNameEs,
            storyNameFr = remoteTranslation.storyNameFr,
            storyNameDe = remoteTranslation.storyNameDe,
            storyNamePt = remoteTranslation.storyNamePt,
            storyNameHi = remoteTranslation.storyNameHi,
            storyNameAr = remoteTranslation.storyNameAr
        )
    }
}

// ✅ Local Entity to Domain Model Mapping
fun StoryNameTranslationLocalEntity.toStoryNameTranslationDomainModel(): StoryNameTranslationDomainModel {
    return StoryNameTranslationDomainModel(
        storyNameTranslationId = storyNameTranslationId,
        storyDocumentId = storyDocumentId,
        storyId = storyId,
        storyNameEn = storyNameEn,
        storyNameEs = storyNameEs,
        storyNameFr = storyNameFr,
        storyNameDe = storyNameDe,
        storyNamePt = storyNamePt,
        storyNameHi = storyNameHi,
        storyNameAr = storyNameAr,
        createdAt = createdAt
    )
}

// ✅ Local Entity List to Domain Model List Mapping
fun List<StoryNameTranslationLocalEntity>.toStoryNameTranslationDomainModelList(): List<StoryNameTranslationDomainModel> {
    return map { it.toStoryNameTranslationDomainModel() }
}

// ✅ Remote to Domain Model Mapping
fun StoryNameTranslationRemoteModel.toStoryNameTranslationDomainModel(): StoryNameTranslationDomainModel {
    return StoryNameTranslationDomainModel(
        storyNameTranslationId = documentId,
        storyDocumentId = "", // Not available in remote model
        storyId = id,
        storyNameEn = storyNameEn,
        storyNameEs = storyNameEs,
        storyNameFr = storyNameFr,
        storyNameDe = storyNameDe,
        storyNamePt = storyNamePt,
        storyNameHi = storyNameHi,
        storyNameAr = storyNameAr
    )
}

// ✅ Remote List to Domain Model List Mapping
fun List<StoryNameTranslationRemoteModel>.remoteToStoryNameTranslationDomainModelList(): List<StoryNameTranslationDomainModel> {
    return map { it.toStoryNameTranslationDomainModel() }
}