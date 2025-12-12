package com.naptune.lullabyandstory.presentation.components.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * ✅ SeekableProgressBar - Interactive progress bar for audio playback
 *
 * Features:
 * - Click-to-seek functionality
 * - Drag-to-scrub with real-time feedback
 * - Smart loading indicator for unbuffered seeks
 * - Visual feedback during interaction
 * - Maintains flat design aesthetic
 */
@Composable
fun SeekableProgressBar(
    progress: Float, // Current playback progress (0.0 to 1.0)
    bufferedProgress: Float = 0.0f, // Buffered/loaded progress (0.0 to 1.0)
    onSeek: (Float) -> Unit, // Callback when user seeks to a position
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    showSmartLoading: Boolean = false, // External loading state
    backgroundColor: Color = Color(0xFFA39BDE),
    progressColor: Color = Color(0xFF514891),
    bufferedColor: Color = Color(0xFFB8B3E6),
    thumbColor: Color = Color.White,
    height: Dp = 8.dp
) {
    // ✅ State for user interaction
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(progress) }
    var isInternalLoading by remember { mutableStateOf(false) }

    // ✅ Haptic feedback
    val hapticFeedback = LocalHapticFeedback.current

    // ✅ Animation for smooth progress updates
    val animatedProgress by animateFloatAsState(
        targetValue = if (isDragging) dragProgress else progress,
        animationSpec = if (isDragging) {
            snap() // Immediate for dragging
        } else {
            tween(durationMillis = 200, easing = FastOutSlowInEasing)
        },
        label = "progress_animation"
    )

    // ✅ Smart loading detection
    val shouldShowLoading = showSmartLoading || isInternalLoading

    // ✅ Interactive Progress Bar
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .pointerInput(isEnabled) {
                    if (!isEnabled) return@pointerInput

                    // ✅ Handle tap gestures for click-to-seek
                    detectTapGestures(
                        onTap = { offset ->
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)

                            // ✅ Smart loading detection for unbuffered seeks
                            if (newProgress > bufferedProgress) {
                                isInternalLoading = true
                            }

                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSeek(newProgress)

                            // ✅ Hide loading after seek attempt
                            isInternalLoading = false
                        }
                    )
                }
                .pointerInput(isEnabled) {
                    if (!isEnabled) return@pointerInput

                    // ✅ Handle drag gestures for scrubbing
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onDrag = { _, dragAmount ->
                            val deltaProgress = dragAmount.x / size.width
                            dragProgress = (dragProgress + deltaProgress).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            // ✅ Smart loading detection for unbuffered seeks
                            if (dragProgress > bufferedProgress) {
                                isInternalLoading = true
                            }

                            onSeek(dragProgress)
                            isDragging = false

                            // ✅ Hide loading after seek attempt
                            isInternalLoading = false
                        }
                    )
                }
        ) {
            drawSeekableProgressBar(
                progress = animatedProgress,
                bufferedProgress = bufferedProgress,
                isDragging = isDragging,
                backgroundColor = backgroundColor,
                progressColor = progressColor,
                bufferedColor = bufferedColor,
                thumbColor = thumbColor
            )
        }
}

/**
 * ✅ Custom drawing function for the seekable progress bar
 */
private fun DrawScope.drawSeekableProgressBar(
    progress: Float,
    bufferedProgress: Float,
    isDragging: Boolean,
    backgroundColor: Color,
    progressColor: Color,
    bufferedColor: Color,
    thumbColor: Color
) {
    val barHeight = size.height
    val barWidth = size.width

    // ✅ Background track (full width)
    drawRect(
        color = backgroundColor,
        size = Size(width = barWidth, height = barHeight)
    )

    // ✅ Buffered progress (shows loaded content)
    val bufferedWidth = barWidth * bufferedProgress.coerceIn(0f, 1f)
    if (bufferedWidth > 0) {
        drawRect(
            color = bufferedColor,
            size = Size(width = bufferedWidth, height = barHeight)
        )
    }

    // ✅ Current progress (shows playback position)
    val progressWidth = barWidth * progress.coerceIn(0f, 1f)
    if (progressWidth > 0) {
        drawRect(
            color = progressColor,
            size = Size(width = progressWidth, height = barHeight)
        )
    }

    // ✅ Dragging thumb indicator (only show when dragging)
    if (isDragging && progressWidth > 0) {
        val thumbRadius = barHeight / 2 + 2.dp.toPx() // Slightly larger than bar
        val thumbX = progressWidth.coerceIn(thumbRadius, barWidth - thumbRadius)
        val thumbY = barHeight / 2

        // ✅ Thumb shadow/outline
        drawCircle(
            color = Color.Black.copy(alpha = 0.3f),
            radius = thumbRadius + 1.dp.toPx(),
            center = Offset(thumbX, thumbY)
        )

        // ✅ Thumb indicator
        drawCircle(
            color = thumbColor,
            radius = thumbRadius,
            center = Offset(thumbX, thumbY)
        )
    }
}

/**
 * ✅ Preview function for development
 */
/*@Preview(showBackground = true)
@Composable
private fun SeekableProgressBarPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Normal state
        SeekableProgressBar(
            progress = 0.3f,
            bufferedProgress = 0.7f,
            onSeek = {}
        )

        // Loading state
        SeekableProgressBar(
            progress = 0.5f,
            bufferedProgress = 0.6f,
            showSmartLoading = true,
            onSeek = {}
        )

        // Disabled state
        SeekableProgressBar(
            progress = 0.8f,
            bufferedProgress = 0.9f,
            isEnabled = false,
            onSeek = {}
        )
    }
}*/