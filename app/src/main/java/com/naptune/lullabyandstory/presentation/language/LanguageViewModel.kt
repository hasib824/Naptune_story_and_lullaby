package com.naptune.lullabyandstory.presentation.language

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.data.model.Language
import com.naptune.lullabyandstory.data.model.getSupportedLanguages
import com.naptune.lullabyandstory.utils.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val languageManager: LanguageManager,
    private val languageStateManager: com.naptune.lullabyandstory.domain.manager.LanguageStateManager
) : ViewModel() {

    private val _currentLanguage = MutableStateFlow(
        Language("en", "English", "English", "🇺🇸", "https://flagcdn.com/w80/us.png")
    )
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showLanguageConfirmDialog = MutableStateFlow(false)
    val showLanguageConfirmDialog: StateFlow<Boolean> = _showLanguageConfirmDialog.asStateFlow()

    private val _pendingLanguageCode = MutableStateFlow("")

    init {
        loadCurrentLanguage()
    }

    private fun loadCurrentLanguage() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val languageCode = languageManager.getLanguage()
                val currentLang = getSupportedLanguages().find { it.code == languageCode }
                    ?: Language("en", "English", "English", "🇺🇸", "https://flagcdn.com/w80/us.png")
                _currentLanguage.value = currentLang
            } catch (e: Exception) {
                // Fallback to default language on error
                _currentLanguage.value = Language("en", "English", "English", "🇺🇸", "https://flagcdn.com/w80/us.png")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Show confirmation dialog first
    fun requestLanguageChange(languageCode: String) {
        _pendingLanguageCode.value = languageCode
        _showLanguageConfirmDialog.value = true
    }

    // ✅ WhatsApp-Style Professional Language Change (No Recreation)
    fun changeLanguageInstantly(languageCode: String, context: Context) {
        try {
            android.util.Log.d("LanguageViewModel", "🚀 WhatsApp-style language change to: $languageCode")

            // 1. Update language state immediately (triggers reactive chain)
            viewModelScope.launch {
                languageStateManager.updateCurrentLanguage(languageCode)
            }

            // 2. Update context configuration only (WhatsApp approach - no system change)
            updateContextConfiguration(context, languageCode)

            // 4. Close dialog
            _showLanguageConfirmDialog.value = false

            android.util.Log.d("LanguageViewModel", "✅ WhatsApp-style language change completed: $languageCode")
        } catch (e: Exception) {
            android.util.Log.e("LanguageViewModel", "❌ Language change failed: ${e.message}")
            _showLanguageConfirmDialog.value = false
        }
    }

    // ✅ WhatsApp-Style Context Configuration Update
    private fun updateContextConfiguration(context: Context, languageCode: String) {
        try {
            val locale = java.util.Locale(languageCode)
            val configuration = android.content.res.Configuration(context.resources.configuration)
            configuration.setLocale(locale)

            // Update resources without recreation
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

            android.util.Log.d("LanguageViewModel", "🔧 Context configuration updated for: $languageCode")
        } catch (e: Exception) {
            android.util.Log.e("LanguageViewModel", "❌ Context update failed: ${e.message}")
        }
    }

    // App restart approach (smoother UX)
    fun changeLanguageWithRestart(languageCode: String, context: Context) {
        viewModelScope.launch {
            try {
                android.util.Log.d("LanguageViewModel", "🔄 Changing language with restart to: $languageCode")

                // Mark this as a manual change to override automatic detection
                languageManager.setLanguage(languageCode, isManualChange = true)

                // Close dialog
                _showLanguageConfirmDialog.value = false

                // Small delay for UI
                kotlinx.coroutines.delay(300)

                // Restart app cleanly
                if (context is Activity) {
                    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    context.finish()
                }

                android.util.Log.d("LanguageViewModel", "✅ App restarting with new language: $languageCode")
            } catch (e: Exception) {
                android.util.Log.e("LanguageViewModel", "❌ Language restart failed: ${e.message}")
                _showLanguageConfirmDialog.value = false
            }
        }
    }

    fun dismissLanguageConfirmDialog() {
        _showLanguageConfirmDialog.value = false
        _pendingLanguageCode.value = ""
    }

    fun showLanguageChangeToast(context: Context, targetLanguageCode: String) {
        viewModelScope.launch {
            try {
                // Get target language info directly (not current which might be old)
                val targetLang = getSupportedLanguages().find { it.code == targetLanguageCode }
                    ?: Language("en", "English", "English", "🇺🇸", "https://flagcdn.com/w80/us.png")

                val toastMessage = when (targetLanguageCode) {
                    "en" -> "Language changed to English"
                    "es" -> "Idioma cambiado a Español"
                    "fr" -> "Langue changée en Français"
                    "de" -> "Sprache auf Deutsch geändert"
                    "pt" -> "Idioma alterado para Português"
                    "hi" -> "भाषा हिंदी में बदल गई"
                    "ar" -> "تم تغيير اللغة إلى العربية"
                    else -> "Language changed successfully"
                }

                Toast.makeText(context, "${targetLang.flagEmoji} $toastMessage", Toast.LENGTH_SHORT).show()
                android.util.Log.d("LanguageViewModel", "🎯 Toast shown for ${targetLang.nativeName}: $toastMessage")
            } catch (e: Exception) {
                android.util.Log.e("LanguageViewModel", "❌ Toast failed: ${e.message}")
            }
        }
    }

    fun getAvailableLanguages(): List<Language> {
        return getSupportedLanguages()
    }


    fun getCurrentLanguageCode(): String {
        return _currentLanguage.value.code
    }

    fun isCurrentLanguageRTL(): Boolean {
        return _currentLanguage.value.isRTL
    }

    /**
     * Initialize language on app start - usually called from MainActivity
     */
    fun initializeLanguage() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val initialLanguageCode = languageManager.initializeLanguage()
                val initialLanguage = getSupportedLanguages().find { it.code == initialLanguageCode }
                    ?: Language("en", "English", "English", "🇺🇸", "https://flagcdn.com/w80/us.png")
                _currentLanguage.value = initialLanguage
            } catch (e: Exception) {
                // Fallback to default language on error
                _currentLanguage.value = Language("en", "English", "English", "🇺🇸", "https://flagcdn.com/w80/us.png")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get language by code - useful for components that need specific language info
     */
    fun getLanguageByCode(code: String): Language? {
        return getSupportedLanguages().find { it.code == code }
    }

    /**
     * Get current language info for toast display
     */
    fun getCurrentLanguageInfo(): Language {
        return _currentLanguage.value
    }

    /**
     * Get pending language for dialog display
     */
    fun getPendingLanguage(): Language {
        val pendingCode = _pendingLanguageCode.value
        return getSupportedLanguages().find { it.code == pendingCode }
            ?: Language("en", "English", "English", "🇺🇸", "https://flagcdn.com/w80/us.png")
    }
}