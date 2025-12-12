package com.naptune.lullabyandstory.presentation.components.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.naptune.lullabyandstory.ui.theme.PrimaryColor

/**
 * Shimmer loading skeleton for StoryItemContainerNew
 * Matches exact dimensions and structure of the original component
 *
 * Dimensions from StoryItemContainerNew.kt:
 * - Height: (screenWidth - 48) / 2
 * - Corner radius: 16.dp
 * - Text padding top: 8.dp
 * - Typography: titleSmall
 */
@Composable
fun StoryItemShimmer(
    modifier: Modifier = Modifier
) {
    // Calculate same height as original story item
    val storyItemHeight = (LocalConfiguration.current.screenWidthDp - 48) / 2

    Column(
        modifier = modifier
    ) {
        // Image placeholder with shimmer effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(storyItemHeight.dp) // Same height calculation as original
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryColor) // Base background
                .shimmerEffect() // Apply shimmer animation
        ) {
            // Bottom label placeholder (where "Story" text appears)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Small shimmer box for the label
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
        }

        // Title text placeholder with shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f) // 85% width for realistic text appearance
                .height(20.dp) // Approximate height for titleSmall
                .padding(top = 8.dp) // Same as original
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
    }
}
