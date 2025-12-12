package com.naptune.lullabyandstory.data.model.fcm

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for FCM token registration request
 * Sent to server: https://notifier.appswave.xyz/register-device
 */
data class FcmTokenRequest(
    @SerializedName("app_ref")
    val appRef: String,           // APP#fe0b4e1ee9ee5e20

    @SerializedName("app_secret")
    val appSecret: String,        // ABCDEFGHIJ

    @SerializedName("device_token")
    val deviceToken: String,      // FCM token from Firebase

    @SerializedName("topic")
    val topic: String = "global", // Subscribe to global topic

    @SerializedName("platform")
    val platform: String = "android"
)
