package com.naptune.lullabyandstory.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


// ✅ Cached gradient for performance
val overlayGradient = Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        Color.Black.copy(alpha = 0.0f),
        Color.Black.copy(alpha = 0.1f),
        Color.Black.copy(alpha = 0.25f),
        Color.Black.copy(alpha = 0.5f),
        Color.Black.copy(alpha = 0.8f)
    ),
    startY = 0f,
    endY = Float.POSITIVE_INFINITY
)

@Composable
fun NaptuneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    MaterialTheme(
        typography = Typography,
        content = content
    )
}

// ✅ Alias for backwards compatibility
@Composable
fun LullabyAndStoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    NaptuneTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
