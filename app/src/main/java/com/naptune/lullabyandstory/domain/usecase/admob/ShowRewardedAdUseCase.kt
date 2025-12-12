package com.naptune.lullabyandstory.domain.usecase.admob

import android.app.Activity
import android.util.Log
import com.naptune.lullabyandstory.domain.model.RewardedAdShowResult
import com.naptune.lullabyandstory.domain.repository.AdMobRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ShowRewardedAdUseCase @Inject constructor(
    private val adMobRepository: AdMobRepository
) {
    suspend operator fun invoke(
        adUnitId: String,
        activity: Activity
    ): Flow<RewardedAdShowResult> {
        Log.d("ShowRewardedAdUseCase", "🎬 UseCase: Showing rewarded ad - Unit: $adUnitId")
        return adMobRepository.showRewardedAd(adUnitId, activity)
    }
}