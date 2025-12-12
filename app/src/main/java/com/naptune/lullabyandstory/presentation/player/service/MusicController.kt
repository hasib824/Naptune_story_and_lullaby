package com.naptune.lullabyandstory.presentation.player.service

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri
import kotlinx.coroutines.cancel
import java.io.File

@Singleton
class MusicController @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var positionUpdateJob: Job? = null
    private var durationFetchJob: Job? = null
    private var playerListener: Player.Listener? = null // ✅ Store listener for proper cleanup
    private val scope = CoroutineScope(Dispatchers.Main)

    // State flows for UI
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isPreparing = MutableStateFlow(true)
    val isPreparing: StateFlow<Boolean> = _isPreparing.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()

    private val _isControllerReady = MutableStateFlow(false)
    val isControllerReady: StateFlow<Boolean> = _isControllerReady.asStateFlow()

    // New state for tracking audio source and loop settings
    private val _isFromStory = MutableStateFlow(false)
    val isFromStory: StateFlow<Boolean> = _isFromStory.asStateFlow()

    private val _shouldLoop = MutableStateFlow(false)
    val shouldLoop: StateFlow<Boolean> = _shouldLoop.asStateFlow()

    // Volume control state
    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    // Enhanced playback state detection
    private val _isActuallyPlaying = MutableStateFlow(false)
    val isActuallyPlaying: StateFlow<Boolean> = _isActuallyPlaying.asStateFlow()

    // Audio preparation tracking
    private val _isAudioLoaded = MutableStateFlow(false)
    val isAudioLoaded: StateFlow<Boolean> = _isAudioLoaded.asStateFlow()

    // Timeout tracking for audio start
    private var audioStartTimeoutJob: Job? = null
    private val AUDIO_START_TIMEOUT = 8000L // 8 seconds timeout

    // Music state preservation
    private val _currentAudioItem = MutableStateFlow<AudioItemState?>(null)
    val currentAudioItem: StateFlow<AudioItemState?> = _currentAudioItem.asStateFlow()

    // ✅ NEW: Story completion state for auto-play next story
    private val _hasCompletedPlayback = MutableStateFlow(false)
    val hasCompletedPlayback: StateFlow<Boolean> = _hasCompletedPlayback.asStateFlow()

    init {
        initializeController()
    }

    // Data class for preserving audio state
    data class AudioItemState(
        val id: String,
        val documentId: String,
        val title: String,
        val artist: String,
        val imageUrl: String,
        val audioUrl: String,
        val isFromStory: Boolean,
        val position: Long = 0L,
        val isPlaying: Boolean = false,
        val story_listen_time_in_millis: Long = 0L
    )

    /**
     * Enhanced audio playbook with state preservation and proper duration handling
     */
    fun playAudioWithSourceAwareness(
        audioUrl: String,
        title: String,
        artist: String,
        imageUrl: String,
        isFromStory: Boolean,
        audioId: String = "",
        documentId: String,
        story_listen_time_in_millis: Long = 0L
    ) {
        Log.d("MusicController", "🎵 ========== PLAY AUDIO START ===========")
        Log.d("MusicController", "🎵 playAudioWithSourceAwareness: $title (ID: $audioId)")
        Log.d("MusicController", "🎵 isFromStory: $isFromStory, URL: $audioUrl")

        val currentAudio = _currentAudioItem.value
        Log.d("MusicController", "🎵 Current audio: ${currentAudio?.id ?: "NULL"} - ${currentAudio?.title ?: "NULL"}")

        // Reset all states for new audio
        resetAudioStates()

        // ✅ NEW: Reset completion flag for new audio load
        _hasCompletedPlayback.value = false

        // Set initial loading states
        _isPreparing.value = true
        _isReady.value = false
        _isPlaying.value = false
        _isActuallyPlaying.value = false
        _isAudioLoaded.value = false

        // Update audio source state
        _isFromStory.value = isFromStory

        // Set loop behavior: Lullaby = loop, Story = no loop
        val shouldLoop = !isFromStory
        _shouldLoop.value = shouldLoop

        // Determine the correct audio source
        val finalAudioUrl = determineAudioSource(audioUrl, isFromStory)

        // Save current audio state for preservation
        _currentAudioItem.value = AudioItemState(
            id = audioId,
            title = title,
            artist = artist,
            imageUrl = imageUrl,
            audioUrl = finalAudioUrl,
            isFromStory = isFromStory,
            documentId = documentId,
            story_listen_time_in_millis = story_listen_time_in_millis
        )

        Log.d("MusicController", "🎯 Audio source: $finalAudioUrl")
        Log.d("MusicController", "🔄 Loop setting: $shouldLoop")

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(imageUrl.toUri())
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(finalAudioUrl)
            .setMediaMetadata(mediaMetadata)
            .build()

        try {
            mediaController?.let { controller ->
                // Set repeat mode based on source
                if (shouldLoop) {
                    controller.repeatMode = Player.REPEAT_MODE_ONE
                    Log.d("MusicController", "🔄 LULLABY - Loop enabled")
                } else {
                    controller.repeatMode = Player.REPEAT_MODE_OFF
                    Log.d("MusicController", "📖 STORY - Loop disabled")
                }

                controller.setMediaItem(mediaItem)
                controller.prepare()
                controller.play()

                // Start timeout for audio start detection
                startAudioStartTimeout()

                // ✅ Set manual duration for testing (2:28) - Comment out for auto detection
               // setManualDuration(2, 28)

                // Start duration fetching with retry mechanism (as backup)
                fetchAudioDurationWithRetry()

                Log.d("MusicController", "✅ Audio playback initiated with enhanced tracking")
            }
        } catch (e: Exception) {
            Log.e("MusicController", "❌ Error starting playback: ${e.message}")
            _isPreparing.value = false
            _isActuallyPlaying.value = false
        }
    }

    /**
     * Enhanced duration fetching with retry mechanism
     */
    private fun fetchAudioDurationWithRetry() {
        durationFetchJob?.cancel()
        durationFetchJob = scope.launch {
            var attempts = 0
            val maxAttempts = 15 // Try for 15 seconds

            Log.d("MusicController", "🕒 Starting duration fetch...")

            while (attempts < maxAttempts) {
                val controller = mediaController
                if (controller != null) {
                    val rawDuration = controller.duration

                    Log.d("MusicController", "🕒 Duration attempt ${attempts + 1}: $rawDuration")

                    // Check if duration is valid (not C.TIME_UNSET)
                    if (rawDuration != C.TIME_UNSET && rawDuration > 0) {
                        _duration.value = rawDuration
                        val durationInSeconds = rawDuration / 1000
                        Log.d("MusicController", "✅ Duration fetched successfully: $durationInSeconds seconds")

                        // Also try MediaMetadataRetriever as backup for validation
                        validateDurationWithRetriever(controller.currentMediaItem?.localConfiguration?.uri.toString())
                        break
                    }
                }

                attempts++
                delay(1000) // Wait 1 second before retry
            }

            if (attempts >= maxAttempts) {
                Log.w("MusicController", "⚠️ Could not fetch duration after $maxAttempts attempts")
                // Try MediaMetadataRetriever as last resort
                tryGetDurationWithRetriever()
            }
        }
    }

    /**
     * Validate duration using MediaMetadataRetriever
     */
    private fun validateDurationWithRetriever(audioUrl: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(audioUrl)
                val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durationFromRetriever = durationString?.toLongOrNull() ?: 0L

                if (durationFromRetriever > 0) {
                    val currentDuration = _duration.value
                    Log.d("MusicController", "🔍 Duration validation - ExoPlayer: ${currentDuration/1000}s, Retriever: ${durationFromRetriever/1000}s")

                    // If ExoPlayer duration is 0 or very different, use retriever value
                    if (currentDuration == 0L || Math.abs(currentDuration - durationFromRetriever) > 5000) {
                        _duration.value = durationFromRetriever
                        Log.d("MusicController", "🔧 Duration corrected using MediaMetadataRetriever: ${durationFromRetriever/1000}s")
                    }
                }
                retriever.release()
            } catch (e: Exception) {
                Log.e("MusicController", "❌ Error validating duration with retriever: ${e.message}")
            }
        }
    }

    /**
     * Try to get duration using MediaMetadataRetriever as last resort
     */
    private fun tryGetDurationWithRetriever() {
        _currentAudioItem.value?.let { audioState ->
            scope.launch(Dispatchers.IO) {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(audioState.audioUrl)
                    val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val duration = durationString?.toLongOrNull() ?: 0L

                    if (duration > 0) {
                        _duration.value = duration
                        Log.d("MusicController", "🆘 Duration retrieved as last resort: ${duration/1000} seconds")
                    }
                    retriever.release()
                } catch (e: Exception) {
                    Log.e("MusicController", "❌ Last resort duration fetch failed: ${e.message}")
                }
            }
        }
    }

    /**
     * Resume existing audio (enhanced for service disconnection)
     */
    fun resumeExistingAudio(): Boolean {
        return _currentAudioItem.value?.let { audioState ->
            Log.d("MusicController", "🔄 Resuming existing audio: ${audioState.title}")

            mediaController?.let { controller ->
                if (controller.currentMediaItem != null) {
                    // Audio already loaded, just play
                    Log.d("MusicController", "▶️ Audio already loaded - resuming playback")
                    if (!controller.isPlaying) {
                        controller.play()
                    }

                    // Update UI states
                    _isFromStory.value = audioState.isFromStory
                    _shouldLoop.value = !audioState.isFromStory
                    _isPreparing.value = false
                    _isReady.value = true

                    // Retry duration fetch if needed
                    if (_duration.value == 0L) {
                        fetchAudioDurationWithRetry()
                    }

                    // ✅ Start position tracking for existing audio
                    if (controller.isPlaying) {
                        Log.d("MusicController", "🚀 Starting position tracking for resumed audio")
                        startPositionTracking()
                    }

                    Log.d("MusicController", "✅ Resumed existing audio successfully")
                    return true
                } else {
                    // Service was disconnected - reload the audio
                    Log.d("MusicController", "🔄 Service disconnected - reloading audio")
                    playAudioWithSourceAwareness(
                        audioUrl = audioState.audioUrl,
                        title = audioState.title,
                        artist = audioState.artist,
                        imageUrl = audioState.imageUrl,
                        isFromStory = audioState.isFromStory,
                        audioId = audioState.id,
                        documentId = audioState.documentId,
                        story_listen_time_in_millis = audioState.story_listen_time_in_millis
                    )
                    return true
                }
            } ?: run {
                // Controller not ready - wait and retry
                Log.d("MusicController", "⏳ Controller not ready - will retry when connected")

                // Try to reconnect and then reload
                scope.launch {
                    _isControllerReady.collect { ready ->
                        if (ready) {
                            Log.d("MusicController", "🔄 Controller ready - reloading audio")
                            playAudioWithSourceAwareness(
                                audioUrl = audioState.audioUrl,
                                title = audioState.title,
                                artist = audioState.artist,
                                imageUrl = audioState.imageUrl,
                                isFromStory = audioState.isFromStory,
                                audioId = audioState.id,
                                documentId = audioState.documentId,
                                story_listen_time_in_millis = audioState.story_listen_time_in_millis
                            )
                            cancel()
                        }
                    }
                }
                return true
            }
        } ?: false
    }

    /**
     * Check if there's existing audio (enhanced for service lifecycle)
     */
    fun hasActiveAudio(): Boolean {
        val hasAudioState = _currentAudioItem.value != null
        val hasMediaItem = mediaController?.currentMediaItem != null

        Log.d("MusicController", "🔍 hasActiveAudio - AudioState: $hasAudioState, MediaItem: $hasMediaItem")

        // Return true if we have audio state, even if service was disconnected
        return hasAudioState
    }

    /**
     * Determines the correct audio source based on content type
     */
    private fun determineAudioSource(audioPath: String, isFromStory: Boolean): String {
        return if (isFromStory) {
            // For stories: Use test audio from appwrite
           // val testAudioUrl = "https://appwrite.taagidtech.com/v1/storage/buckets/66f7d15b0029596f5c3c/files/66f95210000a85a56d30/view?project=617c2ffb656e7&project=617c2ffb656e7&mode=admin"
          //  val testAudioUrl = "https://appwrite.taagidtech.com/v1/storage/buckets/67cc757a000d061ca29e/files/67cc770d001a234699c6/view?project=671e0ca70034a1e99b3d&project=671e0ca70034a1e99b3d&mode=admin"

            Log.d("MusicController", "🎵 Using test audio for story: $audioPath")
            audioPath
        } else {
            // For lullabies: Use local storage if available, fallback to online
            if (isLocalFileAvailable(audioPath)) {
                Log.d("MusicController", "📁 Using local file for lullaby")
                audioPath
            } else {
                Log.d("MusicController", "🌐 Local file not found, using online source")
                convertLocalPathToOnlineUrl(audioPath)
            }
        }
    }

    /**
     * Checks if a local audio file exists
     */
    private fun isLocalFileAvailable(localPath: String): Boolean {
        return try {
            if (localPath.startsWith("http://") || localPath.startsWith("https://")) {
                false
            } else {
                val file = File(localPath)
                val exists = file.exists() && file.canRead()
                Log.d("MusicController", "📁 Local file check: $localPath - exists: $exists")
                exists
            }
        } catch (e: Exception) {
            Log.e("MusicController", "❌ Error checking local file: ${e.message}")
            false
        }
    }

    /**
     * Converts a local path to an online URL
     */
    private fun convertLocalPathToOnlineUrl(localPath: String): String {
        if (localPath.startsWith("http://") || localPath.startsWith("https://")) {
            return localPath
        }

        val fileName = File(localPath).name
        // Update this URL according to your server
        val baseUrl = "https://your-server.com/api/audio"
        val onlineUrl = "$baseUrl/$fileName"

        Log.d("MusicController", "🌐 Converted: $localPath -> $onlineUrl")
        return onlineUrl
    }

    /**
     * Enhanced volume control with state management
     */
    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        _volume.value = clampedVolume
        mediaController?.volume = clampedVolume

        Log.d("MusicController", "🔊 Volume set to: ${(clampedVolume * 100).toInt()}%")
    }

    /**
     * Get current volume
     */
    fun getVolume(): Float {
        return _volume.value
    }

    /**
     * Toggle loop mode
     */
    fun toggleLoopMode() {
        val newLoopState = !_shouldLoop.value
        _shouldLoop.value = newLoopState

        mediaController?.let { controller ->
            controller.repeatMode = if (newLoopState) {
                Player.REPEAT_MODE_ONE
            } else {
                Player.REPEAT_MODE_OFF
            }
        }

        Log.d("MusicController", "🔄 Loop mode toggled to: $newLoopState")
    }

    /**
     * Set loop mode explicitly
     */
    fun setLoopMode(shouldLoop: Boolean) {
        _shouldLoop.value = shouldLoop

        mediaController?.let { controller ->
            controller.repeatMode = if (shouldLoop) {
                Player.REPEAT_MODE_ONE
            } else {
                Player.REPEAT_MODE_OFF
            }
        }

        Log.d("MusicController", "🔄 Loop mode set to: $shouldLoop")
    }

    fun playAudioWhenReady(audioUrl: String, title: String, artist: String, imageUrl: String) {
        if (_isControllerReady.value) {
            // Use the enhanced method with default parameters
        } else {
            scope.launch {
                _isControllerReady.collect { ready ->
                    if (ready) {
                        cancel()
                    }
                }
            }
        }
    }

    private fun initializeController() {
        Log.d("MusicController", "🚀 Initializing MediaController...")
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                _isControllerReady.value = true
                Log.d("MusicController", "✅ MediaController connected successfully!")
                setupPlayerListener()
            } catch (e: Exception) {
                Log.e("MusicController", "❌ Failed to connect MediaController: ${e.message}")
                _isControllerReady.value = false
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        // ✅ Remove old listener first to prevent duplicate listeners
        playerListener?.let {
            mediaController?.removeListener(it)
            Log.d("MusicController", "🧹 Removed old player listener")
        }

        // ✅ Create and store new listener
        playerListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                Log.d("MusicController", "🎛️ Playback state changed: ${getPlaybackStateString(playbackState)}")

                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _isPreparing.value = true
                        _isReady.value = false
                        _isPlaying.value = false
                        Log.d("MusicController", "🔄 Buffering...")
                    }
                    Player.STATE_READY -> {
                        _isReady.value = true
                        _isAudioLoaded.value = true

                        // Get duration immediately when ready
                        val duration = mediaController?.duration ?: C.TIME_UNSET
                        if (duration != C.TIME_UNSET && duration > 0) {
                            _duration.value = duration
                            val durationInSeconds = duration / 1000
                            Log.d("MusicController", "✅ Duration available on ready: $durationInSeconds seconds")
                        } else {
                            Log.d("MusicController", "⏳ Duration not ready yet, will retry...")
                            // Start retry mechanism
                            fetchAudioDurationWithRetry()
                        }

                        val isPlayingNow = mediaController?.playWhenReady == true
                        _isPlaying.value = isPlayingNow

                        Log.d("MusicController", "✅ Ready to play - playWhenReady: $isPlayingNow")

                        if (isPlayingNow) {
                            startActualPlaybackDetection()
                            // ✅ Force start position tracking when ready and playing
                            Log.d("MusicController", "🚀 Starting position tracking from STATE_READY")
                            startPositionTracking()
                        } else {
                            Log.d("MusicController", "⏳ Ready but not playing yet - keeping loading state")
                        }

                        // Update current audio state
                        _currentAudioItem.value?.let { currentState ->
                            _currentAudioItem.value = currentState.copy(
                                isPlaying = isPlayingNow,
                                position = mediaController?.currentPosition ?: 0L
                            )
                        }
                    }
                    Player.STATE_ENDED -> {
                        _isPreparing.value = false
                        _isReady.value = false
                        _isPlaying.value = false
                        _isActuallyPlaying.value = false

                        if (_isFromStory.value) {
                            Log.d("MusicController", "📖 Story ended - emitting completion signal for auto-play")
                            _hasCompletedPlayback.value = true // ✅ NEW: Signal completion for auto-play
                        } else {
                            Log.d("MusicController", "🎵 Lullaby ended - will loop if enabled")
                            // ✅ Don't emit completion for lullabies (they loop automatically)
                        }
                    }
                    Player.STATE_IDLE -> {
                        _isPreparing.value = false
                        _isReady.value = false
                        _isPlaying.value = false
                        _isActuallyPlaying.value = false
                    }
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                Log.d("MusicController", "▶️ PlayWhenReady changed: $playWhenReady, reason: $reason")

                val playbackState = mediaController?.playbackState ?: -1
                val isReadyToPlay = playbackState == Player.STATE_READY
                val isPlayingNow = playWhenReady && isReadyToPlay

                Log.d("MusicController", "🎛️ State check - PlaybackState: $playbackState, ReadyToPlay: $isReadyToPlay, IsPlayingNow: $isPlayingNow")

                _isPlaying.value = isPlayingNow

                // Start actual playback detection if conditions are met
                if (isPlayingNow && isReadyToPlay) {
                    startActualPlaybackDetection()
                } else if (!playWhenReady) {
                    // User paused
                    _isActuallyPlaying.value = false
                    _isPreparing.value = false
                    cancelAudioStartTimeout()
                }

                // Update audio state
                _currentAudioItem.value?.let { currentState ->
                    _currentAudioItem.value = currentState.copy(isPlaying = isPlayingNow)
                }

                if (isPlayingNow) {
                    Log.d("MusicController", "🚀 Calling startPositionTracking() - Audio should be playing")
                    startPositionTracking()
                } else {
                    Log.d("MusicController", "🛑 Calling stopPositionTracking() - Audio not playing")
                    stopPositionTracking()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                _currentMediaItem.value = mediaItem

                Log.d("MusicController", "📱 Media item transition - reason: $reason")

                // Enhanced duration handling with delay and retry
                scope.launch {
                    // Wait a bit for the media to be fully loaded
                    delay(500)

                    val rawDuration = mediaController?.duration ?: C.TIME_UNSET
                    if (rawDuration != C.TIME_UNSET && rawDuration > 0) {
                        _duration.value = rawDuration
                        Log.d("MusicController", "📱 Duration from transition: ${rawDuration / 1000} seconds")
                    } else {
                        Log.d("MusicController", "📱 Duration not ready on transition, starting fetch...")
                        fetchAudioDurationWithRetry()
                    }
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                super.onPlayerError(error)
                Log.e("MusicController", "❌ Player error: ${error.message}")
                _isPreparing.value = false
                _isActuallyPlaying.value = false
                cancelAudioStartTimeout()
            }
        }

        mediaController?.addListener(playerListener!!)
        Log.d("MusicController", "✅ Player listener attached")
    }

    fun pauseAudio() {
        mediaController?.pause()
        Log.d("MusicController", "⏸️ Audio paused")
    }
    
    /**
     * ✅ Completely stop audio and clear all state (for stop button)
     */
    fun stopAudio() {
        Log.d("MusicController", "🛑 Completely stopping audio")

        // Stop the media player and clear media item
        mediaController?.let { controller ->
            controller.stop()
            controller.clearMediaItems() // This should dismiss notification
            Log.d("MusicController", "🧹 Media items cleared from player")
        }

        // Stop position tracking
        stopPositionTracking()

        // Clear all audio state
        clearAudioState()

        Log.d("MusicController", "✅ Audio completely stopped and state cleared")
    }

    fun resumeAudio() {
        mediaController?.play()
        Log.d("MusicController", "▶️ Audio resumed")

        // ✅ Start position tracking when resuming
        if (mediaController?.isPlaying == true) {
            Log.d("MusicController", "🚀 Starting position tracking from resume")
            startPositionTracking()
        }
    }

    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
                Log.d("MusicController", "⏸️ Toggled to pause")
                stopPositionTracking()
            } else {
                controller.play()
                Log.d("MusicController", "▶️ Toggled to play")

                // ✅ Start position tracking when toggling to play
                Log.d("MusicController", "🚀 Starting position tracking from toggle play")
                startPositionTracking()
            }
        }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)

        // Update audio state with new position
        _currentAudioItem.value?.let { currentState ->
            _currentAudioItem.value = currentState.copy(position = position)
        }
    }

    fun getCurrentPosition(): Long {
        return mediaController?.currentPosition ?: 0L
    }

    fun getDuration(): Long {
        val rawDuration = mediaController?.duration ?: C.TIME_UNSET
        return if (rawDuration != C.TIME_UNSET && rawDuration > 0) rawDuration else _duration.value
    }

    private fun startPositionTracking() {
        Log.d("MusicController", "🚀 startPositionTracking() called")
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            Log.d("MusicController", "🔄 Position tracking coroutine started")
            while (mediaController?.isPlaying == true) {
                val isPlayingCheck = mediaController?.isPlaying ?: false
                val currentPos = mediaController?.currentPosition ?: 0L
                val rawDuration = mediaController?.duration ?: C.TIME_UNSET

                Log.d("MusicController curr pos", "🔄 Position tracking coroutine started")
                // ✅ Enhanced duration handling - only log valid durations
                val validDuration = if (rawDuration != C.TIME_UNSET && rawDuration > 0) {
                    // Update duration if we get a valid one during tracking
                    if (_duration.value == 0L || _duration.value != rawDuration) {
                        _duration.value = rawDuration
                        Log.d("MusicController", "✅ Duration updated during tracking: ${rawDuration / 1000}s")
                    }
                    rawDuration
                } else {
                    _duration.value
                }

                // ✅ Only log when duration is valid to avoid spam
                if (validDuration > 0) {
                    Log.d("MusicController", "🔄 Position: $currentPos / $validDuration (${validDuration/1000}s)")
                } else {
                    Log.d("MusicController", "🔄 Position: $currentPos / Duration loading...")
                }

                _currentPosition.value = currentPos

                // Update audio state position
                _currentAudioItem.value?.let { currentState ->
                    _currentAudioItem.value = currentState.copy(position = currentPos)
                }

                delay(1000)
            }
            Log.d("MusicController", "⏹️ Position tracking loop ended - isPlaying: ${mediaController?.isPlaying}")
        }
    }

    private fun stopPositionTracking() {
        Log.d("MusicController", "🛑 stopPositionTracking() called")
        positionUpdateJob?.cancel()
        positionUpdateJob = null
        Log.d("MusicController", "✅ Position tracking stopped")
    }

    /**
     * Reset all audio states for new playback
     */
    private fun resetAudioStates() {
        audioStartTimeoutJob?.cancel()
        durationFetchJob?.cancel()
        _isPreparing.value = true
        _isReady.value = false
        _isPlaying.value = false
        _isActuallyPlaying.value = false
        _isAudioLoaded.value = false
        _duration.value = 0L
        _currentPosition.value = 0L
    }

    /**
     * Start timeout for audio start detection
     */
    private fun startAudioStartTimeout() {
        audioStartTimeoutJob?.cancel()
        audioStartTimeoutJob = scope.launch {
            delay(AUDIO_START_TIMEOUT)
            if (_isPreparing.value && !_isActuallyPlaying.value) {
                Log.e("MusicController", "❌ Audio start timeout - stopping loading state")
                _isPreparing.value = false
            }
        }
    }

    /**
     * Cancel audio start timeout
     */
    private fun cancelAudioStartTimeout() {
        audioStartTimeoutJob?.cancel()
    }

    /**
     * Start actual playback detection based on position changes
     */
    private fun startActualPlaybackDetection() {
        scope.launch {
            var previousPosition = 0L
            var stableCount = 0

            for (i in 1..50) { // Check for 5 seconds max
                delay(100)

                val currentPosition = mediaController?.currentPosition ?: 0L
                val isPlaying = mediaController?.isPlaying == true

                if (!isPlaying) {
                    Log.d("MusicController", "⏸️ Playback stopped during detection")
                    break
                }

                if (currentPosition > previousPosition && currentPosition > 0) {
                    // Position is moving - audio is actually playing!
                    Log.d("MusicController", "✅ Actual playback detected! Position: $currentPosition")
                    _isActuallyPlaying.value = true
                    _isPreparing.value = false
                    cancelAudioStartTimeout()

                    // ✅ Start position tracking when actual playback is detected
                    Log.d("MusicController", "🚀 Starting position tracking from actual playback detection")
                    startPositionTracking()
                    break
                } else if (currentPosition == previousPosition) {
                    stableCount++
                    if (stableCount > 30) { // 3 seconds of no progress
                        Log.w("MusicController", "⚠️ Position not moving after 3s - might be buffering")
                        break
                    }
                } else {
                    stableCount = 0
                }

                previousPosition = currentPosition
            }
        }
    }

    /**
     * Get playback state as string for logging
     */
    private fun getPlaybackStateString(state: Int): String {
        return when (state) {
            Player.STATE_IDLE -> "IDLE"
            Player.STATE_BUFFERING -> "BUFFERING"
            Player.STATE_READY -> "READY"
            Player.STATE_ENDED -> "ENDED"
            else -> "UNKNOWN($state)"
        }
    }

    /**
     * ✅ Set manual duration (for testing or when duration is known)
     */
    fun setManualDuration(minutes: Int, seconds: Int) {
        val totalSeconds = (minutes * 60) + seconds
        val durationInMs = totalSeconds * 1000L

        _duration.value = durationInMs

        Log.d("MusicController", "🕒 Manual duration set: ${minutes}:${String.format("%02d", seconds)} (${durationInMs}ms)")
    }

    /**
     * ✅ Set manual duration in milliseconds
     */
    fun setManualDurationMs(durationMs: Long) {
        _duration.value = durationMs

        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        Log.d("MusicController", "🕒 Manual duration set: ${minutes}:${String.format("%02d", seconds)} (${durationMs}ms)")
    }

    /**
     * ✅ Set manual duration in seconds
     */
    fun setManualDurationSeconds(totalSeconds: Long) {
        val durationMs = totalSeconds * 1000L
        setManualDurationMs(durationMs)
    }
    fun forceStartPositionTracking() {
        Log.d("MusicController", "🔧 Force starting position tracking...")
        if (mediaController?.isPlaying == true) {
            startPositionTracking()
            Log.d("MusicController", "✅ Position tracking force started")
        } else {
            Log.d("MusicController", "⚠️ Cannot start tracking - audio not playing")
        }
    }

    /**
     * Check if position tracking is active
     */
    fun isPositionTrackingActive(): Boolean {
        return positionUpdateJob?.isActive == true
    }
    fun clearAudioState() {
        Log.d("MusicController", "🧹 Clearing audio state")
        _currentAudioItem.value = null
        _isFromStory.value = false
        _shouldLoop.value = false
        _isPreparing.value = false
        _isReady.value = false
        _isPlaying.value = false
        _isActuallyPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
        durationFetchJob?.cancel()
        audioStartTimeoutJob?.cancel()
    }

    /**
     * ✅ NEW: Manually reset completion flag
     * Used for manual navigation or when completion state needs to be cleared
     */
    fun resetCompletionFlag() {
        _hasCompletedPlayback.value = false
        Log.d("MusicController", "🔄 Completion flag manually reset")
    }

    /**
     * Force reconnect to service (for debugging)
     */
    fun forceReconnect() {
        Log.d("MusicController", "🔄 Force reconnecting to service...")

        // Save current state
        val savedAudioState = _currentAudioItem.value

        // Release and reconnect
        controllerFuture?.let { MediaController.releaseFuture(it) }
        _isControllerReady.value = false

        // Reinitialize
        initializeController()

        // Restore state when ready
        if (savedAudioState != null) {
            scope.launch {
                _isControllerReady.collect { ready ->
                    if (ready) {
                        Log.d("MusicController", "🔄 Reconnected - restoring audio: ${savedAudioState.title}")
                        _currentAudioItem.value = savedAudioState
                        cancel()
                    }
                }
            }
        }
    }

    /**
     * Get audio duration with fallback methods
     */
    fun getAudioDurationSafely(): Long {
        // Try ExoPlayer first
        val exoPlayerDuration = mediaController?.duration ?: C.TIME_UNSET
        if (exoPlayerDuration != C.TIME_UNSET && exoPlayerDuration > 0) {
            return exoPlayerDuration
        }

        // Fallback to cached duration
        val cachedDuration = _duration.value
        if (cachedDuration > 0) {
            return cachedDuration
        }

        // If no duration available, try to fetch it
        fetchAudioDurationWithRetry()
        return 0L
    }

    /**
     * Check if audio is actually playing (enhanced detection)
     */
    fun isAudioReallyPlaying(): Boolean {
        val isPlayingState = _isPlaying.value
        val isActuallyPlayingState = _isActuallyPlaying.value
        val mediaControllerPlaying = mediaController?.isPlaying == true

        return isPlayingState && isActuallyPlayingState && mediaControllerPlaying
    }

    /**
     * Get comprehensive audio state for debugging
     */
    fun getAudioDebugInfo(): String {
        val controller = mediaController
        return buildString {
            appendLine("🎵 ========== AUDIO DEBUG INFO ==========")
            appendLine("Controller Ready: ${_isControllerReady.value}")
            appendLine("Is Preparing: ${_isPreparing.value}")
            appendLine("Is Ready: ${_isReady.value}")
            appendLine("Is Playing: ${_isPlaying.value}")
            appendLine("Is Actually Playing: ${_isActuallyPlaying.value}")
            appendLine("Current Position: ${_currentPosition.value / 1000}s")
            appendLine("Duration: ${_duration.value / 1000}s")
            appendLine("Volume: ${_volume.value}")
            appendLine("Should Loop: ${_shouldLoop.value}")
            appendLine("Is From Story: ${_isFromStory.value}")

            if (controller != null) {
                appendLine("--- ExoPlayer State ---")
                appendLine("Playback State: ${getPlaybackStateString(controller.playbackState)}")
                appendLine("Play When Ready: ${controller.playWhenReady}")
                appendLine("Is Playing: ${controller.isPlaying}")
                appendLine("Raw Duration: ${controller.duration}")
                appendLine("Current Position: ${controller.currentPosition}")
                appendLine("Repeat Mode: ${controller.repeatMode}")
            } else {
                appendLine("--- ExoPlayer State ---")
                appendLine("MediaController: NULL")
            }

            _currentAudioItem.value?.let { audio ->
                appendLine("--- Current Audio ---")
                appendLine("Title: ${audio.title}")
                appendLine("ID: ${audio.id}")
                appendLine("URL: ${audio.audioUrl}")
                appendLine("Is From Story: ${audio.isFromStory}")
            }

            appendLine("========================================")
        }
    }

    /**
     * Enhanced duration getter with multiple fallbacks
     */
    fun getDurationWithFallbacks(): Long {
        // 1. Try ExoPlayer duration
        val exoPlayerDuration = mediaController?.duration ?: C.TIME_UNSET
        if (exoPlayerDuration != C.TIME_UNSET && exoPlayerDuration > 0) {
            // Update cached duration if ExoPlayer has valid duration
            if (_duration.value != exoPlayerDuration) {
                _duration.value = exoPlayerDuration
                Log.d("MusicController", "🔄 Duration synchronized: ${exoPlayerDuration / 1000}s")
            }
            return exoPlayerDuration
        }

        // 2. Try cached duration
        val cachedDuration = _duration.value
        if (cachedDuration > 0) {
            return cachedDuration
        }

        // 3. Start background fetch if not already running
        if (durationFetchJob?.isActive != true) {
            Log.d("MusicController", "🕒 Starting background duration fetch...")
            fetchAudioDurationWithRetry()
        }

        return 0L
    }

    fun release() {
        Log.d("MusicController", "🧹 Releasing MusicController resources...")

        // Stop tracking jobs
        stopPositionTracking()
        durationFetchJob?.cancel()
        audioStartTimeoutJob?.cancel()

        // ✅ CRITICAL FIX: Remove listener before releasing
        playerListener?.let {
            mediaController?.removeListener(it)
            Log.d("MusicController", "🧹 Removed player listener")
        }
        playerListener = null

        // Cancel scope and release controller
        scope.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }

        Log.d("MusicController", "✅ MusicController resources released")
    }
}