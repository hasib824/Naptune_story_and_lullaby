package com.naptune.lullabyandstory.data.model.fcm

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for FCM token registration response
 * Received from server after successful registration
 */
data class FcmTokenResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("device_id")
    val deviceId: String?
)
