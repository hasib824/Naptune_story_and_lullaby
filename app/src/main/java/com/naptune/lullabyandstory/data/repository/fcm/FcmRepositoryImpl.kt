package com.naptune.lullabyandstory.data.repository.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.naptune.lullabyandstory.BuildConfig
import com.naptune.lullabyandstory.data.datastore.FcmPreferences
import com.naptune.lullabyandstory.data.model.fcm.FcmTokenRequest
import com.naptune.lullabyandstory.data.model.fcm.FcmTokenResponse
import com.naptune.lullabyandstory.data.network.fcm.FcmRemoteDataSource
import com.naptune.lullabyandstory.domain.repository.fcm.FcmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository Implementation for FCM operations
 * Handles token generation, registration, and topic subscription
 * Follows Clean Architecture - Data Layer
 */
@Singleton
class FcmRepositoryImpl @Inject constructor(
    private val fcmRemoteDataSource: FcmRemoteDataSource,
    private val fcmPreferences: FcmPreferences,
    private val firebaseMessaging: FirebaseMessaging
) : FcmRepository {

    private val TAG = "FcmRepositoryImpl"
    private val GLOBAL_TOPIC = "global"

    /**
     * Get current FCM token from Firebase
     * Generates new token if needed
     */
    override suspend fun getFcmToken(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔑 Requesting FCM token from Firebase...")
            val token = firebaseMessaging.token.await()
            fcmPreferences.saveFcmToken(token)

            // ✅ LOGGING: Token generated successfully
            Log.d(TAG, "✅ FCM TOKEN GENERATED SUCCESSFULLY!")
            Log.d(TAG, "📝 Token (first 30 chars): ${token.take(30)}...")
            Log.d(TAG, "📏 Token length: ${token.length} characters")

            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "❌ FAILED TO GET FCM TOKEN", e)
            Log.e(TAG, "Error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Register FCM token with the server
     * Subscribes to global topic on success
     */
    override suspend fun registerToken(token: String): Result<FcmTokenResponse> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📡 Sending token to server: ${BuildConfig.FCM_SERVER_URL}")
                Log.d(TAG, "🔐 App Reference: ${BuildConfig.FCM_APP_REF}")
                Log.d(TAG, "📝 Token (first 30 chars): ${token.take(30)}...")

                // Create registration request
                val request = FcmTokenRequest(
                    appRef = BuildConfig.FCM_APP_REF,
                    appSecret = BuildConfig.FCM_APP_SECRET,
                    deviceToken = token,
                    topic = GLOBAL_TOPIC,
                    platform = "android"
                )

                // Register with server
                val result = fcmRemoteDataSource.registerToken(request)

                if (result.isSuccess) {
                    val response = result.getOrThrow()

                    // Save registration status
                    fcmPreferences.setTokenRegistered(true)
                    fcmPreferences.updateLastTokenSync()
                    response.deviceId?.let { fcmPreferences.saveDeviceId(it) }

                    // Subscribe to global topic
                    subscribeToTopic(GLOBAL_TOPIC)

                    // ✅ LOGGING: Token sent to server successfully
                    Log.d(TAG, "════════════════════════════════════════")
                    Log.d(TAG, "✅ FCM TOKEN SENT TO SERVER SUCCESSFULLY!")
                    Log.d(TAG, "════════════════════════════════════════")
                    Log.d(TAG, "📱 Device ID: ${response.deviceId}")
                    Log.d(TAG, "🌍 Subscribed to topic: $GLOBAL_TOPIC")
                    Log.d(TAG, "⏰ Registered at: ${System.currentTimeMillis()}")
                    Log.d(TAG, "════════════════════════════════════════")

                    Result.success(response)
                } else {
                    val error = result.exceptionOrNull() ?: Exception("Registration failed")
                    Log.e(TAG, "❌ FAILED TO SEND TOKEN TO SERVER")
                    Log.e(TAG, "Error: ${error.message}")
                    Result.failure(error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ TOKEN REGISTRATION EXCEPTION", e)
                Log.e(TAG, "Error: ${e.message}")
                Result.failure(e)
            }
        }

    /**
     * Unregister FCM token from the server
     */
    override suspend fun unregisterToken(): Result<FcmTokenResponse> =
        withContext(Dispatchers.IO) {
            try {
                val token = fcmPreferences.fcmToken.first() ?: return@withContext Result.failure(
                    Exception("No FCM token found")
                )

                val request = FcmTokenRequest(
                    appRef = BuildConfig.FCM_APP_REF,
                    appSecret = BuildConfig.FCM_APP_SECRET,
                    deviceToken = token,
                    topic = GLOBAL_TOPIC,
                    platform = "android"
                )

                val result = fcmRemoteDataSource.unregisterToken(request)

                if (result.isSuccess) {
                    fcmPreferences.setTokenRegistered(false)
                    unsubscribeFromTopic(GLOBAL_TOPIC)
                    Log.d(TAG, "Token unregistered successfully")
                }

                result
            } catch (e: Exception) {
                Log.e(TAG, "Token unregistration failed", e)
                Result.failure(e)
            }
        }

    /**
     * Subscribe to FCM topic
     */
    override suspend fun subscribeToTopic(topic: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                firebaseMessaging.subscribeToTopic(topic).await()
                Log.d(TAG, "Subscribed to topic: $topic")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to subscribe to topic: $topic", e)
                Result.failure(e)
            }
        }

    /**
     * Unsubscribe from FCM topic
     */
    override suspend fun unsubscribeFromTopic(topic: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                firebaseMessaging.unsubscribeFromTopic(topic).await()
                Log.d(TAG, "Unsubscribed from topic: $topic")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unsubscribe from topic: $topic", e)
                Result.failure(e)
            }
        }

    /**
     * Check if token is registered with server
     */
    override suspend fun isTokenRegistered(): Boolean {
        return fcmPreferences.isTokenRegistered.first()
    }

    /**
     * Get stored FCM token from preferences
     */
    override fun getStoredToken(): Flow<String?> {
        return fcmPreferences.fcmToken
    }

    /**
     * Get device ID from preferences
     */
    override fun getDeviceId(): Flow<String?> {
        return fcmPreferences.deviceId
    }

    /**
     * Refresh FCM token - Get new token and register with server
     */
    override suspend fun refreshToken(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Delete old token instance (forces new token generation)
            firebaseMessaging.deleteToken().await()

            // Get new token
            val newToken = firebaseMessaging.token.await()
            fcmPreferences.saveFcmToken(newToken)

            // Register new token with server
            registerToken(newToken)

            Log.d(TAG, "Token refreshed successfully")
            Result.success(newToken)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh token", e)
            Result.failure(e)
        }
    }
}
