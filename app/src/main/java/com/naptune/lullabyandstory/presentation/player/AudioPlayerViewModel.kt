package com.naptune.lullabyandstory.presentation.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.domain.usecase.story.CheckIfItemIsFavouriteUseCase
import com.naptune.lullabyandstory.domain.usecase.story.ToogleStoryFavouriteUseCase
import com.naptune.lullabyandstory.domain.usecase.story.FetchStoriesUsecase
import com.naptune.lullabyandstory.domain.usecase.lullaby.CheckIfLullabyIsFavouriteUseCase
import com.naptune.lullabyandstory.domain.usecase.lullaby.ToggleLullabyFavouriteUseCase
import com.naptune.lullabyandstory.presentation.player.service.MusicController
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.data.manager.AdManager
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.presentation.player.timermodal.operations.TimerAlarmManager
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.time.LocalTime
import javax.inject.Inject

// ✅ Data class for controller state
data class PlayerControllerState(
    val isPlaying: Boolean,
    val isPreparing: Boolean,
    val isActuallyPlaying: Boolean,
    val isAudioLoaded: Boolean,
    val isReady: Boolean
)

// ✅ Data class for timer countdown UI state
data class TimerCountdownState(
    val isTimerActive: Boolean = false,
    val remainingTimeText: String = "", // Format: "5:15m"
    val remainingMillis: Long = 0L,
    val isVisible: Boolean = false
)

/**
 * ViewModel for Audio Player screen.
 * REFACTORED: Now follows Single Responsibility Principle (SRP).
 * Ad management logic delegated to unified AdManager.
 *
 * Responsibilities:
 * - Audio playback control and state management
 * - Favourite toggle for lullabies and stories
 * - Timer management for sleep timer
 * - Story auto-play functionality
 *
 * Ad management delegated to: AdManager (shared across all ViewModels)
 */
