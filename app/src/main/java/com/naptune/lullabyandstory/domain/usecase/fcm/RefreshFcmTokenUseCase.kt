package com.naptune.lullabyandstory.domain.usecase.fcm

import com.naptune.lullabyandstory.domain.repository.fcm.FcmRepository
import javax.inject.Inject

/**
 * Use Case: Refresh FCM Token
 * - Delete old token
 * - Get new token from Firebase
 * - Register new token with server
 * Returns: New FCM token
 */
class RefreshFcmTokenUseCase @Inject constructor(
    private val fcmRepository: FcmRepository
) {
    suspend operator fun invoke(): Result<String> {
        return fcmRepository.refreshToken()
    }
}
