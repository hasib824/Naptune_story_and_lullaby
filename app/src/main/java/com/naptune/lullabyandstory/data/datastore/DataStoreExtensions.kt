package com.naptune.lullabyandstory.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers

/**
 * ✅ Single source of truth for DataStore instances
 * All DataStore access should go through these extensions
 */

// Main app preferences DataStore
val Context.appDataStore: DataStore<Preferences> by preferencesDataStore("app_preferences")

// Timer preferences DataStore (kept separate for feature isolation)
val Context.timerDataStore: DataStore<Preferences> by preferencesDataStore("timer_alarm_prefs")

// FCM preferences DataStore (kept separate for feature isolation)
val Context.fcmDataStore: DataStore<Preferences> by preferencesDataStore("fcm_preferences")

/**
 * ✅ Preference keys - centralized location
 * Prevents typos and ensures consistency
 */
object PreferenceKeys {
    // Language keys
    val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    val IS_CUSTOM_LANGUAGE_SELECTED = booleanPreferencesKey("is_custom_language_selected")

    // Splash screen key
    val SPLASH_SCREEN_SHOWN = booleanPreferencesKey("splash_screen_shown")
}

/**
 * ✅ Helper function for synchronous DataStore reads
 * Used in critical startup paths (attachBaseContext, Compose remember blocks)
 * Fast due to DataStore's in-memory cache after first read
 */
fun Context.getLanguageSync(): String {
    return try {
        runBlocking(Dispatchers.IO) {
            appDataStore.data.first()[PreferenceKeys.SELECTED_LANGUAGE] ?: "en"
        }
    } catch (e: Exception) {
        android.util.Log.e("DataStore", "Failed to read language: ${e.message}")
        "en" // Fallback
    }
}

fun Context.getSplashScreenShownSync(): Boolean {
    return try {
        runBlocking(Dispatchers.IO) {
            appDataStore.data.first()[PreferenceKeys.SPLASH_SCREEN_SHOWN] ?: false
        }
    } catch (e: Exception) {
        android.util.Log.e("DataStore", "Failed to read splash screen status: ${e.message}")
        false // Fallback
    }
}

fun Context.getCustomLanguageSelectedSync(): Boolean {
    return try {
        runBlocking(Dispatchers.IO) {
            appDataStore.data.first()[PreferenceKeys.IS_CUSTOM_LANGUAGE_SELECTED] ?: false
        }
    } catch (e: Exception) {
        android.util.Log.e("DataStore", "Failed to read custom language flag: ${e.message}")
        false // Fallback
    }
}
