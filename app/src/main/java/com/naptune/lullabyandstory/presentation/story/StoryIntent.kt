package com.naptune.lullabyandstory.presentation.story

import android.app.Activity
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.model.StoryDomainModel

sealed class StoryIntent {
    object FetchStories : StoryIntent()
    object DownloadStory : StoryIntent()
    object toogleStoryFavourite : StoryIntent()
    data class ChangeCategory(val category: StoryCategory) : StoryIntent()
    
    // AdMob related intents
    object InitializeAds : StoryIntent()
    data class LoadBannerAd(
        val adUnitId: String,
        val adSizeType: AdSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
    ) : StoryIntent()
    data class DestroyBannerAd(val adUnitId: String) : StoryIntent()
    
    // Rewarded Ad intents
    data class LoadRewardedAd(val adUnitId: String) : StoryIntent()
    data class ShowRewardedAd(
        val adUnitId: String, 
        val activity: Activity,
        val story: StoryDomainModel
    ) : StoryIntent()
}