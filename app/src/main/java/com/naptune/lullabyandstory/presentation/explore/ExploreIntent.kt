package com.naptune.lullabyandstory.presentation.explore

import android.app.Activity
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel

/**
 * MVI Intent for ExploreScreen user actions
 */
sealed class ExploreIntent {
    // Content category switching (Lullaby ↔ Story)
    data class ChangeContentCategory(val category: ExploreContentCategory) : ExploreIntent()

    // Filter category switching (All/Popular/Free)
    data class ChangeFilterCategory(val category: ExploreFilterCategory) : ExploreIntent()

    // Initial data loading
    object LoadData : ExploreIntent()

    // Lullaby actions
    data class DownloadLullaby(val lullaby: LullabyDomainModel) : ExploreIntent()
    data class ToggleLullabyFavourite(val lullabyId: String) : ExploreIntent()

    // Story actions
    data class ToggleStoryFavourite(val storyId: String) : ExploreIntent()

    // AdMob Intents
    object InitializeAds : ExploreIntent()
    data class LoadBannerAd(val adUnitId: String, val adSizeType: AdSizeType) : ExploreIntent()
    object DestroyBannerAd : ExploreIntent()
    data class LoadRewardedAd(val adUnitId: String) : ExploreIntent()
    data class ShowRewardedAdForLullaby(
        val adUnitId: String,
        val activity: Activity,
        val lullaby: LullabyDomainModel
    ) : ExploreIntent()
    data class ShowRewardedAdForStory(
        val adUnitId: String,
        val activity: Activity,
        val story: StoryDomainModel
    ) : ExploreIntent()
}
