package com.naptune.lullabyandstory.presentation.favourite

import android.app.Activity
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.domain.model.AdSizeType

sealed class FavouriteIntent {
    object LoadFavourites : FavouriteIntent()
    data class ChangeCategory(val category: FavouriteCategory) : FavouriteIntent()
    // ❌ REMOVED: Navigation intents
    // data class OnLullabyClick(val lullaby: LullabyDomainModel) : FavouriteIntent()
    // data class OnStoryClick(val story: StoryDomainModel) : FavouriteIntent()
    data class ToggleLullabyFavourite(val lullabyId: String) : FavouriteIntent()
    data class ToggleStoryFavourite(val storyId: String) : FavouriteIntent()
    
    // AdMob related intents
    object InitializeAds : FavouriteIntent()
    data class LoadBannerAd(
        val adUnitId: String,
        val adSizeType: AdSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
    ) : FavouriteIntent()
    data class DestroyBannerAd(val adUnitId: String) : FavouriteIntent()
    
    // Rewarded Ad intents
    data class LoadRewardedAd(val adUnitId: String) : FavouriteIntent()
    data class ShowRewardedAdForStory(
        val adUnitId: String, 
        val activity: Activity,
        val story: StoryDomainModel
    ) : FavouriteIntent()
    data class ShowRewardedAdForLullaby(
        val adUnitId: String, 
        val activity: Activity,
        val lullaby: LullabyDomainModel
    ) : FavouriteIntent()
}
