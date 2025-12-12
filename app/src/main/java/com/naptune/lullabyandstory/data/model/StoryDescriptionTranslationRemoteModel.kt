package com.naptune.lullabyandstory.data.model

/**
 * ✅ Story Description Translation Remote Model for Appwrite API
 * Maps to story description translation collection attributes
 */
data class StoryDescriptionTranslationRemoteModel(
    val documentId: String = "",
    val id: String = "",

    // ✅ All supported language story descriptions
    val storyDescriptionEn: String = "",
    val storyDescriptionEs: String = "",
    val storyDescriptionFr: String = "",
    val storyDescriptionDe: String = "",
    val storyDescriptionPt: String = "",
    val storyDescriptionHi: String = "",
    val storyDescriptionAr: String = ""
)