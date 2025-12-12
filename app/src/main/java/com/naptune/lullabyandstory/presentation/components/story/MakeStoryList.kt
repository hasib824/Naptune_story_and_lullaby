package com.naptune.lullabyandstory.presentation.components.story

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.presentation.components.common.PremiumItemComponent
import com.naptune.lullabyandstory.presentation.components.common.WatchRewardedAdComponent
import com.naptune.lullabyandstory.ui.theme.AccentColor
import com.naptune.lullabyandstory.ui.theme.overlayGradient

@Composable
fun MakeStoryList(
    data: List<StoryDomainModel>,
    onStoryItemClick: (StoryDomainModel) -> Unit,
    isCollapsed: Boolean,
    isBottomSheetVisible: Boolean,
    scrollEnabled: Boolean = true,
    // ✅ Currently playing story ID
    currentlyPlayingId: String? = null,
    // ✅ NEW: Dynamic content padding based on mini controller
    contentBottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    // ✅ NEW: Rewarded ad callback
    onAdButtonClick: ((StoryDomainModel) -> Unit)? = null,
    // ✅ NEW: Session-unlocked item IDs via rewarded ads
    adUnlockedIds: Set<String> = emptySet(),
    // ✅ FIX: Premium status to hide badges for premium users
    isPremium: Boolean = false
) {
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    // ✅ Debug: Log unlocked IDs received
    Log.d("MakeStoryList", "🔓 Received adUnlockedIds: $adUnlockedIds (count: ${adUnlockedIds.size})")

    // ✅ PERFORMANCE FIX: Smart image preloading for better scroll performance (Issue #16)
    LaunchedEffect(data) {
        // Preload images for the first 8 items (2 screens worth for LazyColumn)
        data.take(6).forEach { story ->
            if (story.imagePath.isNotBlank()) {
                val request = ImageRequest.Builder(context)
                    .data(story.imagePath)
                    .size(400, 200) // Story item size (wider than grid items)
                    .memoryCacheKey("${story.imagePath}-Story")
                    .diskCacheKey("${story.imagePath}-Story")
                    .build()

                // Enqueue for preloading
                imageLoader.enqueue(request)
                Log.d("MakeStoryList", "🚀 Preloading story image: ${story.storyName}")
            }
        }
    }

    var counter by remember() { mutableStateOf(1) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(
            bottom = 20.dp + contentBottomPadding,
            top = 4.dp
        ),
        userScrollEnabled = scrollEnabled,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = data,
            key = { story -> story.documentId }, // ✅ Stable key
            contentType = { "StoryItem" } // ✅ PERFORMANCE FIX: ContentType for composition reuse
        ) { item ->
            // ✅ NEW: Check if item is unlocked via rewarded ad in current session
            val isUnlocked = item.documentId in adUnlockedIds

            // ✅ Debug: Log unlock check for each item
            Log.d("MakeStoryList", "📖 Item: ${item.storyName}, ID: ${item.documentId}, Unlocked: $isUnlocked")

            StoryItemContainerNew(
                onNavigateToStoryManager = onStoryItemClick,
                item = item,
                // ✅ Pass if this item is currently playing
                isCurrentlyPlaying = currentlyPlayingId == item.documentId,
                // ✅ Pass ad button callback
                onAdButtonClick = onAdButtonClick,
                // ✅ NEW: Pass unlock status to hide ad badge if unlocked
                isUnlockedViaAd = isUnlocked,
                isPurchased = isPremium
            )
        }
    }
}


// Story Item Container Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryItemContainerNew(
    item: StoryDomainModel,
    onNavigateToStoryManager: (StoryDomainModel) -> Unit,
    modifier: Modifier = Modifier,
    // ✅ NEW: Is this item currently playing?
    isCurrentlyPlaying: Boolean = false,
    // ✅ NEW: Ad button callback
    onAdButtonClick: ((StoryDomainModel) -> Unit)? = null,
    // ✅ NEW: Is item unlocked via rewarded ad for current session?
    isUnlockedViaAd: Boolean = false,
    // ✅ FIX: Is user a premium subscriber?
    isPurchased: Boolean = false
) {
    val itemDownloaded = false

    val storyItemHeight = (LocalConfiguration.current.screenWidthDp - 56) / 2

    // ✅ UX FIX: Determine if this item should show ad badge
    // Only show ad badge if: item is free, callback exists, not unlocked via ad, AND user is NOT premium
    val shouldShowAdBadge = item.isFree && onAdButtonClick != null && !isUnlockedViaAd && !isPurchased

    // ✅ Debug log
    Log.d("StoryItem", "📚 ${item.storyName} - Playing: $isCurrentlyPlaying, Unlocked: $isUnlockedViaAd, ShowAd: $shouldShowAdBadge")

    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(storyItemHeight.dp)
                // ✅ Add gradient border if currently playing (same as LullabyItemOptimized)
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
        ) {


            AsyncImage(
                model = item.imagePath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )

            // Gradient Border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(overlayGradient)
            )

            // Content Wrapper
            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        // ✅ UX FIX: If item has ad badge, clicking anywhere triggers ad
                        if (shouldShowAdBadge) {
                            Log.d("StoryItem", "📺 Triggering reward ad for: ${item.storyName}")
                            onAdButtonClick?.invoke(item)
                        } else {
                            // ✅ Normal behavior: navigate to story manager
                            onNavigateToStoryManager(item)
                        }
                    }
                    .padding(8.dp),

                ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Right Badge
/*                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)

                    ) {
                        if (!isPurchased) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0x33FFFFFF),
                                border = BorderStroke(1.dp, Color(0x4DFFFFFF))
                            ) {
                                Text(
                                    text = stringResource(R.string.badge_pro),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        } else if (!itemDownloaded) {
                            *//* Icon(
                                 painter = painterResource(R.drawable.downloadic),
                                 contentDescription = "Download",
                                 tint = Color.White,
                             )*//*
                        }
                    }*/

                  if (!item.isFree && !isPurchased) {
                        // ✅ Free users see PRO badge only on premium items
                        PremiumItemComponent(
                            modifier = Modifier,
                            onProButtonClick = { },
                            item = item
                        )
                    }
                    if (shouldShowAdBadge) {
                        // ✅ Free users can watch ads to unlock free items
                        WatchRewardedAdComponent(
                            onAdButtonClick = {}, // Empty click - whole item handles it now
                            item = item
                        )
                    }

                    // Bottom info
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.isFavourite) {
                            Icon(
                                painter = painterResource(R.drawable.favoriteic),
                                contentDescription = "Favourite",
                                tint = Color.Unspecified, // এটা important!

                            )
                        }
                        Text(
                            modifier = Modifier.padding(start = 4.dp, top = 0.5.dp),
                            text = stringResource(R.string.label_story_info),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        // Title
        Text(
            text = item.storyName,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2, // ✅ Prevent overflow recomposition
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        /*
        * text = lullaby.musicName, // ViewModel level এ pre-computed
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 2, // ✅ Prevent overflow recomposition
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis*/
    }
}
