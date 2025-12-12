package com.naptune.lullabyandstory.domain.usecase.fcm

import com.naptune.lullabyandstory.domain.repository.fcm.FcmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case: Get Stored FCM Token
 * Returns Flow of stored FCM token from preferences
 */
class GetFcmTokenUseCase @Inject constructor(
    private val fcmRepository: FcmRepository
) {
    operator fun invoke(): Flow<String?> {
        return fcmRepository.getStoredToken()
    }
}
