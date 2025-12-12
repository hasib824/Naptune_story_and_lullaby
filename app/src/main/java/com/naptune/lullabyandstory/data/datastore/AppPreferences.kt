package com.naptune.lullabyandstory.data.datastore

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val LAST_SYNC_TIME_LULLABY = longPreferencesKey("last_sync_time_lullaby")
        private val LAST_SYNC_TIME_STORY = longPreferencesKey("last_sync_time_story")
        private val IS_HOME_DATA_SYNCED = booleanPreferencesKey("is_home_data_synced")
        private const val SYNC_INTERVAL_HOURS = 24 // দিনে একবার

       // For timer
        val LOCAL_TIME_KEY = stringPreferencesKey("selected_local_time")
        val INDEX_KEY = intPreferencesKey("selected_index")
        val USER_NAME = stringPreferencesKey("user_name")

        // For story font size
        val STORY_FONT_SIZE = floatPreferencesKey("story_font_size")

        // For navigation bottom padding
        val REQUIRED_PADDING_BOTTOM = floatPreferencesKey("required_padding_bottom")

        // For language management
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val IS_CUSTOM_LANGUAGE_SELECTED = booleanPreferencesKey("is_custom_language_selected")

        // For splash screen management (separate from language first launch)
        val SPLASH_SCREEN_SHOWN = booleanPreferencesKey("splash_screen_shown")
    }

    // Data save korun
    suspend fun saveTime(localTime: LocalTime) {

        context.appDataStore.edit { preferences ->
            preferences[LOCAL_TIME_KEY] = localTime.toString()
        }
    }

    // Data save korun
    suspend fun saveIndex(index: Int) {
        context.appDataStore.edit { preferences ->
            preferences[INDEX_KEY] = index
            Log.e("Selected Time 20:", "$index")
        }
    }

    fun getIndex(): Flow<Int> {
        return context.appDataStore.data.map { preferences ->
            val i = preferences[INDEX_KEY] ?: -1
            Log.e("Selected Time 30:", "$i")
            i
        }

    }

    // Data read korun
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTime(): Flow<LocalTime> {
        return context.appDataStore.data.map { preferences ->
            val timeString = preferences[LOCAL_TIME_KEY] ?: "00:05" // Default value
            try {
                LocalTime.parse(timeString) // toString() format automatically parse hobe
            } catch (e: Exception) {
                LocalTime.of(0, 30) // Error hole default return korun
            }
        }
    }



    // Single value save
    suspend fun saveUserName(name: String) {
        context.appDataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }

    // ✅ Story font size save/load methods
    suspend fun saveStoryFontSize(fontSize: Float) {
        context.appDataStore.edit { preferences ->
            preferences[STORY_FONT_SIZE] = fontSize
        }
        Log.d("AppPreferences", "📝 Story font size saved: ${fontSize}sp")
    }

    fun getStoryFontSize(): Flow<Float> {
        return context.appDataStore.data.map { preferences ->
            val fontSize = preferences[STORY_FONT_SIZE] ?: 16f // Default 16sp
            Log.d("AppPreferences", "📖 Story font size loaded: ${fontSize}sp")
            fontSize
        }
    }

    suspend fun getStoryFontSizeOnce(): Float {
        return context.appDataStore.data.map { preferences ->
            preferences[STORY_FONT_SIZE] ?: 16f
        }.first()
    }

    /**
     * Check if sync is needed (24 hours passed since last sync)
     */
    suspend fun isSyncNeeded(isFromStory: Boolean= false): Boolean {
        val lastSyncTime = getLastSyncTime(isFromStory)
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastSyncTime
        val hoursPassed = TimeUnit.MILLISECONDS.toHours(timeDifference)
        
        // ✅ ADD DEBUGGING
        Log.d("AppPreferences", "🕒 Sync Check - Last sync: $lastSyncTime")
        Log.d("AppPreferences", "🕒 Current time: $currentTime") 
        Log.d("AppPreferences", "🕒 Hours passed: $hoursPassed")
        Log.d("AppPreferences", "🕒 Sync needed: ${hoursPassed >= SYNC_INTERVAL_HOURS}")
        
        return hoursPassed >= SYNC_INTERVAL_HOURS
    }

    /**
     * Get last sync timestamp
     */
    private suspend fun getLastSyncTime(isFromStory: Boolean): Long {
        return context.appDataStore.data.map { preferences ->
            preferences[ if (isFromStory) LAST_SYNC_TIME_STORY else LAST_SYNC_TIME_LULLABY] ?: 0L
        }.first()  // ✅ Use first() instead of collect
    }

    suspend fun isHomeDataSynced(): Boolean {
        return context.appDataStore.data.map { preferences ->
            preferences[IS_HOME_DATA_SYNCED] ?: false // ✅ Default false if not set
        }.first() // ✅ Use first() to get actual boolean value
    }
    
    /**
     * Set home data synced status
     */
    suspend fun setHomeDataSynced(isSynced: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[IS_HOME_DATA_SYNCED] = isSynced
        }
        Log.d("AppPreferences", "🏠 Home data synced status set to: $isSynced")
    }

    /**
     * Update last sync time to current time
     */
    suspend fun updateLastSyncTime(isFromStory: Boolean= false) {
        context.appDataStore.edit { preferences ->
            preferences[if (isFromStory) LAST_SYNC_TIME_STORY else LAST_SYNC_TIME_LULLABY] = System.currentTimeMillis()
        }
    }

    /**
     * Get last sync time as Flow
     */
    fun getLastSyncTimeFlow(isFromStory: Boolean= false): Flow<Long> {
        return context.appDataStore.data.map { preferences ->
            preferences[if (isFromStory) LAST_SYNC_TIME_STORY else LAST_SYNC_TIME_LULLABY] ?: 0L
        }
    }

    /**
     * Reset sync time (for testing purposes)
     */
    suspend fun resetSyncTime(isFromStory: Boolean= false) {
        context.appDataStore.edit { preferences ->
            preferences[if (isFromStory) LAST_SYNC_TIME_STORY else LAST_SYNC_TIME_LULLABY] = 0L
        }
    }

    /**
     * Check time remaining until next sync
     */
    suspend fun getTimeUntilNextSync(isFromStory: Boolean= false): Long {
        val lastSyncTime = getLastSyncTime(isFromStory)
        val currentTime = System.currentTimeMillis()
        val nextSyncTime = lastSyncTime + TimeUnit.HOURS.toMillis(SYNC_INTERVAL_HOURS.toLong())
        return maxOf(0, nextSyncTime - currentTime)
    }

    /**
     * Save required bottom padding from scaffold
     */
    suspend fun saveRequiredPaddingBottom(paddingDp: Float) {
        context.appDataStore.edit { preferences ->
            preferences[REQUIRED_PADDING_BOTTOM] = paddingDp
        }
        Log.d("AppPreferences", "💾 Required padding bottom saved: ${paddingDp}dp")
    }

    /**
     * Get required bottom padding as Flow
     */
    fun getRequiredPaddingBottom(): Flow<Float> {
        return context.appDataStore.data.map { preferences ->
            val padding = preferences[REQUIRED_PADDING_BOTTOM] ?: 12f // Default 12dp
            Log.d("AppPreferences", "📱 Required padding bottom loaded: ${padding}dp")
            padding
        }
    }

    /**
     * Get required bottom padding once (suspend function)
     */
    suspend fun getRequiredPaddingBottomOnce(): Float {
        return context.appDataStore.data.map { preferences ->
            preferences[REQUIRED_PADDING_BOTTOM] ?: 12f
        }.first()
    }

    // Language management methods
    suspend fun saveString(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        context.appDataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }

    suspend fun getString(key: String, defaultValue: String): String {
        val prefKey = stringPreferencesKey(key)
        return context.appDataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }.first()
    }

    suspend fun saveBoolean(key: String, value: Boolean) {
        val prefKey = booleanPreferencesKey(key)
        context.appDataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }

    suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val prefKey = booleanPreferencesKey(key)
        return context.appDataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }.first()
    }

    // Specific language preference methods (following the plan)
    suspend fun saveLanguage(languageCode: String) {
        saveString("selected_language", languageCode)
    }

    suspend fun getLanguage(): String {
        return getString("selected_language", "en")
    }

    // ✅ Synchronous read using shared extension
    fun getLanguageSync(): String {
        return context.getLanguageSync()
    }

    suspend fun isFirstLaunch(): Boolean {
        return getBoolean("is_first_launch", true)
    }

    suspend fun setFirstLaunchComplete() {
        saveBoolean("is_first_launch", false)
    }

    // ✅ Splash screen tracking (separate from language first launch)
    suspend fun isSplashScreenShown(): Boolean {
        return getBoolean("splash_screen_shown", false) // Default false = not shown yet
    }

    suspend fun setSplashScreenShown() {
        saveBoolean("splash_screen_shown", true)
    }

    // ✅ Synchronous splash screen check using shared extension
    fun isSplashScreenShownSync(): Boolean {
        return context.getSplashScreenShownSync()
    }

    // ✅ Custom language selection flag (for hybrid language system)
    suspend fun setCustomLanguageSelected(isCustom: Boolean) {
        saveBoolean("is_custom_language_selected", isCustom)
        Log.d("AppPreferences", "🔒 Custom language flag set to: $isCustom")
    }

    suspend fun isCustomLanguageSelected(): Boolean {
        return getBoolean("is_custom_language_selected", false)
    }

    // ✅ Synchronous custom language check using shared extension
    fun isCustomLanguageSelectedSync(): Boolean {
        return context.getCustomLanguageSelectedSync()
    }
}