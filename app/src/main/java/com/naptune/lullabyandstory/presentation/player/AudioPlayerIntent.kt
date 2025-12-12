package com.naptune.lullabyandstory.presentation.player

import com.naptune.lullabyandstory.domain.model.AdSizeType
import java.time.LocalTime

sealed class AudioPlayerIntent {
    object PlayPause : AudioPlayerIntent()
    object Previous : AudioPlayerIntent()
    object Next : AudioPlayerIntent()
    data class ToggleFavourite(val isFromStory: Boolean, val audioId: String) : AudioPlayerIntent()
    object ReadStory : AudioPlayerIntent()
    object OpenTimer : AudioPlayerIntent()
    object ToggleLoop : AudioPlayerIntent() // ✅ New intent for manual loop control
    data class VolumeChange(val volume: Float) : AudioPlayerIntent() // ✅ Enhanced volume control
    data class SeekTo(val position: Float) : AudioPlayerIntent()
    data class LoadAudio(
        val audioItem: AudioItem,
        val isFromStory: Boolean,
        val documentId: String,
        val isUserSelection: Boolean = true // ✅ NEW: Track if user manually selected (vs notification/service)
    ) : AudioPlayerIntent()
    data class CheckIfItemIsFavourite(val documentId: String, val isFromStory: Boolean = true) : AudioPlayerIntent()
    data class ForceLoadAudio(
        val audioItem: AudioItem,
        val isFromStory: Boolean,
        val documentId: String
    ) : AudioPlayerIntent() // ✅ Force load bypassing same audio check
    
    // ✅ NEW: Story navigation intents
    object LoadStoryList : AudioPlayerIntent()

    object StopTimerAlarm : AudioPlayerIntent()
    object NavigateToPreviousStory : AudioPlayerIntent()
    object NavigateToNextStory : AudioPlayerIntent()
    // ❌ REMOVED: Navigation intents
    // data class NavigateToStoryReader(val item : AudioItem) : AudioPlayerIntent()
    
    // ✅ NEW: AdMob related intents
    object InitializeAds : AudioPlayerIntent()
    data class LoadBannerAd(
        val adUnitId: String,
        val adSizeType: AdSizeType = AdSizeType.MEDIUM_RECTANGLE
    ) : AudioPlayerIntent()
    data class DestroyBannerAd(val adUnitId: String) : AudioPlayerIntent()
    
    // ✅ NEW: Timer related intents
    data class ScheduleTimer(val time: LocalTime, val index: Int) : AudioPlayerIntent()
    data class SaveTimerSettings(val time: LocalTime, val index: Int) : AudioPlayerIntent()
}
