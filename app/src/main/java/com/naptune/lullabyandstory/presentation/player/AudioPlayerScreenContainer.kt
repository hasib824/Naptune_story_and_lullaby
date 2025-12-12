package com.naptune.lullabyandstory.presentation.player

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.presentation.navigation.Screen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AudioPlayerScreenContainer(
    navController: NavController,
    audioId: String,
    isFromStory: Boolean,
    musicPath: String,
    musicName: String,
    imagePath: String,
    fromNotification: Boolean = false,
    viewModel: AudioPlayerViewModel = hiltViewModel(),
    documentId: String,
    musicLocalPath: String,
    // ✅ NEW: Callback function for story reader navigation
    onNavigateToStoryReader: ((StoryDomainModel) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    val current = LocalContext.current

    val appPreferences = remember { AppPreferences(current) }

    var visible by remember { mutableStateOf(true) }

    var hasLoaded by rememberSaveable { mutableStateOf(false) }


    fun handleReadStoryClick() {
        if (onNavigateToStoryReader != null && isFromStory) {
            val currentStory = viewModel.getCurrentStoryForReader()
            if (currentStory != null) {
                Log.d("AudioPlayerScreenContainer", "📖 Navigating to story reader: ${currentStory.storyName}")
                onNavigateToStoryReader(currentStory)
            } else {
                Log.w("AudioPlayerScreenContainer", "⚠️ Cannot navigate - no current story")
            }
        }
    }

    // ✅ Enhanced audio loading with notification state preservation
    // ✅ NEW: Track if initial load is complete to prevent override on back navigation


   // val isBackNavigation = navController.previousBackStackEntry != null

    // Load audio only if necessary
    LaunchedEffect(hasLoaded) {
      //  Log.d("AudioPlayerScreenContainer", "🚀 LaunchedEffect triggered - audioId: $audioId, fromNotification: $fromNotification, isBackNavigation: $isBackNavigation")

        // Skip initialization if back navigation and audio is already loaded
      /*  if (isBackNavigation && (uiState.audioItem != null || viewModel.hasAudioToDisplay())) {
            Log.d("AudioPlayerScreenContainer", "⬅️ Back navigation detected - skipping initialization, current audio: ${uiState.audioItem?.storyName ?: uiState.audioItem?.musicName}")
            viewModel.refreshFromController() // Ensure UI syncs with MusicController
            return@LaunchedEffect
        }*/
        // ✅ Skip loading if we already have current audio and this is just a recomposition
        if (!hasLoaded) {
            println("🔄 প্রথমবার লোড হচ্ছে...")
            // API / Database call এখানে করো
            hasLoaded = true
        }
        else{
            return@LaunchedEffect
        }

        viewModel.debugCurrentState()
        // ✅ Check favourite based on audio type
        viewModel.handleIntent(
            AudioPlayerIntent.CheckIfItemIsFavourite(
                documentId = documentId,
                isFromStory = isFromStory
            )
        )

        if (fromNotification) {
            // ✅ Coming from notification - restore existing audio first but allow new audio selection
            Log.d("AudioPlayerScreenContainer", "🔔 From notification - restoring existing audio state")

            // Mark that we came from notification so ViewModel can handle new audio properly
            viewModel.setFromNotificationFlag(true)

            // Don't load new audio immediately, let ViewModel check for existing audio
            // The ViewModel's checkForExistingAudio() will handle this in init

        } else {
            // ✅ Normal navigation - load the specific audio
            Log.d("AudioPlayerScreenContainer", "🎵 Normal navigation - loading specific audio")

            // Clear notification flag for normal navigation
            viewModel.setFromNotificationFlag(false)

            // Create audio item with actual data from navigation
            val audioItem = AudioItem(
                id = audioId,
                musicName = if (!isFromStory) musicName else "",
                storyName = if (isFromStory) musicName else "", // Use musicName as story name for stories
                imagePath = imagePath.ifEmpty { "https://images.pexels.com/photos/1287145/pexels-photo-1287145.jpeg" }, // Fallback image
                musicPath =  if (!isFromStory) musicLocalPath else musicPath, // This is the actual audio file URL or local path
                story_listen_time_in_millis = 0L
            )

            Log.d("AudioPlayerScreenContainer", "🎵 Audio item created: ${audioItem.musicName}${audioItem.storyName}")

            // ✅ Use ForceLoadAudio for normal navigation to ensure it always loads
            viewModel.handleIntent(AudioPlayerIntent.ForceLoadAudio(audioItem, isFromStory, documentId))

            // ✅ NEW: Load story list if this is a story
            if (isFromStory) {
                Log.d("AudioPlayerScreenContainer", "📚 Loading story list for navigation")
                viewModel.handleIntent(AudioPlayerIntent.LoadStoryList)
            }
        }

        // ✅ Mark that initial load is complete
    }

    // ✅ Enhanced back navigation handler - goes to Main screen
    val handleBackClick = {
        Log.d("AudioPlayerScreenContainer", "⬅️ Back button pressed")

        // ✅ Always navigate back to main screen instead of previous screen
        // This ensures consistent navigation behavior from notification
       navController.navigate(Screen.Main.route) {
            popUpTo(Screen.Main.route) {
                inclusive = false
            }
        }

    }

    // ✅ Show content based on state
    when {
        uiState.audioItem != null  -> {
            // ✅ Audio loaded successfully - show player

            Log.e("AudioPlayerViewModel Screen","Here I come");

                AudioPlayerScreenNew(
                    item = uiState.audioItem!!,
                    isFavourite = uiState.playerState.isFavourite,
                    isFromStory = uiState.isFromStory,
                    playerState = uiState.playerState,
                    onBackClick = handleBackClick,
                    onPlayPause = {
                        Log.e("AudioPlayerScreenContainer", "On Play Pause")
                        viewModel.handleIntent(AudioPlayerIntent.PlayPause)
                    },
                    onPrevious = {
                        // ✅ NEW: Use story navigation for stories, fallback to current behavior for lullabies
                        if (uiState.isFromStory) {
                            viewModel.handleIntent(AudioPlayerIntent.NavigateToPreviousStory)
                        } else {
                            viewModel.handleIntent(AudioPlayerIntent.Previous)
                        }
                    },
                    onNext = {
                        // ✅ NEW: Use story navigation for stories, fallback to current behavior for lullabies
                        if (uiState.isFromStory) {
                            viewModel.handleIntent(AudioPlayerIntent.NavigateToNextStory)
                        } else {
                            viewModel.handleIntent(AudioPlayerIntent.Next)
                        }
                    },
                    onFavouriteClick = {
                        viewModel.handleIntent(
                            AudioPlayerIntent.ToggleFavourite(
                                isFromStory,
                                documentId
                            )
                        )
                    },
                    onReadStory = {
                        // ✅ NEW: Use callback function for navigation
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
                    // ✅ NEW: Pass story navigation state
                    storyNavigationState = uiState.storyNavigationState,
                    // ✅ NEW: Toast handlers for disabled buttons
                    onPreviousDisabledClick = {
                        // Show toast for disabled previous button
                        android.widget.Toast.makeText(
                            navController.context,
                            navController.context.getString(R.string.story_first_message),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onNextDisabledClick = {
                        // Show toast for disabled next button
                        android.widget.Toast.makeText(
                            navController.context,
                            navController.context.getString(R.string.story_last_message),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    // ✅ NEW: Pass the callback function instead of ViewModel intent
                    onNavigateToStoryReader = { _ ->
                        // This parameter is for compatibility but we use the local function
                        handleReadStoryClick()
                    }
                )
          /*  }*/


        }

        uiState.isLoading || viewModel.shouldShowLoading() -> {
            // ✅ Loading state
            LoadingAudioScreen(onBackClick = handleBackClick)
        }


        else -> {
            // ✅ Error or empty state

            Log.e("AudioPlayerViewModel Err", " abra ka dabra");
            ErrorAudioScreen(
                error = uiState.error,
                onBackClick = handleBackClick,
                onRetry = {
                    // Retry loading the audio
                    val audioItem = AudioItem(
                        id = audioId,
                        musicName = if (!isFromStory) musicName else "",
                        storyName = if (isFromStory) musicName else "",
                        imagePath = imagePath.ifEmpty { "https://images.pexels.com/photos/1287145/pexels-photo-1287145.jpeg" },
                        musicPath = musicPath,
                        story_listen_time_in_millis = 0L
                    )
                    viewModel.handleIntent(AudioPlayerIntent.LoadAudio(audioItem, isFromStory, documentId))
                }
            )
        }
    }
}

@Composable
private fun LoadingAudioScreen(
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
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

        // ✅ Back button in top-left corner
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.content_desc_back),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ErrorAudioScreen(
    error: String?,
    onBackClick: () -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
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

        // ✅ Back button in top-left corner
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.content_desc_back),
                tint = Color.White
            )
        }
    }
}



@Composable
fun TimerModal(
    visible: Boolean,
    onClose: () -> Unit,
    onTimerSet: (Int) -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onClose,
            title = { Text("Set Sleep Timer") },
            text = {
                Column {
                    Text("Choose when to stop playback:")
                    Spacer(modifier = Modifier.height(16.dp))

                    val timerOptions = listOf(15, 30, 45, 60, 90)
                    timerOptions.forEach { minutes ->
                        TextButton(
                            onClick = {
                                onTimerSet(minutes)
                                onClose()
                            }
                        ) {
                            Text("$minutes minutes")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onClose) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Repeat Modal Composable
@Composable
fun RepeatModal(
    visible: Boolean,
    onClose: () -> Unit,
    repeatTime: Int,
    onRepeatTimeSet: (Int) -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onClose,
            title = { Text("Repeat Settings") },
            text = {
                Column {
                    Text("How many times should this repeat?")
                    Spacer(modifier = Modifier.height(16.dp))

                    val repeatOptions = listOf(1, 2, 3, 5, 10)
                    repeatOptions.forEach { times ->
                        TextButton(
                            onClick = {
                                onRepeatTimeSet(times)
                                onClose()
                            }
                        ) {
                            Text("$times times")
                        }
                    }

                    TextButton(
                        onClick = {
                            onRepeatTimeSet(-1) // Infinite
                            onClose()
                        }
                    ) {
                        Text("Infinite")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onClose) {
                    Text("Cancel")
                }
            }
        )
    }
}
