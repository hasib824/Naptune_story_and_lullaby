package com.naptune.lullabyandstory.domain.usecase.fcm

import com.naptune.lullabyandstory.domain.repository.fcm.FcmRepository
import javax.inject.Inject

/**
 * Use Case: Unsubscribe from FCM Topic
 * Unsubscribes device from receiving notifications for a specific topic
 */
class UnsubscribeFromTopicUseCase @Inject constructor(
    private val fcmRepository: FcmRepository
) {
    suspend operator fun invoke(topic: String): Result<Unit> {
        return fcmRepository.unsubscribeFromTopic(topic)
    }
}
