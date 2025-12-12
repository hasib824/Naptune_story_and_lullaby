package com.naptune.lullabyandstory.data.model

/**
 * ✅ Story Name Translation Remote Model for Appwrite API
 * Maps to story name translation collection attributes
 */
data class StoryNameTranslationRemoteModel(
    val documentId: String = "",
    val id: String = "",

    // ✅ All supported language story names
    val storyNameEn: String = "",
    val storyNameEs: String = "",
    val storyNameFr: String = "",
    val storyNameDe: String = "",
    val storyNamePt: String = "",
    val storyNameHi: String = "",
    val storyNameAr: String = ""
)