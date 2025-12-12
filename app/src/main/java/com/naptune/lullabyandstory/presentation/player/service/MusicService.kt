package com.naptune.lullabyandstory.presentation.player.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.naptune.lullabyandstory.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.UnstableApi
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var musicController: MusicController

    private var mediaSession: MediaSession? = null
    private var servicePlayerListener: Player.Listener? = null // ✅ Store listener for proper cleanup

    /**
     * ✅ Manage foreground service state based on playback
     * Keeps notification visible even when paused
     */
    private fun updateForegroundState(isPlaying: Boolean) {
        val player = mediaSession?.player

        if (player != null && player.currentMediaItem != null) {
            // ✅ Audio loaded - keep as foreground service (even if paused)
            Log.d("MusicService", "⬆️ Moving to foreground (Playing: $isPlaying)")
            // MediaSessionService automatically handles foreground notification
            // We just need to ensure we don't call stopForeground()
        } else {
            // ✅ No audio - can stop foreground
            Log.d("MusicService", "⬇️ No audio - allowing background")
        }
    }

    @OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        Log.d("MusicService", "🚀 MusicService onCreate - Starting service...")

        // ✅ Create PendingIntent to open MainActivity with specific audio player action
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                // ✅ Add action to indicate we want to go to audio player
                action = "OPEN_AUDIO_PLAYER"
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // ✅ Create and store listener for proper cleanup
        servicePlayerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d("MusicService", "🎵 Player state changed - Playing: $isPlaying")

                // ✅ Check completion when user clicks play (from notification)
                if (isPlaying) {
                    val hasCompleted = musicController.hasCompletedPlayback.value
                    val isFromStory = musicController.currentAudioItem.value?.isFromStory == true
                    val currentAudio = musicController.currentAudioItem.value

                    Log.d("MusicService", "⏯️ Play detected - Completed: $hasCompleted, Story: $isFromStory")

                    if (hasCompleted && isFromStory && currentAudio != null) {
                        // ✅ Last story completed - force reload from beginning
                        Log.d("MusicService", "🔄 Last story completed - force reloading from notification")

                        // Pause player first
                        exoPlayer.pause()

                        // Reset completion flag
                        musicController.resetCompletionFlag()

                        // Force reload audio from beginning
                        musicController.stopAudio()
                        musicController.playAudioWithSourceAwareness(
                            audioUrl = currentAudio.audioUrl,
                            title = currentAudio.title,
                            artist = "Neptune",
                            imageUrl = currentAudio.imageUrl,
                            isFromStory = currentAudio.isFromStory,
                            audioId = currentAudio.id,
                            documentId = currentAudio.documentId,
                            story_listen_time_in_millis = currentAudio.story_listen_time_in_millis
                        )

                        Log.d("MusicService", "✅ Story force reloaded from notification")
                        return // Don't call updateForegroundState yet
                    }
                }

                updateForegroundState(isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d("MusicService", "🎵 Playback state: $playbackState")
                // Keep foreground when audio is loaded (ready/buffering/playing/paused)
                if (playbackState != Player.STATE_IDLE) {
                    updateForegroundState(exoPlayer.isPlaying)
                }
            }
        }

        exoPlayer.addListener(servicePlayerListener!!)
        Log.d("MusicService", "✅ Service player listener attached")

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(object : MediaSession.Callback {
                @UnstableApi
                override fun onPlaybackResumption(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {

                    Log.d("MusicService", "▶️ Playback Resumption Requested by ${controller.packageName}")

                    // ✅ Check completion state before playing
                    val hasCompleted = musicController.hasCompletedPlayback.value
                    val isFromStory = musicController.currentAudioItem.value?.isFromStory == true
                    val currentAudio = musicController.currentAudioItem.value

                    Log.d("MusicService", "⏯️ Notification State - Completed: $hasCompleted, Story: $isFromStory")

                    if (hasCompleted && isFromStory && currentAudio != null) {
                        // ✅ Last story completed - force reload from beginning
                        Log.d("force MusicService", "🔄 Last story completed - force reloading from notification")

                        // Reset completion flag
                        musicController.resetCompletionFlag()

                        // Force reload audio from beginning
                        musicController.stopAudio()
                        musicController.playAudioWithSourceAwareness(
                            audioUrl = currentAudio.audioUrl,
                            title = currentAudio.title,
                            artist = "Neptune",
                            imageUrl = currentAudio.imageUrl,
                            isFromStory = currentAudio.isFromStory,
                            audioId = currentAudio.id,
                            documentId = currentAudio.documentId,
                            story_listen_time_in_millis = currentAudio.story_listen_time_in_millis
                        )

                        Log.d("MusicService", "✅ Story force reloaded from notification")
                    } else {
                        // ✅ Normal play
                        Log.d("MusicService", "⏯️ Normal play from notification")
                        exoPlayer.play()
                    }

                    // Return current media item
                    val currentMediaItem = exoPlayer.currentMediaItem
                    val mediaItems = listOfNotNull(currentMediaItem)

                    return Futures.immediateFuture(
                        MediaSession.MediaItemsWithStartPosition(
                            mediaItems,
                            /* startIndex = */ 0,
                            /* startPositionMs = */ exoPlayer.currentPosition
                        )
                    )
                }

                // ✅ Override to customize notification behavior and remove previous button
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    Log.d("MusicService", "🔗 Controller connected: ${controller.packageName}")
                    
                    // ✅ Create custom player commands without previous button
                    val customPlayerCommands = Player.Commands.Builder()
                        .addAll(Player.Commands.Builder().addAllCommands().build())
                        .remove(Player.COMMAND_SEEK_TO_PREVIOUS) // ✅ Remove previous button
                        .build()
                    
                    return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                        .setAvailablePlayerCommands(customPlayerCommands) // Use player commands instead
                        .build()
                }
            })
            .build()

        Log.d("MusicService", "✅ MediaSession created successfully")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.d("MusicService", "🎯 onGetSession called for: ${controllerInfo.packageName}")
        return mediaSession
    }
    
    // ✅ Handle notification dismissal attempts - keep service alive if audio exists
    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d("MusicService", "📱 Task removed - checking audio state...")

        val player = mediaSession?.player

        // ✅ Keep service alive if there's ANY audio loaded, regardless of play state
        if (player != null && player.currentMediaItem != null) {
            Log.d("MusicService", "🎵 Audio exists (playing: ${player.isPlaying}) - keeping service alive")
            Log.d("MusicService", "🎵 Current position: ${player.currentPosition}ms, Duration: ${player.duration}ms")

            // ✅ CRITICAL: Don't call super.onTaskRemoved() to prevent service stop
            // This keeps the service alive even when task is removed multiple times
            // super.onTaskRemoved(rootIntent) // ❌ Removed - causes service to stop

            // ✅ Notification will persist because MediaSession is still active
        } else {
            Log.d("MusicService", "⏹️ No audio loaded - allowing service to stop")
            super.onTaskRemoved(rootIntent) // ✅ Only call super when no audio
            stopSelf()
        }
    }

    override fun onDestroy() {
        Log.d("MusicService", "🧹 MusicService onDestroy")

        // ✅ CRITICAL FIX: Remove listener before releasing player
        servicePlayerListener?.let {
            exoPlayer.removeListener(it)
            Log.d("MusicService", "🧹 Removed service player listener")
        }
        servicePlayerListener = null

        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }

        Log.d("MusicService", "✅ MusicService destroyed")
        super.onDestroy()
    }
}
