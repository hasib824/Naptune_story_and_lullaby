package com.naptune.lullabyandstory.presentation.story.storymanager

import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.model.StoryDomainModel

sealed class StoryManagerIntent {
    // AdMob related intents
    object InitializeAds : StoryManagerIntent()
    data class LoadBannerAd(
        val adUnitId: String,
        val adSizeType: AdSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
    ) : StoryManagerIntent()
    data class DestroyBannerAd(val adUnitId: String) : StoryManagerIntent()
    
    // Story streaming intent
    data class CheckNetworkForStoryStream(
        val story: StoryDomainModel,
        val onSuccess: (StoryDomainModel) -> Unit
    ) : StoryManagerIntent()
}