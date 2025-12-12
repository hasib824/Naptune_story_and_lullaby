package com.naptune.lullabyandstory.domain.repository.fcm

import com.naptune.lullabyandstory.data.model.fcm.FcmTokenResponse
import kotlinx.coroutines.flow.Flow

/**
 * Domain Repository Interface for FCM operations
 * Clean Architecture - Domain Layer contract
 * Implementation in data layer: FcmRepositoryImpl
 */
interface FcmRepository {

    /**
     * Get FCM token from Firebase
     * @return Result<String> - FCM token or error
     */
    suspend fun getFcmToken(): Result<String>

    /**
     * Register FCM token with the server
     * @param token FCM token to register
     * @return Result<FcmTokenResponse> - Server response or error
     */
    suspend fun registerToken(token: String): Result<FcmTokenResponse>

    /**
     * Unregister FCM token from the server
     * @return Result<FcmTokenResponse> - Server response or error
     */
    suspend fun unregisterToken(): Result<FcmTokenResponse>

    /**
     * Subscribe to FCM topic
     * @param topic Topic name to subscribe to
     * @return Result<Unit> - Success or error
     */
    suspend fun subscribeToTopic(topic: String): Result<Unit>

    /**
     * Unsubscribe from FCM topic
     * @param topic Topic name to unsubscribe from
     * @return Result<Unit> - Success or error
     */
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit>

    /**
     * Check if token is registered with server
     * @return Boolean - true if registered
     */
    suspend fun isTokenRegistered(): Boolean

    /**
     * Get stored FCM token from preferences
     * @return Flow<String?> - FCM token flow
     */
    fun getStoredToken(): Flow<String?>

    /**
     * Get device ID from preferences
     * @return Flow<String?> - Device ID flow
     */
    fun getDeviceId(): Flow<String?>

    /**
     * Refresh FCM token - Delete old and get new token
     * @return Result<String> - New FCM token or error
     */
    suspend fun refreshToken(): Result<String>
}
