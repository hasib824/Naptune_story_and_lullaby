package com.naptune.lullabyandstory.presentation.player.timermodal.operations


import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.net.toUri
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.data.datastore.TimerPreferences
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class TimerAlarmManager(
    private val context: Context,
    private val timerPreferences: TimerPreferences
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val ALARM_REQUEST_CODE = 123
        private const val TAG = "TimerAlarmManager"
    }

    /**
     * Schedule new alarm (automatically cancels previous one)
     */
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun scheduleAlarm(selectedTime: LocalTime, timerIndex: Int): Boolean {
        Log.d(TAG, "🚀 Starting alarm scheduling for: $selectedTime, index: $timerIndex")
        
        // Debug permission status for troubleshooting
        debugPermissionStatus()

        // Enhanced permission check for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "⚠️ Exact alarm permission not granted")
                Log.d(TAG, "📱 Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                Log.d(TAG, "🔧 Opening permission request...")
                requestExactAlarmPermission()
                return false
            } else {
                Log.d(TAG, "✅ Exact alarm permission already granted")
            }
        } else {
            Log.d(TAG, "📱 Android ${Build.VERSION.RELEASE} - No permission check needed")
        }

        // Cancel any existing alarm first
        cancelAlarm(false)

        // Handle "End of Story" timer specially (index 5)
        if (timerIndex == 5) { // End of Story timer
            Log.d(TAG, "📖 Setting up 'End of Story' timer")
            
            // For "End of Story", we need to schedule when the current audio ends
            // This is a special case that doesn't use a time-based approach
            
            // Create pending intent for End of Story
            val intent = Intent(context, AppCloseReceiver::class.java).apply {
                putExtra("TIMER_INDEX", timerIndex)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // For "End of Story", we save the alarm state but don't set a system alarm
            // The story completion will be handled by the AudioPlayerViewModel
            timerPreferences.saveAlarmState(true, "end_of_story", Long.MAX_VALUE)
            
            Log.d(TAG, "✅ End of Story timer set up successfully")
            
            // Show success message
            android.widget.Toast.makeText(
                context,
                "End of Story timer set",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            
            return true
        }

        // Regular timer logic (existing code)
        // Calculate target time
        val currentDateTime = LocalDateTime.now()
        val targetDateTime = currentDateTime
            .plusHours(selectedTime.hour.toLong())
            .plusMinutes(selectedTime.minute.toLong())

        val targetTimeMillis = targetDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Validation check
        val currentMillis = System.currentTimeMillis()
        val delayMillis = targetTimeMillis - currentMillis

        if (delayMillis <= 0) {
            Log.e(TAG, "Target time is in the past!")
            Toast.makeText(context, context.getString(R.string.toast_timer_past_time), Toast.LENGTH_SHORT).show()
            return false
        }

        // Create pending intent
        val intent = Intent(context, AppCloseReceiver::class.java).apply {
            putExtra("TIMER_INDEX", timerIndex) // Pass timer index to receiver
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return try {
            // Set the alarm
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                targetTimeMillis,
                pendingIntent
            )

            // Save alarm state using DataStore with exact end time
            timerPreferences.saveAlarmState(true, selectedTime.toString(), targetTimeMillis)

            // Logs and feedback
            val delayMinutes = delayMillis / (1000 * 60)
            Log.d(TAG, "✅ Alarm scheduled successfully")
            Log.d(TAG, "Current time: $currentDateTime")
            Log.d(TAG, "Target time: $targetDateTime")
            Log.d(TAG, "Delay: $delayMinutes minutes")

            val message = if (selectedTime.hour >= 1) {
                val hours = selectedTime.hour
                "Timer set for $hours hour${if (hours > 1) "s" else ""}"
            } else {
                "Timer set for ${selectedTime.minute} minute${if (selectedTime.minute > 1) "s" else ""}"
            }

            Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT
            ).show()

            true

        } catch (e: SecurityException) {
            Log.e(TAG, "❌ SecurityException: ${e.message}")
            Toast.makeText(context, context.getString(R.string.toast_timer_permission_denied), Toast.LENGTH_SHORT).show()
            false
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while scheduling: ${e.message}")
            Toast.makeText(context, context.getString(R.string.toast_timer_failed), Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Cancel current alarm
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun cancelAlarm(showToast: Boolean = true): Boolean {
        Log.d(TAG, "Cancelling alarm...")

        return try {
            val intent = Intent(context, AppCloseReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()

                // Clear saved state using DataStore
                timerPreferences.saveAlarmState(false, "", 0L)
                timerPreferences.resetTimerSettings()

                Log.d(TAG, "✅ Alarm cancelled successfully")
                if(showToast)
                {
                    Toast.makeText(context, context.getString(R.string.toast_timer_cancelled), Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                Log.d(TAG, "No active alarm found to cancel")
                // Clear any stale state using DataStore
                timerPreferences.saveAlarmState(false, "", 0L)
                false
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cancelling alarm: ${e.message}")
            false
        }
    }

    /**
     * Check if alarm is currently active
     */
    fun isAlarmActive(): Boolean {
        return try {
            val intent = Intent(context, AppCloseReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            val isActive = pendingIntent != null
            Log.d(TAG, "Alarm active status: $isActive")
            isActive

        } catch (e: Exception) {
            Log.e(TAG, "Error checking alarm status: ${e.message}")
            false
        }
    }

    /**
     * Get saved alarm time (if any)
     */
    suspend fun getSavedAlarmTime(): String? {
        val time = timerPreferences.getSavedAlarmTime()
        Log.d(TAG, "Saved alarm time: $time")
        return time
    }

    /**
     * Cancel alarm when app closes (call this from Activity onDestroy)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun cancelAlarmOnAppClose() {
        val wasActive = timerPreferences.isAlarmActive()
        if (wasActive) {
            Log.d(TAG, "App closing - cancelling active alarm")
            cancelAlarm()
        }
    }

    /**
     * Clean up orphan alarms (call this from Activity onCreate)
     */
    suspend fun cleanupOrphanAlarms() {
        val wasActive = timerPreferences.isAlarmActive()
        if (wasActive && !isAlarmActive()) {
            Log.d(TAG, "Cleaning up orphan alarm state")
            timerPreferences.saveAlarmState(false, "", 0L)
        }
    }

    /**
     * Request exact alarm permission for Android 12+
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestExactAlarmPermission() {
        Log.d(TAG, "🔔 Requesting exact alarm permission...")
        Log.d(TAG, "📱 Device: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}")

        // Try multiple approaches for different manufacturers
        val intents = listOf(
            // Standard Android approach
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                if (context !is Activity) {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            },
            // Alternative approach for some devices
            Intent("android.settings.REQUEST_SCHEDULE_EXACT_ALARM").apply {
                data = Uri.parse("package:${context.packageName}")
                if (context !is Activity) {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            },
            // Fallback to app details
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                if (context !is Activity) {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
        )

        var success = false
        
        for ((index, intent) in intents.withIndex()) {
            try {
                Log.d(TAG, "🔄 Trying approach ${index + 1}: ${intent.action}")
                context.startActivity(intent)
                
                val message = when (index) {
                    0 -> "Please enable 'Alarms & reminders' permission"
                    1 -> "Please enable 'Schedule exact alarms' permission"
                    else -> "Go to Permissions → Alarms & reminders → Enable"
                }
                
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                Log.d(TAG, "✅ Successfully launched settings with approach ${index + 1}")
                success = true
                break
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Approach ${index + 1} failed: ${e.message}")
                if (index == intents.size - 1) {
                    // Last attempt failed
                    Log.e(TAG, "💥 All approaches failed")
                }
            }
        }

        if (!success) {
            Toast.makeText(
                context, 
                "Cannot open settings automatically. Please go to Settings > Apps > ${context.applicationInfo.loadLabel(context.packageManager)} > Permissions > Alarms & reminders", 
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Check if exact alarm permission is granted
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val canSchedule = alarmManager.canScheduleExactAlarms()
            Log.d(TAG, "🔍 Permission check result: $canSchedule")
            canSchedule
        } else {
            Log.d(TAG, "📱 Android ${Build.VERSION.RELEASE} - Permission not required")
            true
        }
    }

    /**
     * Get user-friendly permission status message
     */
    fun getPermissionStatusMessage(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                "Timer permission is enabled ✅"
            } else {
                "Timer permission required. Tap to enable in settings."
            }
        } else {
            "Timer is ready to use"
        }
    }

    /**
     * Debug method to log all permission-related information
     */
    fun debugPermissionStatus() {
        Log.d(TAG, "================== ALARM PERMISSION DEBUG ==================")
        Log.d(TAG, "📱 Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        Log.d(TAG, "🤖 Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        Log.d(TAG, "📦 Package: ${context.packageName}")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val canSchedule = alarmManager.canScheduleExactAlarms()
            Log.d(TAG, "⏰ Can Schedule Exact Alarms: $canSchedule")
            
            if (!canSchedule) {
                Log.d(TAG, "❌ Permission Status: DENIED")
                Log.d(TAG, "🔧 Action Required: Enable 'Alarms & reminders' in app settings")
                Log.d(TAG, "📍 Path: Settings > Apps > ${context.applicationInfo.loadLabel(context.packageManager)} > Permissions")
            } else {
                Log.d(TAG, "✅ Permission Status: GRANTED")
            }
        } else {
            Log.d(TAG, "ℹ️ Permission not required for this Android version")
        }
        
        Log.d(TAG, "==========================================================")
    }

    /**
     * Save alarm state to DataStore
     */
    suspend fun saveAlarmState(isActive: Boolean, time: String, endTimeMillis: Long = 0L) {
        timerPreferences.saveAlarmState(isActive, time, endTimeMillis)
    }

    /**
     * Get alarm status info for debugging
     */
    suspend fun getAlarmInfo(): String {
        val isActive = timerPreferences.isAlarmActive()
        val savedTime = getSavedAlarmTime()
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        return """
            Alarm Active: $isActive
            Saved Time: $savedTime
            Can Schedule Exact: $canScheduleExact
            Android Version: ${Build.VERSION.SDK_INT}
        """.trimIndent()
    }

    // ✅ NEW: Timer settings storage methods

    /**
     * Save timer settings (time and index) to DataStore
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveTimerSettings(time: LocalTime, index: Int) {
        timerPreferences.saveTimerSettings(time, index)
    }

    /**
     * Get saved timer time from DataStore
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getSavedTimerTime(): LocalTime {
        return timerPreferences.getSavedTimerTime()
    }

    /**
     * Get saved timer index from DataStore
     */
    suspend fun getSavedTimerIndex(): Int {
        return timerPreferences.getSavedTimerIndex()
    }

    /**
     * Get timer settings as a pair (time, index)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTimerSettings(): Pair<LocalTime, Int> {
        return timerPreferences.getTimerSettings()
    }

    /**
     * Reset timer settings to default values
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun resetTimerSettings() {
        timerPreferences.resetTimerSettings()
    }

    /**
     * Check if timer settings are available (not default values)
     */
    suspend fun hasTimerSettings(): Boolean {
        return timerPreferences.hasTimerSettings()
    }

    // ✅ NEW: Timer countdown methods for UI

    /**
     * Get timer end timestamp (when alarm will trigger) - uses stored exact time
     */
    suspend fun getTimerEndTime(): Long? {
        val isActive = timerPreferences.isAlarmActive()
        return if (isActive) {
            val endTime = timerPreferences.getSavedAlarmEndTimeMillis()
            if (endTime > 0L) endTime else null
        } else {
            null
        }
    }

    /**
     * Get remaining time in milliseconds until alarm triggers
     */
    suspend fun getRemainingTimeMillis(): Long {
        val endTime = getTimerEndTime()
        return if (endTime != null) {
            val currentTime = System.currentTimeMillis()
            maxOf(0L, endTime - currentTime)
        } else {
            0L
        }
    }

    /**
     * Format remaining time to "5:15m" format for UI display
     */
    fun formatRemainingTime(remainingMillis: Long): String {
        if (remainingMillis <= 0) return ""

        val totalSeconds = remainingMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return "${minutes}:${seconds.toString().padStart(2, '0')}m"
    }

    /**
     * Check if timer countdown should be visible
     */
    suspend fun shouldShowCountdown(): Boolean {
        val isActive = timerPreferences.isAlarmActive()
        val remainingTime = getRemainingTimeMillis()
        return isActive && remainingTime > 0
    }

    /**
     * Get saved alarm end time in milliseconds (for optimized countdown)
     */
    suspend fun getSavedAlarmEndTimeMillis(): Long {
        return timerPreferences.getSavedAlarmEndTimeMillis()
    }
}