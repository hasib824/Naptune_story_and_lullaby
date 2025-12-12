package com.naptune.lullabyandstory.presentation.favourite

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.presentation.components.lullaby.LullabyItemOptimized
import com.naptune.lullabyandstory.presentation.components.story.MakeStoryList
import com.naptune.lullabyandstory.presentation.components.admob.SmoothBannerAdSection
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.ui.theme.FavouriteCategoryButtonitemActiveColor
import com.naptune.lullabyandstory.ui.theme.FavouriteCategoryButtonitemInActiveColor
import com.naptune.lullabyandstory.ui.theme.PrimaryColor
import com.naptune.lullabyandstory.ui.theme.PrimarySurfaceColor
import com.naptune.lullabyandstory.presentation.components.RewardVideoBottomSheet
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteScreen(
    onNavigateToAudioPlayer: (LullabyDomainModel) -> Unit,
    onNavigateToStoryAudioPlayer: (StoryDomainModel) -> Unit,
    onNavigateToStoryManager: (StoryDomainModel) -> Unit, // ✅ NEW: Story manager navigation
    viewModel: FavouriteViewModel = hiltViewModel(),
    // ✅ NEW: Dynamic content padding based on mini controller
    contentBottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // ✅ NEW: Reward Video Bottom Sheet state
    var showWatchAdSheet by remember { mutableStateOf(false) }
    var selectedLullabyForAd by remember { mutableStateOf<LullabyDomainModel?>(null) }
    var selectedStoryForAd by remember { mutableStateOf<StoryDomainModel?>(null) }
    val watchAdSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Get Activity context for rewarded ads
    val context = LocalContext.current
    val activity = context as Activity

    // ✅ ARCHITECTURE FIX: Ad initialization moved to ViewModel init block
    // Ads are only initialized for free users in FavouriteViewModel

    // Get currently playing states from ViewModel
    val currentlyPlayingLullabyId by viewModel.currentlyPlayingLullabyId.collectAsStateWithLifecycle()
    val currentlyPlayingStoryId by viewModel.currentlyPlayingStoryId.collectAsStateWithLifecycle()

    // ✅ FIX: Collect premium status to pass to components
    val isPurchased by viewModel.isPurchased.collectAsStateWithLifecycle()

    // ✅ FIXED: Use when with local variable to enable smart cast
    when (val currentState = uiState) {
        is FavouriteUiState.isLoading -> {
            LoadingScreen()
        }

        is FavouriteUiState.Content -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // ✅ Reusable Smooth Banner Ad Component with heart-themed loading

                if(!isPurchased)
                {
                    SmoothBannerAdSection(
                        isNetworkAvailable = isNetworkAvailable,
                        adState = currentState.adState,
                        loadingText = "Loading Favourites Ad...", // Custom loading text for FavouriteScreen
                        enableDebugLogging = true // Enable debug logging for FavouriteScreen
                    )
                }

                FavouriteContentScreen(
                    contentState = currentState, // ✅ Now smart cast works
                    currentlyPlayingLullabyId = currentlyPlayingLullabyId,
                    currentlyPlayingStoryId = currentlyPlayingStoryId,
                    onNavigateToAudioPlayer = onNavigateToAudioPlayer,
                    onNavigateToStoryAudioPlayer = onNavigateToStoryAudioPlayer,
                    onNavigateToStoryManager = onNavigateToStoryManager, // ✅ NEW: Pass story manager callback
                    onCategoryChange = { category ->
                        viewModel.handleIntent(FavouriteIntent.ChangeCategory(category))
                    },
                    onToggleLullabyFavourite = { lullabyId ->
                        viewModel.handleIntent(FavouriteIntent.ToggleLullabyFavourite(lullabyId))
                    },
                    onToggleStoryFavourite = { storyId ->
                        viewModel.handleIntent(FavouriteIntent.ToggleStoryFavourite(storyId))
                    },
                    // ✅ Pass dynamic padding
                    contentBottomPadding = contentBottomPadding,
                    // ✅ NEW: Rewarded ad callbacks - Show modal first
                    onLullabyAdClick = { lullaby ->
                        Log.d(
                            "FavouriteScreen",
                            "🎵 AD button clicked for lullaby: ${lullaby.musicName}"
                        )
                        selectedLullabyForAd = lullaby
                        selectedStoryForAd = null
                        showWatchAdSheet = true
                    },
                    onStoryAdClick = { story ->
                        Log.d(
                            "FavouriteScreen",
                            "📚 AD button clicked for story: ${story.storyName}"
                        )
                        selectedStoryForAd = story
                        selectedLullabyForAd = null
                        showWatchAdSheet = true
                    },
                    viewModel = viewModel,
                    isPurchased = isPurchased
                )
            }
        }

        is FavouriteUiState.Error -> {
            ErrorScreen(
                message = currentState.message, // ✅ Smart cast works here too
                onRetry = {
                    viewModel.handleIntent(FavouriteIntent.LoadFavourites)
                }
            )
        }
    }

    // ✅ Reward Video Bottom Sheet
    if (showWatchAdSheet) {
        RewardVideoBottomSheet(
            onDismiss = {
                showWatchAdSheet = false
                selectedLullabyForAd = null
                selectedStoryForAd = null
            },
            onWatchClick = {
                // When user clicks "Watch", trigger the rewarded ad
                selectedLullabyForAd?.let { lullaby ->
                    Log.d("FavouriteScreen", "🎵 Watch clicked - Showing rewarded ad for lullaby: ${lullaby.musicName}")
                    viewModel.handleIntent(
                        FavouriteIntent.ShowRewardedAdForLullaby(
                            adUnitId = AdMobDataSource.TEST_REWARDED_AD_UNIT_ID,
                            activity = activity,
                            lullaby = lullaby
                        )
                    )
                }
                selectedStoryForAd?.let { story ->
                    Log.d("FavouriteScreen", "📚 Watch clicked - Showing rewarded ad for story: ${story.storyName}")
                    viewModel.handleIntent(
                        FavouriteIntent.ShowRewardedAdForStory(
                            adUnitId = AdMobDataSource.TEST_REWARDED_AD_UNIT_ID,
                            activity = activity,
                            story = story
                        )
                    )
                }
                showWatchAdSheet = false
                selectedLullabyForAd = null
                selectedStoryForAd = null
            },
            sheetState = watchAdSheetState
        )
    }
}

