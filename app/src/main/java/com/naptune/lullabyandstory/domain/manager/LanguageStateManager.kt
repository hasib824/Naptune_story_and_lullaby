package com.naptune.lullabyandstory.domain.manager

import android.util.Log
import com.naptune.lullabyandstory.utils.LanguageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages current language state across the app with reactive updates
 * Provides a single source of truth for current language
 */
@Singleton
class LanguageStateManager @Inject constructor(
    private val languageManager: LanguageManager
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ✅ Reactive current language state
    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    // ✅ Language change trigger
    private val _languageChangeEvent = MutableStateFlow<Long>(0)
    val languageChangeEvent: StateFlow<Long> = _languageChangeEvent.asStateFlow()

    init {
        // Initialize with current language
        scope.launch {
            try {
                val currentLang = languageManager.getLanguage()
                _currentLanguage.value = currentLang
                Log.d("LanguageStateManager", "🌍 Initialized with language: $currentLang")
            } catch (e: Exception) {
                Log.e("LanguageStateManager", "❌ Error initializing language: ${e.message}")
                _currentLanguage.value = "en" // Fallback
            }
        }
    }

    /**
     * Get current language synchronously (for immediate access)
     */
    fun getCurrentLanguageSync(): String {
        return _currentLanguage.value
    }

    /**
     * Update current language and notify all observers
     */
    suspend fun updateCurrentLanguage(languageCode: String) {
        try {
            Log.d("LanguageStateManager", "🔄 Updating language: ${_currentLanguage.value} → $languageCode")

            // Update LanguageManager
            languageManager.setLanguage(languageCode, isManualChange = true)

            // Update state
            _currentLanguage.value = languageCode

            // Trigger change event
            _languageChangeEvent.value = System.currentTimeMillis()

            Log.d("LanguageStateManager", "✅ Language updated successfully to: $languageCode")

        } catch (e: Exception) {
            Log.e("LanguageStateManager", "❌ Error updating language: ${e.message}")
        }
    }

    /**
     * Refresh current language from LanguageManager
     * Useful for detecting external language changes
     */
    suspend fun refreshCurrentLanguage() {
        try {
            val latestLanguage = languageManager.getLanguage()
            if (latestLanguage != _currentLanguage.value) {
                Log.d("LanguageStateManager", "🔄 Language changed externally: ${_currentLanguage.value} → $latestLanguage")
                _currentLanguage.value = latestLanguage
                _languageChangeEvent.value = System.currentTimeMillis()
            }
        } catch (e: Exception) {
            Log.e("LanguageStateManager", "❌ Error refreshing language: ${e.message}")
        }
    }

    /**
     * Check if device language changed and update if needed
     */
    suspend fun checkAndUpdateFromDevice() {
        try {
            if (languageManager.shouldFollowDeviceLanguage()) {
                val currentAppLanguage = languageManager.getLanguage()
                val currentDeviceLanguage = languageManager.detectDeviceLanguagePublic()

                if (currentDeviceLanguage != currentAppLanguage &&
                    languageManager.isLanguageSupported(currentDeviceLanguage)) {

                    Log.d("LanguageStateManager", "📱 Device language changed: $currentAppLanguage → $currentDeviceLanguage")
                    updateCurrentLanguage(currentDeviceLanguage)
                }
            }
        } catch (e: Exception) {
            Log.e("LanguageStateManager", "❌ Error checking device language: ${e.message}")
        }
    }

    /**
     * Get reactive flow for language changes
     */
    fun observeLanguageChanges(): Flow<String> {
        return currentLanguage
    }

    /**
     * Check if current language is RTL
     */
    fun isCurrentLanguageRTL(): Boolean {
        return when (_currentLanguage.value) {
            "ar" -> true
            else -> false
        }
    }

    /**
     * ✅ NEW: Add cleanup method
     * Cancels the coroutine scope to stop all background operations
     */
    fun cleanup() {
        Log.d("LanguageStateManager", "🧹 Cleaning up LanguageStateManager")
        scope.cancel()
        Log.d("LanguageStateManager", "✅ LanguageStateManager cleaned up")
    }
}