package com.naptune.lullabyandstory.data.model.fcm

import kotlinx.serialization.Serializable

/**
 * FCM Notification Payload Data Model
 * Represents the data sent with push notifications
 */
@Serializable
data class NotificationPayload(
    val title: String? = null,
    val body: String? = null,
    val imageUrl: String? = null,
    val screenRoute: String? = null,  // "1"=Main, "2"=Lullaby, "3"=Story, "4"=Player, "5"=StoryManager
    val contentId: String? = null,    // Required for screenRoute "4" and "5"
    val deepLink: String? = null,
    val actionUrl: String? = null,
    val data: Map<String, String>? = null
) {
    companion object {
        // Screen route constants
        const val ROUTE_MAIN = "1"
        const val ROUTE_LULLABY = "2"
        const val ROUTE_STORY = "3"
        const val ROUTE_AUDIO_PLAYER = "4"
        const val ROUTE_STORY_MANAGER = "5"
    }
}
