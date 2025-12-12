package com.naptune.lullabyandstory.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class TranslationDomainModel(
    val translationId: String,
    val lullabyDocumentId: String,
    val lullabyId: String,
    val musicNameEn: String,
    val musicNameEs: String,
    val musicNameFr: String,
    val musicNameDe: String,
    val musicNamePt: String,
    val musicNameHi: String,
    val musicNameAr: String
) {
    /**
     * Get music name based on current language code with smart fallback
     */
    fun getMusicName(languageCode: String = "en"): String {
        return when (languageCode) {
            "en" -> musicNameEn
            "es" -> musicNameEs
            "fr" -> musicNameFr
            "de" -> musicNameDe
            "pt" -> musicNamePt
            "hi" -> musicNameHi
            "ar" -> musicNameAr
            else -> musicNameEn.takeIf { it.isNotEmpty() }
                ?: musicNameEs.takeIf { it.isNotEmpty() }
                ?: "Unknown"
        }
    }

    /**
     * Check if translation exists for given language
     */
    fun hasTranslationFor(languageCode: String): Boolean {
        return when (languageCode) {
            "en" -> musicNameEn.isNotEmpty()
            "es" -> musicNameEs.isNotEmpty()
            "fr" -> musicNameFr.isNotEmpty()
            "de" -> musicNameDe.isNotEmpty()
            "pt" -> musicNamePt.isNotEmpty()
            "hi" -> musicNameHi.isNotEmpty()
            "ar" -> musicNameAr.isNotEmpty()
            else -> false
        }
    }

    /**
     * Get all available language codes for this translation
     */
    fun getAvailableLanguages(): List<String> {
        val availableLanguages = mutableListOf<String>()
        if (musicNameEn.isNotEmpty()) availableLanguages.add("en")
        if (musicNameEs.isNotEmpty()) availableLanguages.add("es")
        if (musicNameFr.isNotEmpty()) availableLanguages.add("fr")
        if (musicNameDe.isNotEmpty()) availableLanguages.add("de")
        if (musicNamePt.isNotEmpty()) availableLanguages.add("pt")
        if (musicNameHi.isNotEmpty()) availableLanguages.add("hi")
        if (musicNameAr.isNotEmpty()) availableLanguages.add("ar")
        return availableLanguages
    }
}