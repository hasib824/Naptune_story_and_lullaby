package com.naptune.lullabyandstory.data.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.naptune.lullabyandstory.data.model.fcm.NotificationPayload
import com.naptune.lullabyandstory.domain.repository.fcm.FcmRepository
import com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper
import com.naptune.lullabyandstory.utils.fcm.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Firebase Cloud Messaging Service
 * Receives push notifications and handles token refresh
 *
 * This service is automatically triggered by Firebase when:
 * 1. A new FCM message is received
 * 2. The FCM token is refreshed
 */
@AndroidEntryPoint
class NaptuneMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmRepository: FcmRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val TAG = "NaptuneMessagingService"

    /**
     * Called when a new FCM message is received
     * Handles both notification and data payloads
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")

        // Extract notification data
        val notificationPayload = extractNotificationPayload(message)

        // Log notification details
        Log.d(TAG, "Notification: title=${notificationPayload.title}, body=${notificationPayload.body}")
        Log.d(TAG, "Data payload: screenRoute=${notificationPayload.screenRoute}, contentId=${notificationPayload.contentId}")

        // Display notification
        displayNotification(notificationPayload)

        // Track notification received (analytics)
        trackNotificationReceived(notificationPayload)
    }

    /**
     * Called when FCM token is refreshed
     * This happens when:
     * - App is first installed
     * - App data is cleared
     * - Device is restored
     * - Token expires
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "════════════════════════════════════════")
        Log.d(TAG, "🔄 FCM TOKEN REFRESH TRIGGERED")
        Log.d(TAG, "════════════════════════════════════════")
        Log.d(TAG, "📝 New token (first 30 chars): ${token.take(30)}...")
        Log.d(TAG, "📏 Token length: ${token.length} characters")

        // Save token and register with server
        serviceScope.launch {
            try {
                // Save token locally
                fcmRepository.getFcmToken()

                // Register with server
                Log.d(TAG, "📡 Registering new token with server...")
                val result = fcmRepository.registerToken(token)

                if (result.isSuccess) {
                    Log.d(TAG, "✅ NEW TOKEN REGISTERED SUCCESSFULLY")
                    Log.d(TAG, "════════════════════════════════════════")
                } else {
                    Log.e(TAG, "❌ FAILED TO REGISTER NEW TOKEN")
                    Log.e(TAG, "Error: ${result.exceptionOrNull()?.message}")
                    Log.e(TAG, "════════════════════════════════════════")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR HANDLING NEW TOKEN", e)
                Log.e(TAG, "Error: ${e.message}")
                Log.e(TAG, "════════════════════════════════════════")
            }
        }
    }

    /**
     * Extract notification payload from RemoteMessage
     * Handles both notification payload and data payload
     */
    private fun extractNotificationPayload(message: RemoteMessage): NotificationPayload {
        // Priority 1: Check notification payload (sent via Firebase Console)
        val notification = message.notification
        val title = notification?.title
        val body = notification?.body
        val imageUrl = notification?.imageUrl?.toString()

        // Priority 2: Extract data payload (sent via API)
        val data = message.data
        val screenRoute = data["screenRoute"] ?: data["screen_route"]
        val contentId = data["contentId"] ?: data["content_id"]
        val deepLink = data["deepLink"] ?: data["deep_link"]
        val actionUrl = data["actionUrl"] ?: data["action_url"]

        return NotificationPayload(
            title = title ?: data["title"],
            body = body ?: data["body"],
            imageUrl = imageUrl ?: data["imageUrl"] ?: data["image_url"],
            screenRoute = screenRoute,
            contentId = contentId,
            deepLink = deepLink,
            actionUrl = actionUrl,
            data = data
        )
    }

    /**
     * Display notification using NotificationHelper
     * Launches coroutine since showNotification is a suspend function
     */
    private fun displayNotification(payload: NotificationPayload) {
        serviceScope.launch {
            try {
                notificationHelper.showNotification(
                    title = payload.title ?: "Naptune",
                    message = payload.body ?: "",
                    imageUrl = payload.imageUrl,
                    screenRoute = payload.screenRoute,
                    contentId = payload.contentId,
                    deepLink = payload.deepLink
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error displaying notification", e)
            }
        }
    }

    /**
     * Track notification received event (for analytics)
     */
    private fun trackNotificationReceived(payload: NotificationPayload) {
        analyticsHelper.logNotificationReceived(
            title = payload.title,
            screenRoute = payload.screenRoute,
            hasImage = payload.imageUrl != null
        )
        Log.d(TAG, "✅ Notification analytics tracked")
    }

    /**
     * Cleanup when service is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        // ServiceScope will be automatically cancelled when service is destroyed
    }
}
