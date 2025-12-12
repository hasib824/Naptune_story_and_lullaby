package com.naptune.lullabyandstory.domain.usecase

import android.content.Context
import com.naptune.lullabyandstory.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Use case for sending feedback via email
 * Follows Clean Architecture principles by encapsulating business logic
 */
class SendFeedbackUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    /**
     * Execute the send feedback operation
     * @param context Android context needed for launching email intent
     * @return Result indicating success or failure with appropriate message
     */
    suspend operator fun invoke(context: Context): Result<String> {
        return try {
            profileRepository.sendFeedback(context)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}