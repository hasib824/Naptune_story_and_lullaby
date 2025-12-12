package com.naptune.lullabyandstory.utils.fcm

import android.content.Intent
import android.util.Log
import androidx.navigation.NavController
import com.naptune.lullabyandstory.data.model.fcm.NotificationPayload

/**
 * Deep Link Router for FCM Notifications
 * Routes to appropriate screen based on notification payload
 *
 * Screen Routes:
 * "1" -> MainScreen
 * "2" -> LullabyScreen
 * "3" -> StoryScreen
 * "4" -> AudioPlayerScreen (requires contentId)
 * "5" -> StoryManagerScreen (requires contentId)
 */
object DeepLinkRouter {

    private const val TAG = "DeepLinkRouter"

    /**
     * Route to screen based on intent extras from notification
     * Called from MainActivity when launched from notification
     *
     * @param intent Intent from notification tap
     * @param navController Navigation controller for Compose Navigation
     */
    fun handleNotificationIntent(intent: Intent, navController: NavController) {
        val isFromNotification = intent.getBooleanExtra("FROM_NOTIFICATION", false)
        if (!isFromNotification) return

        val screenRoute = intent.getStringExtra("SCREEN_ROUTE")
        val contentId = intent.getStringExtra("CONTENT_ID")
        val deepLink = intent.getStringExtra("DEEP_LINK")

        Log.d(TAG, "Handling notification intent: route=$screenRoute, contentId=$contentId")

        // Route to appropriate screen
        when (screenRoute) {
            NotificationPayload.ROUTE_MAIN -> {
                // Navigate to main screen (default - no action needed)
                Log.d(TAG, "Routing to MainScreen")
            }

            NotificationPayload.ROUTE_LULLABY -> {
                // Navigate to lullaby screen
                navController.navigate("lullaby_screen") {
                    launchSingleTop = true
                }
                Log.d(TAG, "Routing to LullabyScreen")
            }

            NotificationPayload.ROUTE_STORY -> {
                // Navigate to story screen
                navController.navigate("story_screen") {
                    launchSingleTop = true
                }
                Log.d(TAG, "Routing to StoryScreen")
            }

            NotificationPayload.ROUTE_AUDIO_PLAYER -> {
                // Navigate to audio player screen (requires contentId)
                if (contentId != null) {
                    navController.navigate("audio_player_screen/$contentId") {
                        launchSingleTop = true
                    }
                    Log.d(TAG, "Routing to AudioPlayerScreen with contentId=$contentId")
                } else {
                    Log.w(TAG, "AudioPlayerScreen requires contentId, routing to MainScreen instead")
                }
            }

            NotificationPayload.ROUTE_STORY_MANAGER -> {
                // Navigate to story manager screen (requires contentId)
                if (contentId != null) {
                    navController.navigate("story_manager_screen/$contentId") {
                        launchSingleTop = true
                    }
                    Log.d(TAG, "Routing to StoryManagerScreen with contentId=$contentId")
                } else {
                    Log.w(TAG, "StoryManagerScreen requires contentId, routing to MainScreen instead")
                }
            }

            else -> {
                // Unknown route or no route - stay on current screen
                Log.w(TAG, "Unknown screen route: $screenRoute, staying on current screen")
            }
        }

        // Clear intent extras to prevent re-routing on configuration changes
        intent.removeExtra("FROM_NOTIFICATION")
        intent.removeExtra("SCREEN_ROUTE")
        intent.removeExtra("CONTENT_ID")
        intent.removeExtra("DEEP_LINK")
    }

    /**
     * Check if intent is from notification
     */
    fun isFromNotification(intent: Intent): Boolean {
        return intent.getBooleanExtra("FROM_NOTIFICATION", false)
    }

    /**
     * Get screen route from intent
     */
    fun getScreenRoute(intent: Intent): String? {
        return intent.getStringExtra("SCREEN_ROUTE")
    }

    /**
     * Get content ID from intent
     */
    fun getContentId(intent: Intent): String? {
        return intent.getStringExtra("CONTENT_ID")
    }
}
