package com.naptune.lullabyandstory.utils.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.naptune.lullabyandstory.MainActivity
import com.naptune.lullabyandstory.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for displaying FCM notifications
 * Handles notification channels, styles, and actions
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "NotificationHelper"
    private val CHANNEL_ID = "naptune_notifications"
    private val CHANNEL_NAME = "Naptune Notifications"
    private var notificationId = 1000

    init {
        createNotificationChannel()
    }

    /**
     * Create notification channel (required for Android O+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = context.getString(R.string.notification_channel_description)
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }

    /**
     * Show notification with optional image and routing
     *
     * @param title Notification title
     * @param message Notification message
     * @param imageUrl Optional image URL for big picture style
     * @param screenRoute Screen route code ("1"-"5")
     * @param contentId Content ID for player/story screens
     * @param deepLink Optional deep link URL
     */
    suspend fun showNotification(
        title: String,
        message: String,
        imageUrl: String? = null,
        screenRoute: String? = null,
        contentId: String? = null,
        deepLink: String? = null
    ) {
        withContext(Dispatchers.IO) {
            try {
                // Create intent for notification tap
                val intent = createNotificationIntent(screenRoute, contentId, deepLink)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Build notification
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setColor(context.getColor(R.color.notification_accent))

                // Add big text style for long messages
                if (message.length > 50) {
                    builder.setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(message)
                    )
                }

                // Add big picture style if image URL provided
                imageUrl?.let { url ->
                    val bitmap = downloadImage(url)
                    bitmap?.let {
                        builder.setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(it)
                                .bigLargeIcon(null as Bitmap?) // Hide large icon when expanded
                        )
                        builder.setLargeIcon(it)
                    }
                }

                // Show notification
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(notificationId++, builder.build())

                Log.d(TAG, "Notification displayed: title=$title, route=$screenRoute")
            } catch (e: Exception) {
                Log.e(TAG, "Error showing notification", e)
            }
        }
    }

    /**
     * Create intent for notification tap
     * Routes to appropriate screen based on screenRoute parameter
     */
    private fun createNotificationIntent(
        screenRoute: String?,
        contentId: String?,
        deepLink: String?
    ): Intent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            // Add routing data
            putExtra("FROM_NOTIFICATION", true)
            screenRoute?.let { putExtra("SCREEN_ROUTE", it) }
            contentId?.let { putExtra("CONTENT_ID", it) }
            deepLink?.let { putExtra("DEEP_LINK", it) }
        }

        return intent
    }

    /**
     * Download image from URL for big picture notification
     * @return Bitmap or null if download fails
     */
    private suspend fun downloadImage(imageUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.doInput = true
                connection.connect()
                val input = connection.getInputStream()
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download notification image: $imageUrl", e)
                null
            }
        }
    }

    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancelAll()
        Log.d(TAG, "All notifications cancelled")
    }

    /**
     * Cancel specific notification by ID
     */
    fun cancelNotification(notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
        Log.d(TAG, "Notification cancelled: ID=$notificationId")
    }
}
