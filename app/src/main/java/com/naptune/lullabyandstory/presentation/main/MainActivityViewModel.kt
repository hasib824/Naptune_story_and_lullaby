package com.naptune.lullabyandstory.presentation.main

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences
) : ViewModel() {

    init {
        Log.d("MainActivityViewModel", "🏁 MainActivityViewModel initialized")
    }


    /**
     * Public method to reset timer settings - called from MainActivity
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun resetTimerSettings() {
        Log.d("MainActivityViewModel", "🔄 Resetting timer settings to defaults")
        
        viewModelScope.launch {
            try {
                // ✅ Reset to default values
                val defaultTime = LocalTime.of(0, 5) // 5 minutes default
                val defaultIndex = 0 // First option (5 Min)
                
                appPreferences.saveTime(defaultTime)
                appPreferences.saveIndex(defaultIndex)
                //  appPreferences.saveTimerCustomExpanded(false) // Custom not expanded
                
                Log.d("MainActivityViewModel", "✅ Timer settings reset - Time: $defaultTime, Index: $defaultIndex")
                
            } catch (e: Exception) {
                Log.e("MainActivityViewModel", "❌ Error resetting timer settings: ${e.message}", e)
            }
        }
    }
}