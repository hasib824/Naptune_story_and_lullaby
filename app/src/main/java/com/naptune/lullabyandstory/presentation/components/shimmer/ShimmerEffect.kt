package com.naptune.lullabyandstory.presentation.components.shimmer

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Shimmer effect colors matching the app's dark theme
 * Very subtle shimmer for loading skeleton screens
 */
object ShimmerColors {
    val shimmerBase = Color(0xFF2A3648) // Slightly lighter than PrimaryColor
    val shimmerHighlight = Color(0xFF3D4A5E) // Subtle highlight
    val shimmerEnd = Color(0xFF2A3648) // Same as base for smooth loop
}

/**
 * Creates a subtle shimmer effect that animates from left to right
 *
 * @param shimmerSpeed Animation duration in milliseconds (default: 800ms)
 * @return Modifier with animated shimmer background
 */
@Composable
fun Modifier.shimmerEffect(
    shimmerSpeed: Int = 800
): Modifier {
    // Infinite transition for continuous shimmer animation
    val transition = rememberInfiniteTransition(label = "shimmer")

    // Animate the shimmer position from 0f to 1000f
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = shimmerSpeed,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    // ✅ PERFORMANCE FIX: Use drawWithCache to avoid gradient allocation every frame (Issue #20)
    return this.drawWithCache {
        onDrawBehind {
            val offset = translateAnim.value
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        ShimmerColors.shimmerBase,
                        ShimmerColors.shimmerHighlight,
                        ShimmerColors.shimmerEnd
                    ),
                    start = Offset(offset - 200f, 0f),
                    end = Offset(offset, 0f)
                )
            )
        }
    }
}

/**
 * Alternative shimmer effect with custom colors
 *
 * @param baseColor Base shimmer color
 * @param highlightColor Highlight shimmer color
 * @param shimmerSpeed Animation duration in milliseconds
 */
@Composable
fun Modifier.shimmerEffectCustom(
    baseColor: Color,
    highlightColor: Color,
    shimmerSpeed: Int = 800
): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer_custom")

    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = shimmerSpeed,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate_custom"
    )

    // ✅ PERFORMANCE FIX: Use drawWithCache for custom shimmer too (Issue #20)
    return this.drawWithCache {
        onDrawBehind {
            val offset = translateAnim.value
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        baseColor,
                        highlightColor,
                        baseColor
                    ),
                    start = Offset(offset - 200f, 0f),
                    end = Offset(offset, 0f)
                )
            )
        }
    }
}
