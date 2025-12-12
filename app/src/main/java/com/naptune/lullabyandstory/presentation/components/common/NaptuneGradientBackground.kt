package com.naptune.lullabyandstory.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.naptune.lullabyandstory.ui.theme.GradientBottomColor
import com.naptune.lullabyandstory.ui.theme.PrimaryColor
import com.naptune.lullabyandstory.ui.theme.GlowPinkColor
import com.naptune.lullabyandstory.ui.theme.StoryManagerBackground

@Composable
fun NaptuneGradientBackground(
    isStoryReaderScreen: Boolean = false,
    isPremiumScreen: Boolean = false,
    content: @Composable () -> Unit,

) {
    Box(
        modifier = Modifier
            .fillMaxSize().then(
                if(isStoryReaderScreen) {
                    Modifier.background(PrimaryColor)
                } else {
                    Modifier.background( PrimaryColor
                       // brush = gradientBackground()
                    )
                }
            )
    ) {
        // ✅ Blue glow at top-right corner (only for premium screen)
        if (isPremiumScreen) {
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp
            val glowSize = screenWidth
            val glowOffsetY = -(glowSize * 0.38f)
            val glowOffsetX = (glowSize * 0.35f)
            val glowColor = Color(0xFF0072F2)

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(glowSize)
                    .offset(x = glowOffsetX, y = glowOffsetY)
                    .blur(85.dp)
                    .zIndex(-1f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.5f),
                                glowColor.copy(alpha = 0.3f),
                                glowColor.copy(alpha = 0.23f),
                                glowColor.copy(alpha = 0.2f),
                                glowColor.copy(alpha = 0.1f),
                                glowColor.copy(alpha = 0.07f),
                                glowColor.copy(alpha = 0.05f),
                                glowColor.copy(alpha = 0.002f),
                                glowColor.copy(alpha = 0.001f),
                                Color.Transparent
                            ),
                            radius = with(LocalDensity.current) { (glowSize * 0.38f).toPx() }
                        )
                    )
            )
        }

        content()
    }
}
//StoryManagerBackground
@Composable
private fun gradientBackground(): Brush = Brush.verticalGradient(
    colors = listOf(
        PrimaryColor,    // 5F55AB - Top purple
        GradientBottomColor  // 332E4E - Bottom dark purple
    ),
    startY = 0f, // Start from very top (status bar area)
    endY = Float.POSITIVE_INFINITY
)
