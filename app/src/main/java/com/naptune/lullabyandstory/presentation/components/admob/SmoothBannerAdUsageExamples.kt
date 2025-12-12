package com.naptune.lullabyandstory.presentation.components.admob

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naptune.lullabyandstory.presentation.main.AdUiState

/**
 * ✅ Usage Examples for SmoothBannerAdSection Component
 * 
 * This file demonstrates how to use the reusable banner ad component
 * across different screens in the app.
 */

// ✅ Example 1: Basic usage in any screen
@Composable
fun ExampleScreenWithBannerAd(
    isNetworkAvailable: Boolean,
    adState: AdUiState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Other content...
        
        item {
            // ✅ Simple banner ad integration
            SmoothBannerAdSection(
                isNetworkAvailable = isNetworkAvailable,
                adState = adState
            )
        }
        
        // More content...
    }
}

// ✅ Example 2: Custom height banner
@Composable
fun ExampleScreenWithLargeBanner(
    isNetworkAvailable: Boolean,
    adState: AdUiState
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Content above banner
        
        // ✅ Large banner for premium placement
        LargeBannerAdSection(
            isNetworkAvailable = isNetworkAvailable,
            adState = adState
        )
        
        // Content below banner
    }
}

// ✅ Example 3: Compact banner for constrained layouts
@Composable
fun ExampleScreenWithCompactBanner(
    isNetworkAvailable: Boolean,
    adState: AdUiState
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header content
        
        // ✅ Compact banner (50dp height)
        CompactBannerAdSection(
            isNetworkAvailable = isNetworkAvailable,
            adState = adState
        )
        
        // Main content
    }
}

// ✅ Example 4: Multiple banner ads on same screen
@Composable
fun ExampleScreenWithMultipleBanners(
    isNetworkAvailable: Boolean,
    topAdState: AdUiState,
    bottomAdState: AdUiState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // ✅ Top banner
            SmoothBannerAdSection(
                isNetworkAvailable = isNetworkAvailable,
                adState = topAdState,
                loadingText = "Loading Top Ad..."
            )
        }
        
        // Main content items here...
        
        item {
            // ✅ Bottom banner with different styling
            SmoothBannerAdSection(
                isNetworkAvailable = isNetworkAvailable,
                adState = bottomAdState,
                bannerHeight = 70.dp,
                loadingText = "Loading Bottom Ad..."
            )
        }
    }
}

// ✅ Example 5: Integration with ViewModel pattern
@Composable
fun ExampleViewModelIntegration(
    viewModel: YourViewModel // Replace with actual ViewModel
) {
    // Collect states from ViewModel
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Use the banner component
    when (val currentState = uiState) {
        is YourUiState.Content -> {
            LazyColumn {
                item {
                    SmoothBannerAdSection(
                        isNetworkAvailable = isNetworkAvailable,
                        adState = currentState.adState,
                        enableDebugLogging = true // Enable for debugging
                    )
                }
                
                // Other content items...
            }
        }
        // Handle other states...
    }
}

// ✅ Placeholder classes for compilation (replace with actual implementations)
interface YourViewModel {
    val isNetworkAvailable: kotlinx.coroutines.flow.StateFlow<Boolean>
    val uiState: kotlinx.coroutines.flow.StateFlow<YourUiState>
}

sealed class YourUiState {
    data class Content(val adState: AdUiState) : YourUiState()
}