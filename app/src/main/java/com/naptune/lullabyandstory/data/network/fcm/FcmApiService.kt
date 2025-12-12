package com.naptune.lullabyandstory.data.network.fcm

import com.naptune.lullabyandstory.data.model.fcm.FcmTokenRequest
import com.naptune.lullabyandstory.data.model.fcm.FcmTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API Service for FCM token management
 * Base URL: https://notifier.appswave.xyz/
 */
interface FcmApiService {

    /**
     * Register FCM device token with the server
     * Endpoint: POST /register-device
     */
    @POST("register-device")
    suspend fun registerDevice(
        @Body request: FcmTokenRequest
    ): Response<FcmTokenResponse>

    /**
     * Unregister FCM device token from the server
     * Endpoint: POST /unregister-device
     */
    @POST("unregister-device")
    suspend fun unregisterDevice(
        @Body request: FcmTokenRequest
    ): Response<FcmTokenResponse>
}
