package com.naptune.lullabyandstory.presentation.main

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.presentation.components.common.SectionHeader
import com.naptune.lullabyandstory.presentation.components.shimmer.MainScreenLoadingSkeleton
import com.naptune.lullabyandstory.presentation.components.lullaby.LullabyItemOptimized
import com.naptune.lullabyandstory.presentation.components.story.StoryItemContainerNew
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.presentation.components.admob.SmoothBannerAdSection
import com.naptune.lullabyandstory.presentation.components.common.responsive.rememberScreenDimensionManager
import com.naptune.lullabyandstory.presentation.components.RewardVideoBottomSheet
import com.naptune.lullabyandstory.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToLullaby: () -> Unit = {},
    onNavigateToStoryScreen: () -> Unit = {},
    onNavigateToSleepSounds: () -> Unit = {},
    onNavigateToFavourite: () -> Unit = {},
    // ✅ NEW: Direct callback functions for navigation
    onNavigateToAudioPlayer: (LullabyDomainModel) -> Unit,
    onNavigateToStoryAudioPlayer: (StoryDomainModel) -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToStoryManager: (StoryDomainModel) -> Unit,
    // ✅ NEW: Dynamic content padding based on mini controller
    contentBottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val systemUiController = rememberSystemUiController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()

    // ✅ NEW: Reward Video Bottom Sheet state
    var showWatchAdSheet by remember { mutableStateOf(false) }
    var selectedLullabyForAd by remember { mutableStateOf<LullabyDomainModel?>(null) }
    var selectedStoryForAd by remember { mutableStateOf<StoryDomainModel?>(null) }
    val watchAdSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ✅ NEW: Get currently playing states from ViewModel
    val currentlyPlayingLullabyId: String? by viewModel.currentlyPlayingLullabyId.collectAsStateWithLifecycle()
    val currentlyPlayingStoryId: String? by viewModel.currentlyPlayingStoryId.collectAsStateWithLifecycle()

    // ✅ MVI FIX: Premium status now part of uiState (single source of truth)
    // Accessed via: (uiState as? MainUiState.Content)?.isPremium ?: false

    // ❌ REMOVED: Navigation events handling
    // val navigationEvents by viewModel.navigationEvents.collectAsState()


    // Get Activity context for rewarded ads
    val context = LocalContext.current
    val activity = context as Activity

    LaunchedEffect(Unit) {
        systemUiController.isSystemBarsVisible = true
        // Load rewarded ad when screen opens
        /*viewModel.handleIntent(MainIntent.LoadRewardedAd(AdMobDataSource.TEST_REWARDED_AD_UNIT_ID))
        
        Log.d("MainScreen", "🏠 MainScreen composed - Fetching home data...")
        // viewModel.handleIntent(MainIntent.FetchHomeData)

        // ✅ Initialize AdMob and load banner ad
        viewModel.handleIntent(MainIntent.InitializeAds)
        viewModel.handleIntent(
            MainIntent.LoadBannerAd(
                adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
            )
        )

        // ✅ DEBUG: Test story debugging
        viewModel.debugDataSources()*/
    }

    // ❌ REMOVED: Navigation events handling
    // LaunchedEffect(navigationEvents) { ... }

    // ✅ AnimatedContent for smooth crossfade transition between loading and content
    // To remove transition: Delete AnimatedContent wrapper and keep only the when statement
    AnimatedContent(
        targetState = uiState,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
            fadeOut(animationSpec = tween(300))
        },
        label = "main_screen_state_transition",
        // ✅ FIX: Only animate when state TYPE changes (Loading/Content/Error), not when Content data changes
        contentKey = { state ->
            when (state) {
                is MainUiState.Loading -> "loading"
                is MainUiState.Content -> "content"
                is MainUiState.Error -> "error"
            }
        }
    ) { currentState ->
        when (currentState) {
            is MainUiState.Loading -> {
                // ✅ NEW: Shimmer skeleton instead of loading indicator
                MainScreenLoadingSkeleton(
                    contentBottomPadding = contentBottomPadding
                )
            }

            is MainUiState.Content -> {
            MainContent(
                uiState = currentState,
                onNavigateToLullaby = onNavigateToLullaby,
                onNavigateToStoryScreen = onNavigateToStoryScreen,
                onNavigateToFavourite = onNavigateToFavourite,
                // ✅ NEW: Direct callback functions with analytics tracking
                onLullabyClick = { lullaby ->
                    viewModel.trackLullabySelected(lullaby)
                    onNavigateToAudioPlayer(lullaby)
                },
                onStoryClick = { story ->
                    viewModel.trackStorySelected(story)
                    onNavigateToStoryAudioPlayer(story)
                },
                onPageChanged = { page ->
                    viewModel.handleIntent(MainIntent.OnPageChanged(page))
                },
                onDownloadLullabyClick = { lullaby ->
                    viewModel.handleIntent(MainIntent.OnLullabyDownloadClick(lullaby))
                },
                onNavigateToStoryManager = onNavigateToStoryManager,
                // ✅ Pass dynamic padding
                contentBottomPadding = contentBottomPadding,
                // ✅ Pass network state for banner ad
                isNetworkAvailable = isNetworkAvailable,
                // ✅ NEW: Pass currently playing IDs
                currentlyPlayingLullabyId = currentlyPlayingLullabyId,
                currentlyPlayingStoryId = currentlyPlayingStoryId,
                // ✅ NEW: Rewarded ad callbacks - Show modal first
                onLullabyAdClick = { lullaby ->
                    Log.d("MainScreen", "🎵 AD button clicked for lullaby: ${lullaby.musicName}")
                    selectedLullabyForAd = lullaby
                    selectedStoryForAd = null
                    showWatchAdSheet = true
                },
                onStoryAdClick = { story ->
                    Log.d("MainScreen", "📚 AD button clicked for story: ${story.storyName}")
                    selectedStoryForAd = story
                    selectedLullabyForAd = null
                    showWatchAdSheet = true
                },
                // ✅ MVI FIX: Get premium status from state
                isPurchased = currentState.isPremium
            )
        }

            is MainUiState.Error -> {
                ErrorContent(
                    message = currentState.message,
                    onRetry = {
                        viewModel.handleIntent(MainIntent.FetchHomeData)
                    }
                )
            }
        }
    } // End AnimatedContent

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
                    Log.d("MainScreen", "🎵 Watch clicked - Showing rewarded ad for lullaby: ${lullaby.musicName}")
                    viewModel.handleIntent(
                        MainIntent.ShowRewardedAdForLullaby(
                            adUnitId = AdMobDataSource.TEST_REWARDED_AD_UNIT_ID,
                            activity = activity,
                            lullaby = lullaby
                        )
                    )
                }
                selectedStoryForAd?.let { story ->
                    Log.d("MainScreen", "📚 Watch clicked - Showing rewarded ad for story: ${story.storyName}")
                    viewModel.handleIntent(
                        MainIntent.ShowRewardedAdForStory(
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
private fun MainContent(
    uiState: MainUiState.Content,
    onNavigateToLullaby: () -> Unit,
    onNavigateToStoryScreen: () -> Unit,
    onNavigateToFavourite: () -> Unit,
    onLullabyClick: (LullabyDomainModel) -> Unit,
    onStoryClick: (StoryDomainModel) -> Unit,
    onPageChanged: (Int) -> Unit,
    onDownloadLullabyClick: (LullabyDomainModel) -> Unit,
    onNavigateToStoryManager: (StoryDomainModel) -> Unit,
    // ✅ NEW: Dynamic content padding
    contentBottomPadding: Dp = 0.dp,
    // ✅ NEW: Network state for banner ad
    isNetworkAvailable: Boolean,
    // ✅ NEW: Currently playing IDs for gradient border
    currentlyPlayingLullabyId: String? = null,
    currentlyPlayingStoryId: String? = null,
    // ✅ NEW: Rewarded ad callbacks
    onLullabyAdClick: ((LullabyDomainModel) -> Unit)? = null,
    onStoryAdClick: ((StoryDomainModel) -> Unit)? = null,
    isPurchased: Boolean = false

    ) {
    val dimensionManager = rememberScreenDimensionManager()

    // ✅ FIX: Remember scroll state to prevent scroll to top on recomposition
    val listState = rememberLazyListState()

    Column {

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = contentBottomPadding)
        ) {
            // ✅ Professional Full-Width Banner Ad - dynamic space allocation
            /*     item {
                     BannerAdComposable(
                         bannerAd = uiState.bannerAd
                     )
                 }*/

            /*  item {
                  Spacer(modifier = Modifier.height(0.dp)) // Top spacing
              }*/

            // Today's Pick Section
            item {
                // ✅ Reusable Smooth Banner Ad Component
                if(!isPurchased)
                {
                    SmoothBannerAdSection(
                        isNetworkAvailable = isNetworkAvailable,
                        adState = uiState.adState,
                        enableDebugLogging = true // Enable debug logging for MainScreen
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                TodaysPickSection(
                    lullabies = uiState.todaysPickLullabies,
                    story = uiState.todaysPickStory,
                    currentPage = uiState.currentTodaysPickPage,
                    onPageChanged = onPageChanged,
                    onLullabyClick = onLullabyClick,
                    onNavigateToStoryManager = onNavigateToStoryManager,
                    // ✅ NEW: Pass download callback and ui state
                    onDownloadLullabyClick = onDownloadLullabyClick,
                    uiState = uiState,
                    // ✅ Pass rewarded ad callbacks
                    onLullabyAdClick = onLullabyAdClick,
                    onStoryAdClick = onStoryAdClick,
                    // ✅ NEW: Pass currently playing IDs
                    currentlyPlayingLullabyId = currentlyPlayingLullabyId,
                    currentlyPlayingStoryId = currentlyPlayingStoryId,
                    isPurchased = isPurchased
                )
            }

            // Popular Lullaby Section
            item {
                PopularLullabySection(
                    uiState = uiState,
                    lullabies = uiState.popularLullabies,
                    onNavigateToLullaby = onNavigateToLullaby,
                    onLullabyClick = onLullabyClick,
                    onDownloadLullabyClick = onDownloadLullabyClick,
                    // ✅ Pass rewarded ad callback
                    onLullabyAdClick = onLullabyAdClick,
                    // ✅ NEW: Pass currently playing lullaby ID
                    currentlyPlayingLullabyId = currentlyPlayingLullabyId,
                    // ✅ FIX: Pass premium status
                    isPurchased = isPurchased
                )
            }

            // Popular Story Section
            item {
                PopularStorySection(
                    stories = uiState.popularStories,
                    onNavigateToStoryScreen = onNavigateToStoryScreen,
                    onNavigateToStoryManager = onNavigateToStoryManager, // ✅ For reading story
                    onStoryAudioClick = onStoryClick, // ✅ For playing story audio
                    // ✅ Pass rewarded ad callback
                    onStoryAdClick = onStoryAdClick,
                    // ✅ NEW: Pass currently playing story ID
                    currentlyPlayingStoryId = currentlyPlayingStoryId,
                    // ✅ NEW: Pass UI state for unlock status
                    uiState = uiState,
                    isPurchased = isPurchased
                )
            }

            // ✅ NEW: Favourites Section (max 4 items mixed lullabies + stories)
            if (uiState.favouriteLullabies.isNotEmpty() || uiState.favouriteStories.isNotEmpty()) {
                item {
                    FavouritesSection(
                        lullabies = uiState.favouriteLullabies,
                        stories = uiState.favouriteStories,
                        onNavigateToFavourite = onNavigateToFavourite,
                        onLullabyClick = onLullabyClick,
                        onStoryAudioClick = onStoryClick,
                        onNavigateToStoryManager = onNavigateToStoryManager,
                        onDownloadLullabyClick = onDownloadLullabyClick,
                        uiState = uiState,
                        currentlyPlayingLullabyId = currentlyPlayingLullabyId,
                        currentlyPlayingStoryId = currentlyPlayingStoryId,
                        onLullabyAdClick = onLullabyAdClick,
                        onStoryAdClick = onStoryAdClick,
                        // ✅ FIX: Pass premium status
                        isPremium = isPurchased
                    )
                }
            }

            item {
                //  Spacer(modifier = Modifier.height(16.dp)) // Bottom spacing for player
            }
        }
    }
}

@Composable
private fun TodaysPickSection(
    lullabies: List<LullabyDomainModel>,
    story: StoryDomainModel?,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    onLullabyClick: (LullabyDomainModel) -> Unit,
    onNavigateToStoryManager: (StoryDomainModel) -> Unit,
    // ✅ NEW: Download callback
    onDownloadLullabyClick: (LullabyDomainModel) -> Unit,
    // ✅ NEW: UI State for download tracking
    uiState: MainUiState.Content,
    // ✅ NEW: Currently playing IDs for gradient border
    currentlyPlayingLullabyId: String? = null,
    currentlyPlayingStoryId: String? = null,
    // ✅ NEW: Rewarded ad callbacks
    onLullabyAdClick: ((LullabyDomainModel) -> Unit)? = null,
    onStoryAdClick: ((StoryDomainModel) -> Unit)? = null,
    isPurchased: Boolean = false

    ) {
    val dimensionManager = rememberScreenDimensionManager()
    Column(
        modifier = Modifier
            .padding(horizontal = 0.dp)
            .zIndex(10f)
    ) {
        SectionHeader(
            title = stringResource(R.string.section_todays_pick),
            showSeeAll = false
        )

        // ViewPager with content
        val pagerState = rememberPagerState(
            initialPage = 0, // Always start from 0
            pageCount = { 2 }
        )
        val scope = rememberCoroutineScope()

        // 🔧 CUSTOMIZABLE TIMING
        val autoChangeInterval = 5000L // Auto change every 5 seconds
        val pauseAfterUserInteraction = 5000L // Pause 5 seconds after user swipe

        // ✅ PERFORMANCE FIX: Single LaunchedEffect to manage all auto-scroll logic
        LaunchedEffect(pagerState) {
            var lastInteractionTime = 0L
            var lastSettledPage = pagerState.settledPage

            // Monitor user interactions
            launch {
                snapshotFlow { pagerState.isScrollInProgress }
                    .collect { isScrolling ->
                        if (isScrolling) {
                            lastInteractionTime = System.currentTimeMillis()
                        }
                    }
            }

            // Monitor page changes from user swipes
            launch {
                snapshotFlow { pagerState.settledPage }
                    .collect { settledPage ->
                        if (settledPage != lastSettledPage) {
                            Log.d("TodaysPickSection", "👆 User swiped to page: $settledPage")
                            lastSettledPage = settledPage
                            onPageChanged(settledPage)
                            lastInteractionTime = System.currentTimeMillis()
                        }
                    }
            }

            // Sync with ViewModel state changes
            launch {
                snapshotFlow { currentPage }
                    .collect { vmPage ->
                        if (pagerState.currentPage != vmPage) {
                            Log.d("TodaysPickSection", "🔄 ViewModel page changed to: $vmPage")
                            pagerState.animateScrollToPage(
                                page = vmPage,
                                animationSpec = tween(durationMillis = 350)
                            )
                        }
                    }
            }

            // Auto-scroll loop
            while (true) {
                delay(autoChangeInterval)

                val timeSinceInteraction = System.currentTimeMillis() - lastInteractionTime
                if (timeSinceInteraction >= pauseAfterUserInteraction &&
                    !pagerState.isScrollInProgress) {
                    val nextPage = (pagerState.currentPage + 1) % 2
                    Log.d("TodaysPickSection", "⏰ Auto changing to page: $nextPage")
                    pagerState.animateScrollToPage(
                        page = nextPage,
                        animationSpec = tween(durationMillis = 500)
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> {
                    // Lullaby Page - Show 2 lullabies
                    if (lullabies.isNotEmpty()) {
                        TodaysPickLullabyPage(
                            lullabies = lullabies,
                            onLullabyClick = onLullabyClick,
                            onDownloadLullabyClick = onDownloadLullabyClick,
                            uiState = uiState,
                            onLullabyAdClick = onLullabyAdClick,
                            currentlyPlayingLullabyId = currentlyPlayingLullabyId,
                            isPurchased = isPurchased
                        )
                    }
                }

                1 -> {
                    // Story Page - Show 1 story
                    story?.let {
                        TodaysPickStoryPage(
                            story = it,
                            onStoryClick = onNavigateToStoryManager,
                            onStoryAdClick = onStoryAdClick,
                            currentlyPlayingStoryId = currentlyPlayingStoryId,
                            // ✅ NEW: Pass session-unlocked IDs
                            adUnlockedIds = uiState.adUnlockedIds,
                            isPurchased = isPurchased
                        )
                    }
                }
            }
        }

        // Page Indicators (Dots)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(2) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == pagerState.targetPage) 8.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.targetPage)
                                Color.White
                            else
                                Color.White.copy(alpha = 0.3f)
                        )
                )
                if (index < 1) {
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }
    }
}

@Composable
private fun TodaysPickLullabyPage(
    lullabies: List<LullabyDomainModel>,
    onLullabyClick: (LullabyDomainModel) -> Unit,
    onDownloadLullabyClick: (LullabyDomainModel) -> Unit,
    uiState: MainUiState.Content,
    // ✅ NEW: Currently playing lullaby ID for gradient border
    currentlyPlayingLullabyId: String? = null,
    onLullabyAdClick: ((LullabyDomainModel) -> Unit)? = null,
    isPurchased: Boolean = false

    ) {
    val dimensionManager = rememberScreenDimensionManager()
    // ✅ OPTIMIZED: Fixed 2 items using regular Row - Much smoother than LazyGrid
    Row(
        modifier = Modifier
            .fillMaxWidth().background(Color.Red.copy(alpha = 0.0f))
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        lullabies.take(2).forEach { lullaby ->
            key(lullaby.documentId) {
                // ✅ NEW: Check if lullaby is unlocked via ad
                val isUnlocked = lullaby.documentId in uiState.adUnlockedIds

                LullabyItemOptimized(
                    lullaby = lullaby,
                    downloadOnClick = onDownloadLullabyClick,
                    onPlayLullabyClick = onLullabyClick,
                    isDownloaded = lullaby.documentId in uiState.downloadedItems,
                    isDownloading = lullaby.documentId in uiState.downloadingItems,
                    downloadProgress = uiState.downloadProgress[lullaby.documentId] ?: 0,
                    isFavourite = lullaby.isFavourite,
                    modifier = Modifier.weight(1f),
                    onAdButtonClick = onLullabyAdClick,
                    isCurrentlyPlaying = currentlyPlayingLullabyId == lullaby.documentId,
                    // ✅ NEW: Pass unlock status
                    isUnlockedViaAd = isUnlocked,
                    // ✅ FIX: Pass premium status to hide badges for premium users
                    isPurchased = isPurchased
                )
            }
        }

        // Fill remaining space if less than 2 items
        repeat(2 - minOf(2, lullabies.size)) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TodaysPickStoryPage(
    story: StoryDomainModel,
    onStoryClick: (StoryDomainModel) -> Unit,
    // ✅ NEW: Currently playing story ID for gradient border
    currentlyPlayingStoryId: String? = null,
    onStoryAdClick: ((StoryDomainModel) -> Unit)? = null,
    // ✅ NEW: Session-unlocked item IDs via rewarded ads
    adUnlockedIds: Set<String> = emptySet(),
    isPurchased: Boolean = false
) {
    val dimensionManager = rememberScreenDimensionManager()
    // ✅ FIXED: Use StoryItemContainerNew directly to avoid nested LazyColumn

    // ✅ NEW: Check if story is unlocked via ad
    val isUnlocked = story.documentId in adUnlockedIds

    StoryItemContainerNew(
        item = story,
        onNavigateToStoryManager = onStoryClick,
        modifier = Modifier.padding(horizontal = 20.dp),
        onAdButtonClick = onStoryAdClick,
        isCurrentlyPlaying = currentlyPlayingStoryId == story.documentId,
        // ✅ NEW: Pass unlock status
        isUnlockedViaAd = isUnlocked,
        isPurchased = isPurchased
    )


}

@Composable
private fun PopularLullabySection(
    lullabies: List<LullabyDomainModel>,
    onNavigateToLullaby: () -> Unit,
    onLullabyClick: (LullabyDomainModel) -> Unit,
    uiState: MainUiState.Content,
    onDownloadLullabyClick: (LullabyDomainModel) -> Unit,
    // ✅ NEW: Currently playing lullaby ID for gradient border
    currentlyPlayingLullabyId: String? = null,
    // ✅ NEW: Rewarded ad callback
    onLullabyAdClick: ((LullabyDomainModel) -> Unit)? = null,
    // ✅ FIX: Is user a premium subscriber?
    isPurchased: Boolean = false
) {
    val dimensionManager = rememberScreenDimensionManager()

    Column(modifier = Modifier.zIndex(9f)) {

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // ✅ REUSABLE: SectionHeader component
            SectionHeader(
                title = stringResource(R.string.section_popular_lullaby),
                showSeeAll = true,
                onSeeAllClick = {
                    Log.d("MainScreen", "🎵 Navigate to Lullaby clicked")
                    onNavigateToLullaby()
                }
            )

            // ✅ OPTIMIZED: Fixed 4 items in 2x2 grid using regular Column/Row - Much smoother than LazyGrid
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // First row - 2 items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    lullabies.take(2).forEach { lullaby ->
                        key(lullaby.documentId) {
                            // ✅ NEW: Check if lullaby is unlocked via ad
                            val isUnlocked = lullaby.documentId in uiState.adUnlockedIds

                            LullabyItemOptimized(
                                lullaby = lullaby,
                                downloadOnClick = onDownloadLullabyClick,
                                onPlayLullabyClick = onLullabyClick,
                                isDownloaded = lullaby.documentId in uiState.downloadedItems,
                                isDownloading = lullaby.documentId in uiState.downloadingItems,
                                downloadProgress = uiState.downloadProgress[lullaby.documentId]
                                    ?: 0,
                                isFavourite = lullaby.isFavourite,
                                modifier = Modifier.weight(1f),
                                isCurrentlyPlaying = currentlyPlayingLullabyId == lullaby.documentId,
                                onAdButtonClick = onLullabyAdClick,
                                isUnlockedViaAd = isUnlocked,
                                isPurchased = isPurchased
                            )
                        }
                    }

                    // Fill remaining space if less than 2 items
                    repeat(2 - minOf(2, lullabies.size)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Second row - 2 items
                if (lullabies.size > 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        lullabies.drop(2).take(2).forEach { lullaby ->
                            key(lullaby.documentId) {
                                // ✅ NEW: Check if lullaby is unlocked via ad
                                val isUnlocked = lullaby.documentId in uiState.adUnlockedIds

                                LullabyItemOptimized(
                                    lullaby = lullaby,
                                    downloadOnClick = onDownloadLullabyClick,
                                    onPlayLullabyClick = onLullabyClick,
                                    isDownloaded = lullaby.documentId in uiState.downloadedItems,
                                    isDownloading = lullaby.documentId in uiState.downloadingItems,
                                    downloadProgress = uiState.downloadProgress[lullaby.documentId]
                                        ?: 0,
                                    isFavourite = lullaby.isFavourite,
                                    modifier = Modifier.weight(1f),
                                    isCurrentlyPlaying = currentlyPlayingLullabyId == lullaby.documentId,
                                    onAdButtonClick = onLullabyAdClick,
                                    isUnlockedViaAd = isUnlocked,
                                    isPurchased = isPurchased
                                )
                            }
                        }

                        // Fill remaining space if less than 4 total items
                        repeat(2 - minOf(2, lullabies.size - 2)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } // End Column with padding

      /*  SectionWithGlow(
            glowZIndex = -1f // Glow stays behind content within its Box
        ) {

        } // End SectionWithGlow*/
    }
}

@Composable
private fun PopularStorySection(
    stories: List<StoryDomainModel>,
    onNavigateToStoryScreen: () -> Unit,
    onNavigateToStoryManager: (StoryDomainModel) -> Unit, // ✅ For reading
    onStoryAudioClick: (StoryDomainModel) -> Unit, // ✅ For playing audio
    // ✅ NEW: Currently playing story ID for gradient border
    currentlyPlayingStoryId: String? = null,
    // ✅ NEW: Rewarded ad callback
    onStoryAdClick: ((StoryDomainModel) -> Unit)? = null,
    // ✅ NEW: UI state for unlock status
    uiState: MainUiState.Content,
    isPurchased: Boolean = false
) {
    val dimensionManager = rememberScreenDimensionManager()
    Column(modifier = Modifier.zIndex(8f)) {

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)

        )
        {
            // ✅ REUSABLE: SectionHeader component
            SectionHeader(
                title = stringResource(R.string.section_popular_story),
                showSeeAll = true,
                onSeeAllClick = {
                    Log.d("MainScreen", "📚 Navigate to Story clicked")
                    onNavigateToStoryScreen()
                }
            )

            // ✅ OPTIMIZED: Fixed 4 story items using regular Column - Much smoother than LazyColumn for fixed items
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                stories.take(4).forEach { story ->
                    key(story.documentId) {
                        // ✅ NEW: Check if story is unlocked via ad
                        val isUnlocked = story.documentId in uiState.adUnlockedIds

                        StoryItemContainerNew(
                            item = story,
                            onNavigateToStoryManager = onNavigateToStoryManager, // ✅ For reading
                            // onStoryAudioClick = onStoryAudioClick // ✅ For playing - if your component supports it
                            onAdButtonClick = onStoryAdClick,
                            isCurrentlyPlaying = currentlyPlayingStoryId == story.documentId,
                            isUnlockedViaAd = isUnlocked,
                            isPurchased = isPurchased
                        )
                    }
                }
            }
        } // End Column with padding

       /* SectionWithGlow(
            glowZIndex = -1f // Glow stays behind content within its Box
        ) {

        } // End SectionWithGlow*/
    }
}

/**
 * Favourites Section - Shows mixed lullabies and stories (max 4 items)
 * Displays lullabies in grid (2 per row) and stories vertically
 */
@Composable
private fun FavouritesSection(
    lullabies: List<LullabyDomainModel>,
    stories: List<StoryDomainModel>,
    onNavigateToFavourite: () -> Unit,
    onLullabyClick: (LullabyDomainModel) -> Unit,
    onStoryAudioClick: (StoryDomainModel) -> Unit,
    onNavigateToStoryManager: (StoryDomainModel) -> Unit,
    onDownloadLullabyClick: (LullabyDomainModel) -> Unit,
    uiState: MainUiState.Content,
    currentlyPlayingLullabyId: String? = null,
    currentlyPlayingStoryId: String? = null,
    onLullabyAdClick: ((LullabyDomainModel) -> Unit)? = null,
    onStoryAdClick: ((StoryDomainModel) -> Unit)? = null,
    // ✅ FIX: Is user a premium subscriber?
    isPremium: Boolean = false
) {
    val dimensionManager = rememberScreenDimensionManager()
    Column(modifier = Modifier.zIndex(8f)) {

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
        ) {
            // Section Header
            SectionHeader(
                title = stringResource(R.string.nav_favourite),
                showSeeAll = true,
                onSeeAllClick = {
                    Log.d("MainScreen", "⭐ Navigate to Favourites clicked")
                    onNavigateToFavourite()
                }
            )

        //    Spacer(modifier = Modifier.height(8.dp))

            // Lullabies Section (2 per row)
            if (lullabies.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    lullabies.take(2).forEach { lullaby ->
                        key(lullaby.documentId) {
                            // ✅ NEW: Check if lullaby is unlocked via ad
                            val isUnlocked = lullaby.documentId in uiState.adUnlockedIds

                            LullabyItemOptimized(
                                lullaby = lullaby,
                                downloadOnClick = onDownloadLullabyClick,
                                onPlayLullabyClick = onLullabyClick,
                                isDownloaded = lullaby.documentId in uiState.downloadedItems,
                                isDownloading = lullaby.documentId in uiState.downloadingItems,
                                downloadProgress = uiState.downloadProgress[lullaby.documentId] ?: 0,
                                isFavourite = lullaby.isFavourite,
                                modifier = Modifier.weight(1f),
                                onAdButtonClick = onLullabyAdClick,
                                isCurrentlyPlaying = currentlyPlayingLullabyId == lullaby.documentId,
                                isUnlockedViaAd = isUnlocked,
                                isPurchased = isPremium
                            )
                        }
                    }

                    // Fill remaining space if only 1 lullaby
                    if (lullabies.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Stories Section (vertical list)
            if (stories.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    stories.forEach { story ->
                        key(story.documentId) {
                            // ✅ NEW: Check if story is unlocked via ad
                            val isUnlocked = story.documentId in uiState.adUnlockedIds

                            StoryItemContainerNew(
                                item = story,
                                onNavigateToStoryManager = onNavigateToStoryManager,
                                onAdButtonClick = onStoryAdClick,
                                isCurrentlyPlaying = currentlyPlayingStoryId == story.documentId,
                                isUnlockedViaAd = isUnlocked,
                                isPurchased = isPremium
                            )
                        }
                    }
                }
            }
        } // End Column with padding
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    val dimensionManager = rememberScreenDimensionManager()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.error_loading_content),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

/**
 * Reusable circular glow effect component for sections
 * Creates a soft, blurred circular gradient behind content
 */
//@Composable
// ✅ PERFORMANCE FIX: Removed unused SectionWithGlow function (Issue #15)
// The function was never called - all usages are commented out
