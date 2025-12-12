package com.naptune.lullabyandstory.data.fcm

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.naptune.lullabyandstory.domain.repository.fcm.FcmRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * WorkManager Worker for background FCM token synchronization
 * Ensures token is registered with server even when app is in background
 *
 * Triggered by:
 * - Periodic sync (every 24 hours)
 * - Manual refresh request
 * - App startup
 */
@HiltWorker
class TokenUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fcmRepository: FcmRepository
) : CoroutineWorker(context, workerParams) {

    private val TAG = "TokenUploadWorker"

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting token upload work...")

            // Check if token is already registered
            val isRegistered = fcmRepository.isTokenRegistered()

            if (isRegistered) {
                // Token already registered, verify it's still valid
                val storedToken = fcmRepository.getStoredToken().first()

                if (storedToken != null) {
                    Log.d(TAG, "Token already registered and valid")
                    return Result.success()
                }
            }

            // Get or refresh FCM token
            val tokenResult = fcmRepository.getFcmToken()

            if (tokenResult.isFailure) {
                Log.e(TAG, "Failed to get FCM token", tokenResult.exceptionOrNull())
                return Result.retry()
            }

            val token = tokenResult.getOrThrow()

            // Register token with server
            val registerResult = fcmRepository.registerToken(token)

            if (registerResult.isSuccess) {
                Log.d(TAG, "Token uploaded successfully")
                Result.success()
            } else {
                Log.e(TAG, "Failed to upload token", registerResult.exceptionOrNull())
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token upload work failed", e)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "fcm_token_upload_work"
    }
}
