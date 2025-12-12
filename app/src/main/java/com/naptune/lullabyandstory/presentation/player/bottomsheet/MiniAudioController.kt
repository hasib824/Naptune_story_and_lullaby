package com.naptune.lullabyandstory.presentation.player.bottomsheet

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.presentation.components.common.responsive.getAudioManagerScreenResponisiveSizes
import com.naptune.lullabyandstory.presentation.components.common.responsive.rememberScreenDimensionManager
import com.naptune.lullabyandstory.ui.theme.AccentColor
import com.naptune.lullabyandstory.ui.theme.AccentColorGradientLeft
import com.naptune.lullabyandstory.ui.theme.AccentColorGradientRight
import com.naptune.lullabyandstory.ui.theme.SecondaryColor

// ✅ PERFORMANCE FIX: Pre-compute shadow colors to avoid allocation on every frame
private val MiniControllerShadowColor = Color.Black.copy(alpha = 0.3f)
private val PlayPauseButtonShadowColor = Color.Black.copy(alpha = 0.80f)

@Composable
fun MiniAudioController(
    audioInfo: AudioInfo,
    isVisible: Boolean,
    isPlaying: Boolean,
    bottomOffset: Dp = 0.dp,
    onControllerClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val screenDimensionManager = rememberScreenDimensionManager()
    val responsiveSizes = getAudioManagerScreenResponisiveSizes(screenDimensionManager)

    // ✅ PERFORMANCE FIX: AnimatedVisibility with graphicsLayer for GPU acceleration (Issue #18)
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250)
        ) + fadeOut(animationSpec = tween(250)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(responsiveSizes.miniAudioControllerSize)
                .padding(horizontal = 20.dp)
                .offset(y = -bottomOffset)
                .graphicsLayer {} // ✅ Hint for GPU layer creation
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = MiniControllerShadowColor // ✅ Use pre-computed color
                )
                .zIndex(1000f), // Ensure it's above everything
            shape = RoundedCornerShape(12.dp),
           /* border = BorderStroke(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        SelectedItemBorderColorBottom,
                        SelectedItemBorderColorTop
                    ) // Green → Blue
                )
            ),*/
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent // Transparent to show gradient below
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onControllerClick() }
                    .background(
                        SecondaryColor.copy(alpha = 0.9f)
                       /* brush = Brush.horizontalGradient(
                            colors = listOf(
                                AccentColorGradientLeft.copy(alpha = 0.9f),
                                AccentColorGradientRight.copy(alpha = 0.9f)
                            )
                        )*/
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ✅ NEW: Clickable area for opening AudioPlayerScreenNew (excluding buttons)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        ,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Audio Image
                    AsyncImage(
                    model = audioInfo.imagePath.ifEmpty { "https://images.pexels.com/photos/1287145/pexels-photo-1287145.jpeg" },
                    contentDescription = "Audio Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
                )

                // Center: Audio Title and Category
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = audioInfo.musicName,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = if (audioInfo.isFromStory) "Story" else "Lullaby",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal
                    )
                }
                } // ✅ Close clickable Row

                // ✅ Buttons Row (not clickable for opening screen)
                Row(modifier = Modifier.wrapContentSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically)
                {


                Box(
                    Modifier
                        .width(28.dp)
                        .height(28.dp)
                        .shadow(
                            elevation = 3.dp,                // blur ≈ 4
                            shape = CircleShape,
                            clip = true,                    // keep shadow outside
                            ambientColor = PlayPauseButtonShadowColor, // ✅ Use pre-computed color
                            spotColor = PlayPauseButtonShadowColor // ✅ Use pre-computed color
                        )
                        .clip(CircleShape)
                        .background(Color.Transparent, CircleShape)
                        .clickable(onClick = onPlayPauseClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                        contentDescription = if (isPlaying) "Pause Audio" else "Play Audio",
                        tint = Color.Unspecified,
                    )
                }

                // Right: Stop Button



                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(Color.Transparent, CircleShape)
                        .clickable(onClick = onStopClick)
                        .padding(0.dp)

                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mini_controller_stop),
                        contentDescription = "Stop Audio",
                        tint = Color.Unspecified,
                        modifier = modifier.wrapContentSize()
                    )
                }
                }
            }
        }
    }
}

@Composable
fun MiniAudioControllerContainer(
    globalAudioPlayerManager: GlobalAudioPlayerManager,
    isBottomSheetVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val isAudioRunning by globalAudioPlayerManager.isAudioRunning.collectAsState()
    val isAudioPlaying by globalAudioPlayerManager.isAudioPlaying.collectAsState()
    val hasActiveAudio by globalAudioPlayerManager.hasActiveAudio.collectAsState()
    val currentAudioInfo by globalAudioPlayerManager.currentAudioInfo.collectAsState()

    // ✅ OPTIMISTIC UI: Local state for immediate hiding on stop click
    var isStoppingLocally by remember { mutableStateOf(false) }

    // ✅ Reset local stopping state when audio changes (new audio loaded)
    LaunchedEffect(currentAudioInfo?.documentId) {
        if (currentAudioInfo != null) {
            isStoppingLocally = false
            Log.d("MiniController", "🔄 Reset local stopping state for new audio: ${currentAudioInfo!!.musicName}")
        }
    }

    // Calculate dynamic offset based on bottom sheet visibility
    val bottomOffset by animateDpAsState(
        targetValue = if (isBottomSheetVisible) 66.dp else 0.dp,
        animationSpec = tween(260),
        label = "controller_offset"
    )

    // ✅ FIXED: Show controller when audio is ACTIVE (playing OR paused) but bottom sheet is hidden
    // AND not locally stopping (for immediate feedback)
    val shouldShow = hasActiveAudio && !isBottomSheetVisible && currentAudioInfo != null && !isStoppingLocally

    // ✅ Debug logging
    LaunchedEffect(hasActiveAudio, isBottomSheetVisible, currentAudioInfo, isStoppingLocally) {
        Log.d("MiniController", "🎵 Visibility - HasActive: $hasActiveAudio, SheetVisible: $isBottomSheetVisible, HasInfo: ${currentAudioInfo != null}, LocalStopping: $isStoppingLocally, ShouldShow: $shouldShow")
    }

    if (currentAudioInfo != null) {
        MiniAudioController(
            audioInfo = currentAudioInfo!!,
            isVisible = shouldShow,
            isPlaying = isAudioPlaying,
            bottomOffset = bottomOffset,
            onControllerClick = {
                // ✅ Show existing audio player without reloading
                globalAudioPlayerManager.showExistingAudioPlayer()
            },
            onPlayPauseClick = {
                // ✅ Toggle play/pause using same pattern as stop
                globalAudioPlayerManager.togglePlayPause()
            },
            onStopClick = {
                // ✅ OPTIMISTIC UI: Immediately hide controller for instant feedback
                Log.d("MiniController", "🛑 Stop clicked - hiding immediately (optimistic UI)")
                isStoppingLocally = true

                // ✅ Then stop audio in background (state updates will follow)
                globalAudioPlayerManager.stopAudio()
            },
            modifier = modifier
        )
    }
}