package com.naptune.lullabyandstory.domain.model

import androidx.compose.runtime.Immutable

/**
 * ✅ Story Name Translation Domain Model
 * Clean Architecture domain representation
 */
@Immutable
data class StoryNameTranslationDomainModel(
    val storyNameTranslationId: String = "",
    val storyDocumentId: String = "",
    val storyId: String = "",

    // ✅ All supported language story names
    val storyNameEn: String = "",
    val storyNameEs: String = "",
    val storyNameFr: String = "",
    val storyNameDe: String = "",
    val storyNamePt: String = "",
    val storyNameHi: String = "",
    val storyNameAr: String = "",

    val createdAt: Long = 0L
) {
    /**
     * ✅ Get story name based on language code with smart fallback
     * Language Priority: Requested Language → English → Original
     */
    fun getStoryName(languageCode: String, originalName: String): String {
        val translatedName = when (languageCode) {
            "en" -> storyNameEn
            "es" -> storyNameEs
            "fr" -> storyNameFr
            "de" -> storyNameDe
            "pt" -> storyNamePt
            "hi" -> storyNameHi
            "ar" -> storyNameAr
            else -> storyNameEn // Fallback to English
        }

        return when {
            translatedName.isNotBlank() -> translatedName
            storyNameEn.isNotBlank() -> storyNameEn // Fallback to English
            else -> originalName // Final fallback to original
        }
    }
}