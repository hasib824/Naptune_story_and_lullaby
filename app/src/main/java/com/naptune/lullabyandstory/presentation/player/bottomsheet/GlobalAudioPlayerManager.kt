package com.naptune.lullabyandstory.presentation.player.bottomsheet

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.naptune.lullabyandstory.presentation.player.AudioPlayerViewModel
import com.naptune.lullabyandstory.presentation.player.service.MusicController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.cancel
import javax.inject.Inject
import javax.inject.Singleton

// Data class for mini controller info
data class AudioInfo(
    val imagePath: String,
    val musicName: String,
    val isFromStory: Boolean,
    val documentId: String
)

@Singleton
class GlobalAudioPlayerManager @Inject constructor() {
    
    private val _bottomSheetState = MutableStateFlow<AudioPlayerBottomSheetState?>(null)
    val bottomSheetState: StateFlow<AudioPlayerBottomSheetState?> = _bottomSheetState.asStateFlow()
    
    private val _isVisible = MutableStateFlow(false)
    val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()
    
    // ✅ NEW: Audio running state for mini controller (for backward compatibility)
    private val _isAudioRunning = MutableStateFlow(false)
    val isAudioRunning: StateFlow<Boolean> = _isAudioRunning.asStateFlow()
    
    // ✅ NEW: Audio playing state for play/pause button
    private val _isAudioPlaying = MutableStateFlow(false)
    val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying.asStateFlow()
    
    // ✅ NEW: Audio active state (loaded audio - playing OR paused)
    private val _hasActiveAudio = MutableStateFlow(false)
    val hasActiveAudio: StateFlow<Boolean> = _hasActiveAudio.asStateFlow()

    private val _currentAudioInfo = MutableStateFlow<AudioInfo?>(null)
    val currentAudioInfo: StateFlow<AudioInfo?> = _currentAudioInfo.asStateFlow()

    // ✅ NEW: Reference to AudioPlayerViewModel for stop functionality
    private var audioPlayerViewModel: AudioPlayerViewModel? = null
    
    // ✅ NEW: MusicController for direct state observing
    private var musicController: MusicController? = null
    private var stateObserverJob: Job? = null
    private var audioInfoObserverJob: Job? = null // ✅ NEW: Separate job for audio info updates
    // ✅ Use SupervisorJob for proper cancellation handling
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    fun showAudioPlayer(
        audioId: String,
        isFromStory: Boolean,
        musicPath: String,
        musicName: String,
        imagePath: String,
        documentId: String,
        musicLocalPath: String? = null,
        fromNotification: Boolean = false,
        isUserSelection: Boolean = true, // ✅ NEW: Track if user manually selected audio
        onNavigateToStoryReader: ((com.naptune.lullabyandstory.domain.model.StoryDomainModel) -> Unit)? = null,
        story_listen_time_in_millis: Long = 0L
    ) {
        Log.d("GlobalAudioPlayerManager", "🎵 Showing audio player: $musicName")
        Log.d("GlobalAudioPlayerManager", "🎵 From notification: $fromNotification, User selection: $isUserSelection")

        _bottomSheetState.value = AudioPlayerBottomSheetState(
            audioId = audioId,
            isFromStory = isFromStory,
            musicPath = musicPath,
            musicName = musicName,
            imagePath = imagePath,
            documentId = documentId,
            musicLocalPath = musicLocalPath,
            fromNotification = fromNotification,
            isUserSelection = isUserSelection, // ✅ Pass to state
            story_listen_time_in_millis = story_listen_time_in_millis,
            onNavigateToStoryReader = onNavigateToStoryReader,
            onDismiss = ::hideAudioPlayer
        )
        
        _isVisible.value = true
        
        // ✅ Update audio info for mini controller
        updateCurrentAudioInfo(
            AudioInfo(
                imagePath = imagePath,
                musicName = musicName,
                isFromStory = isFromStory,
                documentId = documentId
            )
        )
    }
    
    fun hideAudioPlayer() {
        Log.d("GlobalAudioPlayerManager", "🎵 Hiding audio player")
        _isVisible.value = false
        _bottomSheetState.value = null
        // Audio info remains for mini controller
    }
    
