package com.naptune.lullabyandstory.presentation.main

import android.app.Activity
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.domain.model.AdSizeType

sealed class MainIntent {
    object FetchHomeData : MainIntent()
    data class OnPageChanged(val page: Int) : MainIntent()
    // ✅ NEW: Navigation intents
    // ❌ REMOVED: Navigation intents - handled by callbacks now
    // data class OnLullabyClick(val lullaby: LullabyDomainModel) : MainIntent()
    // data class OnStoryClick(val story: StoryDomainModel) : MainIntent()

    data class OnLullabyDownloadClick(val lullaby: LullabyDomainModel) : MainIntent()
    
    // AdMob related intents
    object InitializeAds : MainIntent()
    data class LoadBannerAd(
        val adUnitId: String,
        val adSizeType: AdSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
    ) : MainIntent()
    data class DestroyBannerAd(val adUnitId: String) : MainIntent()
    
    // Rewarded Ad intents
    data class LoadRewardedAd(val adUnitId: String) : MainIntent()
    data class ShowRewardedAdForStory(
        val adUnitId: String, 
        val activity: Activity,
        val story: StoryDomainModel
    ) : MainIntent()
    data class ShowRewardedAdForLullaby(
        val adUnitId: String, 
        val activity: Activity,
        val lullaby: LullabyDomainModel
    ) : MainIntent()
}