@HiltViewModel
class AudioPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicController: MusicController,
    private val toogleStoryFavouriteUseCase: ToogleStoryFavouriteUseCase,
    private val checkIfItemIsFavouriteUseCase: CheckIfItemIsFavouriteUseCase,
    private val toggleLullabyFavouriteUseCase: ToggleLullabyFavouriteUseCase,
    private val checkIfLullabyIsFavouriteUseCase: CheckIfLullabyIsFavouriteUseCase,
    private val fetchStoriesUsecase: FetchStoriesUsecase,
    // ✅ SRP FIX: Single unified ad manager instead of 3 ad use cases
    private val adManager: AdManager,
    private val appPreferences: AppPreferences,
    private val timerAlarmManager: TimerAlarmManager,
    private val analyticsHelper: com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper,
    private val billingManager: com.naptune.lullabyandstory.data.billing.BillingManager
) : ViewModel() {


    private val _uiState = MutableStateFlow(AudioPlayerUiState())

    // ✅ Combine base state with ad state from AdManager
    val uiState: StateFlow<AudioPlayerUiState> = combine(
        _uiState,
        adManager.adState
    ) { baseState, adState ->
        baseState.copy(
            bannerAd = adState.bannerAd  // ✅ Sync banner ad from AdManager
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AudioPlayerUiState()
    )

    // ✅ Premium status from BillingManager
    val isPurchased: StateFlow<Boolean> = billingManager.isPurchased.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true  // Default to non-premium until billing initializes
    )

    // ✅ NEW: Immediate timer countdown state for instant UI updates
    private val _timerCountdownState = MutableStateFlow(TimerCountdownState())
    val timerCountdownState: StateFlow<TimerCountdownState> = _timerCountdownState.asStateFlow()

    // ✅ Flag to track if we came from notification
    private var isFromNotificationRestore = false

    // ✅ Flag to allow override of same audio check
    private var allowAudioOverride = false

    // ✅ Favourite job tracking
    private var favouriteJob: Job? = null

    // ✅ Timer management is now injected via constructor
    private var timerCountdownJob: Job? = null

    init {
        Log.d(
            "AudioPlayerViewModel",
            "🏁 AudioPlayerViewModel initialized - Starting music controller observation"
        )
        observeMusicControllerState()

        // ✅ NEW: Observe story completion for auto-play next story
        observeStoryCompletion()

        // ✅ Check if there's existing audio when ViewModel is created
        checkForExistingAudio()

        // ✅ OPTIMIZED: Load story list if currently playing a story (with WhileSubscribed)
        viewModelScope.launch {
            musicController.currentAudioItem
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null
                )
                .collect { audioItem ->
                    if (audioItem?.isFromStory == true && _uiState.value.storyNavigationState.storyList.isEmpty()) {
                        Log.d("AudioPlayerViewModel", "📚 Auto-loading story list for current story")
                        loadStoryList()
                    }
                }
        }

        // ✅ Initialize ads for free users
        viewModelScope.launch {
            billingManager.isPurchased.collect { isPremium ->
                if (!isPremium) {
                    Log.d("AudioPlayerViewModel", "📢 Free user - Initializing ads")
                    adManager.initializeAds()
                    adManager.loadBannerAd(
                        adUnitId = com.naptune.lullabyandstory.data.network.admob.AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                        adSizeType = com.naptune.lullabyandstory.domain.model.AdSizeType.MEDIUM_RECTANGLE,
                        placement = "audio_player_screen"
                    )
                } else {
                    Log.d("AudioPlayerViewModel", "👑 Premium user - Skipping ads")
                }
            }
        }
    }

    /**
     * ✅ Set notification flag to control audio loading behavior
     */


    @RequiresApi(Build.VERSION_CODES.O)
    fun handleIntent(intent: AudioPlayerIntent) {
        Log.d("AudioPlayerViewModel", "🎯 Intent received: $intent")
        when (intent) {
            is AudioPlayerIntent.PlayPause -> togglePlayPause()
            is AudioPlayerIntent.Previous -> playPrevious()
            is AudioPlayerIntent.Next -> playNext()
            is AudioPlayerIntent.ToggleFavourite -> toggleFavourite(
                intent.isFromStory,
                intent.audioId
            )

            is AudioPlayerIntent.ReadStory -> readStory()
            is AudioPlayerIntent.OpenTimer -> openTimer()
            is AudioPlayerIntent.VolumeChange -> changeVolume(intent.volume)
            is AudioPlayerIntent.SeekTo -> seekToPosition(intent.position)
            is AudioPlayerIntent.LoadAudio -> {
                // ✅ Check if this is user selection of same audio
                val currentAudio = musicController.currentAudioItem.value
                val isSameAudio = currentAudio != null &&
                        isSameAudioCurrentlyPlaying(intent.audioItem, intent.documentId)

                if (isSameAudio && intent.isUserSelection && !isFromNotificationRestore) {
                    // ✅ User manually selected same audio → Restart from beginning
                    Log.d(
                        "AudioPlayerViewModel",
                        "🔄 User re-selected same audio - restarting from beginning"
                    )
                    musicController.stopAudio()
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(100) // Small delay for clean stop
                        forceLoadNewAudio(intent.audioItem, intent.isFromStory, intent.documentId)
                    }
                } else {
                    // ✅ Different audio OR from notification → Normal load (will resume if needed)
                    Log.d(
                        "AudioPlayerViewModel",
                        "🎵 Loading audio - Same: $isSameAudio, UserSelection: ${intent.isUserSelection}, FromNotification: $isFromNotificationRestore"
                    )
                    loadAudio(intent.audioItem, intent.isFromStory, intent.documentId)
                }
            }

            is AudioPlayerIntent.ForceLoadAudio -> {
                // ✅ Smart routing: Check if this is the same audio currently playing
                if (isSameAudioCurrentlyPlaying(intent.audioItem, intent.documentId)) {
                    Log.d(
                        "AudioPlayerViewModel",
                        "🔄 ForceLoad detected same audio - routing to same audio handler"
                    )
                    handleSameAudioReload(intent.audioItem, intent.isFromStory, intent.documentId)
                } else {
                    Log.d(
                        "AudioPlayerViewModel",
                        "🔥 ForceLoad detected different audio - proceeding with force load"
                    )
                    forceLoadNewAudio(intent.audioItem, intent.isFromStory, intent.documentId)
                }
            }

            is AudioPlayerIntent.ToggleLoop -> toggleLoop()
            is AudioPlayerIntent.CheckIfItemIsFavourite -> {
                if (intent.isFromStory) {
                    getIfFavourite(intent.documentId)
                } else {
                    getIfLullabyFavourite(intent.documentId)
                }
            }
            // ✅ NEW: Story navigation intents
            is AudioPlayerIntent.LoadStoryList -> loadStoryList()
            is AudioPlayerIntent.NavigateToPreviousStory -> navigateToPreviousStory()
            is AudioPlayerIntent.NavigateToNextStory -> navigateToNextStory()
            // ✅ REMOVED: Navigation intent handling - using callbacks now
            // is AudioPlayerIntent.NavigateToStoryReader -> navigateToStoryReader(intent.item)
            // ✅ NEW: AdMob intent handling
            is AudioPlayerIntent.InitializeAds -> initializeAds()
            is AudioPlayerIntent.LoadBannerAd -> loadBannerAd(intent.adUnitId, intent.adSizeType)
            is AudioPlayerIntent.DestroyBannerAd -> destroyBannerAd(intent.adUnitId)
            // ✅ NEW: Timer intent handling
            is AudioPlayerIntent.ScheduleTimer -> scheduleTimer(intent.time, intent.index)
            is AudioPlayerIntent.SaveTimerSettings -> saveTimerSettings(intent.time, intent.index)
            is AudioPlayerIntent.StopTimerAlarm -> stopTimer()
        }
    }

    fun setFromNotificationFlag(fromNotification: Boolean) {
        isFromNotificationRestore = fromNotification
        Log.d("AudioPlayerViewModel", "🔔 Notification flag set to: $fromNotification")

        // ✅ When user navigates from notification, allow them to select new audio
        if (fromNotification) {
            allowAudioOverride = true
        }
    }

    /**
     * ✅ Check if the requested audio is the same as currently playing
     */
    private fun isSameAudioCurrentlyPlaying(audioItem: AudioItem, documentId: String): Boolean {
        val currentAudio = musicController.currentAudioItem.value

        if (currentAudio == null) {
            Log.d("AudioPlayerViewModel", "🔍 No current audio - not same")
            return false
        }

        // ✅ Multi-level comparison for reliability
        val sameId = currentAudio.id == audioItem.id
        val sameDocumentId = currentAudio.documentId == documentId
        val sameUrl = currentAudio.audioUrl == audioItem.musicPath
        val sameTitleCheck = (
                (currentAudio.isFromStory && currentAudio.title == audioItem.storyName) ||
                        (!currentAudio.isFromStory && currentAudio.title == audioItem.musicName)
                )

        Log.d("AudioPlayerViewModel", "🔍 Same audio check:")
        Log.d(
            "AudioPlayerViewModel",
            "🔍 - Same ID: $sameId (${currentAudio.id} vs ${audioItem.id})"
        )
        Log.d(
            "AudioPlayerViewModel",
            "🔍 - Same Doc ID: $sameDocumentId (${currentAudio.documentId} vs $documentId)"
        )
        Log.d("AudioPlayerViewModel", "🔍 - Same URL: $sameUrl")
        Log.d("AudioPlayerViewModel", "🔍 - Same Title: $sameTitleCheck")

        // ✅ Consider same if any two major criteria match
        val isSame =
            (sameId && sameDocumentId) || (sameId && sameTitleCheck) || (sameDocumentId && sameUrl)

        Log.d(
            "AudioPlayerViewModel",
            "🔍 Final result: ${if (isSame) "SAME AUDIO" else "DIFFERENT AUDIO"}"
        )
        return isSame
    }

    /**
     * ✅ Handle same audio reload without restarting
     */
    private fun handleSameAudioReload(
        audioItem: AudioItem,
        isFromStory: Boolean,
        documentId: String
    ) {
        Log.d("AudioPlayerViewModel", "🔄 Handling same audio reload - preserving state")

        val currentAudio = musicController.currentAudioItem.value
        val isCurrentlyPlaying = musicController.isPlaying.value
        val isActuallyPlaying = musicController.isActuallyPlaying.value
        val currentPosition = musicController.getCurrentPosition()

        Log.d(
            "AudioPlayerViewModel",
            "🔄 Current state - Playing: $isCurrentlyPlaying, Actually: $isActuallyPlaying, Position: $currentPosition"
        )

        // ✅ Update UI state to match the requested item (in case of UI inconsistency)
        _uiState.value = _uiState.value.copy(
            isLoading = false, // No loading for same audio
            audioItem = audioItem,
            isFromStory = isFromStory,
            error = null, // Clear any errors
            playerState = _uiState.value.playerState.copy(
                isLoading = false,
                isPlaying = isActuallyPlaying // Use actual playing state
            )
        )

        // ✅ Handle playback state
        if (musicController.hasActiveAudio()) {
            if (!isCurrentlyPlaying) {
                Log.d("AudioPlayerViewModel", "▶️ Same audio is paused - resuming playback")
                musicController.resumeAudio()
            } else {
                Log.d("AudioPlayerViewModel", "🔄 Same audio is playing - continuing")
                // Audio is already playing, just ensure UI is in sync
            }
        } else {
            // ✅ Fallback: if somehow controller lost state, restore it
            Log.w(
                "AudioPlayerViewModel",
                "⚠️ Controller has no active audio - attempting to restore"
            )
            if (currentAudio != null) {
                musicController.resumeExistingAudio()
            } else {
                // Last resort: reload the audio
                Log.w("AudioPlayerViewModel", "⚠️ No current audio state - falling back to reload")
                forceLoadNewAudio(audioItem, isFromStory, documentId)
            }
        }

        Log.d("AudioPlayerViewModel", "✅ Same audio handling completed")
    }

    /**
     * ✅ Enhanced debug method with same audio detection info
     */
    fun debugCurrentState() {
        try {
            val currentAudio = musicController.currentAudioItem.value
            val uiAudio = _uiState.value.audioItem
            val isControllerReady = musicController.isControllerReady.value
            val hasActiveAudio = musicController.hasActiveAudio()
            val isActuallyPlaying = musicController.isActuallyPlaying.value

            Log.d("AudioPlayerViewModel debug", "🔍 ============ DEBUG STATE ============")
            Log.d("AudioPlayerViewModel debug", "🔍 Controller Ready: $isControllerReady")
            Log.d("AudioPlayerViewModel debug", "🔍 Has Active Audio: $hasActiveAudio")
            Log.d("AudioPlayerViewModel debug", "🔍 Is Actually Playing: $isActuallyPlaying")
            Log.d(
                "AudioPlayerViewModel debug",
                "🔍 Controller Audio ID: ${currentAudio?.id ?: "NULL"}"
            )
            Log.d(
                "AudioPlayerViewModel debug",
                "🔍 Controller Audio Doc ID: ${currentAudio?.documentId ?: "NULL"}"
            )
            Log.d(
                "AudioPlayerViewModel",
                "🔍 Controller Audio Title: ${currentAudio?.title ?: "NULL"}"
            )
            Log.d(
                "AudioPlayerViewModel",
                "🔍 Controller IsFromStory: ${currentAudio?.isFromStory ?: "NULL"}"
            )
            Log.d("AudioPlayerViewModel", "🔍 UI Audio ID: ${uiAudio?.id ?: "NULL"}")
            Log.d("AudioPlayerViewModel", "🔍 UI Audio Music: ${uiAudio?.musicName ?: "NULL"}")
            Log.d("AudioPlayerViewModel", "🔍 UI Audio Story: ${uiAudio?.storyName ?: "NULL"}")
            Log.d("AudioPlayerViewModel", "🔍 From Notification: $isFromNotificationRestore")
            Log.d("AudioPlayerViewModel", "🔍 Allow Override: $allowAudioOverride")
            Log.d(
                "AudioPlayerViewModel",
                "🔍 Music Controller isPlaying: ${musicController.isPlaying.value}"
            )

            // ✅ Test same audio detection with current UI audio
            if (uiAudio != null && currentAudio != null) {
                val wouldBeSame = isSameAudioCurrentlyPlaying(uiAudio, currentAudio.documentId)
                Log.d("AudioPlayerViewModel", "🔍 Same Audio Detection Test: $wouldBeSame")
            }

            Log.d("AudioPlayerViewModel", "🔍 =====================================")
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "❌ Debug state error: ${e.message}")
        }
    }

    fun forceLoadNewAudio(audioItem: AudioItem, isFromStory: Boolean, documentId: String, isAutoNavigation: Boolean = false) {
        Log.d(
            "AudioPlayerViewModel",
            "🔥 FORCE loading new audio: ${if (isFromStory) audioItem.storyName else audioItem.musicName}"
        )
        Log.d("AudioPlayerViewModel", "🔥 Audio ID: ${audioItem.id}, Auto-navigation: $isAutoNavigation")

        // ✅ NEW: Smart timer preservation logic for manual navigation
        if (!isAutoNavigation) {
            // Check if "End of Story" timer is active
            viewModelScope.launch {
                try {
                    val savedTime = timerAlarmManager.getSavedAlarmTime()
                    val isEndOfStoryTimer = savedTime == "end_of_story"

                    if (isEndOfStoryTimer) {
                        // ✅ Reset "End of Story" timer during manual navigation
                        stopTimer()
                        Log.d("AudioPlayerViewModel", "⏰ 'End of Story' timer RESET for manually selected audio (story-specific timer)")
                    } else {
                        // ✅ Preserve regular timers (5, 10, 15, 30 min, 1 hour) during manual navigation
                        Log.d("AudioPlayerViewModel", "⏰ Regular timer PRESERVED during manual navigation (session-based timer)")
                    }
                } catch (e: Exception) {
                    Log.e("AudioPlayerViewModel", "❌ Error checking timer type: ${e.message}")
                }
            }
        } else {
            Log.d("AudioPlayerViewModel", "⏰ Timer preserved during auto-navigation to next story")
        }

        viewModelScope.launch {
            try {
                // ✅ Set immediate total time from story_listen_time_in_millis if available
                val immediateTotal = if (isFromStory && audioItem.story_listen_time_in_millis > 0) {
                    audioItem.story_listen_time_in_millis.formatTime()
                } else {
                    "0:00"
                }

                // ✅ Set loading state and audio item immediately for UI with smart prediction
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    audioItem = audioItem,
                    isFromStory = isFromStory,
                    playerState = _uiState.value.playerState.copy(
                        isLoading = true,
                        isPlaying = true, // ✅ SMART: Predict auto-play to prevent flash
                        currentTime = "0:00", // ✅ FIXED: Reset current time to show loading indicator
                        totalTime = immediateTotal // Set immediate total time
                    )
                )

                val audioUrl = audioItem.musicPath
                val title = if (isFromStory) audioItem.storyName else audioItem.musicName
                val artist = "Neptune"

                Log.d(
                    "AudioPlayerViewModel",
                    "🔥 FORCE - Audio details - URL: $audioUrl, Title: $title, IsFromStory: $isFromStory"
                )

                // ✅ Call musicController - it will handle all state management
                musicController.playAudioWithSourceAwareness(
                    audioUrl = audioUrl,
                    title = title,
                    artist = artist,
                    imageUrl = audioItem.imagePath,
                    isFromStory = isFromStory,
                    audioId = audioItem.id,
                    documentId = documentId,
                    story_listen_time_in_millis = audioItem.story_listen_time_in_millis
                )
                //  musicController.setManualDuration(5, 1)

                // ✅ FIXED: Don't set isLoading to false - let the observer handle it based on controller state
                // This allows the loading indicator to show properly while audio is preparing
                // _uiState.value = _uiState.value.copy(
                //     isLoading = false // ❌ REMOVED: This was hiding the loading indicator
                // )

                Log.d("AudioPlayerViewModel", "✅ FORCE loading initiated successfully")

            } catch (e: Exception) {
                Log.e("AudioPlayerViewModel", "❌ FORCE loading error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message,
                    playerState = _uiState.value.playerState.copy(
                        isLoading = false,
                        isPlaying = false
                    )
                )
            }

            // ✅ Reset flags
            isFromNotificationRestore = false
            allowAudioOverride = false
        }
    }

    /**
     * ✅ Enhanced existing audio check with service disconnection handling
     */
    private fun checkForExistingAudio() {
        viewModelScope.launch {
            Log.d("AudioPlayerViewModel", "🔍 Checking for existing audio...")

            if (musicController.hasActiveAudio()) {
                Log.d("AudioPlayerViewModel", "🔄 Found existing audio, attempting to restore state")

                // Get current audio state from controller
                val currentAudioState = musicController.currentAudioItem.value
                if (currentAudioState != null) {
                    Log.d("AudioPlayerViewModel", "✅ Restoring audio: ${currentAudioState.title}")
                    Log.d(
                        "AudioPlayerViewModel",
                        "🔍 Audio details - ID: ${currentAudioState.id}, isFromStory: ${currentAudioState.isFromStory}"
                    )

                    // ✅ Create AudioItem from saved state
                    val restoredAudioItem = AudioItem(
                        id = currentAudioState.id,
                        musicName = if (!currentAudioState.isFromStory) currentAudioState.title else "",
                        storyName = if (currentAudioState.isFromStory) currentAudioState.title else "",
                        imagePath = currentAudioState.imageUrl,
                        musicPath = currentAudioState.audioUrl,
                        story_listen_time_in_millis = currentAudioState.story_listen_time_in_millis
                    )

                    // ✅ Update UI state immediately
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, // Don't show loading for existing audio
                        audioItem = restoredAudioItem,
                        isFromStory = currentAudioState.isFromStory,
                        playerState = _uiState.value.playerState.copy(
                            isPlaying = currentAudioState.isPlaying,
                            isLoading = false
                        )
                    )

                    // ✅ Attempt to resume existing audio (handles service disconnection)
                    try {
                        if (musicController.resumeExistingAudio()) {
                            Log.d(
                                "AudioPlayerViewModel",
                                "✅ Successfully restored existing audio state"
                            )
                        } else {
                            Log.w(
                                "AudioPlayerViewModel",
                                "⚠️ Failed to resume existing audio - will reload on play"
                            )
                            // Don't show error - audio will reload when user clicks play
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "AudioPlayerViewModel",
                            "❌ Error resuming existing audio: ${e.message}"
                        )
                        // Keep the UI state but audio will reload on play
                    }
                } else {
                    Log.w(
                        "AudioPlayerViewModel",
                        "⚠️ No current audio state found despite hasActiveAudio being true"
                    )
                }
            } else {
                Log.d("AudioPlayerViewModel", "ℹ️ No existing audio found")
            }
        }
    }

    private fun observeMusicControllerState() {
        viewModelScope.launch {
            // ✅ OPTIMIZED: Single combined state flow with WhileSubscribed
            launch {
                combine(
                    musicController.isPlaying,
                    musicController.isPreparing,
                    musicController.isActuallyPlaying,
                    musicController.isAudioLoaded,
                    musicController.isReady
                ) { isPlaying, isPreparing, isActuallyPlaying, isAudioLoaded, isReady ->
                    PlayerControllerState(
                        isPlaying = isPlaying,
                        isPreparing = isPreparing,
                        isActuallyPlaying = isActuallyPlaying,
                        isAudioLoaded = isAudioLoaded,
                        isReady = isReady
                    )
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = PlayerControllerState(false, false, false, false, false)
                )
                .collect { controllerState ->
                    Log.d("AudioPlayerViewModel", "🔄 Controller state: $controllerState")

                    val currentState = _uiState.value.playerState
                    _uiState.value = _uiState.value.copy(
                        playerState = currentState.copy(
                            isPlaying = controllerState.isActuallyPlaying, // ✅ Use actually playing
                            isLoading = shouldShowLoadingFromController(controllerState)
                        )
                    )
                }
            }

            // ✅ OPTIMIZED: isFromStory with WhileSubscribed
            launch {
                musicController.isFromStory
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = false
                    )
                    .collect { isFromStory ->
                        _uiState.value = _uiState.value.copy(isFromStory = isFromStory)
                    }
            }

            // ✅ OPTIMIZED: shouldLoop with WhileSubscribed
            launch {
                musicController.shouldLoop
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = false
                    )
                    .collect { shouldLoop ->
                        val currentState = _uiState.value.playerState
                        _uiState.value = _uiState.value.copy(
                            playerState = currentState.copy(isLooping = shouldLoop)
                        )
                    }
            }

            // ✅ OPTIMIZED: volume with WhileSubscribed
            launch {
                musicController.volume
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = 1.0f
                    )
                    .collect { volume ->
                        val currentState = _uiState.value.playerState
                        _uiState.value = _uiState.value.copy(
                            playerState = currentState.copy(volume = volume)
                        )
                    }
            }

            // ✅ OPTIMIZED: currentAudioItem with WhileSubscribed
            launch {
                musicController.currentAudioItem
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = null
                    )
                    .collect { currentAudio ->
                    // ✅ If we have current audio but no UI audio item, restore it
                    if (currentAudio != null && _uiState.value.audioItem == null) {
                        Log.d(
                            "AudioPlayerViewModel",
                            "🔄 Auto-restoring UI audio item from controller state"
                        )

                        val restoredAudioItem = AudioItem(
                            id = currentAudio.id,
                            musicName = if (!currentAudio.isFromStory) currentAudio.title else "",
                            storyName = if (currentAudio.isFromStory) currentAudio.title else "",
                            imagePath = currentAudio.imageUrl,
                            musicPath = currentAudio.audioUrl,
                            story_listen_time_in_millis = currentAudio.story_listen_time_in_millis
                        )

                        _uiState.value = _uiState.value.copy(
                            audioItem = restoredAudioItem,
                            isFromStory = currentAudio.isFromStory,
                            isLoading = false
                        )
                    }

                    Log.d("AudioPlayerViewModel", "🎵 State updated from controller")
                }
            }

            // ✅ OPTIMIZED: Observe position and duration with WhileSubscribed
            launch {
                Log.d("AudioPlayerViewModel", "🚀 Starting position/duration flow collection")
                combine(
                    musicController.currentPosition,
                    musicController.duration
                ) { currentPosition, duration ->
                    Log.d(
                        "AudioPlayerViewModel",
                        "🔄 Flow combine called - Position: $currentPosition, Duration: $duration"
                    )
                    Pair(currentPosition, duration)
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = Pair(0L, 0L)
                )
                .collect { (currentPosition, duration) ->

                    Log.e(
                        "Progress ViewModel",
                        "🎵 Collecting position and duration: $currentPosition , $duration"
                    )

                    // ✅ Validate both position and duration before processing
                    val validPosition = if (currentPosition >= 0) currentPosition else 0L
                    val validDuration = if (duration > 0) duration else 0L

                    val currentState = _uiState.value.playerState

                    // Use story_listen_time_in_millis for progress calculation if available for stories
                    val totalDurationForProgress =
                        if (_uiState.value.isFromStory && _uiState.value.audioItem?.story_listen_time_in_millis != null && _uiState.value.audioItem!!.story_listen_time_in_millis > 0) {
                            _uiState.value.audioItem!!.story_listen_time_in_millis
                        } else {
                            validDuration
                        }

                    val progress = if (totalDurationForProgress > 0) {
                        (validPosition.toFloat() / totalDurationForProgress.toFloat()).coerceIn(
                            0f,
                            1f
                        )
                    } else 0f

                    val formattedCurrentTime = validPosition.formatTime()

                    // Use story_listen_time_in_millis for stories, otherwise use MediaPlayer duration
                    val formattedTotalTime =
                        if (_uiState.value.isFromStory && _uiState.value.audioItem?.story_listen_time_in_millis != null && _uiState.value.audioItem!!.story_listen_time_in_millis > 0) {
                            _uiState.value.audioItem!!.story_listen_time_in_millis.formatTime()
                        } else if (validDuration > 0) {
                            validDuration.formatTime()
                        } else {
                            "0:00"
                        }

                    Log.e(
                        "Progress ViewModel",
                        "🎵 Validated - Position: $validPosition, Duration: $validDuration"
                    )
                    Log.e(
                        "Progress ViewModel",
                        "🎵 Formatted times - Current: $formattedCurrentTime, Total: $formattedTotalTime, Progress: $progress"
                    )

                    _uiState.value = _uiState.value.copy(
                        playerState = currentState.copy(
                            progress = progress,
                            currentTime = formattedCurrentTime,
                            totalTime = formattedTotalTime
                        )
                    )

                    Log.d(
                        "AudioPlayerViewModel",
                        "✅ UI State updated - Progress: $progress, Current: $formattedCurrentTime, Total: $formattedTotalTime"
                    )
                }
            }
        }
    }

    /**
     * ✅ OPTIMIZED: Observe story completion for auto-play next story
     * Now uses WhileSubscribed to stop when no UI is observing
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeStoryCompletion() {
        viewModelScope.launch {
            musicController.hasCompletedPlayback
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = false
                )
                .collect { hasCompleted ->
                    if (hasCompleted && _uiState.value.isFromStory) {
                        Log.d(
                            "AudioPlayerViewModel",
                            "🎬 Story playback completed - checking for next story"
                        )
                        handleStoryCompletion()
                    }
                }
        }
    }

    /**
     * ✅ Enhanced loadAudio with comprehensive state preservation check and override support
     */
    private fun loadAudio(audioItem: AudioItem, isFromStory: Boolean, documentId: String, isAutoNavigation: Boolean = false) {
        viewModelScope.launch {
            Log.d("AudioPlayerViewModel", "📝 ========== LOAD AUDIO START ===========")
            Log.d(
                "AudioPlayerViewModel",
                "📝 Loading audio: ${if (isFromStory) audioItem.storyName else audioItem.musicName}"
            )
            Log.d("AudioPlayerViewModel", "📝 Audio ID: ${audioItem.id}")
            Log.d(
                "AudioPlayerViewModel",
                "🔔 From notification: $isFromNotificationRestore, Allow override: $allowAudioOverride"
            )

            val currentAudioState = musicController.currentAudioItem.value
            Log.d(
                "AudioPlayerViewModel",
                "📝 Current audio in controller: ${currentAudioState?.id ?: "NULL"}"
            )

            // ✅ Check if this is the same audio that's already playing BUT allow override if flag is set
            if (currentAudioState != null && isSameAudioCurrentlyPlaying(
                    audioItem,
                    documentId
                ) && !allowAudioOverride
            ) {
                Log.d(
                    "AudioPlayerViewModel",
                    "🔄 Same audio detected in loadAudio - using enhanced same audio handler"
                )
                handleSameAudioReload(audioItem, isFromStory, documentId)

                // ✅ Reset flags after handling same audio
                isFromNotificationRestore = false
                allowAudioOverride = false
                return@launch
            }

            // ✅ Check if we should preserve current audio vs load new one (notification case)
            if (currentAudioState != null && audioItem.id == "current_playing" && isFromNotificationRestore) {
                Log.d(
                    "AudioPlayerViewModel",
                    "🎯 Special case: Restoring current playing audio from notification"
                )

                // This is a placeholder audioItem from notification - restore the real one
                val restoredAudioItem = AudioItem(
                    id = currentAudioState.id,
                    musicName = if (!currentAudioState.isFromStory) currentAudioState.title else "",
                    storyName = if (currentAudioState.isFromStory) currentAudioState.title else "",
                    imagePath = currentAudioState.imageUrl,
                    musicPath = currentAudioState.audioUrl,
                    story_listen_time_in_millis = currentAudioState.story_listen_time_in_millis
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    audioItem = restoredAudioItem,
                    isFromStory = currentAudioState.isFromStory,
                    playerState = _uiState.value.playerState.copy(
                        isLoading = false,
                        isPlaying = currentAudioState.isPlaying
                    )
                )

                // Resume existing audio
                if (musicController.resumeExistingAudio()) {
                    Log.d("AudioPlayerViewModel", "✅ Successfully restored audio from notification")
                }

                // ✅ Reset notification flag but keep override flag for next selection
                isFromNotificationRestore = false
                return@launch
            }

            // ✅ New audio loading - this handles both new audio and overridden same audio
            Log.d("AudioPlayerViewModel", "🎵 Loading new audio (or overriding existing)")
            Log.d(
                "AudioPlayerViewModel",
                "📊 Will load: ${audioItem.id}, Override was: $allowAudioOverride, Auto-navigation: $isAutoNavigation"
            )

            // ✅ NEW: Smart timer preservation logic for manual navigation
            if (!isAutoNavigation) {
                // Check if "End of Story" timer is active
                val savedTime = timerAlarmManager.getSavedAlarmTime()
                val isEndOfStoryTimer = savedTime == "end_of_story"

                if (isEndOfStoryTimer) {
                    // ✅ Reset "End of Story" timer during manual navigation
                    stopTimer()
                    Log.d("AudioPlayerViewModel", "⏰ 'End of Story' timer RESET for manually selected audio (story-specific timer)")
                } else {
                    // ✅ Preserve regular timers (5, 10, 15, 30 min, 1 hour) during manual navigation
                    Log.d("AudioPlayerViewModel", "⏰ Regular timer PRESERVED during manual navigation (session-based timer)")
                }
            } else {
                Log.d("AudioPlayerViewModel", "⏰ Timer preserved during auto-navigation to next story")
            }

            // ✅ Set immediate total time from story_listen_time_in_millis if available
            val immediateTotal = if (isFromStory && audioItem.story_listen_time_in_millis > 0) {
                audioItem.story_listen_time_in_millis.formatTime()
            } else {
                "0:00"
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                audioItem = audioItem,
                isFromStory = isFromStory,
                playerState = _uiState.value.playerState.copy(
                    isLoading = true,
                    isPlaying = true, // ✅ SMART: Predict auto-play to prevent icon flash
                    currentTime = "0:00", // ✅ FIXED: Reset current time to show loading indicator
                    totalTime = immediateTotal // Set immediate total time
                )
            )

            try {
                val audioUrl = audioItem.musicPath
                val title = if (isFromStory) audioItem.storyName else audioItem.musicName
                val artist = "Neptune"

                Log.d(
                    "AudioPlayerViewModel",
                    "📝 Audio details - URL: $audioUrl, Title: $title, IsFromStory: $isFromStory"
                )

                // ✅ Use the enhanced method with source awareness and state preservation
                musicController.playAudioWithSourceAwareness(
                    audioUrl = audioUrl,
                    title = title,
                    artist = artist,
                    imageUrl = audioItem.imagePath,
                    isFromStory = isFromStory,
                    audioId = audioItem.id,
                    documentId = documentId,
                    story_listen_time_in_millis = audioItem.story_listen_time_in_millis
                )

                // ✅ FIXED: Don't set isLoading to false - let the observer handle it based on controller state
                // This allows the loading indicator to show properly while audio is preparing
                // _uiState.value = _uiState.value.copy(
                //     isLoading = false,
                //     playerState = _uiState.value.playerState.copy(isLoading = false)
                //     // ❌ REMOVED: This was hiding the loading indicator immediately
                // )

                // Log the expected behavior
                if (isFromStory) {
                    Log.d(
                        "AudioPlayerViewModel",
                        "📜 Story audio loaded - will play from online source without looping"
                    )
                } else {
                    Log.d(
                        "AudioPlayerViewModel",
                        "🎵 Lullaby audio loaded - will play from local storage with looping"
                    )
                }

                Log.d("AudioPlayerViewModel", "✅ Audio loading completed")
            } catch (e: Exception) {
                Log.e("AudioPlayerViewModel", "❌ Error loading audio: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message,
                    playerState = _uiState.value.playerState.copy(isLoading = false)
                )
            } finally {
                // ✅ Always reset flags after loading attempt
                isFromNotificationRestore = false
                allowAudioOverride = false
            }
        }
    }

    private fun togglePlayPause() {
        Log.d("AudioPlayerViewModel", "⏯️ Toggle play/pause")

        // ✅ Enhanced check for audio state and service connection
        val hasAudioItem = _uiState.value.audioItem != null
        val hasActiveAudio = musicController.hasActiveAudio()
        val controllerReady = musicController.isControllerReady.value

        Log.d(
            "toogle AudioPlayerViewModel",
            "⏯️ State check - AudioItem: $hasAudioItem, ActiveAudio: $hasActiveAudio, ControllerReady: $controllerReady"
        )

        when {
            // Case 1: We have UI audio item but controller doesn't have active audio
            hasAudioItem && !hasActiveAudio -> {
                Log.d(
                    "toogle Force AudioPlayerViewModel ",
                    "🔄 Audio exists but controller disconnected - attempting restore"
                )

                val audioItem = _uiState.value.audioItem!!
                val isFromStory = _uiState.value.isFromStory
                val documentId = musicController.currentAudioItem.value?.documentId ?: audioItem.id

                // Force reload the audio
                forceLoadNewAudio(audioItem, isFromStory, documentId)
            }

            hasActiveAudio && musicController.hasCompletedPlayback.value -> {

                val audioItem = _uiState.value.audioItem!!
                val isFromStory = _uiState.value.isFromStory
                val documentId = musicController.currentAudioItem.value?.documentId ?: audioItem.id

                Log.d("toogle Force AudioPlayerViewModel", "🔄 Last story completed - restarting from beginning")

                // Reset completion flag before restarting
                musicController.resetCompletionFlag()

                // Force reload the audio to restart from beginning
                forceLoadNewAudio(audioItem, isFromStory, documentId)
            }

            // Case 2: Normal toggle for active audio
            hasActiveAudio && controllerReady -> {

                Log.d("toogle normal AudioPlayerViewModel", "⏯️ Normal toggle for active audio")

                // ✅ Analytics: Track play/pause before toggle
                val wasPlaying = musicController.isPlaying.value
                val currentAudio = musicController.currentAudioItem.value

                musicController.togglePlayPause()

                // ✅ Analytics: Log play or pause event based on state change
                if (currentAudio != null) {
                    val playbackPositionMs = musicController.currentPosition.value
                    if (wasPlaying) {
                        // Was playing, now pausing
                        // Use playback position as session duration (time spent in current playback)
                        analyticsHelper.logAudioPause(
                            contentId = currentAudio.documentId,
                            playbackPositionMs = playbackPositionMs,
                            sessionDurationMs = playbackPositionMs
                        )
                    } else {
                        // Was paused, now playing
                        analyticsHelper.logAudioPlay(
                            contentId = currentAudio.documentId,
                            contentType = if (_uiState.value.isFromStory) "story" else "lullaby",
                            playbackPositionMs = playbackPositionMs
                        )
                    }
                }
            }

            // Case 3: Controller not ready
            !controllerReady -> {
                Log.w(
                    "toogle not ready AudioPlayerViewModel",
                    "⚠️ Controller not ready - cannot toggle"
                )
                // Optionally show error or try to reconnect
            }

            // Case 4: No audio at all
            else -> {
                Log.w("toogle no audioAudioPlayerViewModel", "⚠️ No audio available to toggle")
                // Optionally show message to user
            }
        }
    }

    fun startPlayback() {
        Log.d("AudioPlayerViewModel", "▶️ Start playback")
        musicController.resumeAudio()
    }

    private fun playPrevious() {
        Log.d("AudioPlayerViewModel", "⏪ Previous track")

        // ✅ Analytics: Track skip backward
        val currentAudio = musicController.currentAudioItem.value
        if (currentAudio != null) {
            val playbackPositionMs = musicController.currentPosition.value
            val contentDurationMs = musicController.getDuration()
            val skipPercentage = if (contentDurationMs > 0) (playbackPositionMs.toFloat() / contentDurationMs) * 100 else 0f

            analyticsHelper.logAudioSkip(
                contentId = currentAudio.documentId,
                skipDirection = "backward",
                playbackPositionMs = playbackPositionMs,
                contentDurationMs = contentDurationMs,
                skipPercentage = skipPercentage
            )
        }

        musicController.seekTo(0)
    }

    private fun playNext() {
        Log.d("AudioPlayerViewModel", "⏩ Next track")

        // ✅ Analytics: Track skip forward
        val currentAudio = musicController.currentAudioItem.value
        if (currentAudio != null) {
            val playbackPositionMs = musicController.currentPosition.value
            val contentDurationMs = musicController.getDuration()
            val skipPercentage = if (contentDurationMs > 0) (playbackPositionMs.toFloat() / contentDurationMs) * 100 else 0f

            analyticsHelper.logAudioSkip(
                contentId = currentAudio.documentId,
                skipDirection = "forward",
                playbackPositionMs = playbackPositionMs,
                contentDurationMs = contentDurationMs,
                skipPercentage = skipPercentage
            )
        }

        val duration = musicController.getDuration()
        musicController.seekTo(duration)
    }

    private fun toggleFavourite(isFromStory: Boolean, audioId: String) {
        Log.d(
            "AudioPlayerViewModel",
            "❤️ Toggle favourite - isFromStory: $isFromStory, audioId: $audioId"
        )

        viewModelScope.launch {
            try {
                // ✅ Check current favorite status before toggling
                val wasFavorite = _uiState.value.playerState.isFavourite
                val currentAudio = musicController.currentAudioItem.value

                if (isFromStory) {
                    // Story favourite toggle
                    toogleStoryFavouriteUseCase(audioId)
                    Log.d("AudioPlayerViewModel", "✅ Story favourite toggled")

                    // ✅ Analytics: Track add/remove favorite
                    if (currentAudio != null) {
                        if (wasFavorite) {
                            analyticsHelper.logFavoriteRemoved(
                                contentType = "story",
                                contentId = audioId,
                                contentName = currentAudio.title,
                                sourceScreen = "player"
                            )
                        } else {
                            analyticsHelper.logFavoriteAdded(
                                contentType = "story",
                                contentId = audioId,
                                contentName = currentAudio.title,
                                sourceScreen = "player"
                            )
                        }
                    }

                    // Re-check story favourite status
                    getIfFavourite(audioId)
                } else {
                    // ✅ Lullaby favourite toggle
                    toggleLullabyFavouriteUseCase(audioId)
                    Log.d("AudioPlayerViewModel", "✅ Lullaby favourite toggled")

                    // ✅ Analytics: Track add/remove favorite
                    if (currentAudio != null) {
                        if (wasFavorite) {
                            analyticsHelper.logFavoriteRemoved(
                                contentType = "lullaby",
                                contentId = audioId,
                                contentName = currentAudio.title,
                                sourceScreen = "player"
                            )
                        } else {
                            analyticsHelper.logFavoriteAdded(
                                contentType = "lullaby",
                                contentId = audioId,
                                contentName = currentAudio.title,
                                sourceScreen = "player"
                            )
                        }
                    }

                    // Re-check lullaby favourite status
                    getIfLullabyFavourite(audioId)
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerViewModel", "❌ Error toggling favourite: ${e.message}")
            }
        }
    }

    private fun readStory() {
        Log.d("AudioPlayerViewModel", "📖 Read story")
        // Navigate to story reading screen or show story text
    }

    private fun openTimer() {
        Log.d("AudioPlayerViewModel", "⏰ Open timer")
        // Open timer modal/dialog
    }

    /**
     * ✅ Enhanced volume control with slider integration
     */
    private fun changeVolume(volume: Float) {
        Log.d("AudioPlayerViewModel", "🔊 Volume changed: ${(volume * 100).toInt()}%")

        // ✅ Analytics: Track volume change
        val currentAudio = musicController.currentAudioItem.value
        val oldVolume = _uiState.value.playerState.volume
        if (currentAudio != null && oldVolume != volume) {
            val changeAmount = volume - oldVolume
            analyticsHelper.logAudioVolumeChanged(
                contentId = currentAudio.documentId,
                oldVolume = oldVolume,
                newVolume = volume,
                changeAmount = changeAmount
            )
        }

        // ✅ Update UI state immediately for smooth slider experience
        _uiState.value = _uiState.value.copy(
            playerState = _uiState.value.playerState.copy(volume = volume)
        )

        // Apply volume to music controller
        musicController.setVolume(volume)
    }

    /**
     * ✅ Get current volume for slider initialization
     */
    fun getCurrentVolume(): Float {
        return musicController.getVolume()
    }

    private fun seekToPosition(position: Float) {
        Log.d("AudioPlayerViewModel", "🎯 Seek to position: ${(position * 100).toInt()}%")
        val duration = musicController.getDuration()
        val seekPosition = (position * duration).toLong()
        musicController.seekTo(seekPosition)
    }

    /**
     * Toggle loop mode - useful for manual loop control
     */
    private fun toggleLoop() {
        Log.d("AudioPlayerViewModel", "🔄 Toggle loop mode")

        // ✅ Analytics: Track loop toggle (check current loop state from UI state)
        val currentAudio = musicController.currentAudioItem.value
        val currentLoopState = _uiState.value.playerState.isLooping
        if (currentAudio != null) {
            analyticsHelper.logAudioLoopToggled(
                contentId = currentAudio.documentId,
                loopEnabled = !currentLoopState,  // Will be toggled to opposite state
                playbackPositionMs = musicController.currentPosition.value
            )
        }

        musicController.toggleLoopMode()
    }

    /**
     * Set loop mode explicitly
     */
    fun setLoopMode(shouldLoop: Boolean) {
        Log.d("AudioPlayerViewModel", "🔄 Set loop mode: $shouldLoop")

        // ✅ Analytics: Track loop mode change
        val currentAudio = musicController.currentAudioItem.value
        if (currentAudio != null) {
            analyticsHelper.logAudioLoopToggled(
                contentId = currentAudio.documentId,
                loopEnabled = shouldLoop,
                playbackPositionMs = musicController.currentPosition.value
            )
        }

        musicController.setLoopMode(shouldLoop)
    }

    /**
     * ✅ Enhanced loading state check
     */
    fun shouldShowLoading(): Boolean {
        val uiLoading = _uiState.value.playerState.isLoading || _uiState.value.isLoading
        val hasAudioItem = _uiState.value.audioItem != null

        // Show loading if:
        // 1. UI is explicitly loading
        // 2. We have audio item but it's not actually playing yet
        return uiLoading || (hasAudioItem && !musicController.isActuallyPlaying.value && musicController.isPreparing.value)
    }

    /**
     * ✅ OPTIMIZED: Smart loading state that prevents play/pause blinks
     */
    private fun shouldShowLoadingFromController(controllerState: PlayerControllerState): Boolean {
        return when {
            // ✅ REAL LOADING: Still preparing new audio (legitimate loading)
            controllerState.isPreparing && !controllerState.isReady -> true

            // ✅ FIXED: Removed the condition that caused play/pause blinks
            // OLD: controllerState.isReady && controllerState.isPlaying && !controllerState.isActuallyPlaying -> true
            // This condition caused brief loading during normal play/pause transitions

            // ✅ BUFFERING: Only show loading if we're not ready yet
            !controllerState.isReady && controllerState.isPreparing -> true

            // Everything else should not show loading (including play/pause transitions)
            else -> false
        }
    }

    /**
     * ✅ Enhanced play/pause button visibility
     */
    fun shouldShowPlayPause(): Boolean {
        val hasAudio = _uiState.value.audioItem != null || musicController.hasActiveAudio()
        val isNotLoading = !shouldShowLoading()
        return hasAudio && isNotLoading
    }

    /**
     * ✅ Completely stop audio playback from external sources (like mini controller)
     */
    fun stopAudio() {
        Log.d("AudioPlayerViewModel", "🛑 Stopping audio completely from external source")
        if (musicController.hasActiveAudio()) {
            musicController.stopAudio()
        }
    }

    /**
     * ✅ Toggle play/pause from external sources (like mini controller) - same pattern as stopAudio
     */
    fun togglePlayPauseExternal() {
        Log.d("AudioPlayerViewModel", "⏯️ Toggle play/pause from external source")
        if (musicController.hasActiveAudio()) {
            musicController.togglePlayPause()
        }
    }

    /**
     * ✅ Get current play/pause state for UI
     */
    fun getPlayPauseState(): PlayPauseState {
        return when {
            shouldShowLoading() -> PlayPauseState.LOADING
            _uiState.value.playerState.isPlaying -> PlayPauseState.PAUSE
            else -> PlayPauseState.PLAY
        }
    }

    enum class PlayPauseState {
        LOADING, PLAY, PAUSE
    }

    /**
     * ✅ Check if we have any audio to display
     */
    fun hasAudioToDisplay(): Boolean {
        return _uiState.value.audioItem != null || musicController.hasActiveAudio()
    }

    /**
     * ✅ Force refresh UI from controller state (useful for debugging)
     */
    fun refreshFromController() {
        viewModelScope.launch {
            checkForExistingAudio()
        }
    }

    // ✅ Enhanced getIfFavourite for stories
    fun getIfFavourite(documentId: String) {
        favouriteJob?.cancel()
        favouriteJob = viewModelScope.launch {
            try {
                checkIfItemIsFavouriteUseCase(documentId).collect { isFavourite ->
                    Log.d("AudioPlayerViewModel", "❤️ Story favourite status: $isFavourite")

                    _uiState.update { current ->
                        current.copy(
                            playerState = current.playerState.copy(isFavourite = isFavourite)
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerViewModel", "❌ Error checking story favourite: ${e.message}")
            }
        }
    }

    // ✅ New function for lullaby favourite checking
    fun getIfLullabyFavourite(lullabyId: String) {
        favouriteJob?.cancel()
        favouriteJob = viewModelScope.launch {
            try {
                checkIfLullabyIsFavouriteUseCase(lullabyId).collect { isFavourite ->
                    Log.d("AudioPlayerViewModel", "❤️ Lullaby favourite status: $isFavourite")

                    _uiState.update { current ->
                        current.copy(
                            playerState = current.playerState.copy(isFavourite = isFavourite)
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerViewModel", "❌ Error checking lullaby favourite: ${e.message}")
            }
        }
    }

    // ✅ NEW: Story navigation methods

    /**
     * Load story list and determine current story position
     */
    private fun loadStoryList() {
        Log.d("AudioPlayerViewModel", "📚 Loading story list for navigation...")

        _uiState.update { current ->
            current.copy(
                storyNavigationState = current.storyNavigationState.copy(
                    isLoadingStories = true,
                    storyListError = null
                )
            )
        }

        viewModelScope.launch {
            try {
                fetchStoriesUsecase().collect { storyList ->
                    Log.d("AudioPlayerViewModel", "📚 Fetched ${storyList.size} stories")

                    val currentAudio = musicController.currentAudioItem.value
                    val currentStoryIndex = if (currentAudio?.isFromStory == true) {
                        storyList.indexOfFirst { it.documentId == currentAudio.documentId }
                    } else {
                        -1
                    }

                    Log.d("AudioPlayerViewModel", "📚 Current story index: $currentStoryIndex")

                    val canGoToPrevious = currentStoryIndex > 0
                    val canGoToNext =
                        currentStoryIndex >= 0 && currentStoryIndex < storyList.size - 1

                    _uiState.update { current ->
                        current.copy(
                            storyNavigationState = StoryNavigationState(
                                storyList = storyList,
                                currentStoryIndex = currentStoryIndex,
                                isLoadingStories = false,
                                storyListError = null,
                                canGoToPrevious = canGoToPrevious,
                                canGoToNext = canGoToNext
                            )
                        )
                    }

                    Log.d(
                        "AudioPlayerViewModel",
                        "📚 Navigation state updated - Prev: $canGoToPrevious, Next: $canGoToNext"
                    )
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerViewModel", "❌ Error loading story list: ${e.message}")
                _uiState.update { current ->
                    current.copy(
                        storyNavigationState = current.storyNavigationState.copy(
                            isLoadingStories = false,
                            storyListError = e.message ?: "Failed to load stories"
                        )
                    )
                }
            }
        }
    }

    /**
     * ✅ UPDATED: Handle story completion with "End of Story" timer support
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleStoryCompletion() {
        viewModelScope.launch {
            // ✅ Check if "End of Story" timer is active
            // For "End of Story", check DataStore only (no AlarmManager PendingIntent exists)
            val isEndOfStoryTimerActive = try {
                val savedTime = timerAlarmManager.getSavedAlarmTime()
                // ✅ FIXED: For "End of Story" timer, just check if it's saved in DataStore
                // (it doesn't schedule an actual AlarmManager alarm, so isAlarmActive() returns false)
                savedTime == "end_of_story"
            } catch (e: Exception) {
                Log.e("AudioPlayerViewModel", "❌ Error checking End of Story timer: ${e.message}")
                false
            }

            val navigationState = _uiState.value.storyNavigationState

            if (isEndOfStoryTimerActive) {
                Log.d(
                    "AudioPlayerViewModel",
                    "📖 End of Story timer is active - navigating to next story WITHOUT auto-play"
                )

                if (navigationState.canGoToNext) {
                    // ✅ Cancel the timer since it has completed its purpose
                    timerAlarmManager.cancelAlarm(false)

                    // ✅ NEW: Reset timer countdown UI state to remove filled icon
                    _timerCountdownState.value = TimerCountdownState()

                    // ✅ Reset completion flag before loading next story
                    musicController.resetCompletionFlag()

                    // ✅ Navigate to next story but DON'T auto-play
                    navigateToNextStoryWithoutAutoPlay()
                } else {
                    Log.d(
                        "AudioPlayerViewModel",
                        "🏁 End of Story timer: Last story reached - cancelling timer"
                    )
                    // Cancel timer since we've reached the last story
                    timerAlarmManager.cancelAlarm(false)
                    
                    // ✅ NEW: Reset timer countdown UI state for last story too
                    _timerCountdownState.value = TimerCountdownState()
                }
            } else if (navigationState.canGoToNext) {
                Log.d(
                    "AudioPlayerViewModel",
                    "▶️ Normal story completion - auto-playing next story"
                )

                // Reset completion flag before loading next story
                musicController.resetCompletionFlag()

                // Small delay for smooth transition (gives user moment to see completion)
                delay(500)
                navigateToNextStory(isAutoNavigation = true) // ✅ Mark as auto-navigation to preserve timer
            } else {
                Log.d(
                    "AudioPlayerViewModel",
                    "🏁 Last story completed - keeping completion flag for manual restart"
                )
                // ✅ CHANGED: DON'T reset flag for last story - let user restart it manually via togglePlayPause
                // musicController.resetCompletionFlag()
            }
        }
    }

    /**
     * ✅ NEW: Navigate to next story WITHOUT auto-playing it
     * Used specifically for "End of Story" timer functionality
     */
    private fun navigateToNextStoryWithoutAutoPlay() {
        val navigationState = _uiState.value.storyNavigationState

        if (!navigationState.canGoToNext) {
            Log.w("AudioPlayerViewModel", "⚠️ Cannot go to next story")
            return
        }

        val storyList = navigationState.storyList
        val currentIndex = navigationState.currentStoryIndex
        val nextIndex = currentIndex + 1

        if (nextIndex < 0 || nextIndex >= storyList.size) {
            Log.e("AudioPlayerViewModel", "❌ Invalid next story index: $nextIndex")
            return
        }

        val nextStory = storyList[nextIndex]
        Log.d(
            "AudioPlayerViewModel",
            "➡️ Navigating to next story WITHOUT auto-play: ${nextStory.storyName}"
        )

        // ✅ Load the next story audio but don't start playback
        loadStoryAudioWithoutAutoPlay(nextStory)

        // Show toast to user about timer completion
        viewModelScope.launch {
            android.widget.Toast.makeText(
                context,
                "Story ended - Next story loaded (tap play to start)",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * ✅ NEW: Load story audio without auto-playing it
     * Used for "End of Story" timer functionality
     */
    private fun loadStoryAudioWithoutAutoPlay(story: StoryDomainModel) {
        Log.d(
            "AudioPlayerViewModel",
            "🎵 Loading audio for story WITHOUT auto-play: ${story.storyName}"
        )

        val audioItem = AudioItem(
            id = story.id.ifEmpty { "story_${System.currentTimeMillis()}" },
            musicName = "",
            storyName = story.storyName,
            imagePath = story.imagePath.ifEmpty { "https://images.pexels.com/photos/1287145/pexels-photo-1287145.jpeg" },
            musicPath = story.storyAudioPath,
            story_listen_time_in_millis = story.story_listen_time_in_millis
        )

        // ✅ Update UI state with new story info for instant title change
        Log.d("AudioPlayerViewModel", "🔄 Updating UI with new story (paused): ${story.storyName}")
        _uiState.update { current ->
            current.copy(
                audioItem = audioItem,
                isFromStory = true,
                playerState = current.playerState.copy(
                    isLoading = false, // Don't show loading
                    isPlaying = false  // Show as paused/ready to play
                )
            )
        }

        // ✅ Prepare the audio in MusicController but don't start playback
        musicController.playAudioWithSourceAwareness(
            audioUrl = audioItem.musicPath,
            title = audioItem.storyName,
            artist = "Neptune",
            imageUrl = audioItem.imagePath,
            isFromStory = true,
            audioId = audioItem.id,
            documentId = story.documentId,
            story_listen_time_in_millis = audioItem.story_listen_time_in_millis
        )

        // ✅ Immediately pause after loading to prevent auto-play
        viewModelScope.launch {
            delay(100) // Small delay to let audio start loading
            musicController.pauseAudio()
            Log.d("AudioPlayerViewModel", "⏸️ Audio loaded and paused - ready for user to play")
        }

        // Update navigation state after loading new story
        viewModelScope.launch {
            delay(100)
            loadStoryList() // Refresh navigation state with new current story
        }
    }

    /**
     * Navigate to previous story in the list
     */
    private fun navigateToPreviousStory() {
        val navigationState = _uiState.value.storyNavigationState

        if (!navigationState.canGoToPrevious) {
            Log.w("AudioPlayerViewModel", "⚠️ Cannot go to previous story")
            musicController.resetCompletionFlag() // ✅ Reset flag even if navigation fails
            return
        }

        // ✅ Reset completion flag for manual navigation
        musicController.resetCompletionFlag()

        val storyList = navigationState.storyList
        val currentIndex = navigationState.currentStoryIndex
        val previousIndex = currentIndex - 1

        if (previousIndex < 0 || previousIndex >= storyList.size) {
            Log.e("AudioPlayerViewModel", "❌ Invalid previous story index: $previousIndex")
            return
        }

        val previousStory = storyList[previousIndex]
        Log.d("AudioPlayerViewModel", "⬅️ Navigating to previous story: ${previousStory.storyName}")

        loadStoryAudio(previousStory)
    }

    /**
     * Navigate to next story in the list
     * @param isAutoNavigation true if called from auto-play after story completion, false if manual button press
     */
    private fun navigateToNextStory(isAutoNavigation: Boolean = false) {
        val navigationState = _uiState.value.storyNavigationState

        if (!navigationState.canGoToNext) {
            Log.w("AudioPlayerViewModel", "⚠️ Cannot go to next story")
            musicController.resetCompletionFlag() // ✅ Reset flag even if navigation fails
            return
        }

        // ✅ Reset completion flag for manual navigation
        musicController.resetCompletionFlag()

        val storyList = navigationState.storyList
        val currentIndex = navigationState.currentStoryIndex
        val nextIndex = currentIndex + 1

        if (nextIndex < 0 || nextIndex >= storyList.size) {
            Log.e("AudioPlayerViewModel", "❌ Invalid next story index: $nextIndex")
            return
        }

        val nextStory = storyList[nextIndex]
        Log.d("AudioPlayerViewModel", "➡️ Navigating to next story: ${nextStory.storyName}, Auto: $isAutoNavigation")

        loadStoryAudio(nextStory, isAutoNavigation)
    }

    // ✅ NEW: Get current story with full details including description for callback navigation
    fun getCurrentStoryForReader(): StoryDomainModel? {
        val currentAudioItem = _uiState.value.audioItem
        val navigationState = _uiState.value.storyNavigationState

        if (!_uiState.value.isFromStory || currentAudioItem == null) {
            Log.w("AudioPlayerViewModel", "⚠️ Cannot get story - not currently playing a story")
            return null
        }

        // ✅ Find story from loaded list (includes description)
        val foundStory = if (navigationState.storyList.isNotEmpty()) {
            navigationState.storyList.find { story ->
                story.storyName == currentAudioItem.storyName ||
                        story.id == currentAudioItem.id ||
                        story.documentId == musicController.currentAudioItem.value?.documentId
            }
        } else null

        return if (foundStory != null) {
            Log.d("AudioPlayerViewModel", "✅ Found story with description: ${foundStory.storyName}")
            foundStory.copy(isFavourite = _uiState.value.playerState.isFavourite)
        } else {
            Log.d("AudioPlayerViewModel", "📖 Creating story from current audio item")
            val musicControllerAudio = musicController.currentAudioItem.value
            StoryDomainModel(
                id = currentAudioItem.id,
                storyName = currentAudioItem.storyName,
                storyDescription = "", // Will be empty but that's okay for reader
                imagePath = currentAudioItem.imagePath,
                storyAudioPath = currentAudioItem.musicPath,
                documentId = musicControllerAudio?.documentId ?: "",
                isFavourite = _uiState.value.playerState.isFavourite
            )
        }
    }

    // ✅ REMOVED: clearNavigationEvent() - using callbacks now
    // fun clearNavigationEvent() { ... }

    /**
     * Load audio for a specific story
     * @param isAutoNavigation true if called from auto-play, false if manual navigation
     */
    private fun loadStoryAudio(story: StoryDomainModel, isAutoNavigation: Boolean = false) {
        Log.d("AudioPlayerViewModel", "🎵 Loading audio for story: ${story.storyName}, Auto: $isAutoNavigation")

        val audioItem = AudioItem(
            id = story.id.ifEmpty { "story_${System.currentTimeMillis()}" },
            musicName = "",
            storyName = story.storyName,
            imagePath = story.imagePath.ifEmpty { "https://images.pexels.com/photos/1287145/pexels-photo-1287145.jpeg" },
            musicPath = story.storyAudioPath,
            story_listen_time_in_millis = story.story_listen_time_in_millis
        )

        // ✅ NEW: Immediately update UI state with new story info for instant title change
        Log.d("AudioPlayerViewModel", "🔄 Updating UI with new story: ${story.storyName}")
        _uiState.update { current ->
            current.copy(
                audioItem = audioItem,
                isFromStory = true,
                playerState = current.playerState.copy(
                    isLoading = true // Show loading during transition
                )
            )
        }
        Log.d(
            "AudioPlayerViewModel",
            "✅ UI state updated - Story name should now show: ${audioItem.storyName}"
        )

        // Use force load to ensure the new story loads, pass auto-navigation flag
        forceLoadNewAudio(audioItem, true, story.documentId, isAutoNavigation)

        // Update navigation state after loading new story
        viewModelScope.launch {
            // Small delay to ensure audio loads first
            kotlinx.coroutines.delay(100)
            loadStoryList() // Refresh navigation state with new current story
        }
    }

    // ✅ NEW: AdMob functionality

    /**
     * Initialize AdMob SDK
     */
    private fun initializeAds() {
        adManager.initializeAds()
    }

    /**
     * Load banner ad (Medium Rectangle)
     */
    private fun loadBannerAd(
        adUnitId: String,
        adSizeType: com.naptune.lullabyandstory.domain.model.AdSizeType
    ) {
        adManager.loadBannerAd(adUnitId, adSizeType, placement = "audio_player_screen")
    }

    /**
     * Destroy banner ad
     */
    private fun destroyBannerAd(adUnitId: String) {
        adManager.destroyBannerAd(adUnitId)
    }

    // ✅ NEW: Timer scheduling functionality moved from Screen

    /**
     * Save timer settings to preferences without scheduling alarm
     * @param time LocalTime to save
     * @param index Timer option index for saving to preferences
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTimerSettings(time: LocalTime, index: Int) {
        Log.d("AudioPlayerViewModel", "💾 Saving timer settings - time: $time, index: $index")

        viewModelScope.launch {
            try {
                // ✅ NEW: Save timer settings to TimerAlarmManager DataStore
                timerAlarmManager.saveTimerSettings(time, index)

                // ✅ Save expanded state based on selected index
                val isCustom = index == 3
                // appPreferences.saveTimerCustomExpanded(isCustom)
                Log.d(
                    "AudioPlayerViewModel",
                    "💾 Saved timer settings using TimerAlarmManager DataStore - Index: $index, Expanded: $isCustom"
                )

            } catch (e: Exception) {
                Log.e("AudioPlayerViewModel", "❌ Error saving timer settings: ${e.message}", e)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun stopTimer() {
        // ✅ Analytics: Track timer cancellation (only if timer was active)
        val wasTimerActive = _timerCountdownState.value.isTimerActive
        if (wasTimerActive) {
            val remainingMinutes = (_timerCountdownState.value.remainingMillis / 60000).toInt()
            val currentAudio = musicController.currentAudioItem.value
            analyticsHelper.logTimerCancelled(
                contentId = currentAudio?.documentId ?: "none",
                remainingMinutes = remainingMinutes
            )
        }

        // ✅ IMMEDIATE: Update UI state instantly
        _timerCountdownState.value = TimerCountdownState()
        Log.d("AudioPlayerViewModel", "⚡ Timer UI stopped immediately")

        // Cancel background job
        timerCountdownJob?.cancel()

        // ✅ Background: Cancel alarm asynchronously (without showing toast)
        viewModelScope.launch {
            try {
                timerAlarmManager.cancelAlarm(showToast = false)
                Log.d("AudioPlayerViewModel", "✅ Timer alarm cancelled in background")
            } catch (e: Exception) {
                Log.e("AudioPlayerViewModel", "❌ Error cancelling timer: ${e.message}")
            }
        }
    }

    /**
     * Schedule timer alarm with the given time and index
     * @param time LocalTime when the alarm should trigger
     * @param index Timer option index for saving to preferences
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleTimer(time: LocalTime, index: Int) {
        Log.d("AudioPlayerViewModel", "⏰ Scheduling timer for: $time, index: $index")

        // ✅ Analytics: Track if this is a new timer or modification
        val wasTimerActive = _timerCountdownState.value.isTimerActive

        // ✅ IMPORTANT: Cancel any existing timer first (every new timer setting resets previous one)
        stopTimer()
        Log.d("AudioPlayerViewModel", "🔄 Previous timer cancelled before setting new one")

        // ✅ Analytics: Track timer set/modified
        val durationMinutes = time.hour * 60 + time.minute
        val currentAudio = musicController.currentAudioItem.value
        if (wasTimerActive) {
            // Timer was modified
            analyticsHelper.logTimerModified(
                contentId = currentAudio?.documentId ?: "none",
                durationMinutes = durationMinutes,
                timerType = if (index == 5) "end_of_content" else "fixed_duration"
            )
        } else {
            // New timer set
            analyticsHelper.logTimerSet(
                contentId = currentAudio?.documentId ?: "none",
                durationMinutes = durationMinutes,
                timerType = if (index == 5) "end_of_content" else "fixed_duration"
            )
        }

        // ✅ Handle "End of Story" timer specially (index 5)
        if (index == 5) { // End of Story timer
            Log.d("AudioPlayerViewModel", "📖 Setting up 'End of Story' timer - showing text")

            // ✅ Set timer active state immediately with "End of story" text
            _timerCountdownState.value = TimerCountdownState(
                isTimerActive = true,
                remainingTimeText = "End of story", // ✅ Show "End of story" text
                remainingMillis = 0L,
                isVisible = true // ✅ Show the text
            )

            // ✅ Background: Save settings and set up timer state
            viewModelScope.launch {
                try {
                    saveTimerSettings(time, index)
                    val isScheduled = timerAlarmManager.scheduleAlarm(time, index)

                    if (isScheduled) {
                        Log.d("AudioPlayerViewModel", "✅ End of Story timer set up successfully")
                    } else {
                        Log.w(
                            "AudioPlayerViewModel",
                            "⚠️ End of Story timer setup failed - resetting UI"
                        )
                        _timerCountdownState.value = TimerCountdownState()
                    }
                } catch (e: Exception) {
                    Log.e(
                        "AudioPlayerViewModel",
                        "❌ Error setting up End of Story timer: ${e.message}"
                    )
                    _timerCountdownState.value = TimerCountdownState()
                }
            }
            return
        }

        // ✅ Regular timer logic (existing code)
        // IMMEDIATE: Update UI state instantly for superfast response
        val initialCountdown = formatTimerCountdown(time)
        val remainingMillis = (time.hour * 60 + time.minute) * 60 * 1000L

        _timerCountdownState.value = TimerCountdownState(
            isTimerActive = true,
            remainingTimeText = initialCountdown,
            remainingMillis = remainingMillis,
            isVisible = true
        )
        Log.d(
            "AudioPlayerViewModel",
            "⚡ INSTANT UI update: timer icon changed & countdown showing: $initialCountdown"
        )

        // ✅ Background: Handle all DataStore operations asynchronously
        viewModelScope.launch {
            try {
                // Save timer settings and schedule alarm in background
                saveTimerSettings(time, index)
                val isScheduled = timerAlarmManager.scheduleAlarm(time, index) // ✅ Pass timer index

                if (isScheduled) {
                    Log.d("AudioPlayerViewModel", "✅ Timer scheduled successfully in background")
                    // Start optimized countdown job
                    startTimerCountdownJob()
                } else {
                    Log.w("AudioPlayerViewModel", "⚠️ Timer scheduling failed - resetting UI")
                    _timerCountdownState.value = TimerCountdownState()
                }
            } catch (e: Exception) {
                Log.e(
                    "AudioPlayerViewModel",
                    "❌ Error in background timer operations: ${e.message}"
                )
                _timerCountdownState.value = TimerCountdownState()
            }
        }
    }

    /**
     * Check if exact alarm permissions are available
     */
    fun canScheduleExactAlarms(): Boolean {
        return timerAlarmManager.canScheduleExactAlarms()
    }

    /**
     * Get permission status message for timer
     */
    fun getTimerPermissionStatus(): String {
        return timerAlarmManager.getPermissionStatusMessage()
    }

    /**
     * Get saved timer index from TimerAlarmManager as Flow
     */
    fun getSavedTimerIndex(): Flow<Int> {
        return flow {
            emit(timerAlarmManager.getSavedTimerIndex())
        }
    }

    /**
     * Get saved timer time from TimerAlarmManager as Flow
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getSavedTimerTime(): Flow<LocalTime> {
        return flow {
            emit(timerAlarmManager.getSavedTimerTime())
        }
    }

    /**
     * Helper function to format LocalTime to countdown text
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatTimerCountdown(time: LocalTime): String {
        val totalMinutes = time.hour * 60 + time.minute
        return if (totalMinutes <= 0) "" else "${totalMinutes}:00m"
    }

    /**
     * Get timer countdown state as Flow that updates every second
     * Format: "5:15m" with 16sp SansSerif white 50% opacity
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimerCountdownFlow(): Flow<TimerCountdownState> {
        return flow {
            while (true) {
                try {
                    val shouldShow = timerAlarmManager.shouldShowCountdown()
                    val remainingMillis = timerAlarmManager.getRemainingTimeMillis()
                    val formattedTime = timerAlarmManager.formatRemainingTime(remainingMillis)

                    val countdownState = TimerCountdownState(
                        isTimerActive = shouldShow,
                        remainingTimeText = formattedTime,
                        remainingMillis = remainingMillis,
                        isVisible = shouldShow && formattedTime.isNotEmpty()
                    )

                    emit(countdownState)

                    // Stop emitting if timer is not active or expired
                    if (!shouldShow || remainingMillis <= 0) {
                        break
                    }

                    // Wait 1 second before next update
                    delay(1000L)

                } catch (e: Exception) {
                    Log.e("AudioPlayerViewModel", "❌ Error in timer countdown: ${e.message}")
                    // Emit empty state on error
                    emit(TimerCountdownState())
                    break
                }
            }
        } // Removed distinctUntilChanged to ensure smooth countdown updates
    }

    /**
     * Start super-optimized timer countdown background job
     * Uses minimal DataStore reads and efficient timing
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startTimerCountdownJob() {
        // Cancel existing job
        timerCountdownJob?.cancel()
        Log.d("AudioPlayerViewModel", "🚀 Starting optimized countdown job")

        timerCountdownJob = viewModelScope.launch {
            try {
                // ✅ Get end time once to minimize DataStore reads
                val endTimeMillis = timerAlarmManager.getSavedAlarmEndTimeMillis()
                if (endTimeMillis <= 0) {
                    Log.w("AudioPlayerViewModel", "⚠️ No valid end time found")
                    return@launch
                }

                while (true) {
                    val currentTime = System.currentTimeMillis()
                    val remainingMillis = maxOf(0, endTimeMillis - currentTime)

                    if (remainingMillis <= 0) {
                        // Timer finished
                        _timerCountdownState.value = TimerCountdownState()
                        Log.d("AudioPlayerViewModel", "⏰ Timer countdown finished")
                        break
                    }

                    // ✅ Direct calculation - no DataStore reads in loop
                    val formattedTime = timerAlarmManager.formatRemainingTime(remainingMillis)
                    _timerCountdownState.value = TimerCountdownState(
                        isTimerActive = true,
                        remainingTimeText = formattedTime,
                        remainingMillis = remainingMillis,
                        isVisible = true
                    )

                    delay(1000L)
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerViewModel", "❌ Timer countdown job error: ${e.message}")
                _timerCountdownState.value = TimerCountdownState()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("AudioPlayerViewModel", "🧹 ViewModel cleared")

        // Cancel timer countdown job
        timerCountdownJob?.cancel()

        // ⚠️ IMPORTANT: Do NOT destroy banner ad here!
        // Banner ads use shared adUnitId stored in AdMobDataSource singleton.
        // Destroying here would also destroy other screens' banner ads,
        // causing them to vanish when user navigates between screens.
        // The ad will be recreated when user navigates to this screen again.
        Log.d("AudioPlayerViewModel", "🧹 ViewModel cleared - Banner ad preserved (shared across screens)")

        // Don't release the music controller - let it continue playing in background
    }
}

// Extension function to format time
fun Long.formatTime(): String {
    val minutes = this / 60000
    val seconds = (this % 60000) / 1000
    return String.format("%d:%02d", minutes, seconds)
}

// Extension function to convert milliseconds to minutes
fun Long.formatToMinutes(): String {
    val minutes = this / 60000
    return "$minutes"
}
