package com.naptune.lullabyandstory.presentation.lullaby

import android.app.Activity
import android.content.Context
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.AdSizeType

sealed class LullabyIntent {

    object FetchLullabies : LullabyIntent()
    data class DownloadLullabyItem(val lullabyItem: LullabyDomainModel) : LullabyIntent()
    data class ChangeCategory(val category: LullabyCategory) : LullabyIntent()
    
    // AdMob related intents
    object InitializeAds : LullabyIntent()
    data class LoadBannerAd(
        val adUnitId: String,
        val adSizeType: AdSizeType = AdSizeType.BANNER
    ) : LullabyIntent()
    data class DestroyBannerAd(val adUnitId: String) : LullabyIntent()
    
    // Rewarded Ad intents
    data class LoadRewardedAd(val adUnitId: String) : LullabyIntent()
    data class ShowRewardedAd(
        val adUnitId: String, 
        val activity: Activity,
        val lullaby: LullabyDomainModel
    ) : LullabyIntent()

}