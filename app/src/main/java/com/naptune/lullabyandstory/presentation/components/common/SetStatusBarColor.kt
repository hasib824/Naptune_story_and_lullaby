@file:Suppress("DEPRECATION")

package com.naptune.lullabyandstory.presentation.components.common

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SetStatusBarColor(
    color: Color = Color.Transparent,
    darkIcons: Boolean = true
) {
    val view = LocalView.current
    
    SideEffect {
        try {
            val window = (view.context as ComponentActivity).window

            // ✅ API level specific status bar color handling
            if (Build.VERSION.SDK_INT >= 36) {
                // API 36+ requires specific handling for status bar background
                window.statusBarColor = color.toArgb()
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
            } else {
                // Standard handling for older versions
                window.statusBarColor = color.toArgb()
            }

            // ✅ Enhanced system UI management for AdMob compatibility
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false

            // ✅ Ensure edge-to-edge is properly maintained
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
        } catch (e: Exception) {
            android.util.Log.e("SetStatusBarColor", "❌ Error setting status bar: ${e.message}")
        }
    }
}