@Composable
private fun FavouriteContentScreen(
    contentState: FavouriteUiState.Content,
    currentlyPlayingLullabyId: String?,
    currentlyPlayingStoryId: String?,
    onNavigateToAudioPlayer: (LullabyDomainModel) -> Unit,
    onNavigateToStoryAudioPlayer: (StoryDomainModel) -> Unit,
    onNavigateToStoryManager: (StoryDomainModel) -> Unit, // ✅ NEW: Story manager navigation
    onCategoryChange: (FavouriteCategory) -> Unit,
    onToggleLullabyFavourite: (String) -> Unit,
    onToggleStoryFavourite: (String) -> Unit,
    // ✅ NEW: Dynamic content padding
    contentBottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    // ✅ NEW: Rewarded ad callbacks
    onLullabyAdClick: ((LullabyDomainModel) -> Unit)? = null,
    onStoryAdClick: ((StoryDomainModel) -> Unit)? = null,
    // ✅ Analytics
    viewModel: FavouriteViewModel,
    // ✅ FIX: Premium status
    isPurchased: Boolean = false
) {
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = when (contentState.currentCategory) {
            FavouriteCategory.LULLABY -> 0
            FavouriteCategory.STORY -> 1
        },
        pageCount = { 2 }
    )

    // ✅ PERFORMANCE FIX: Single source of truth to avoid double syncing (Issue #22)
    LaunchedEffect(Unit) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val newCategory = when (page) {
                    0 -> FavouriteCategory.LULLABY
                    1 -> FavouriteCategory.STORY
                    else -> FavouriteCategory.LULLABY
                }
                if (contentState.currentCategory != newCategory) {
                    onCategoryChange(newCategory)
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        // Category buttons
        FavouriteCategoryButtons(
            currentPage = pagerState.currentPage,
            onCategoryChange = { page ->
                scope.launch {
                    pagerState.animateScrollToPage(page)
                }
            }
        )

        // ✅ Capture viewModel for lambda usage
        val vm = viewModel

        // HorizontalPager for swipeable content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    FavouriteLullabiesContent(
                        lullabies = contentState.favouriteLullabies,
                        onLullabyClick = { lullaby ->
                            // ✅ Track analytics before navigation
                            vm.trackLullabyPlayedFromFavourites(lullaby)
                            onNavigateToAudioPlayer(lullaby)
                        },
                        onToggleFavourite = onToggleLullabyFavourite,
                        currentlyPlayingId = currentlyPlayingLullabyId,
                        contentBottomPadding = contentBottomPadding,
                        onLullabyAdClick = onLullabyAdClick,
                        adUnlockedIds = contentState.adUnlockedIds,
                        isPremium = isPurchased
                    )
                }

                1 -> {
                    FavouriteStoriesContent(
                        stories = contentState.favouriteStories,
                        onStoryClick = { story ->
                            // ✅ Track analytics before navigation
                            vm.trackStoryPlayedFromFavourites(story)
                            onNavigateToStoryManager(story)
                        },
                        onToggleFavourite = onToggleStoryFavourite,
                        currentlyPlayingId = currentlyPlayingStoryId,
                        contentBottomPadding = contentBottomPadding,
                        onStoryAdClick = onStoryAdClick,
                        adUnlockedIds = contentState.adUnlockedIds,
                        isPremium = isPurchased
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = PrimaryColor,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.loading_favourites_message),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "😕",
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.error_something_wrong),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor
                )
            ) {
                Text(
                    text = stringResource(R.string.action_try_again),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun FavouriteCategoryButtons(
    currentPage: Int,
    onCategoryChange: (Int) -> Unit
) {

    Column( modifier = Modifier
        .fillMaxWidth()
        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)
        .clip(RoundedCornerShape(100.dp))
        .background(PrimarySurfaceColor)) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoryButton(
                text = stringResource(R.string.tab_lullaby),
                isSelected = currentPage == 0,
                onClick = { onCategoryChange(0) },
                modifier = Modifier.weight(1f)
            )

            CategoryButton(
                text = stringResource(R.string.tab_story),
                isSelected = currentPage == 1,
                onClick = { onCategoryChange(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CategoryButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(
                if (isSelected) {
                    // ✅ Gradient background when selected (left to right: CCE4FC -> 99C8FA)
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFCCE4FC),
                            Color(0xFF99C8FA)
                        )
                    )
                } else {
                    // Solid white with transparency when not selected
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.0f),
                            Color.White.copy(alpha = 0.0f)
                        )
                    )
                }
            )
            /* .border(
                 width = 2.dp,
                 color = Color.White.copy(alpha = 0.16f),
                 shape = RoundedCornerShape(32.dp)
             )*/
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 0.dp),
            text = text,
            fontSize = 16.sp,
            style = MaterialTheme.typography.titleSmall.copy(
                color = if (isSelected) FavouriteCategoryButtonitemActiveColor else FavouriteCategoryButtonitemInActiveColor
            ),
            fontWeight = FontWeight.SemiBold,
        )

        Icon(
            modifier = Modifier.padding(start = 8.dp),
            tint = if (isSelected) FavouriteCategoryButtonitemActiveColor else FavouriteCategoryButtonitemInActiveColor,
            painter = painterResource(
                if (text.equals("lullaby", true))
                    R.drawable.fav_lullaby_icon
                else
                    R.drawable.fav_story_icon
            ),
            contentDescription = "Favourite"
        )
    }
}

