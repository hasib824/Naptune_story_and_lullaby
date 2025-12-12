package com.naptune.lullabyandstory.presentation.lullaby

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.presentation.components.common.LoadingIndicator
import com.naptune.lullabyandstory.presentation.components.lullaby.LullabyGrid
import com.naptune.lullabyandstory.presentation.components.common.FilterHeader
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naptune.lullabyandstory.presentation.components.admob.SmoothBannerAdSection
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.presentation.main.AdUiState
import com.naptune.lullabyandstory.presentation.components.RewardVideoBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState

@Composable
fun LullabyScreen(
    onBackClick: () -> Boolean,
    contentBottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onLullabyClick: (LullabyDomainModel) -> Unit = {},
    viewModel: LullabyViewModel = hiltViewModel()
) {
    // ✅ Get currently playing lullaby ID from ViewModel (which gets it from MusicController)
    val currentlyPlayingLullabyId by viewModel.currentlyPlayingLullabyId.collectAsStateWithLifecycle()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()

    // ✅ MVI FIX: Premium status now part of lullabyUiState (single source of truth)
    // Accessed via: (uiState as? LullabyUiState.Content)?.isPremium ?: false

    val context = androidx.compose.ui.platform.LocalContext.current


    
    // ✅ Debug logs
    Log.d("LullabyScreen", "🎵 Currently playing lullaby ID: $currentlyPlayingLullabyId")

    // ✅ ARCHITECTURE FIX: Ad initialization moved to ViewModel init block
    // Ads are only initialized for free users in LullabyViewModel
    LaunchedEffect(Unit) {
        Log.e("LullabyScreen", "🎵 LullabyScreen composed - ViewModel handles ad initialization")
    }

    val uiState: LullabyUiState = viewModel.lullabyUiState.collectAsStateWithLifecycle().value

    when (uiState) {
        is LullabyUiState.Content -> {
            Log.e("LullabyScreen", "📋 Content State - Downloaded items: ${uiState.downloadedItems}")
            ContentScreen(
                uiState = uiState,
                lullabies = uiState.filteredLullabies, // ✅ Use filtered data for display
                contentBottomPadding = contentBottomPadding,
                downloadOnClick = { lullabyItem ->
                    Log.e("LullabyScreen", "📁 Downloaddd clicked for: ${lullabyItem.musicPath} (ID: $lullabyItem.id)")
                    viewModel.handleIntent( LullabyIntent.DownloadLullabyItem(lullabyItem))
                },
                onLullabyClick = { lullaby ->
                    // ✅ Track analytics before navigation
                    viewModel.trackLullabySelected(lullaby)
                    // ✅ Call the original callback - MusicController will handle the rest
                    Log.e("LullabyItem LullabyScreen", "🎵 Playing lullaby: ${lullaby.musicLocalPath} (ID: ${lullaby.documentId})")
                    onLullabyClick(lullaby)
                },
                onCategoryChange = { category ->
                  //  viewModel.handleIntent(LullabyIntent.ChangeCategory(category))
                },
                // ✅ Pass currently playing ID from MusicController
                currentlyPlayingId = currentlyPlayingLullabyId,
                // ✅ Pass network state and ad state
                isNetworkAvailable = isNetworkAvailable,
                adState = uiState.adState,
                // ✅ Rewarded ad callback
                onAdButtonClick = { lullaby ->
                    Log.d("LullabyScreen", "🎁 Ad button clicked for lullaby: ${lullaby.musicName}")
                    if (context is android.app.Activity) {
                        viewModel.handleIntent(
                            LullabyIntent.ShowRewardedAd(
                                adUnitId = com.naptune.lullabyandstory.data.network.admob.AdMobDataSource.TEST_REWARDED_AD_UNIT_ID,
                                activity = context,
                                lullaby = lullaby
                            )
                        )
                    } else {
                        Log.e("LullabyScreen", "❌ Context is not an Activity, cannot show rewarded ad")
                    }
                },
                // ✅ MVI FIX: Get premium status from state
                isPurchased = uiState.isPremium
            )
        }
        is LullabyUiState.Error -> { }
        LullabyUiState.IsLoading -> {
            LoadingIndicator()
        }
    }
}

@Composable
private fun LullabySorting()
{

}

// ✅ NEW: Filter Header with Title and Toggle Button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentScreen(
    uiState: LullabyUiState.Content,
    lullabies: List<LullabyDomainModel>,
    contentBottomPadding: Dp = 0.dp,
    downloadOnClick: (lullabyItem: LullabyDomainModel) -> Unit,
    onLullabyClick: (lullabyItem: LullabyDomainModel) -> Unit,
    onCategoryChange: (LullabyCategory) -> Unit,
    // ✅ Currently playing lullaby ID
    currentlyPlayingId: String?,
    // ✅ Network state and ad state for reusable component
    isNetworkAvailable: Boolean,
    adState: AdUiState,
    // ✅ Rewarded ad callback
    onAdButtonClick: ((LullabyDomainModel) -> Unit)? = null,
    // ✅ FIX: Premium status to hide badges for premium users
    isPurchased: Boolean = false

) {
    // ✅ NEW: Toggle state for filter (false = All, true = Popular)
    var isShowingPopular by remember { mutableStateOf(false) }

    // ✅ NEW: Watch Ad Bottom Sheet state
    var showWatchAdSheet by remember { mutableStateOf(false) }
    var selectedLullabyForAd by remember { mutableStateOf<LullabyDomainModel?>(null) }
    val watchAdSheetState = rememberModalBottomSheetState()

    // ✅ Determine which lullabies to show based on filter state
    val displayLullabies = if (isShowingPopular) {
        uiState.popularLullabies
    } else {
        lullabies // All lullabies
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // ✅ Reusable Smooth Banner Ad Component with music-themed loading
        if(!isPurchased)
        {
            SmoothBannerAdSection(
                isNetworkAvailable = isNetworkAvailable,
                adState = adState,
                loadingText = "Loading Music Ad...", // Custom loading text for LullabyScreen
                enableDebugLogging = true // Enable debug logging for LullabyScreen
            )
        }


        // ✅ NEW: Filter Header with Title and Toggle Button
        FilterHeader(
            contentType = "Lullabies",
            isShowingPopular = isShowingPopular,
            onFilterClick = {
                isShowingPopular = !isShowingPopular
            }
        )

        // ✅ Show filtered lullabies
        Box(modifier = Modifier.fillMaxSize()) {
            LullabyGrid(
                uiState = uiState,
                lullabies = displayLullabies,
                contentBottomPadding = contentBottomPadding,
                downloadOnClick = downloadOnClick,
                onLullabyClick = onLullabyClick,
                currentlyPlayingId = currentlyPlayingId,
                onAdButtonClick = { lullaby ->
                    // ✅ Show Watch Ad Bottom Sheet instead of directly showing ad
                    selectedLullabyForAd = lullaby
                    showWatchAdSheet = true
                },
                isPremium = isPurchased
            )
        }
    }

    // ✅ Reward Video Bottom Sheet
    if (showWatchAdSheet) {
        RewardVideoBottomSheet(
            onDismiss = {
                showWatchAdSheet = false
                selectedLullabyForAd = null
            },
            onWatchClick = {
                // When user clicks "Watch", trigger the rewarded ad
                selectedLullabyForAd?.let { lullaby ->
                    onAdButtonClick?.invoke(lullaby)
                }
                showWatchAdSheet = false
                selectedLullabyForAd = null
            },
            sheetState = watchAdSheetState
        )
    }
}