    /**
     * ✅ Show existing audio player without reloading audio (for mini controller)
     */
    fun showExistingAudioPlayer() {
        val currentAudio = _currentAudioInfo.value
        if (currentAudio != null) {
            Log.d("GlobalAudioPlayerManager", "🎵 Showing existing audio player: ${currentAudio.musicName}")

            _bottomSheetState.value = AudioPlayerBottomSheetState(
                audioId = currentAudio.documentId,
                isFromStory = currentAudio.isFromStory,
                musicPath = "", // Will use existing loaded audio
                musicName = currentAudio.musicName,
                imagePath = currentAudio.imagePath,
                documentId = currentAudio.documentId,
                musicLocalPath = null,
                fromNotification = true, // ✅ This tells the UI to use existing audio
                isUserSelection = false, // ✅ From mini controller, not user selection
                onNavigateToStoryReader = null,
                onDismiss = ::hideAudioPlayer
            )

            _isVisible.value = true
            Log.d("GlobalAudioPlayerManager", "✅ Existing audio player shown without reload")
        } else {
            Log.w("GlobalAudioPlayerManager", "⚠️ Cannot show existing player - no current audio info")
        }
    }
    
    // ✅ NEW: Audio state management functions
    fun updateAudioRunningState(isRunning: Boolean) {
        Log.d("GlobalAudioPlayerManager", "🎵 Audio running state: $isRunning")
        _isAudioRunning.value = isRunning
    }
    
    fun updateAudioPlayingState(isPlaying: Boolean) {
        Log.d("GlobalAudioPlayerManager", "🎵 Audio playing state: $isPlaying")
        _isAudioPlaying.value = isPlaying
    }
    
    /**
     * ✅ Update active audio state (loaded audio regardless of playing/paused)
     * This is used for mini controller visibility - shows for both playing AND paused
     */
    fun updateActiveAudioState(hasAudio: Boolean) {
        Log.d("GlobalAudioPlayerManager", "🎵 Active audio state: $hasAudio")
        _hasActiveAudio.value = hasAudio
    }
    
    fun updateCurrentAudioInfo(audioInfo: AudioInfo?) {
        Log.d("GlobalAudioPlayerManager", "🎵 Current audio: ${audioInfo?.musicName}")
        _currentAudioInfo.value = audioInfo
    }
    
    fun stopAudio() {
        Log.d("GlobalAudioPlayerManager", "🛑 Completely stopping audio")

        // ✅ Use MusicController directly instead of ViewModel
        musicController?.let { controller ->
            controller.stopAudio()
            Log.d("GlobalAudioPlayerManager", "✅ Audio stopped via MusicController")
        } ?: run {
            Log.w("GlobalAudioPlayerManager", "⚠️ MusicController not available")
        }

        _isAudioRunning.value = false
        _isAudioPlaying.value = false

        // ✅ Add slide down animation delay before hiding mini controller
        scope.launch {
            // Wait for slide down animation to complete (300ms)
            delay(300)

            _hasActiveAudio.value = false // ✅ Clear active audio state after animation
            _currentAudioInfo.value = null

            Log.d("GlobalAudioPlayerManager", "✅ Mini controller hidden after slide down animation")
        }

        // Also hide bottom sheet if visible (immediate)
        if (_isVisible.value) {
            hideAudioPlayer()
        }
    }
    
    /**
     * ✅ Toggle play/pause using MusicController directly with completion detection
     * AND restored audio handling
     */
    fun togglePlayPause() {
        Log.d("GlobalAudioPlayerManager", "⏯️ Toggle play/pause")

        musicController?.let { controller ->
            // ✅ Check completion state before toggling
            val hasCompleted = controller.hasCompletedPlayback.value
            val isFromStory = controller.currentAudioItem.value?.isFromStory == true
            val hasActiveAudio = controller.hasActiveAudio()

            Log.d("GlobalAudioPlayerManager", "⏯️ State - Completed: $hasCompleted, Story: $isFromStory, HasAudio: $hasActiveAudio")

            when {
                // ✅ Case 1: Last story completed - force reload from beginning
                hasActiveAudio && hasCompleted && isFromStory -> {
                    Log.d("GlobalAudioPlayerManager", "🔄 Last story completed - force reloading from beginning")

                    val currentAudio = controller.currentAudioItem.value
                    if (currentAudio != null) {
                        // Reset completion flag
                        controller.resetCompletionFlag()

                        // Force reload audio from beginning
                        controller.stopAudio()
                        controller.playAudioWithSourceAwareness(
                            audioUrl = currentAudio.audioUrl,
                            title = currentAudio.title,
                            artist = "Neptune",
                            imageUrl = currentAudio.imageUrl,
                            isFromStory = currentAudio.isFromStory,
                            audioId = currentAudio.id,
                            documentId = currentAudio.documentId,
                            story_listen_time_in_millis = currentAudio.story_listen_time_in_millis
                        )
                        Log.d("GlobalAudioPlayerManager", "✅ Story force reloaded from beginning")
                    } else {
                        Log.e("GlobalAudioPlayerManager", "❌ Cannot reload - no current audio info")
                    }
                }

                // ✅ Case 2: Normal toggle
                hasActiveAudio -> {
                    Log.d("GlobalAudioPlayerManager", "⏯️ Normal toggle")
                    controller.togglePlayPause()
                }

                // ✅ Case 3: No audio available
                else -> {
                    Log.w("GlobalAudioPlayerManager", "⚠️ No active audio to toggle")
                }
            }

            Log.d("GlobalAudioPlayerManager", "✅ Toggle operation completed")
        } ?: run {
            Log.w("GlobalAudioPlayerManager", "⚠️ MusicController not available for toggle")
        }

        // Note: UI state will be updated automatically via MusicController.isPlaying observer (Line 230-234)
    }
    
