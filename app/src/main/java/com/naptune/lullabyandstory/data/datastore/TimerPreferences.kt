package com.naptune.lullabyandstory.data.datastore

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        // ✅ Using shared DataStore extension from DataStoreExtensions.kt

        // Timer Alarm State Keys
        private val ALARM_ACTIVE = booleanPreferencesKey("alarm_active")
        private val ALARM_TIME = stringPreferencesKey("alarm_time")
        private val ALARM_END_TIME_MILLIS = longPreferencesKey("alarm_end_time_millis") // ✅ NEW: Store exact end time

        // Timer Settings Keys
        private val TIMER_SETTINGS_TIME = stringPreferencesKey("timer_settings_time")
        private val TIMER_SETTINGS_INDEX = intPreferencesKey("timer_settings_index")

        private const val TAG = "TimerPreferences"
    }

    // ✅ Alarm State Methods

    /**
     * Save alarm state (active status, time and end timestamp)
     */
    suspend fun saveAlarmState(isActive: Boolean, time: String, endTimeMillis: Long = 0L) {
        context.timerDataStore.edit { preferences ->
            preferences[ALARM_ACTIVE] = isActive
            preferences[ALARM_TIME] = time
            preferences[ALARM_END_TIME_MILLIS] = endTimeMillis
        }
        Log.d(TAG, "✅ Alarm state saved - Active: $isActive, Time: $time, EndTime: $endTimeMillis")
    }

    /**
     * Get alarm active status
     */
    suspend fun isAlarmActive(): Boolean {
        return context.timerDataStore.data.map { preferences ->
            preferences[ALARM_ACTIVE] ?: false
        }.first()
    }

    /**
     * Get alarm active status as Flow
     */
    fun getAlarmActiveFlow(): Flow<Boolean> {
        return context.timerDataStore.data.map { preferences ->
            preferences[ALARM_ACTIVE] ?: false
        }
    }

    /**
     * Get saved alarm time
     */
    suspend fun getSavedAlarmTime(): String? {
        return context.timerDataStore.data.map { preferences ->
            preferences[ALARM_TIME]
        }.first()
    }

    /**
     * Get saved alarm time as Flow
     */
    fun getSavedAlarmTimeFlow(): Flow<String?> {
        return context.timerDataStore.data.map { preferences ->
            preferences[ALARM_TIME]
        }
    }

    /**
     * Get saved alarm end time in milliseconds
     */
    suspend fun getSavedAlarmEndTimeMillis(): Long {
        return context.timerDataStore.data.map { preferences ->
            preferences[ALARM_END_TIME_MILLIS] ?: 0L
        }.first()
    }

    /**
     * Get saved alarm end time as Flow
     */
    fun getSavedAlarmEndTimeMillisFlow(): Flow<Long> {
        return context.timerDataStore.data.map { preferences ->
            preferences[ALARM_END_TIME_MILLIS] ?: 0L
        }
    }

    // ✅ Timer Settings Methods

    /**
     * Save timer settings (time and index)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveTimerSettings(time: LocalTime, index: Int) {
        context.timerDataStore.edit { preferences ->
            preferences[TIMER_SETTINGS_TIME] = time.toString()
            preferences[TIMER_SETTINGS_INDEX] = index
        }
        Log.d(TAG, "✅ Timer settings saved - Time: $time, Index: $index")
    }

    /**
     * Get saved timer time
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getSavedTimerTime(): LocalTime {
        val timeString = context.timerDataStore.data.map { preferences ->
            preferences[TIMER_SETTINGS_TIME] ?: "00:00"
        }.first()

        return try {
            LocalTime.parse(timeString)
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to parse saved timer time: $timeString, using default")
            LocalTime.of(0, 0)
        }
    }

    /**
     * Get saved timer time as Flow
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getSavedTimerTimeFlow(): Flow<LocalTime> {
        return context.timerDataStore.data.map { preferences ->
            val timeString = preferences[TIMER_SETTINGS_TIME] ?: "00:00"
            try {
                LocalTime.parse(timeString)
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Failed to parse saved timer time: $timeString, using default")
                LocalTime.of(0, 0)
            }
        }
    }

    /**
     * Get saved timer index
     */
    suspend fun getSavedTimerIndex(): Int {
        return context.timerDataStore.data.map { preferences ->
            preferences[TIMER_SETTINGS_INDEX] ?: -1
        }.first()
    }

    /**
     * Get saved timer index as Flow
     */
    fun getSavedTimerIndexFlow(): Flow<Int> {
        return context.timerDataStore.data.map { preferences ->
            preferences[TIMER_SETTINGS_INDEX] ?: -1
        }
    }

    /**
     * Get timer settings as a pair (time, index)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTimerSettings(): Pair<LocalTime, Int> {
        return Pair(getSavedTimerTime(), getSavedTimerIndex())
    }

    /**
     * Reset timer settings to default values
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun resetTimerSettings() {
        context.timerDataStore.edit { preferences ->
            preferences[TIMER_SETTINGS_TIME] = LocalTime.of(0, 0).toString()
            preferences[TIMER_SETTINGS_INDEX] = -1
        }
        Log.d(TAG, "🔄 Timer settings reset to default values")
    }

    /**
     * Check if timer settings are available (not default values)
     */
    suspend fun hasTimerSettings(): Boolean {
        val index = getSavedTimerIndex()
        return index != -1
    }

    /**
     * Clear all timer data (for testing or cleanup)
     */
    suspend fun clearAllTimerData() {
        context.timerDataStore.edit { preferences ->
            preferences.clear()
        }
        Log.d(TAG, "🗑️ All timer data cleared")
    }

    // ✅ Combined Flow Methods for UI

    /**
     * Get combined timer state for UI observation
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimerStateFlow(): Flow<TimerState> {
        return context.timerDataStore.data.map { preferences ->
            val isActive = preferences[ALARM_ACTIVE] ?: false
            val alarmTime = preferences[ALARM_TIME]
            val timeString = preferences[TIMER_SETTINGS_TIME] ?: "00:00"
            val index = preferences[TIMER_SETTINGS_INDEX] ?: -1

            val settingsTime = try {
                LocalTime.parse(timeString)
            } catch (e: Exception) {
                LocalTime.of(0, 0)
            }

            TimerState(
                isAlarmActive = isActive,
                alarmTime = alarmTime,
                settingsTime = settingsTime,
                settingsIndex = index,
                hasSettings = index != -1
            )
        }
    }
}

/**
 * Data class representing the complete timer state
 */
data class TimerState(
    val isAlarmActive: Boolean = false,
    val alarmTime: String? = null,
    val settingsTime: LocalTime,
    val settingsIndex: Int = -1,
    val hasSettings: Boolean = false
)