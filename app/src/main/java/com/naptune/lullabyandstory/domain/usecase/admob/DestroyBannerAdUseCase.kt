package com.naptune.lullabyandstory.domain.usecase.admob

import android.util.Log
import com.naptune.lullabyandstory.domain.repository.AdMobRepository
import javax.inject.Inject

class DestroyBannerAdUseCase @Inject constructor(
    private val adMobRepository: AdMobRepository
) {
    suspend operator fun invoke(adUnitId: String) {
        Log.d("DestroyBannerAdUseCase", "🗑️ UseCase: Destroying banner ad - Unit: $adUnitId")
        adMobRepository.destroyBannerAd(adUnitId)
    }
}