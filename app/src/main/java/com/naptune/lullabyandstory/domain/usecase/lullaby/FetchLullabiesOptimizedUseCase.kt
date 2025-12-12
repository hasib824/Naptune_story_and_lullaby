package com.naptune.lullabyandstory.domain.usecase.lullaby

import android.util.Log
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.data.repository.LullabyRepositoryImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ✅ ULTRA OPTIMIZED: Database-level language-specific query UseCase
 * - No ViewModel processing needed
 * - No translation object overhead
 * - Direct language-specific name from database
 * - Memory efficient (n×7 less data transfer)
 */
class FetchLullabiesOptimizedUseCase @Inject constructor(
    private val lullabyRepository: LullabyRepositoryImpl
) {

    /**
     * Returns lullabies with pre-computed localized names from database query
     * No processing needed in ViewModel - ultra fast!
     */
    operator fun invoke(): Flow<List<LullabyDomainModel>> {
        Log.d("FetchLullabiesOptimizedUseCase", "🚀 Database-optimized lullaby fetch with pre-computed translations")

        return lullabyRepository.getAllLullabiesOptimized()
    }
}