@Composable
private fun FavouriteLullabiesContent(
    lullabies: List<LullabyDomainModel>,
    onLullabyClick: (LullabyDomainModel) -> Unit,
    onToggleFavourite: (String) -> Unit,
    currentlyPlayingId: String? = null,
    // ✅ NEW: Dynamic content padding
    contentBottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    // ✅ NEW: Rewarded ad callback
    onLullabyAdClick: ((LullabyDomainModel) -> Unit)? = null,
    // ✅ NEW: Session-unlocked item IDs
    adUnlockedIds: Set<String> = emptySet(),
    // ✅ FIX: Premium status to hide badges for premium users
    isPremium: Boolean = false
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (lullabies.isEmpty()) {
            EmptyFavouritesContent(
                title = stringResource(R.string.empty_favourites_lullabies_title),
                description = stringResource(R.string.empty_favourites_lullabies_desc)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 4.dp,
                    bottom = (20.dp + contentBottomPadding)
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = (lullabies),
                    key = { lullaby -> lullaby.documentId }, // ✅ Stable key
                    contentType = { "LullabyItem" } // ✅ PERFORMANCE FIX: ContentType for composition reuse
                ) { lullaby ->
                    // ✅ NEW: Check if lullaby is unlocked via ad
                    val isUnlocked = lullaby.documentId in adUnlockedIds

                    // ✅ DEBUG: Log lullaby properties to understand ad badge logic
                    android.util.Log.d("FavouriteLullaby", "🎵 ${lullaby.musicName} - isFree: ${lullaby.isFree}, isDownloaded: ${lullaby.isDownloaded}, isUnlocked: $isUnlocked, hasAdCallback: ${onLullabyAdClick != null}")

                    LullabyItemOptimized(
                        lullaby = lullaby,
                        downloadOnClick = {},
                        onPlayLullabyClick = onLullabyClick,
                        isDownloaded = lullaby.isDownloaded,
                        isDownloading = false,
                        downloadProgress = if (lullaby.isDownloaded) 100 else 0,
                        isFavourite = lullaby.isFavourite,
                        isCurrentlyPlaying = currentlyPlayingId == lullaby.documentId,
                        onAdButtonClick = onLullabyAdClick,
                        isUnlockedViaAd = isUnlocked,
                        isPurchased = isPremium
                        )
                }
            }
        }
    }
}

