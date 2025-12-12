package com.naptune.lullabyandstory.utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.Log
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageManager @Inject constructor(
    val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context
) {

    companion object {
        const val KEY_LANGUAGE = "selected_language"
        const val KEY_FIRST_LAUNCH = "is_first_launch"
        const val KEY_CUSTOM_LANGUAGE_SELECTED = "isCustomLanguageSelected"
        const val DEFAULT_LANGUAGE = "en"
    }

    // Supported languages list (matches resConfigs in build.gradle)
    private val supportedLanguages = listOf("en", "es", "fr", "de", "pt", "hi", "ar")

    // Regional language mapping (handles regional variants)
    private val languageMapping = mapOf(
        "en" to "en", "en-US" to "en", "en-GB" to "en", "en-CA" to "en", "en-AU" to "en",
        "es" to "es", "es-ES" to "es", "es-MX" to "es", "es-AR" to "es", "es-CO" to "es",
        "fr" to "fr", "fr-FR" to "fr", "fr-CA" to "fr",
        "de" to "de", "de-DE" to "de", "de-AT" to "de", "de-CH" to "de",
        "pt" to "pt", "pt-BR" to "pt", "pt-PT" to "pt",
        "hi" to "hi", "hi-IN" to "hi",
        "ar" to "ar", "ar-SA" to "ar", "ar-AE" to "ar", "ar-EG" to "ar"
    )

    suspend fun initializeLanguage(): String {
        val isFirstLaunch = appPreferences.getBoolean(KEY_FIRST_LAUNCH, true)

        return if (isFirstLaunch) {
            // First launch: detect device language
            val deviceLanguage = detectDeviceLanguage()
            val languageToUse = if (deviceLanguage in supportedLanguages) {
                deviceLanguage
            } else {
                DEFAULT_LANGUAGE
            }

            // Save detected/default language and mark first launch complete
            appPreferences.saveString(KEY_LANGUAGE, languageToUse)
            appPreferences.saveBoolean(KEY_FIRST_LAUNCH, false)

            if (deviceLanguage in supportedLanguages) {
                Log.d("LanguageManager", "✅ First launch: Device language '$deviceLanguage' is supported → Using '$languageToUse'")
            } else {
                Log.d("LanguageManager", "⚠️ First launch: Device language '$deviceLanguage' not supported → Fallback to '$languageToUse'")
            }
            languageToUse
        } else {
            // Subsequent launches: use saved preference
            val savedLanguage = appPreferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE)
            Log.d("LanguageManager", "🔄 Using saved language: $savedLanguage")
            savedLanguage
        }
    }

    private fun detectDeviceLanguage(): String {
        return try {
            val deviceLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }

            // Get full locale (e.g., "en-US", "es-MX") and language only (e.g., "en", "es")
            val fullLocale = deviceLocale.toString().replace("_", "-")
            val languageOnly = deviceLocale.language

            Log.d("LanguageManager", "📱 Device locale: $fullLocale, language: $languageOnly (${deviceLocale.displayName})")

            // Try full locale mapping first, then language-only mapping
            val mappedLanguage = languageMapping[fullLocale] ?: languageMapping[languageOnly] ?: languageOnly

            Log.d("LanguageManager", "🔄 Language mapping: $fullLocale → $mappedLanguage")

            return mappedLanguage

        } catch (e: Exception) {
            Log.e("LanguageManager", "❌ Failed to detect device language: ${e.message}")
            DEFAULT_LANGUAGE
        }
    }

    suspend fun setLanguage(languageCode: String, isManualChange: Boolean = false) {
        if (languageCode in supportedLanguages) {
            // Save to DataStore only
            appPreferences.saveString(KEY_LANGUAGE, languageCode)

            // Set custom language flag based on whether this is manual change
            appPreferences.setCustomLanguageSelected(isManualChange)

            if (isManualChange) {
                Log.d("LanguageManager", "🔒 Custom language selected by user: $languageCode")
            } else {
                Log.d("LanguageManager", "📱 Language auto-detected from device: $languageCode")
            }

            Log.d("LanguageManager", "💾 Language saved to DataStore: $languageCode")
        } else {
            Log.w("LanguageManager", "⚠️ Unsupported language: $languageCode")
        }
    }

    suspend fun getLanguage(): String {
        return appPreferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE)
    }

    fun getCurrentLocale(): Locale {
        val languageCode = runBlocking { getLanguage() }
        return Locale(languageCode)
    }

    fun isLanguageSupported(languageCode: String): Boolean {
        return languageCode in supportedLanguages
    }

    suspend fun getCurrentLanguageInfo(): com.naptune.lullabyandstory.data.model.Language {
        val currentCode = getLanguage()
        return com.naptune.lullabyandstory.data.model.getSupportedLanguages().find { it.code == currentCode }
            ?: com.naptune.lullabyandstory.data.model.Language("en", "English", "English", "🇺🇸", "https://flagcdn.com/w80/us.png")
    }

    // For testing: Reset first launch to re-trigger device language detection
    suspend fun resetFirstLaunch() {
        appPreferences.saveBoolean(KEY_FIRST_LAUNCH, true)
        Log.d("LanguageManager", "🔄 First launch flag reset - next app launch will re-detect device language")
    }

    // For testing: Force device language detection now
    suspend fun forceDeviceLanguageDetection(): String {
        val deviceLanguage = detectDeviceLanguage()
        val languageToUse = if (deviceLanguage in supportedLanguages) {
            deviceLanguage
        } else {
            DEFAULT_LANGUAGE
        }

        // Save detected language but keep first launch flag as is
        appPreferences.saveString(KEY_LANGUAGE, languageToUse)

        Log.d("LanguageManager", "🧪 Force detection: Device language '$deviceLanguage' → Using '$languageToUse'")
        return languageToUse
    }

    // Public method to detect current device language (for app restart detection)
    fun detectDeviceLanguagePublic(): String {
        return detectDeviceLanguage()
    }

    /**
     * Check if user has manually selected a language (hybrid system requirement)
     * Uses synchronous read from DataStore (fast due to in-memory cache)
     */
    fun isCustomLanguageSelected(): Boolean {
        return appPreferences.isCustomLanguageSelectedSync()
    }

    /**
     * Check if app should follow device language (hybrid system requirement)
     */
    fun shouldFollowDeviceLanguage(): Boolean {
        return !isCustomLanguageSelected()
    }

    /**
     * Check if this is truly first launch for toast display
     */
    suspend fun isFirstLaunch(): Boolean {
        return appPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    /**
     * Get language display name for toast
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "es" -> "Español"
            "fr" -> "Français"
            "de" -> "Deutsch"
            "pt" -> "Português"
            "hi" -> "हिंदी"
            "ar" -> "العربية"
            else -> "English"
        }
    }

    /**
     * Clear custom language selection to re-enable automatic device language detection
     * Must be called from coroutine scope
     */
    suspend fun clearCustomLanguageSelection() {
        appPreferences.setCustomLanguageSelected(false)
        Log.d("LanguageManager", "🔓 Custom language selection cleared - device language following re-enabled")
    }
}