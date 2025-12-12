package com.naptune.lullabyandstory.presentation.fcm

/**
 * MVI State - UI state for FCM/Notifications
 * Represents the current state of FCM system
 */
data class FcmUiState(
    val isInitialized: Boolean = false,
    val isLoading: Boolean = false,
    val fcmToken: String? = null,
    val deviceId: String? = null,
    val isTokenRegistered: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val hasNotificationPermission: Boolean = false,
    val subscribedTopics: Set<String> = emptySet(),
    val error: String? = null,
    val successMessage: String? = null
) {
    /**
     * Check if FCM is fully configured
     */
    val isFullyConfigured: Boolean
        get() = isInitialized &&
                isTokenRegistered &&
                fcmToken != null &&
                hasNotificationPermission

    /**
     * Check if there's an active error
     */
    val hasError: Boolean
        get() = error != null
}
