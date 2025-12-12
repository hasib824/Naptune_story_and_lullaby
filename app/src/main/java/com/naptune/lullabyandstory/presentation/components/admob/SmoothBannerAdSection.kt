package com.naptune.lullabyandstory.presentation.components.admob

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.naptune.lullabyandstory.presentation.main.AdUiState

/**
 * ✅ Reusable Smooth Banner Ad Component
 * 
 * Features:
 * - Network-aware visibility (hides when offline, shows when online)
 * - Smooth height animations (0dp → 60dp transitions)
 * - Professional fade in/out transitions
 * - Loading state with progress indicator
 * - Instant ad display when loaded
 * - Content movement animations
 * 
 * Usage:
 * ```kotlin
 * SmoothBannerAdSection(
 *     isNetworkAvailable = isNetworkAvailable,
 *     adState = uiState.adState,
 *     bannerHeight = 60.dp, // Optional custom height
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 */
@Composable
fun SmoothBannerAdSection(
    isNetworkAvailable: Boolean,
    adState: AdUiState,
    modifier: Modifier = Modifier,
    bannerHeight: androidx.compose.ui.unit.Dp = 60.dp,
    loadingText: String = "Loading Ad...",
    enableDebugLogging: Boolean = false,
    useAutoHeight: Boolean = false // ✅ NEW: Allow content-based height when loaded
) {
    // ✅ CRITICAL FIX: Show ad if network is available OR ad is already loaded
    // This prevents ad from vanishing during navigation when network state fluctuates
    val shouldShowAd = isNetworkAvailable || adState.isBannerLoaded

    // ✅ Dynamic height animation for smooth content transitions
    val animatedBannerHeight by animateDpAsState(
        targetValue = if (shouldShowAd) bannerHeight else 0.dp,
        animationSpec = tween(
            durationMillis = 500,
            easing = EaseInOutCubic
        ),
        label = "bannerHeight"
    )

    // ✅ Debug logging (optional)
    if (enableDebugLogging) {
        LaunchedEffect(isNetworkAvailable, adState.isBannerLoaded, shouldShowAd) {
            Log.d("SmoothBannerAdSection", "🔍 Network Available: $isNetworkAvailable")
            Log.d("SmoothBannerAdSection", "🔍 Banner Ad Loaded: ${adState.isBannerLoaded}")
            Log.d("SmoothBannerAdSection", "🔍 Should Show Ad: $shouldShowAd")
            Log.d("SmoothBannerAdSection", "🔍 Banner Height: $animatedBannerHeight")
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .let {
                if (useAutoHeight && adState.isBannerLoaded) {
                    // ✅ Use natural height when ad is loaded and useAutoHeight is enabled
                    it.wrapContentHeight()
                } else {
                    // ✅ Use animated height for loading/network state transitions
                    it.height(animatedBannerHeight)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // ✅ FIXED: Show if network available OR ad already loaded
        // This prevents vanishing when navigating back with loaded ad
        androidx.compose.animation.AnimatedVisibility(
            visible = shouldShowAd,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 400,
                    easing = EaseInOutCubic
                )
            ) + expandVertically(
                animationSpec = tween(
                    durationMillis = 500,
                    easing = EaseInOutCubic
                ),
                expandFrom = Alignment.Top
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = EaseInOutCubic
                )
            ) + shrinkVertically(
                animationSpec = tween(
                    durationMillis = 400,
                    easing = EaseInOutCubic
                ),
                shrinkTowards = Alignment.Top
            )
        ) {
            BannerAdContent(
                adState = adState,
                loadingText = loadingText
            )
        }
    }
}

@Composable
private fun BannerAdContent(
    adState: AdUiState,
    loadingText: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = EaseInOutCubic
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!adState.isBannerLoaded) {
            // ✅ Loading state
            BannerLoadingIndicator(loadingText = loadingText)
        } else {
            // ✅ Actual banner ad - instant display
            BannerAdComposable(bannerAd = adState.bannerAd)
        }
    }
}

@Composable
private fun BannerLoadingIndicator(
    loadingText: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = loadingText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * ✅ Simplified version with minimal parameters for quick usage
 */
@Composable
fun SimpleBannerAdSection(
    isNetworkAvailable: Boolean,
    adState: AdUiState,
    modifier: Modifier = Modifier
) {
    SmoothBannerAdSection(
        isNetworkAvailable = isNetworkAvailable,
        adState = adState,
        modifier = modifier
    )
}

/**
 * ✅ Compact version with smaller height for different screen layouts
 */
@Composable
fun CompactBannerAdSection(
    isNetworkAvailable: Boolean,
    adState: AdUiState,
    modifier: Modifier = Modifier
) {
    SmoothBannerAdSection(
        isNetworkAvailable = isNetworkAvailable,
        adState = adState,
        bannerHeight = 50.dp, // Smaller height
        modifier = modifier
    )
}

/**
 * ✅ Large banner version for premium placements
 */
@Composable
fun LargeBannerAdSection(
    isNetworkAvailable: Boolean,
    adState: AdUiState,
    modifier: Modifier = Modifier
) {
    SmoothBannerAdSection(
        isNetworkAvailable = isNetworkAvailable,
        adState = adState,
        bannerHeight = 90.dp, // Larger height
        modifier = modifier
    )
}