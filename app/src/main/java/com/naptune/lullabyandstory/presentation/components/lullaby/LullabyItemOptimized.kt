package com.naptune.lullabyandstory.presentation.components.lullaby

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import android.widget.Toast
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.presentation.components.common.CircularDownloadProgressBar
import com.naptune.lullabyandstory.presentation.components.common.PremiumItemComponent
import com.naptune.lullabyandstory.presentation.components.common.WatchRewardedAdComponent
import com.naptune.lullabyandstory.ui.theme.AccentColor
import com.naptune.lullabyandstory.ui.theme.PrimaryColor
import com.naptune.lullabyandstory.ui.theme.overlayGradient

// ✅ PERFORMANCE FIX: Pre-compute colors to avoid allocation on every composition
private val PlaceholderGradientTop = PrimaryColor.copy(alpha = 0.7f)
private val PlaceholderGradientBottom = PrimaryColor.copy(alpha = 0.3f)
private val PlaceholderTextColor = Color.White.copy(alpha = 0.8f)

@Composable
fun LullabyItemOptimized(
    lullaby: LullabyDomainModel,
    downloadOnClick: (LullabyDomainModel) -> Unit,
    onPlayLullabyClick: (LullabyDomainModel) -> Unit,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    downloadProgress: Int? = 0,
    isFavourite: Boolean,
    modifier: Modifier = Modifier,
    // ✅ NEW: Is this item currently playing?
    isCurrentlyPlaying: Boolean = false,
    // ✅ NEW: Rewarded ad callback
    onAdButtonClick: ((LullabyDomainModel) -> Unit)? = null,
    // ✅ NEW: Is item unlocked via rewarded ad for current session?
    isUnlockedViaAd: Boolean = false,
    // ✅ NEW: Is user a premium subscriber?
    isPurchased: Boolean = false

    ) {
    Log.e(
        "LullabyItemCurrent",
        "🎵 ${lullaby.musicName} - Downloaded: $isDownloaded, Downloading: $isDownloading, Progress: $downloadProgress, Playing: $isCurrentlyPlaying, Unlocked: $isUnlockedViaAd, DocumentId: ${lullaby.documentId}"
    )

    val context = LocalContext.current

    // ✅ UX FIX: Determine if this item should show ad badge
    // Only show ad badge if: item is free, callback exists, not unlocked via ad, AND user is NOT premium
    val shouldShowAdBadge = lullaby.isFree && onAdButtonClick != null && !isUnlockedViaAd && !isPurchased

    // ✅ DEBUG: Log ad badge logic
    android.util.Log.d("LullabyItemAd", "🎵 ${lullaby.musicName} - isFree: ${lullaby.isFree}, isDownloaded: $isDownloaded, hasCallback: ${onAdButtonClick != null}, isUnlocked: $isUnlockedViaAd, isPremium: $isPurchased, SHOW_BADGE: $shouldShowAdBadge")

    Column(
        modifier = modifier
            .fillMaxWidth()
        // ✅ PERFORMANCE FIX: Removed unnecessary transparent background to reduce overdraw

    ) {

        // ✅ Use Box instead of ConstraintLayout for better performance
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Square Box
                .background(PrimaryColor)
                // ✅ Draw border FIRST (before clipping) so it's visible
                .then(
                    if (isCurrentlyPlaying) {
                        Modifier.border(
                            width = 2.dp,
                            color = AccentColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else {
                        Modifier
                    }
                )
                // ✅ Clip AFTER drawing border so border is visible
                .clip(RoundedCornerShape(16.dp))// ✅ Single background call
                .clickable {
                    // ✅ UX FIX: If item has ad badge, clicking anywhere triggers ad
                    if (shouldShowAdBadge) {
                        Log.d("LullabyItem", "📺 Triggering reward ad for: ${lullaby.musicName}")
                        onAdButtonClick?.invoke(lullaby)
                    } else {
                        // ✅ Normal behavior: play music or download
                        Log.e("LullabyItem", "🎵 Playing lullaby: ${lullaby.musicLocalPath}")
                        if (lullaby.musicLocalPath!=null) {
                            Log.e("LullabyItem 1", "🎵 Playing lullaby: ${lullaby.musicLocalPath}")
                            onPlayLullabyClick(lullaby)
                        } else {
                            // ✅ CHECK: Prevent download if already downloading
                            if (isDownloading) {
                                Toast.makeText(context, context.getString(R.string.toast_download_progress), Toast.LENGTH_SHORT).show()
                                Log.d("LullabyItem", "⚠️ Download already in progress, showing toast")
                            } else {
                                Log.e("LullabyItem", "📥 Starting download for: ${lullaby.musicName}")
                                Toast.makeText(context, context.getString(R.string.toast_download_starting, lullaby.musicName), Toast.LENGTH_SHORT).show()
                                downloadOnClick(lullaby)
                                Log.e("LullabyItem", "📥 Also downloading: ${lullaby.musicName}")
                            }
                        }
                    }
                }) {


            // ✅ USE WORKING VERSION: Direct AsyncImage (no loading issues)
            if (lullaby.imagePath.isNotBlank()) {
                AsyncImage(
                    model = lullaby.imagePath,
                    contentDescription = lullaby.musicName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    // ✅ Still get some optimization benefits from our cache config
                    onSuccess = {
                        Log.d("LullabyItem", "✅ Image loaded: ${lullaby.musicName}")
                    })
            } else {
                // ✅ Show attractive placeholder for empty URLs
                Log.w("LullabyItem", "⚠️ Empty image URL for: ${lullaby.musicName}")
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    PlaceholderGradientTop, // ✅ Use pre-computed color
                                    PlaceholderGradientBottom // ✅ Use pre-computed color
                                )
                            )
                        ), contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.image_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = PlaceholderTextColor // ✅ Use pre-computed color
                        )
                    }
                }
            }

            // ✅ Gradient overlay using cached gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayGradient)
            )

            // ✅ UX FIX: Show badges based on premium status
          /*  if (isPurchased) {
                // 🏆 Premium users see PRO badge on ALL items
                PremiumItemComponent(
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp),
                    onProButtonClick = {},
                    item = lullaby
                )
            } else*/ if (!lullaby.isFree && !isPurchased) {
                // ✅ Free users see PRO badge only on premium items
                PremiumItemComponent(
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp),
                    onProButtonClick = {},
                    item = lullaby
                )
            } else if (shouldShowAdBadge) {
                // ✅ Free users can watch ads to unlock free items
                WatchRewardedAdComponent(
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp),
                    onAdButtonClick = {}, // Empty click - whole item handles it now
                    item = lullaby
                )
            }


            // ✅ Download state UI
            if (!isDownloaded && !lullaby.isDownloaded) {
                // ✅ Show progress
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    if (isDownloading) {
                        CircularDownloadProgressBar(
                            progress = if (downloadProgress != null && downloadProgress > 5) downloadProgress else 5
                        )
                    }

                    Icon(
                        painter = painterResource(R.drawable.downloadic),
                        modifier = Modifier.clickable {
                            Log.e("LullabyItem", "📁 Download icon clicked for: ${lullaby.musicName}")
                            // ✅ CHECK: Prevent download if already downloading
                            if (isDownloading) {

                                Log.d("LullabyItem", "⚠️ Download already in progress, showing toast")
                            } else {
                                Toast.makeText(context, context.getString(R.string.toast_download_starting, lullaby.musicName), Toast.LENGTH_SHORT).show()
                                downloadOnClick(lullaby)
                            }
                        },
                        contentDescription = "Download",
                        tint = Color.White
                    )
                }

            }

            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if(lullaby.isFavourite)
                {
                    Icon(
                        painter = painterResource(R.drawable.favoriteic),
                        modifier = Modifier.padding(end = 4.dp),
                        contentDescription = "Download",
                        tint = Color.Unspecified
                    )
                }


                // ✅ Text positioned with Alignment instead of constraints
                Text(
                    text = stringResource(R.string.label_lullabies),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,

                )

            }


        }

        // Title text - ✅ OPTIMIZED: ViewModel এ আগেই localized
        Text(
            text = lullaby.musicName, // ViewModel level এ pre-computed
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 2, // ✅ Prevent overflow recomposition
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}