package com.naptune.lullabyandstory.data.model

data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val flagEmoji: String,
    val flagImageUrl: String, // ✅ Flag image URL from CDN
    val isRTL: Boolean = false
)

// Helper function to get supported languages
fun getSupportedLanguages(): List<Language> {
    return listOf(
        Language(
            code = "en",
            name = "English",
            nativeName = "English",
            flagEmoji = "🇺🇸",
            flagImageUrl = "https://flagcdn.com/w80/us.png"
        ),
        Language(
            code = "es",
            name = "Spanish",
            nativeName = "Español",
            flagEmoji = "🇪🇸",
            flagImageUrl = "https://flagcdn.com/w80/es.png"
        ),
        Language(
            code = "fr",
            name = "French",
            nativeName = "Français",
            flagEmoji = "🇫🇷",
            flagImageUrl = "https://flagcdn.com/w80/fr.png"
        ),
        Language(
            code = "de",
            name = "German",
            nativeName = "Deutsch",
            flagEmoji = "🇩🇪",
            flagImageUrl = "https://flagcdn.com/w80/de.png"
        ),
        Language(
            code = "pt",
            name = "Portuguese",
            nativeName = "Português",
            flagEmoji = "🇧🇷",
            flagImageUrl = "https://flagcdn.com/w80/br.png"
        ),
        Language(
            code = "hi",
            name = "Hindi",
            nativeName = "हिंदी",
            flagEmoji = "🇮🇳",
            flagImageUrl = "https://flagcdn.com/w80/in.png"
        )
      //  Language("ar", "Arabic", "العربية", "🇸🇦", "https://flagcdn.com/w80/sa.png", isRTL = true)
    )
}