package com.naptune.lullabyandstory.domain.usecase.fcm

import com.naptune.lullabyandstory.domain.repository.fcm.FcmRepository
import javax.inject.Inject

/**
 * Use Case: Subscribe to FCM Topic
 * Subscribes device to receive notifications for a specific topic
 */
class SubscribeToTopicUseCase @Inject constructor(
    private val fcmRepository: FcmRepository
) {
    suspend operator fun invoke(topic: String): Result<Unit> {
        return fcmRepository.subscribeToTopic(topic)
    }
}
