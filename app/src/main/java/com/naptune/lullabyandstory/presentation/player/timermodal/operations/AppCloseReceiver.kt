package com.naptune.lullabyandstory.presentation.player.timermodal.operations

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.presentation.player.service.MusicController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * ✅ NEW: Enhanced BroadcastReceiver for smart timer behavior
 * - Regular timers (5min, 10min, etc.): Pause audio
 * - End of Story: Send broadcast to navigate to next story
 * - No process kill, app stays alive
 */
@AndroidEntryPoint
class AppCloseReceiver : BroadcastReceiver() {

    @Inject
    lateinit var musicController: MusicController

    @Inject
    lateinit var analyticsHelper: com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper

    companion object {
        private const val TAG = "AppCloseReceiver"
        const val ACTION_NAVIGATE_TO_NEXT_STORY = "com.naptune.lullabyandstory.NAVIGATE_TO_NEXT_STORY"

        // Timer index constants
        const val TIMER_INDEX_REGULAR_5MIN = 0
        const val TIMER_INDEX_REGULAR_10MIN = 1
        const val TIMER_INDEX_REGULAR_15MIN = 2
        const val TIMER_INDEX_REGULAR_30MIN = 3
        const val TIMER_INDEX_REGULAR_1HR = 4
        const val TIMER_INDEX_END_OF_STORY = 5
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "⏰ Timer alarm triggered!")

        if (context == null || intent == null) {
            Log.e(TAG, "❌ Context or Intent is null")
            return
        }

        // ✅ Extract timer index from Intent extras
        val timerIndex = intent.getIntExtra("TIMER_INDEX", -1)
        Log.d(TAG, "⏰ Timer index: $timerIndex")

        when (timerIndex) {
            TIMER_INDEX_REGULAR_5MIN,
            TIMER_INDEX_REGULAR_10MIN,
            TIMER_INDEX_REGULAR_15MIN,
            TIMER_INDEX_REGULAR_30MIN,
            TIMER_INDEX_REGULAR_1HR -> {
                // ✅ Regular timer - pause audio immediately
                Log.d(TAG, "⏸️ Regular timer triggered - pausing audio")

                // ✅ Analytics: Track timer completed
                val currentAudio = musicController.currentAudioItem.value
                val durationMinutes = when (timerIndex) {
                    TIMER_INDEX_REGULAR_5MIN -> 5
                    TIMER_INDEX_REGULAR_10MIN -> 10
                    TIMER_INDEX_REGULAR_15MIN -> 15
                    TIMER_INDEX_REGULAR_30MIN -> 30
                    TIMER_INDEX_REGULAR_1HR -> 60
                    else -> 0
                }
                analyticsHelper.logTimerCompleted(
                    contentId = currentAudio?.documentId ?: "none",
                    durationMinutes = durationMinutes,
                    timerType = "fixed_duration",
                    didStopPlayback = musicController.hasActiveAudio()
                )

                if (musicController.hasActiveAudio()) {
                    musicController.pauseAudio()
                    Log.d(TAG, "✅ Audio paused successfully")

                    // Show toast notification
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_timer_audio_paused),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.w(TAG, "⚠️ No active audio to pause")
                }
            }

            TIMER_INDEX_END_OF_STORY -> {
                // ✅ End of Story - send broadcast to navigate to next story
                Log.d(TAG, "📖 End of Story timer triggered - sending navigation broadcast")

                // ✅ Analytics: Track end of story timer completed
                val currentAudio = musicController.currentAudioItem.value
                analyticsHelper.logTimerCompleted(
                    contentId = currentAudio?.documentId ?: "none",
                    durationMinutes = 0, // End of story doesn't have fixed duration
                    timerType = "end_of_content",
                    didStopPlayback = false // End of story doesn't stop, it navigates
                )

                val navigationIntent = Intent(ACTION_NAVIGATE_TO_NEXT_STORY)
                context.sendBroadcast(navigationIntent)

                Log.d(TAG, "✅ Navigation broadcast sent")

                // Show toast notification
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_story_ended),
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                Log.e(TAG, "❌ Invalid timer index: $timerIndex")
            }
        }

        Log.d(TAG, "✅ Timer action completed - app remains alive")
    }
}
