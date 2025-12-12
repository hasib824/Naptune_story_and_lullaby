package com.naptune.lullabyandstory.presentation.fcm

/**
 * MVI Intent - User actions related to FCM/Notifications
 * Sealed class representing all possible user intentions
 */
sealed class FcmIntent {

    /**
     * Initialize FCM - Get token and register with server
     */
    data object InitializeFcm : FcmIntent()

    /**
     * Request notification permission (Android 13+)
     */
    data object RequestNotificationPermission : FcmIntent()

    /**
     * Refresh FCM token - Force token regeneration
     */
    data object RefreshToken : FcmIntent()

    /**
     * Enable/Disable notifications
     */
    data class SetNotificationsEnabled(val enabled: Boolean) : FcmIntent()

    /**
     * Subscribe to a topic
     */
    data class SubscribeToTopic(val topic: String) : FcmIntent()

    /**
     * Unsubscribe from a topic
     */
    data class UnsubscribeFromTopic(val topic: String) : FcmIntent()

    /**
     * Clear error state
     */
    data object ClearError : FcmIntent()
}
