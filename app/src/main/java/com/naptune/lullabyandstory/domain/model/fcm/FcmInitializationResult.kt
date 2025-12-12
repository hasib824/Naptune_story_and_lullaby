package com.naptune.lullabyandstory.domain.model.fcm

/**
 * Domain Model for FCM Initialization Result
 * Returned by InitializeFcmUseCase
 */
data class FcmInitializationResult(
    val token: String,
    val deviceId: String?,
    val isRegistered: Boolean
)
