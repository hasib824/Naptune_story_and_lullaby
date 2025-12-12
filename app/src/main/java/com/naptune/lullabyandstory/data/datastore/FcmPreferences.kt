package com.naptune.lullabyandstory.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore preferences for FCM token and notification settings
 * Stores FCM token, registration status, and last sync timestamp
 * ✅ Uses shared DataStore extension from DataStoreExtensions.kt
 */
@Singleton
class FcmPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ✅ Using shared fcmDataStore extension

    companion object {
        private val FCM_TOKEN = stringPreferencesKey("fcm_token")
        private val IS_TOKEN_REGISTERED = booleanPreferencesKey("is_token_registered")
        private val LAST_TOKEN_SYNC = longPreferencesKey("last_token_sync")
        private val DEVICE_ID = stringPreferencesKey("device_id")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    // FCM Token
    val fcmToken: Flow<String?> = context.fcmDataStore.data.map { prefs ->
        prefs[FCM_TOKEN]
    }

    suspend fun saveFcmToken(token: String) {
        context.fcmDataStore.edit { prefs ->
            prefs[FCM_TOKEN] = token
        }
    }

    // Token Registration Status
    val isTokenRegistered: Flow<Boolean> = context.fcmDataStore.data.map { prefs ->
        prefs[IS_TOKEN_REGISTERED] ?: false
    }

    suspend fun setTokenRegistered(isRegistered: Boolean) {
        context.fcmDataStore.edit { prefs ->
            prefs[IS_TOKEN_REGISTERED] = isRegistered
        }
    }

    // Last Token Sync Timestamp
    val lastTokenSync: Flow<Long> = context.fcmDataStore.data.map { prefs ->
        prefs[LAST_TOKEN_SYNC] ?: 0L
    }

    suspend fun updateLastTokenSync(timestamp: Long = System.currentTimeMillis()) {
        context.fcmDataStore.edit { prefs ->
            prefs[LAST_TOKEN_SYNC] = timestamp
        }
    }

    // Device ID
    val deviceId: Flow<String?> = context.fcmDataStore.data.map { prefs ->
        prefs[DEVICE_ID]
    }

    suspend fun saveDeviceId(deviceId: String) {
        context.fcmDataStore.edit { prefs ->
            prefs[DEVICE_ID] = deviceId
        }
    }

    // Notifications Enabled
    val notificationsEnabled: Flow<Boolean> = context.fcmDataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED] ?: true  // Default enabled
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.fcmDataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    // Clear all FCM data
    suspend fun clearFcmData() {
        context.fcmDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}