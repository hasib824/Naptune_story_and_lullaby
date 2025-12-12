package com.naptune.lullabyandstory.domain.usecase.fcm

import com.naptune.lullabyandstory.domain.model.fcm.FcmInitializationResult
import com.naptune.lullabyandstory.domain.repository.fcm.FcmRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use Case: Initialize FCM System
 * - Get FCM token from Firebase
 * - Register token with server
 * - Subscribe to global topic
 * Returns: FcmInitializationResult with token and device ID
 */
class InitializeFcmUseCase @Inject constructor(
    private val fcmRepository: FcmRepository
) {
    suspend operator fun invoke(): Result<FcmInitializationResult> {
        return try {
            // Step 1: Get FCM token
            val tokenResult = fcmRepository.getFcmToken()
            if (tokenResult.isFailure) {
                return Result.failure(tokenResult.exceptionOrNull() ?: Exception("Failed to get FCM token"))
            }

            val token = tokenResult.getOrThrow()

            // Step 2: Register token with server
            val registerResult = fcmRepository.registerToken(token)
            if (registerResult.isFailure) {
                return Result.failure(registerResult.exceptionOrNull() ?: Exception("Failed to register token"))
            }

            val response = registerResult.getOrThrow()

            // Step 3: Get device ID from repository
            val deviceId = fcmRepository.getDeviceId().first()

            // Return initialization result
            Result.success(
                FcmInitializationResult(
                    token = token,
                    deviceId = deviceId,
                    isRegistered = true
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
