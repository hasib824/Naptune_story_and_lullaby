package com.naptune.lullabyandstory.domain.usecase.admob

import android.util.Log
import com.naptune.lullabyandstory.domain.model.AdLoadResult
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.repository.AdMobRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LoadBannerAdUseCase @Inject constructor(
    private val adMobRepository: AdMobRepository
) {
    suspend operator fun invoke(
        adUnitId: String,
        adSizeType: AdSizeType = AdSizeType.BANNER
    ): Flow<AdLoadResult> = flow {
        Log.d("LoadBannerAdUseCase", "📢 UseCase: Loading banner ad - Unit: $adUnitId, Size: $adSizeType")
        emitAll(adMobRepository.loadBannerAd(adUnitId, adSizeType))
    }
}