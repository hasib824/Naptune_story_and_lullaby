package com.naptune.lullabyandstory.domain.model

import androidx.compose.runtime.Immutable
import com.naptune.lullabyandstory.utils.LanguageManager

@Immutable
data class LullabyDomainModel(
    val documentId: String,
    val id: String,
    val musicName: String, // Original name (fallback)
    val musicPath: String,
    val musicLocalPath: String?,
    val musicSize: String,
    val imagePath: String,
    val musicLength: String,
    val isDownloaded: Boolean,
    val isFavourite: Boolean = false,
    val popularity_count: Long,
    val isFree: Boolean,
    // ✅ NEW: Translation support
    val translation: TranslationDomainModel? = null,

) {
    /**
     * Get music name based on current language with smart fallback
     * Priority: Current Language → English → Original musicName
     */
    fun getLocalizedName(languageCode: String = "en"): String {
        val languageManager: LanguageManager
        return translation?.getMusicName(languageCode) ?: musicName
    }

    /**
     * Check if translation exists for given language
     */
    fun hasTranslationFor(languageCode: String): Boolean {
        return translation?.hasTranslationFor(languageCode) ?: false
    }

    /**
     * Get all available language codes for this lullaby
     */
    fun getAvailableLanguages(): List<String> {
        return translation?.getAvailableLanguages() ?: listOf("en")
    }

    /**
     * Create a copy with translation data
     */
    fun withTranslation(translationData: TranslationDomainModel?): LullabyDomainModel {
        return copy(translation = translationData)
    }
}