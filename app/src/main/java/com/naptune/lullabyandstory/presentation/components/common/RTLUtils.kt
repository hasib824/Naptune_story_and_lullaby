package com.naptune.lullabyandstory.presentation.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection

/**
 * RTL Support Utilities for Naptune App
 * Provides RTL-aware layout and text alignment helpers
 */

@Composable
fun isRtlLayout(): Boolean {
    return LocalLayoutDirection.current == LayoutDirection.Rtl
}

@Composable
fun getStartTextAlign(): TextAlign {
    return if (isRtlLayout()) TextAlign.End else TextAlign.Start
}

@Composable
fun getEndTextAlign(): TextAlign {
    return if (isRtlLayout()) TextAlign.Start else TextAlign.End
}

/**
 * Get text alignment that respects RTL layout direction
 * For body text that should align to the reading direction
 */
@Composable
fun getContentTextAlign(): TextAlign {
    return TextAlign.Start // This automatically handles RTL
}

/**
 * Get text alignment for centered content
 * This remains center regardless of layout direction
 */
fun getCenterTextAlign(): TextAlign {
    return TextAlign.Center
}