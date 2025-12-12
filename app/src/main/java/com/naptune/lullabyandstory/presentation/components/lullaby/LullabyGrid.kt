package com.naptune.lullabyandstory.presentation.components.lullaby

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.imageLoader
import coil.request.ImageRequest
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.presentation.lullaby.LullabyUiState

// ✅ PERFORMANCE FIX: Hoist lambda to avoid allocation on every composition (Issue #13)
private val lullabyKeySelector: (LullabyDomainModel) -> String = { it.id }

@Composable
fun LullabyGrid(
    uiState: LullabyUiState.Content,
    lullabies: List<LullabyDomainModel>,
    contentBottomPadding: Dp = 0.dp,
    downloadOnClick: (lullabyItem: LullabyDomainModel) -> Unit,
    onLullabyClick: (lullabyItem: LullabyDomainModel) -> Unit = {},
    // ✅ Currently playing lullaby ID
    currentlyPlayingId: String? = null,
    // ✅ Rewarded ad callback
    onAdButtonClick: ((LullabyDomainModel) -> Unit)? = null,
    // ✅ FIX: Premium status to hide badges for premium users
    isPremium: Boolean = false

) {
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    // ✅ Smart image preloading for better scroll performance
        LaunchedEffect(lullabies) {
            // Preload images for the first 12 items (2 screens worth)
            lullabies.take(12).forEach { lullaby ->
                if (lullaby.imagePath.isNotBlank()) {
                    val request = ImageRequest.Builder(context)
                        .data(lullaby.imagePath)
                        .size(300, 300) // Medium size for grid
                        .memoryCacheKey("${lullaby.imagePath}-Medium")
                        .diskCacheKey("${lullaby.imagePath}-Medium")
                        .build()

                    // Enqueue for preloading
                    imageLoader.enqueue(request)
                    Log.d("LullabyGrid", "🚀 Preloading image: ${lullaby.musicName}")
                }
            }
        }
    // ✅ PERFORMANCE FIX: rememberLazyGridState enables automatic prefetching (Issue #16)
    val gridState = rememberLazyGridState()

    LazyVerticalGrid(
        modifier = Modifier,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = (20.dp+contentBottomPadding)),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = gridState // ✅ State enables prefetching automatically
    ) {


        items(
            items = lullabies,
            key = lullabyKeySelector // ✅ Hoisted lambda for stable key
        ) { lullaby ->

            val isDownloaded = remember(uiState.downloadedItems) {
                 lullaby.documentId in uiState.downloadedItems

            }

            val downloadProgress = remember(uiState.downloadProgress) {
                uiState.downloadProgress[lullaby.documentId]
            }

            val isDownloading = remember(uiState.downloadingItems) {
                lullaby.documentId in uiState.downloadingItems
            }

            // ✅ NEW: Check if item is unlocked via rewarded ad in current session
            val isUnlocked = remember(uiState.adUnlockedIds) {
                lullaby.documentId in uiState.adUnlockedIds
            }

            LullabyItemOptimized(
                lullaby = lullaby,
                downloadOnClick = downloadOnClick,
                onPlayLullabyClick = onLullabyClick,
                isDownloaded = isDownloaded,
                isDownloading = isDownloading,
                downloadProgress = downloadProgress,
                isFavourite = lullaby.isFavourite,
                isCurrentlyPlaying = currentlyPlayingId == lullaby.documentId,
                onAdButtonClick = onAdButtonClick,
                // ✅ NEW: Pass unlock status to hide ad badge if unlocked
                isUnlockedViaAd = isUnlocked,
                isPurchased = isPremium
            )
        }
    }
}