@Composable
private fun FavouriteStoriesContent(
    stories: List<StoryDomainModel>,
    onStoryClick: (StoryDomainModel) -> Unit,
    onToggleFavourite: (String) -> Unit,
    currentlyPlayingId: String? = null,
    // ✅ NEW: Dynamic content padding
    contentBottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    // ✅ NEW: Rewarded ad callback
    onStoryAdClick: ((StoryDomainModel) -> Unit)? = null,
    // ✅ NEW: Session-unlocked item IDs
    adUnlockedIds: Set<String> = emptySet(),
    isPremium: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp)
    ) {
        if (stories.isEmpty()) {
            EmptyFavouritesContent(
                title = stringResource(R.string.empty_favourites_stories_title),
                description = stringResource(R.string.empty_favourites_stories_desc)
            )
        } else {
            MakeStoryList(
                data = stories,
                onStoryItemClick = onStoryClick,
                isCollapsed = false,
                isBottomSheetVisible = false,
                scrollEnabled = true,
                currentlyPlayingId = currentlyPlayingId,
                contentBottomPadding = contentBottomPadding,
                onAdButtonClick = onStoryAdClick,
                // ✅ NEW: Pass session-unlocked item IDs
                adUnlockedIds = adUnlockedIds,
                isPremium = isPremium
            )
        }
    }
}

@Composable
private fun EmptyFavouritesContent(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            contentScale = ContentScale.Crop,
            model = "https://appswave.xyz/naptune/empty_img.png",
            contentDescription = "Story Image",
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFC8C0D4),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF9B8FA8),
            textAlign = TextAlign.Center
        )
    }
}