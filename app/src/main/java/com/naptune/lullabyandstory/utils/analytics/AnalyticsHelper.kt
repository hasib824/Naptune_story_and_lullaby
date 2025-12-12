package com.naptune.lullabyandstory.utils.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Analytics Helper - Singleton class for tracking all app events
 *
 * Usage:
 * ```kotlin
 * analyticsHelper.logScreenView("main", "MainScreen")
 * analyticsHelper.logLullabySelected(lullabyId, lullabyName, category)
 * ```
 *
 * Features:
 * - Type-safe event logging
 * - Automatic parameter validation
 * - Debug logging in development builds
 * - COPPA compliant (no PII)
 */
@Singleton
class AnalyticsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "AnalyticsHelper"
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    init {
        // Enable debug logging in debug builds
        if (com.naptune.lullabyandstory.BuildConfig.DEBUG) {
            firebaseAnalytics.setAnalyticsCollectionEnabled(true)
            Log.d(TAG, "🔥 Firebase Analytics initialized in DEBUG mode")
        }
    }

    // ====================================================================================
    // SCREEN VIEW TRACKING
    // ====================================================================================

    /**
     * Log screen view event
     * @param screenName Screen identifier (e.g., "main", "lullaby_browse")
     * @param screenClass Screen class name (e.g., "MainScreen")
     */
    fun logScreenView(screenName: String, screenClass: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        logDebug("📱 Screen View: $screenName ($screenClass)")
    }

    // ====================================================================================
    // LULLABY EVENTS
    // ====================================================================================

    /**
     * Log when user selects a lullaby
     */
    fun logLullabySelected(
        lullabyId: String,
        lullabyName: String,
        category: String,
        sourceScreen: String,
        isPremium: Boolean,
        isDownloaded: Boolean
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.LULLABY_SELECTED) {
            param(AnalyticsParams.LULLABY_ID, lullabyId)
            param(AnalyticsParams.LULLABY_NAME, lullabyName)
            param(AnalyticsParams.CATEGORY, category)
            param(AnalyticsParams.SOURCE_SCREEN, sourceScreen)
            param(AnalyticsParams.IS_PREMIUM, if (isPremium) "true" else "false")
            param(AnalyticsParams.IS_DOWNLOADED, if (isDownloaded) "true" else "false")
        }
        logDebug("🎵 Lullaby Selected: $lullabyName from $sourceScreen")
    }

    /**
     * Log when lullaby playback starts
     */
    fun logLullabyPlayStarted(
        lullabyId: String,
        lullabyName: String,
        durationSeconds: Long,
        playbackMethod: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.LULLABY_PLAY_STARTED) {
            param(AnalyticsParams.LULLABY_ID, lullabyId)
            param(AnalyticsParams.LULLABY_NAME, lullabyName)
            param(AnalyticsParams.DURATION_SECONDS, durationSeconds)
            param(AnalyticsParams.PLAYBACK_METHOD, playbackMethod)
        }
        logDebug("▶️ Lullaby Play Started: $lullabyName (${durationSeconds}s)")
    }

    /**
     * Log when lullaby playback completes
     */
    fun logLullabyPlayCompleted(
        lullabyId: String,
        listenDurationSeconds: Long,
        completionPercentage: Float
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.LULLABY_PLAY_COMPLETED) {
            param(AnalyticsParams.LULLABY_ID, lullabyId)
            param(AnalyticsParams.LISTEN_DURATION_SECONDS, listenDurationSeconds)
            param(AnalyticsParams.COMPLETION_PERCENTAGE, completionPercentage.toDouble())
        }
        logDebug("✅ Lullaby Completed: $lullabyId (${completionPercentage}%)")
    }

    // ====================================================================================
    // STORY EVENTS
    // ====================================================================================

    /**
     * Log when user selects a story
     */
    fun logStorySelected(
        storyId: String,
        storyName: String,
        category: String,
        sourceScreen: String,
        isPremium: Boolean,
        interactionType: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.STORY_SELECTED) {
            param(AnalyticsParams.STORY_ID, storyId)
            param(AnalyticsParams.STORY_NAME, storyName)
            param(AnalyticsParams.CATEGORY, category)
            param(AnalyticsParams.SOURCE_SCREEN, sourceScreen)
            param(AnalyticsParams.IS_PREMIUM, if (isPremium) "true" else "false")
            param(AnalyticsParams.INTERACTION_TYPE, interactionType)
        }
        logDebug("📖 Story Selected: $storyName ($interactionType) from $sourceScreen")
    }

    /**
     * Log when story listening starts
     */
    fun logStoryListenStarted(
        storyId: String,
        storyName: String,
        durationSeconds: Long
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.STORY_LISTEN_STARTED) {
            param(AnalyticsParams.STORY_ID, storyId)
            param(AnalyticsParams.STORY_NAME, storyName)
            param(AnalyticsParams.DURATION_SECONDS, durationSeconds)
        }
        logDebug("🎧 Story Listen Started: $storyName")
    }

    /**
     * Log when story reading starts
     */
    fun logStoryReadStarted(
        storyId: String,
        storyName: String,
        pageCount: Int
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.STORY_READ_STARTED) {
            param(AnalyticsParams.STORY_ID, storyId)
            param(AnalyticsParams.STORY_NAME, storyName)
            param(AnalyticsParams.PAGE_COUNT, pageCount.toLong())
        }
        logDebug("📚 Story Read Started: $storyName ($pageCount pages)")
    }

    /**
     * Log when story is completed (listen or read)
     */
    fun logStoryCompleted(
        storyId: String,
        completionType: String,
        timeSpentSeconds: Long
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.STORY_COMPLETED) {
            param(AnalyticsParams.STORY_ID, storyId)
            param(AnalyticsParams.COMPLETION_TYPE, completionType)
            param(AnalyticsParams.TIME_SPENT_SECONDS, timeSpentSeconds)
        }
        logDebug("✅ Story Completed: $storyId ($completionType, ${timeSpentSeconds}s)")
    }

    // ====================================================================================
    // FAVORITE EVENTS
    // ====================================================================================

    /**
     * Log when user adds content to favorites
     */
    fun logAddToFavourites(
        contentType: String,
        contentId: String,
        contentName: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.ADD_TO_FAVOURITES) {
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.CONTENT_NAME, contentName)
        }
        logDebug("❤️ Added to Favourites: $contentName ($contentType)")
    }

    /**
     * Log when user removes content from favorites
     */
    fun logRemoveFromFavourites(
        contentType: String,
        contentId: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.REMOVE_FROM_FAVOURITES) {
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.CONTENT_ID, contentId)
        }
        logDebug("💔 Removed from Favourites: $contentId ($contentType)")
    }

    // ====================================================================================
    // DOWNLOAD EVENTS
    // ====================================================================================

    /**
     * Log when content is downloaded
     */
    fun logContentDownloaded(
        contentType: String,
        contentId: String,
        contentName: String,
        fileSizeMb: Float
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.CONTENT_DOWNLOADED) {
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.CONTENT_NAME, contentName)
            param(AnalyticsParams.FILE_SIZE_MB, fileSizeMb.toDouble())
        }
        logDebug("⬇️ Content Downloaded: $contentName (${fileSizeMb}MB)")
    }

    // ====================================================================================
    // FAVORITES PLAY EVENTS (CRITICAL - User Requested) ⭐
    // ====================================================================================

    /**
     * Log when user plays content FROM favorites screen
     * This tracks content discovery via favorites, separate from browsing
     */
    fun logContentPlayedFromFavourites(
        contentType: String,
        contentId: String,
        contentName: String,
        category: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.CONTENT_PLAYED_FROM_FAVOURITES) {
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.CONTENT_NAME, contentName)
            param(AnalyticsParams.CATEGORY, category)
            param(AnalyticsParams.SOURCE_SCREEN, "favourites")
        }
        logDebug("❤️▶️ Played from Favourites: $contentName ($contentType)")
    }

    // ====================================================================================
    // SESSION UNLOCK EVENTS (Rewarded Ads) ⭐⭐⭐
    // ====================================================================================

    /**
     * Log when user requests to watch rewarded ad
     */
    fun logRewardedAdRequested(
        contentType: String,
        contentId: String,
        contentName: String,
        isPremium: Boolean,
        sourceScreen: String,
        adUnitId: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.REWARDED_AD_REQUESTED) {
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.CONTENT_NAME, contentName)
            param(AnalyticsParams.IS_PREMIUM, if (isPremium) "true" else "false")
            param(AnalyticsParams.SOURCE_SCREEN, sourceScreen)
            param(AnalyticsParams.AD_UNIT_ID, adUnitId)
        }
        logDebug("📺 Rewarded Ad Requested: $contentName from $sourceScreen")
    }

    /**
     * Log when rewarded ad loads successfully
     */
    fun logRewardedAdLoaded(
        adUnitId: String,
        loadTimeMs: Long
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.REWARDED_AD_LOADED) {
            param(AnalyticsParams.AD_UNIT_ID, adUnitId)
            param(AnalyticsParams.LOAD_TIME_MS, loadTimeMs)
        }
        logDebug("✅ Rewarded Ad Loaded: ${loadTimeMs}ms")
    }

    /**
     * Log when rewarded ad fails to load
     */
    fun logRewardedAdLoadFailed(
        adUnitId: String,
        errorCode: String,
        errorMessage: String,
        networkType: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.REWARDED_AD_LOAD_FAILED) {
            param(AnalyticsParams.AD_UNIT_ID, adUnitId)
            param(AnalyticsParams.ERROR_CODE, errorCode)
            param(AnalyticsParams.ERROR_MESSAGE, errorMessage.take(100))
            param(AnalyticsParams.NETWORK_TYPE, networkType)
        }
        logDebug("❌ Rewarded Ad Load Failed: $errorCode")
    }

    /**
     * Log when rewarded ad starts playing
     */
    fun logRewardedAdStarted(
        adUnitId: String,
        contentId: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.REWARDED_AD_STARTED) {
            param(AnalyticsParams.AD_UNIT_ID, adUnitId)
            param(AnalyticsParams.CONTENT_ID, contentId)
        }
        logDebug("▶️ Rewarded Ad Started")
    }

    /**
     * Log when user completes watching rewarded ad
     */
    fun logRewardedAdCompleted(
        adUnitId: String,
        contentType: String,
        contentId: String,
        contentName: String,
        rewardAmount: Int,
        rewardType: String,
        watchDurationSeconds: Int
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.REWARDED_AD_COMPLETED) {
            param(AnalyticsParams.AD_UNIT_ID, adUnitId)
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.CONTENT_NAME, contentName)
            param(AnalyticsParams.REWARD_AMOUNT, rewardAmount.toLong())
            param(AnalyticsParams.REWARD_TYPE, rewardType)
            param(AnalyticsParams.WATCH_DURATION_SECONDS, watchDurationSeconds.toLong())
        }
        logDebug("✅ Rewarded Ad Completed: $contentName unlocked")
    }

    /**
     * Log when user closes ad before completion
     */
    fun logRewardedAdClosedEarly(
        adUnitId: String,
        contentId: String,
        watchedSeconds: Int,
        requiredSeconds: Int
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.REWARDED_AD_CLOSED_EARLY) {
            param(AnalyticsParams.AD_UNIT_ID, adUnitId)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.WATCHED_SECONDS, watchedSeconds.toLong())
            param(AnalyticsParams.REQUIRED_SECONDS, requiredSeconds.toLong())
        }
        logDebug("⏹️ Rewarded Ad Closed Early: ${watchedSeconds}s/${requiredSeconds}s")
    }

    /**
     * Log when content is unlocked via rewarded ad (SessionUnlockManager)
     */
    fun logContentUnlockedViaAd(
        contentType: String,
        contentId: String,
        contentName: String,
        category: String,
        sessionUnlockCount: Int,
        timeSpentBeforeUnlock: Long
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.CONTENT_UNLOCKED_VIA_AD) {
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.CONTENT_NAME, contentName)
            param(AnalyticsParams.CATEGORY, category)
            param(AnalyticsParams.SESSION_UNLOCK_COUNT, sessionUnlockCount.toLong())
            param(AnalyticsParams.TIME_SPENT_SECONDS, timeSpentBeforeUnlock)
        }
        logDebug("🎁 Content Unlocked: $contentName (Total unlocks: $sessionUnlockCount)")
    }

    /**
     * Log when user actually plays unlocked content
     */
    fun logSessionUnlockUsed(
        contentType: String,
        contentId: String,
        contentName: String,
        minutesSinceUnlock: Long
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.SESSION_UNLOCK_USED) {
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.CONTENT_NAME, contentName)
            param(AnalyticsParams.MINUTES_SINCE_UNLOCK, minutesSinceUnlock)
        }
        logDebug("🎁▶️ Session Unlock Used: $contentName (${minutesSinceUnlock}min ago)")
    }

    // ====================================================================================
    // ENHANCED AUDIO PLAYER EVENTS ⭐
    // ====================================================================================

    /**
     * Log audio skip (next/previous)
     */
    fun logAudioSkip(
        contentId: String,
        skipDirection: String,
        playbackPositionMs: Long,
        contentDurationMs: Long,
        skipPercentage: Float
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.AUDIO_SKIP) {
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.SKIP_DIRECTION, skipDirection)
            param(AnalyticsParams.PLAYBACK_POSITION_MS, playbackPositionMs)
            param(AnalyticsParams.CONTENT_DURATION_MS, contentDurationMs)
            param(AnalyticsParams.SKIP_PERCENTAGE, skipPercentage.toDouble())
        }
        logDebug("⏭️ Audio Skip: $skipDirection at ${skipPercentage}%")
    }

    /**
     * Log audio loop toggle
     */
    fun logAudioLoopToggled(
        contentId: String,
        loopEnabled: Boolean,
        playbackPositionMs: Long
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.AUDIO_LOOP_TOGGLED) {
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.LOOP_ENABLED, if (loopEnabled) "true" else "false")
            param(AnalyticsParams.PLAYBACK_POSITION_MS, playbackPositionMs)
        }
        logDebug("🔁 Loop ${if (loopEnabled) "enabled" else "disabled"}")
    }

    /**
     * Log volume change
     */
    fun logAudioVolumeChanged(
        contentId: String,
        oldVolume: Float,
        newVolume: Float,
        changeAmount: Float
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.AUDIO_VOLUME_CHANGED) {
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.OLD_VOLUME, oldVolume.toDouble())
            param(AnalyticsParams.NEW_VOLUME, newVolume.toDouble())
            param(AnalyticsParams.CHANGE_AMOUNT, changeAmount.toDouble())
        }
        logDebug("🔊 Volume: ${oldVolume} → ${newVolume}")
    }

    /**
     * Log playback speed change
     */
    fun logAudioPlaybackSpeedChanged(
        contentId: String,
        oldSpeed: Float,
        newSpeed: Float
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.AUDIO_PLAYBACK_SPEED_CHANGED) {
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.OLD_SPEED, oldSpeed.toDouble())
            param(AnalyticsParams.NEW_SPEED, newSpeed.toDouble())
        }
        logDebug("⏩ Speed: ${oldSpeed}x → ${newSpeed}x")
    }

    // ====================================================================================
    // AUDIO PLAYER EVENTS
    // ====================================================================================

    /**
     * Log audio play event
     */
    fun logAudioPlay(
        contentId: String,
        contentType: String,
        playbackPositionMs: Long
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.AUDIO_PLAY) {
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.PLAYBACK_POSITION_MS, playbackPositionMs)
        }
        logDebug("▶️ Audio Play: $contentId at ${playbackPositionMs}ms")
    }

    /**
     * Log audio pause event
     */
    fun logAudioPause(
        contentId: String,
        playbackPositionMs: Long,
        sessionDurationMs: Long
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.AUDIO_PAUSE) {
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.PLAYBACK_POSITION_MS, playbackPositionMs)
            param(AnalyticsParams.SESSION_DURATION_MS, sessionDurationMs)
        }
        logDebug("⏸️ Audio Pause: $contentId at ${playbackPositionMs}ms")
    }

    /**
     * Log audio seek event
     */
    fun logAudioSeek(
        contentId: String,
        fromPositionMs: Long,
        toPositionMs: Long
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.AUDIO_SEEK) {
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.FROM_POSITION_MS, fromPositionMs)
            param(AnalyticsParams.TO_POSITION_MS, toPositionMs)
        }
        logDebug("⏩ Audio Seek: $contentId from ${fromPositionMs}ms to ${toPositionMs}ms")
    }

    /**
     * Log timer set event
     */
    fun logTimerSet(
        durationMinutes: Int,
        contentPlaying: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.TIMER_SET) {
            param(AnalyticsParams.DURATION_MINUTES, durationMinutes.toLong())
            param(AnalyticsParams.CONTENT_PLAYING, contentPlaying)
        }
        logDebug("⏰ Timer Set: ${durationMinutes}min for $contentPlaying")
    }

    /**
     * Log timer completed event
     */
    fun logTimerCompleted(
        durationMinutes: Int,
        stoppedContentId: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.TIMER_COMPLETED) {
            param(AnalyticsParams.DURATION_MINUTES, durationMinutes.toLong())
            param(AnalyticsParams.STOPPED_CONTENT_ID, stoppedContentId)
        }
        logDebug("⏰ Timer Completed: ${durationMinutes}min, stopped $stoppedContentId")
    }

    // ====================================================================================
    // MONETIZATION EVENTS
    // ====================================================================================

    /**
     * Log when premium screen is viewed
     */
    fun logPremiumViewed(
        sourceScreen: String,
        trigger: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.PREMIUM_VIEWED) {
            param(AnalyticsParams.SOURCE_SCREEN, sourceScreen)
            param(AnalyticsParams.TRIGGER, trigger)
        }
        logDebug("💎 Premium Viewed from $sourceScreen (trigger: $trigger)")
    }

    /**
     * Log when rewarded ad is watched
     */
    fun logRewardedAdWatched(
        adUnitId: String,
        rewardType: String,
        contentId: String,
        rewardEarned: Boolean
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.REWARDED_AD_WATCHED) {
            param(AnalyticsParams.AD_UNIT_ID, adUnitId)
            param(AnalyticsParams.REWARD_TYPE, rewardType)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.REWARD_EARNED, if (rewardEarned) "true" else "false")
        }
        logDebug("📺 Rewarded Ad Watched: $rewardType for $contentId (earned: $rewardEarned)")
    }

    /**
     * Log ad impression
     */
    fun logAdViewed(
        adUnitId: String,
        adType: String,
        placement: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.AD_VIEWED) {
            param(AnalyticsParams.AD_UNIT_ID, adUnitId)
            param(AnalyticsParams.AD_TYPE, adType)
            param(AnalyticsParams.PLACEMENT, placement)
        }
        logDebug("📢 Ad Viewed: $adType at $placement")
    }

    // ====================================================================================
    // SETTINGS & USER BEHAVIOR EVENTS
    // ====================================================================================

    /**
     * Log app first launch
     */
    fun logAppFirstLaunch(deviceLanguage: String) {
        firebaseAnalytics.logEvent(AnalyticsEvents.APP_FIRST_LAUNCH) {
            param(AnalyticsParams.DEVICE_LANGUAGE, deviceLanguage)
            param(AnalyticsParams.TIMESTAMP, System.currentTimeMillis())
        }
        logDebug("🚀 App First Launch (language: $deviceLanguage)")
    }

    /**
     * Log language change
     */
    fun logLanguageChanged(
        fromLanguage: String,
        toLanguage: String,
        method: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.LANGUAGE_CHANGED) {
            param(AnalyticsParams.FROM_LANGUAGE, fromLanguage)
            param(AnalyticsParams.TO_LANGUAGE, toLanguage)
            param(AnalyticsParams.METHOD, method)
        }
        logDebug("🌍 Language Changed: $fromLanguage → $toLanguage ($method)")
    }

    // ====================================================================================
    // ERROR EVENTS
    // ====================================================================================

    /**
     * Log playback error
     */
    fun logPlaybackError(
        contentId: String,
        errorCode: String,
        errorMessage: String,
        networkType: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.PLAYBACK_ERROR) {
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.ERROR_CODE, errorCode)
            param(AnalyticsParams.ERROR_MESSAGE, errorMessage.take(100)) // Limit length
            param(AnalyticsParams.NETWORK_TYPE, networkType)
        }
        logDebug("❌ Playback Error: $contentId - $errorCode")
    }

    /**
     * Log download started
     */
    fun logDownloadStarted(
        contentType: String,
        contentId: String,
        contentName: String,
        sourceScreen: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.DOWNLOAD_STARTED) {
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.CONTENT_NAME, contentName)
            param(AnalyticsParams.SOURCE_SCREEN, sourceScreen)
        }
        logDebug("📥 Download Started: $contentType - $contentName from $sourceScreen")
    }

    /**
     * Log download completed
     */
    fun logDownloadCompleted(
        contentType: String,
        contentId: String,
        contentName: String,
        downloadTimeMs: Long
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.DOWNLOAD_COMPLETED) {
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.CONTENT_NAME, contentName)
            param(AnalyticsParams.DOWNLOAD_TIME_MS, downloadTimeMs)
        }
        logDebug("✅ Download Completed: $contentType - $contentName (${downloadTimeMs}ms)")
    }

    /**
     * Log download failure
     */
    fun logDownloadFailed(
        contentType: String,
        contentId: String,
        errorMessage: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.DOWNLOAD_FAILED) {
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.ERROR_MESSAGE, errorMessage.take(100))
        }
        logDebug("❌ Download Failed: $contentType - $contentId - $errorMessage")
    }

    /**
     * Log downloaded content played (offline usage)
     */
    fun logDownloadedContentPlayed(
        contentType: String,
        contentId: String,
        contentName: String,
        sourceScreen: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.DOWNLOADED_CONTENT_PLAYED) {
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.CONTENT_NAME, contentName)
            param(AnalyticsParams.SOURCE_SCREEN, sourceScreen)
        }
        logDebug("📱 Offline Content Played: $contentType - $contentName from $sourceScreen")
    }

    // ====================================================================================
    // FAVORITE TRACKING
    // ====================================================================================

    /**
     * Log favorite added
     */
    fun logFavoriteAdded(
        contentType: String,
        contentId: String,
        contentName: String,
        sourceScreen: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.ADD_TO_FAVOURITES) {
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.CONTENT_NAME, contentName)
            param(AnalyticsParams.SOURCE_SCREEN, sourceScreen)
        }
        logDebug("❤️ Favorite Added: $contentType - $contentName from $sourceScreen")
    }

    /**
     * Log favorite removed
     */
    fun logFavoriteRemoved(
        contentType: String,
        contentId: String,
        contentName: String,
        sourceScreen: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.REMOVE_FROM_FAVOURITES) {
            param(AnalyticsParams.CONTENT_TYPE, contentType)
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.CONTENT_NAME, contentName)
            param(AnalyticsParams.SOURCE_SCREEN, sourceScreen)
        }
        logDebug("💔 Favorite Removed: $contentType - $contentName from $sourceScreen")
    }

    /**
     * Log favorites screen viewed
     */
    fun logFavoritesScreenViewed() {
        firebaseAnalytics.logEvent(AnalyticsEvents.FAVOURITES_SCREEN_VIEWED) {
            // No additional parameters needed
        }
        logDebug("👀 Favorites Screen Viewed")
    }

    // ====================================================================================
    // TIMER TRACKING
    // ====================================================================================

    /**
     * Log timer set
     */
    fun logTimerSet(
        contentId: String,
        durationMinutes: Int,
        timerType: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.TIMER_SET) {
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.DURATION_MINUTES, durationMinutes.toLong())
            param(AnalyticsParams.TIMER_TYPE, timerType)
        }
        logDebug("⏰ Timer Set: ${durationMinutes}min ($timerType) for $contentId")
    }

    /**
     * Log timer modified
     */
    fun logTimerModified(
        contentId: String,
        durationMinutes: Int,
        timerType: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.TIMER_MODIFIED) {
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.DURATION_MINUTES, durationMinutes.toLong())
            param(AnalyticsParams.TIMER_TYPE, timerType)
        }
        logDebug("🔄 Timer Modified: ${durationMinutes}min ($timerType) for $contentId")
    }

    /**
     * Log timer cancelled
     */
    fun logTimerCancelled(
        contentId: String,
        remainingMinutes: Int
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.TIMER_CANCELLED) {
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.REMAINING_MINUTES, remainingMinutes.toLong())
        }
        logDebug("❌ Timer Cancelled: ${remainingMinutes}min remaining for $contentId")
    }

    /**
     * Log timer completed
     */
    fun logTimerCompleted(
        contentId: String,
        durationMinutes: Int,
        timerType: String,
        didStopPlayback: Boolean
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.TIMER_COMPLETED) {
            param(AnalyticsParams.CONTENT_ID, contentId)
            param(AnalyticsParams.DURATION_MINUTES, durationMinutes.toLong())
            param(AnalyticsParams.TIMER_TYPE, timerType)
            param(AnalyticsParams.DID_STOP_PLAYBACK, if (didStopPlayback) 1L else 0L)
        }
        logDebug("✅ Timer Completed: ${durationMinutes}min ($timerType) - Stopped playback: $didStopPlayback")
    }

    // ====================================================================================
    // USER PROPERTIES
    // ====================================================================================

    /**
     * Set user type property
     */
    fun setUserType(userType: String) {
        firebaseAnalytics.setUserProperty(AnalyticsUserProperties.USER_TYPE, userType)
        logDebug("👤 User Type: $userType")
    }

    /**
     * Set preferred language property
     */
    fun setPreferredLanguage(language: String) {
        firebaseAnalytics.setUserProperty(AnalyticsUserProperties.PREFERRED_LANGUAGE, language)
        logDebug("🌍 Preferred Language: $language")
    }

    /**
     * Set content preference property
     */
    fun setContentPreference(preference: String) {
        firebaseAnalytics.setUserProperty(AnalyticsUserProperties.CONTENT_PREFERENCE, preference)
        logDebug("❤️ Content Preference: $preference")
    }

    /**
     * Set engagement level property
     */
    fun setEngagementLevel(level: String) {
        firebaseAnalytics.setUserProperty(AnalyticsUserProperties.ENGAGEMENT_LEVEL, level)
        logDebug("📊 Engagement Level: $level")
    }

    /**
     * Set device type property
     */
    fun setDeviceType(deviceType: String) {
        firebaseAnalytics.setUserProperty(AnalyticsUserProperties.DEVICE_TYPE, deviceType)
        logDebug("📱 Device Type: $deviceType")
    }

    // ====================================================================================
    // UTILITY METHODS
    // ====================================================================================

    /**
     * Log custom event with custom parameters
     */
    fun logCustomEvent(eventName: String, params: Bundle) {
        firebaseAnalytics.logEvent(eventName, params)
        logDebug("🔥 Custom Event: $eventName")
    }

    /**
     * Set user property
     */
    fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
        logDebug("👤 User Property Set: $name = $value")
    }

    /**
     * Log app open event
     */
    fun logAppOpen() {
        firebaseAnalytics.logEvent(com.google.firebase.analytics.FirebaseAnalytics.Event.APP_OPEN, null)
        logDebug("🚀 App Opened")
    }

    /**
     * Enable/disable analytics collection
     */
    fun setAnalyticsEnabled(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
        logDebug("🔥 Analytics ${if (enabled) "enabled" else "disabled"}")
    }

    // ====================================================================================
    // 🆕 FCM NOTIFICATION EVENTS
    // ====================================================================================

    /**
     * Log when FCM notification is received
     */
    fun logNotificationReceived(
        title: String?,
        screenRoute: String?,
        hasImage: Boolean
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.NOTIFICATION_RECEIVED) {
            param(AnalyticsParams.NOTIFICATION_TITLE, title ?: "unknown")
            param(AnalyticsParams.SCREEN_ROUTE, screenRoute ?: "none")
            param(AnalyticsParams.HAS_IMAGE, if (hasImage) "true" else "false")
        }
        logDebug("🔔 Notification Received: $title (route: $screenRoute)")
    }

    /**
     * Log when FCM notification is opened/tapped
     */
    fun logNotificationOpened(
        title: String?,
        screenRoute: String?,
        contentId: String?
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.NOTIFICATION_OPENED) {
            param(AnalyticsParams.NOTIFICATION_TITLE, title ?: "unknown")
            param(AnalyticsParams.SCREEN_ROUTE, screenRoute ?: "none")
            contentId?.let { param(AnalyticsParams.CONTENT_ID, it) }
        }
        logDebug("📱 Notification Opened: $title → $screenRoute")
    }

    /**
     * Log when FCM token is generated
     */
    fun logFcmTokenGenerated(
        tokenLength: Int
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.FCM_TOKEN_GENERATED) {
            param(AnalyticsParams.TOKEN_LENGTH, tokenLength.toLong())
            param(AnalyticsParams.TIMESTAMP, System.currentTimeMillis())
        }
        logDebug("🔑 FCM Token Generated (length: $tokenLength)")
    }

    /**
     * Log when FCM token is registered with server
     */
    fun logFcmTokenRegistered(
        deviceId: String?,
        isSuccess: Boolean
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.FCM_TOKEN_REGISTERED) {
            deviceId?.let { param(AnalyticsParams.DEVICE_ID, it) }
            param(AnalyticsParams.IS_SUCCESS, if (isSuccess) "true" else "false")
        }
        logDebug("📡 FCM Token Registered: ${if (isSuccess) "SUCCESS" else "FAILED"}")
    }

    /**
     * Log when FCM token is refreshed
     */
    fun logFcmTokenRefreshed(
        reason: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.FCM_TOKEN_REFRESHED) {
            param(AnalyticsParams.REFRESH_REASON, reason)
            param(AnalyticsParams.TIMESTAMP, System.currentTimeMillis())
        }
        logDebug("🔄 FCM Token Refreshed: $reason")
    }

    /**
     * Log when deep link navigation occurs
     */
    fun logDeepLinkNavigated(
        screenRoute: String,
        contentId: String?,
        source: String
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.DEEP_LINK_NAVIGATED) {
            param(AnalyticsParams.SCREEN_ROUTE, screenRoute)
            contentId?.let { param(AnalyticsParams.CONTENT_ID, it) }
            param(AnalyticsParams.SOURCE, source)
        }
        logDebug("🔗 Deep Link: $screenRoute from $source")
    }

    /**
     * Log when notification permission is requested
     */
    fun logNotificationPermissionRequested() {
        firebaseAnalytics.logEvent(AnalyticsEvents.NOTIFICATION_PERMISSION_REQUESTED) {
            // No additional parameters needed
        }
        logDebug("📱 Notification Permission Requested")
    }

    /**
     * Log notification permission result
     */
    fun logNotificationPermissionResult(
        isGranted: Boolean
    ) {
        firebaseAnalytics.logEvent(AnalyticsEvents.NOTIFICATION_PERMISSION_RESULT) {
            param(AnalyticsParams.IS_GRANTED, if (isGranted) "true" else "false")
        }
        logDebug("📱 Notification Permission: ${if (isGranted) "GRANTED" else "DENIED"}")
    }

    /**
     * Debug logging helper
     */
    private fun logDebug(message: String) {
        if (com.naptune.lullabyandstory.BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }
}
