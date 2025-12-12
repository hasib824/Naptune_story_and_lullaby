package com.naptune.lullabyandstory.presentation.story.storyreader

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.naptune.lullabyandstory.presentation.main.AdUiState

@Immutable
data class StoryReaderUiState(
    val fontSize: TextUnit = 16.sp,
    val minFontSize: TextUnit = 14.sp,
    val maxFontSize: TextUnit = 28.sp,
    val isLoading: Boolean = false,
    val error: String? = null,
    val adState: AdUiState = AdUiState()
) {
    val canIncrease: Boolean
        get() = fontSize < maxFontSize
    
    val canDecrease: Boolean
        get() = fontSize > minFontSize
}