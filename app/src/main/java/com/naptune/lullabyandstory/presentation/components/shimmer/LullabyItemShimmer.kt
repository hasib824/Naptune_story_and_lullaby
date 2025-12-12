package com.naptune.lullabyandstory.presentation.components.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.naptune.lullabyandstory.ui.theme.PrimaryColor

/**
 * Shimmer loading skeleton for LullabyItemOptimized
 * Matches exact dimensions and structure of the original component
 *
 * Dimensions from LullabyItemOptimized.kt:
 * - Container: Column with fillMaxWidth()
 * - Image Box: aspectRatio(1f) - Square
 * - Corner radius: 16.dp
 * - Text padding top: 8.dp
 * - Typography: titleSmall
 */
@Composable
fun LullabyItemShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Image placeholder with shimmer effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Square, same as original
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryColor) // Base background
                .shimmerEffect() // Apply shimmer animation
        ) {
            // Bottom label placeholder (where "Lullabies" text appears)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Small shimmer box for the label
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
        }

        // Title text placeholder with shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f) // 80% width for realistic text appearance
                .height(20.dp) // Approximate height for titleSmall
                .padding(top = 8.dp) // Same as original
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
    }
}
