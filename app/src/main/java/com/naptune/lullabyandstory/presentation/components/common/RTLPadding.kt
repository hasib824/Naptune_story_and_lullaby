package com.naptune.lullabyandstory.presentation.components.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * RTL-aware padding utilities for proper layout in both LTR and RTL languages
 */

@Composable
fun rtlAwarePadding(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp
): PaddingValues {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    return if (isRtl) {
        PaddingValues(
            start = end,
            top = top,
            end = start,
            bottom = bottom
        )
    } else {
        PaddingValues(
            start = start,
            top = top,
            end = end,
            bottom = bottom
        )
    }
}

@Composable
fun rtlAwareHorizontalPadding(
    start: Dp = 0.dp,
    end: Dp = 0.dp
): PaddingValues {
    return rtlAwarePadding(start = start, end = end)
}