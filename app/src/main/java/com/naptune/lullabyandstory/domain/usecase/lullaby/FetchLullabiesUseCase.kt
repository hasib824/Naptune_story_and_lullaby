package com.naptune.lullabyandstory.domain.usecase.lullaby

import android.util.Log
import com.naptune.lullabyandstory.domain.manager.LanguageStateManager
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.repository.LullabyDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for fetching lullabies.
 * Updated to use LullabyDataRepository (ISP compliant).
 */
class FetchLullabiesUseCase @Inject constructor(
    private val lullabyDataRepository: LullabyDataRepository,
    private val languageStateManager: LanguageStateManager
) {

    /**
     * ✅ ENHANCED: Returns lullabies with translations (handled at repository level)
     * Now repository automatically provides translation data
     */
    suspend operator fun invoke(): Flow<List<LullabyDomainModel>> {
        Log.d("FetchLullabiesUseCase", "🌍 Enhanced UseCase: FetchLullabiesUseCase with translation support")
        Log.d("FetchLullabiesUseCase", "📞 Calling LullabyDataRepository (translations handled at repository level)...")

        // Repository now handles translation data automatically
        return lullabyDataRepository.syncLullabiesFromRemote()
    }

    /**
     * ✅ NEW: Get current language being used
     */
    fun getCurrentLanguage(): String {
        return languageStateManager.getCurrentLanguageSync()
    }
}
