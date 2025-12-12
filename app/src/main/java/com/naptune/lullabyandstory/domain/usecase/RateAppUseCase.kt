package com.naptune.lullabyandstory.domain.usecase

import android.content.Context
import com.naptune.lullabyandstory.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Use case for rating the app on Play Store
 * Follows Clean Architecture principles by encapsulating business logic
 */
class RateAppUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    /**
     * Execute the rate app operation
     * @param context Android context needed for launching Play Store intent
     * @return Result indicating success or failure with appropriate message
     */
    suspend operator fun invoke(context: Context): Result<String> {
        return try {
            profileRepository.rateApp(context)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}