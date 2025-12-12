package com.naptune.lullabyandstory.domain.usecase.admob

import android.util.Log
import com.naptune.lullabyandstory.domain.repository.AdMobRepository
import javax.inject.Inject

class InitializeAdMobUseCase @Inject constructor(
    private val adMobRepository: AdMobRepository
) {
    suspend operator fun invoke() {
        Log.d("InitializeAdMobUseCase", "🚀 UseCase: Initializing AdMob SDK")
        adMobRepository.initializeMobileAds()
    }
}