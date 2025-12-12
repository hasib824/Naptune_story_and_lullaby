package com.naptune.lullabyandstory.presentation.story.storyreader

import com.naptune.lullabyandstory.domain.model.AdSizeType

sealed class StoryReaderIntent {
    object IncreaseFontSize : StoryReaderIntent()
    object DecreaseFontSize : StoryReaderIntent()
    object LoadSavedFontSize : StoryReaderIntent()
    object InitializeAds : StoryReaderIntent()
    data class LoadBannerAd(val adUnitId: String, val adSizeType: AdSizeType) : StoryReaderIntent()
}