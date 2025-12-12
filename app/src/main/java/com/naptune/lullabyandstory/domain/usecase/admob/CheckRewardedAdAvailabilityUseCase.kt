package com.naptune.lullabyandstory.domain.usecase.admob

import android.util.Log
import com.naptune.lullabyandstory.domain.repository.AdMobRepository
import javax.inject.Inject

class CheckRewardedAdAvailabilityUseCase @Inject constructor(
    private val adMobRepository: AdMobRepository
) {
    operator fun invoke(adUnitId: String): Boolean {
        val isAvailable = adMobRepository.isRewardedAdLoaded(adUnitId)
        Log.d("CheckRewardedAdAvailabilityUseCase", "🔍 UseCase: Checking rewarded ad availability - Unit: $adUnitId, Available: $isAvailable")
        return isAvailable
    }
}