    // ✅ NEW: Register AudioPlayerViewModel for stop functionality
    fun registerAudioPlayerViewModel(viewModel: AudioPlayerViewModel) {
        this.audioPlayerViewModel = viewModel
        Log.d("GlobalAudioPlayerManager", "🎵 AudioPlayerViewModel registered")
    }
    
    fun unregisterAudioPlayerViewModel() {
        this.audioPlayerViewModel = null
        Log.d("GlobalAudioPlayerManager", "🎵 AudioPlayerViewModel unregistered")
    }
    
    /**
     * ✅ Register MusicController for direct state observing (fixes notification sync issue + auto story change)
     */
    fun registerMusicController(controller: MusicController) {
        this.musicController = controller
        Log.d("GlobalAudioPlayerManager", "🎵 MusicController registered")

        // Cancel existing observers
        stateObserverJob?.cancel()
        audioInfoObserverJob?.cancel()

        // Observe playing state changes from notifications/external sources
        stateObserverJob = scope.launch {
            controller.isPlaying.collect { isPlaying ->
                Log.d("GlobalAudioPlayerManager", "🎵 MusicController state changed - Playing: $isPlaying")
                _isAudioPlaying.value = isPlaying
                _isAudioRunning.value = isPlaying
            }
        }

        // ✅ NEW: Observe currentAudioItem to auto-update MiniController info when story changes
        audioInfoObserverJob = scope.launch {
            controller.currentAudioItem.collect { currentAudio ->
                if (currentAudio != null) {
                    // ✅ Auto-update audio info for MiniController
                    val newAudioInfo = AudioInfo(
                        imagePath = currentAudio.imageUrl,
                        musicName = currentAudio.title,
                        isFromStory = currentAudio.isFromStory,
                        documentId = currentAudio.documentId
                    )

                    // Only update if different from current to avoid unnecessary recompositions
                    if (_currentAudioInfo.value?.documentId != currentAudio.documentId) {
                        _currentAudioInfo.value = newAudioInfo
                        Log.d("GlobalAudioPlayerManager", "🔄 Auto-updated audio info: ${currentAudio.title}")
                    }

                    // Update active audio state
                    _hasActiveAudio.value = true
                } else {
                    // No audio playing
                    _hasActiveAudio.value = false
                }
            }
        }
    }
    
    fun unregisterMusicController() {
        stateObserverJob?.cancel()
        stateObserverJob = null
        audioInfoObserverJob?.cancel()
        audioInfoObserverJob = null
        this.musicController = null
        Log.d("GlobalAudioPlayerManager", "🎵 MusicController unregistered")
    }

    fun getCurrentState(): AudioPlayerBottomSheetState? {
        return _bottomSheetState.value
    }

    // ✅ UPDATED: Check if mini controller should be visible (now uses hasActiveAudio)
    fun shouldShowMiniController(): Boolean {
        return _hasActiveAudio.value && !_isVisible.value && _currentAudioInfo.value != null
    }

    // ✅ NEW: Add cleanup method
    fun cleanup() {
        Log.d("GlobalAudioPlayerManager", "🧹 Cleaning up GlobalAudioPlayerManager")

        // Cancel observer jobs
        stateObserverJob?.cancel()
        stateObserverJob = null
        audioInfoObserverJob?.cancel()
        audioInfoObserverJob = null

        // Cancel the entire scope
        scope.cancel()

        // Clear references
        musicController = null
        audioPlayerViewModel = null

        // Reset states
        _isVisible.value = false
        _bottomSheetState.value = null
        _isAudioRunning.value = false
        _isAudioPlaying.value = false
        _hasActiveAudio.value = false
        _currentAudioInfo.value = null

        Log.d("GlobalAudioPlayerManager", "✅ GlobalAudioPlayerManager cleaned up")
    }
}