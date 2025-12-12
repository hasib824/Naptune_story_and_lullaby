package com.naptune.lullabyandstory.presentation.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.toSize

@Composable
fun DraggableAudioPlayer() {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val collapsedHeight = 80.dp
    val expandedHeight = screenHeight
    val imageSizeCollapsed = 60.dp
    val imageSizeExpanded = 250.dp

    val heightPx = with(LocalDensity.current) { expandedHeight.toPx() }
    val collapsedPx = with(LocalDensity.current) { collapsedHeight.toPx() }

    val current = LocalDensity.current

    val playerHeight = remember { Animatable(heightPx) }
    val imageSize = remember { Animatable(with(current) { imageSizeExpanded.toPx() }) }
    val imageCornerRadius = remember { Animatable(0f) } // can animate from 0 to round when collapsed

    val coroutineScope = rememberCoroutineScope()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                coroutineScope.launch {
                    playerHeight.snapTo((playerHeight.value - delta).coerceIn(collapsedPx, heightPx))
                    // Scale image based on height
                    val progress = (playerHeight.value - collapsedPx) / (heightPx - collapsedPx)
                    imageSize.snapTo(
                        lerp(imageSizeCollapsed, imageSizeExpanded, progress).value
                    )
                    imageCornerRadius.snapTo(lerp(20.dp, 0.dp, progress).value)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // Snap to nearest state
                val middle = (heightPx + collapsedPx) / 2
                if (playerHeight.value > middle) {
                    playerHeight.animateTo(heightPx, tween(300))
                    imageSize.animateTo(imageSizeExpanded.value, tween(300))
                    imageCornerRadius.animateTo(0f, tween(300))
                } else {
                    playerHeight.animateTo(collapsedPx, tween(300))
                    imageSize.animateTo(imageSizeCollapsed.value, tween(300))
                    imageCornerRadius.animateTo(20.dp.value, tween(300))
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .nestedScroll(nestedScrollConnection)
            .height(with(LocalDensity.current) { playerHeight.value.toDp() })
            .background(Color.DarkGray)
    ) {
        // Background Image
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .size(with(LocalDensity.current) { imageSize.value.toDp() })
                .clip(RoundedCornerShape(with(LocalDensity.current) { imageCornerRadius.value.toDp() }))
                .background(Color.Red)
        )

        // Controls (Position changes with progress)
        Row(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(with(LocalDensity.current) { imageSize.value.toDp() })
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Red)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Song Name",
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {}) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Clear, contentDescription = null, tint = Color.White)
            }
        }
    }
}

// Linear interpolation for Dp
fun lerp(start: Dp, stop: Dp, fraction: Float): Dp {
    return start + (stop - start) * fraction
}
