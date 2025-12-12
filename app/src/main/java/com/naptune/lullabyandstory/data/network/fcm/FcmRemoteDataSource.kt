package com.naptune.lullabyandstory.data.network.fcm

import android.util.Log
import com.naptune.lullabyandstory.data.model.fcm.FcmTokenRequest
import com.naptune.lullabyandstory.data.model.fcm.FcmTokenResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote Data Source for FCM API operations
 * Handles network calls for token registration/unregistration
 */
@Singleton
class FcmRemoteDataSource @Inject constructor(
    private val fcmApiService: FcmApiService
) {
    private val TAG = "FcmRemoteDataSource"

    /**
     * Register FCM token with the server
     * @return Result<FcmTokenResponse> - Success or failure with error message
     */
    suspend fun registerToken(request: FcmTokenRequest): Result<FcmTokenResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = fcmApiService.registerDevice(request)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d(TAG, "Token registered successfully: ${body.deviceId}")
                        Result.success(body)
                    } else {
                        Log.e(TAG, "Token registration failed: ${body.message}")
                        Result.failure(Exception(body.message ?: "Registration failed"))
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    Log.e(TAG, "Token registration failed: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Token registration exception", e)
                Result.failure(e)
            }
        }

    /**
     * Unregister FCM token from the server
     * @return Result<FcmTokenResponse> - Success or failure with error message
     */
    suspend fun unregisterToken(request: FcmTokenRequest): Result<FcmTokenResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = fcmApiService.unregisterDevice(request)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d(TAG, "Token unregistered successfully")
                        Result.success(body)
                    } else {
                        Log.e(TAG, "Token unregistration failed: ${body.message}")
                        Result.failure(Exception(body.message ?: "Unregistration failed"))
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    Log.e(TAG, "Token unregistration failed: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Token unregistration exception", e)
                Result.failure(e)
            }
        }
}
