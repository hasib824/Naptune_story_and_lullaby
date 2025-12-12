package com.naptune.lullabyandstory.presentation.splash

import androidx.compose.runtime.Immutable

@Immutable
data class SplashState(
    val isLoading: Boolean = true,
    val shouldNavigateToNext: Boolean = false,
    val error: String? = null,
    val progress: Float = 0f
)
