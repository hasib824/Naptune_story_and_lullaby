package com.naptune.lullabyandstory.domain.usecase

import android.content.Context
import com.naptune.lullabyandstory.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Use case for sharing the Naptune app with others
 * Follows Clean Architecture principles by encapsulating business logic
 */
class ShareAppUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    /**
     * Execute the share app operation
     * @param context Android context needed for launching share intent
     * @return Result indicating success or failure with appropriate message
     */
    suspend operator fun invoke(context: Context): Result<String> {
        return try {
            profileRepository.shareApp(context)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}