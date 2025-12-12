package com.naptune.lullabyandstory.data.model

data class TranslationRemoteModel(
    val documentId: String,
    val id: String,
    val musicNameEn: String,
    val musicNameEs: String,
    val musicNameFr: String,
    val musicNameDe: String,
    val musicNamePt: String,
    val musicNameHi: String,
    val musicNameAr: String
) {
    // Current language অনুযায়ী music name get করার function
    fun getMusicName(languageCode: String = "en"): String {
        return when (languageCode) {
            "en" -> musicNameEn
            "es" -> musicNameEs
            "fr" -> musicNameFr
            "de" -> musicNameDe
            "pt" -> musicNamePt
            "hi" -> musicNameHi
            "ar" -> musicNameAr
            else -> musicNameEn.takeIf { it.isNotEmpty() } ?: musicNameEs.takeIf { it.isNotEmpty() } ?: "Unknown"
        }
    }
}