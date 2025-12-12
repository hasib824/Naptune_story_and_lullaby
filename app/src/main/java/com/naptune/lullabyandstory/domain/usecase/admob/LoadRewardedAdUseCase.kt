package com.naptune.lullabyandstory.domain.usecase.admob

import android.util.Log
import com.naptune.lullabyandstory.domain.model.RewardedAdLoadResult
import com.naptune.lullabyandstory.domain.repository.AdMobRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadRewardedAdUseCase @Inject constructor(
    private val adMobRepository: AdMobRepository
) {
    suspend operator fun invoke(adUnitId: String): Flow<RewardedAdLoadResult> {
        Log.d("LoadRewardedAdUseCase", "🎁 UseCase: Loading rewarded ad - Unit: $adUnitId")
        return adMobRepository.loadRewardedAd(adUnitId)
    }
}