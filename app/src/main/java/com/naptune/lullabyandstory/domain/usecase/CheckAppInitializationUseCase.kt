package com.naptune.lullabyandstory.domain.usecase

import com.naptune.lullabyandstory.data.repository.AppInitializationRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class CheckAppInitializationUseCase @Inject constructor(
    private val repository: AppInitializationRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return try {
            // Ensure minimum splash duration
            val startTime = System.currentTimeMillis()
            
            // Perform initialization tasks
            val isInitialized = repository.isAppInitialized()
            
            // Calculate elapsed time and ensure minimum duration
            val elapsedTime = System.currentTimeMillis() - startTime
            val minimumDuration = 2000L // 2.5 seconds minimum
            
            if (elapsedTime < minimumDuration) {
                delay(minimumDuration - elapsedTime)
            }
            
            Result.success(isInitialized)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
