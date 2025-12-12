package com.naptune.lullabyandstory.presentation.player.bottomsheet

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.presentation.player.AudioItem
import com.naptune.lullabyandstory.presentation.player.AudioPlayerIntent
import com.naptune.lullabyandstory.presentation.player.AudioPlayerScreenNew
import com.naptune.lullabyandstory.presentation.player.AudioPlayerViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.activity.ComponentActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AudioPlayerBottomSheetContent(
    state: AudioPlayerBottomSheetState,
    onbackClick : () -> Unit,
    viewModel: AudioPlayerViewModel = hiltViewModel(),
    globalAudioPlayerManager: GlobalAudioPlayerManager? = null
) {
    // Status bar handling moved to parent (NaptuneNavigation)
    
    val uiState by viewModel.uiState.collectAsState()
    val current = LocalContext.current
    val appPreferences = remember { AppPreferences(current) }
    var hasLoaded by rememberSaveable(state.audioId) { mutableStateOf(false) }
    
    // ✅ Register ViewModel with GlobalAudioPlayerManager on first composition
    LaunchedEffect(Unit) {
        globalAudioPlayerManager?.registerAudioPlayerViewModel(viewModel)
    }
    
    // ✅ Sync audio state with GlobalAudioPlayerManager
    LaunchedEffect(uiState.playerState.isPlaying, uiState.audioItem) {
        globalAudioPlayerManager?.updateAudioRunningState(uiState.playerState.isPlaying)
        globalAudioPlayerManager?.updateAudioPlayingState(uiState.playerState.isPlaying)
        
        // ✅ Update active audio state - true if audio is loaded (playing OR paused)
        val hasAudio = uiState.audioItem != null
        globalAudioPlayerManager?.updateActiveAudioState(hasAudio)
        
        Log.d("AudioPlayerBottomSheet", "🎵 State sync - Playing: ${uiState.playerState.isPlaying}, HasAudio: $hasAudio")
        
        // Update current audio info when audio item changes
        if (uiState.audioItem != null) {
            globalAudioPlayerManager?.updateCurrentAudioInfo(
                AudioInfo(
                    imagePath = uiState.audioItem!!.imagePath,
                    musicName = if (uiState.isFromStory) uiState.audioItem!!.storyName else uiState.audioItem!!.musicName,
                    isFromStory = uiState.isFromStory,
                    documentId = state.documentId
                )
            )
        }
    }

    fun handleReadStoryClick() {
        if (state.onNavigateToStoryReader != null && state.isFromStory) {
            val currentStory = viewModel.getCurrentStoryForReader()
            if (currentStory != null) {
                Log.d("AudioPlayerBottomSheet", "📖 Navigating to story reader: ${currentStory.storyName}")
                state.onNavigateToStoryReader.invoke(currentStory)
              //  state.onDismiss() // Close bottom sheet after navigation
            } else {
                Log.w("AudioPlayerBottomSheet", "⚠️ Cannot navigate - no current story")
            }
        }
    }

    // Load audio only once per audioId
    LaunchedEffect(state.audioId, hasLoaded) {
        if (!hasLoaded) {
            Log.d("AudioPlayerBottomSheet", "🚀 Loading audio: ${state.musicName}")
            hasLoaded = true
        } else {
            return@LaunchedEffect
        }

        viewModel.debugCurrentState()
        
        // Check favourite based on audio type
        viewModel.handleIntent(
            AudioPlayerIntent.CheckIfItemIsFavourite(
                documentId = state.documentId,
                isFromStory = state.isFromStory
            )
        )

        if (state.fromNotification) {
            Log.d("AudioPlayerBottomSheet", "🔔 From notification - restoring existing audio state")
            viewModel.setFromNotificationFlag(true)
        } else {
            Log.d("AudioPlayerBottomSheet", "🎵 Normal navigation - loading specific audio")
            viewModel.setFromNotificationFlag(false)

            // Create audio item with actual data
            val audioItem = AudioItem(
                id = state.audioId,
                musicName = if (!state.isFromStory) state.musicName else "",
                storyName = if (state.isFromStory) state.musicName else "",
                imagePath = state.imagePath.ifEmpty { "https://images.pexels.com/photos/1287145/pexels-photo-1287145.jpeg" },
                musicPath = if (!state.isFromStory) state.musicLocalPath ?: state.musicPath else state.musicPath,
                story_listen_time_in_millis = state.story_listen_time_in_millis
            )

            Log.d("AudioPlayerBottomSheet", "🎵 Audio item created: ${audioItem.musicName}${audioItem.storyName}")
            Log.d("AudioPlayerBottomSheet", "🎵 User selection: ${state.isUserSelection}")

            // ✅ Use LoadAudio with isUserSelection flag
            viewModel.handleIntent(
                AudioPlayerIntent.LoadAudio(
                    audioItem = audioItem,
                    isFromStory = state.isFromStory,
                    documentId = state.documentId,
                    isUserSelection = state.isUserSelection // ✅ Pass user selection flag
                )
            )

            // Load story list if this is a story
            if (state.isFromStory) {
                Log.d("AudioPlayerBottomSheet", "📚 Loading story list for navigation")
                viewModel.handleIntent(AudioPlayerIntent.LoadStoryList)
            }
        }
    }

    // Handle back button press
    val handleBackClick = {
        Log.d("AudioPlayerBottomSheet", "⬅️ Back button pressed")
        state.onDismiss()
    }

    // Show content based on state
    when {
        uiState.audioItem != null -> {
            // Audio loaded successfully - show player
            Log.d("AudioPlayerBottomSheet", "✅ Showing audio player UI")
            
            AudioPlayerScreenNew(
                item = uiState.audioItem!!,
                isFavourite = uiState.playerState.isFavourite,
                isFromStory = uiState.isFromStory,
                playerState = uiState.playerState,
                onBackClick = handleBackClick ,
                onPlayPause = {
                    Log.d("AudioPlayerBottomSheet", "▶️ Play/Pause clicked")
                    viewModel.handleIntent(AudioPlayerIntent.PlayPause)
                },
                onPrevious = {
                    if (uiState.isFromStory) {
                        viewModel.handleIntent(AudioPlayerIntent.NavigateToPreviousStory)
                    } else {
                        viewModel.handleIntent(AudioPlayerIntent.Previous)
                    }
                },
                onNext = {
                    if (uiState.isFromStory) {
                        viewModel.handleIntent(AudioPlayerIntent.NavigateToNextStory)
                    } else {
                        viewModel.handleIntent(AudioPlayerIntent.Next)
                    }
                },
                onFavouriteClick = {
                    viewModel.handleIntent(
                        AudioPlayerIntent.ToggleFavourite(
                            state.isFromStory,
                            state.documentId
                        )
                    )
                },
                onReadStory = {
                    handleReadStoryClick()
                },
                onTimerClick = {
                    viewModel.handleIntent(AudioPlayerIntent.OpenTimer)
                },
                onVolumeChange = { volume ->
                    viewModel.handleIntent(AudioPlayerIntent.VolumeChange(volume))
                },
                onSeek = { position ->
                    viewModel.handleIntent(AudioPlayerIntent.SeekTo(position))
                },
                appPreferences = appPreferences,
                storyNavigationState = uiState.storyNavigationState,
                onPreviousDisabledClick = {
                    android.widget.Toast.makeText(
                        current,
                        "This is the first story",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                },
                onNextDisabledClick = {
                    android.widget.Toast.makeText(
                        current,
                        "This is the last story",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                },
                onNavigateToStoryReader = { _ ->
                    handleReadStoryClick()
                }
            )
        }

        uiState.isLoading || viewModel.shouldShowLoading() -> {
            // Loading state
            LoadingAudioContent(onBackClick = handleBackClick)
        }

        else -> {
            // Error or empty state
            Log.d("AudioPlayerBottomSheet", "❌ Showing error state")
            ErrorAudioContent(
                error = uiState.error,
                onBackClick = handleBackClick,
                onRetry = {
                    val audioItem = AudioItem(
                        id = state.audioId,
                        musicName = if (!state.isFromStory) state.musicName else "",
                        storyName = if (state.isFromStory) state.musicName else "",
                        imagePath = state.imagePath.ifEmpty { "https://images.pexels.com/photos/1287145/pexels-photo-1287145.jpeg" },
                        musicPath = state.musicPath,
                        story_listen_time_in_millis = state.story_listen_time_in_millis
                    )
                    viewModel.handleIntent(AudioPlayerIntent.LoadAudio(audioItem, state.isFromStory, state.documentId))
                }
            )
        }
    }
}

@Composable
private fun LoadingAudioContent(
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.audio_loading),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Back button in top-left corner
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ErrorAudioContent(
    error: String?,
    onBackClick: () -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.audio_not_available),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    Text("Go Back")
                }

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Retry")
                }
            }
        }

        // Back button in top-left corner
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}