package com.naptune.lullabyandstory.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.presentation.language.LanguageViewModel

/**
 * Extension functions for easy lullaby name localization in UI
 */

/**
 * Get localized lullaby name with current language from LanguageViewModel
 */
@Composable
fun LullabyDomainModel.getDisplayName(
    languageViewModel: LanguageViewModel = hiltViewModel()
): String {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    return getLocalizedName(currentLanguage.code)
}

/**
 * Get localized lullaby name with specific language code
 */
fun LullabyDomainModel.getDisplayName(languageCode: String): String {
    return getLocalizedName(languageCode)
}

/**
 * Get localized lullaby name with fallback to original
 * This is the most commonly used function in UI components
 */
fun LullabyDomainModel.getDisplayNameWithFallback(languageCode: String? = null): String {
    return when {
        languageCode != null -> getLocalizedName(languageCode)
        translation != null -> getLocalizedName("en") // Default to English if no language specified
        else -> musicName // Original name as final fallback
    }
}

/**
 * Check if lullaby has translation for display purposes
 */
fun LullabyDomainModel.hasTranslationDisplay(): Boolean {
    return translation != null
}

/**
 * Get translation status for debugging
 */
fun LullabyDomainModel.getTranslationDebugInfo(): String {
    return when {
        translation == null -> "No translation"
        translation!!.getAvailableLanguages().isEmpty() -> "Empty translation"
        else -> "Available: ${translation!!.getAvailableLanguages().joinToString(", ")}"
    }
}