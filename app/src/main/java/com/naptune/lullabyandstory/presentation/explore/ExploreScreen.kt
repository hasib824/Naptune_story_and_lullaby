package com.naptune.lullabyandstory.presentation.explore

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.presentation.components.admob.SmoothBannerAdSection
import com.naptune.lullabyandstory.presentation.components.common.LullabyAndStoryCategoryButtons
import com.naptune.lullabyandstory.presentation.components.lullaby.LullabyGrid
import com.naptune.lullabyandstory.presentation.components.story.MakeStoryList
import com.naptune.lullabyandstory.presentation.favourite.CategoryButton
import com.naptune.lullabyandstory.presentation.lullaby.LullabyUiState
import com.naptune.lullabyandstory.ui.theme.PrimaryColor
import com.naptune.lullabyandstory.ui.theme.PrimarySurfaceColor
import kotlinx.coroutines.launch

@Composable
fun ExploreScreen(
    onNavigateToAudioPlayer: (LullabyDomainModel) -> Unit,
    onNavigateToStoryManager: (StoryDomainModel) -> Unit,
    contentBottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as Activity

    // Get currently playing states
    val currentlyPlayingLullabyId by viewModel.currentlyPlayingLullabyId.collectAsState()
    val currentlyPlayingStoryId by viewModel.currentlyPlayingStoryId.collectAsState()

    // Initialize AdMob when screen is composed
    LaunchedEffect(Unit) {
        viewModel.handleIntent(ExploreIntent.InitializeAds)
        viewModel.handleIntent(
            ExploreIntent.LoadBannerAd(
                adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
            )
        )
        viewModel.handleIntent(ExploreIntent.LoadRewardedAd(AdMobDataSource.TEST_REWARDED_AD_UNIT_ID))
    }

    when (val currentState = uiState) {
        is ExploreUiState.IsLoading -> {
            LoadingScreen()
        }

        is ExploreUiState.Content -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // Banner Ad
                SmoothBannerAdSection(
                    isNetworkAvailable = isNetworkAvailable,
                    adState = currentState.adState,
                    loadingText = "Loading Explore Ad...",
                    enableDebugLogging = true
                )

                // Content with tabs
                ExploreContentScreen(
                    contentState = currentState,
                    currentlyPlayingLullabyId = currentlyPlayingLullabyId,
                    currentlyPlayingStoryId = currentlyPlayingStoryId,
                    onNavigateToAudioPlayer = onNavigateToAudioPlayer,
                    onNavigateToStoryManager = onNavigateToStoryManager,
                    onContentCategoryChange = { category ->
                        viewModel.handleIntent(ExploreIntent.ChangeContentCategory(category))
                    },
                    onFilterCategoryChange = { category ->
                        viewModel.handleIntent(ExploreIntent.ChangeFilterCategory(category))
                    },
                    onDownloadLullaby = { lullaby ->
                        viewModel.handleIntent(ExploreIntent.DownloadLullaby(lullaby))
                    },
                    onToggleLullabyFavourite = { lullabyId ->
                        viewModel.handleIntent(ExploreIntent.ToggleLullabyFavourite(lullabyId))
                    },
                    onToggleStoryFavourite = { storyId ->
                        viewModel.handleIntent(ExploreIntent.ToggleStoryFavourite(storyId))
                    },
                    contentBottomPadding = contentBottomPadding,
                    onLullabyAdClick = { lullaby ->
                        Log.d("ExploreScreen", "🎵 Rewarded ad clicked for lullaby: ${lullaby.musicName}")
                        viewModel.handleIntent(
                            ExploreIntent.ShowRewardedAdForLullaby(
                                adUnitId = AdMobDataSource.TEST_REWARDED_AD_UNIT_ID,
                                activity = activity,
                                lullaby = lullaby
                            )
                        )
                    },
                    onStoryAdClick = { story ->
                        Log.d("ExploreScreen", "📚 Rewarded ad clicked for story: ${story.storyName}")
                        viewModel.handleIntent(
                            ExploreIntent.ShowRewardedAdForStory(
                                adUnitId = AdMobDataSource.TEST_REWARDED_AD_UNIT_ID,
                                activity = activity,
                                story = story
                            )
                        )
                    }
                )
            }
        }

        is ExploreUiState.Error -> {
            ErrorScreen(
                message = currentState.message,
                onRetry = {
                    viewModel.handleIntent(ExploreIntent.LoadData)
                }
            )
        }
    }
}

@Composable
private fun ExploreContentScreen(
    contentState: ExploreUiState.Content,
    currentlyPlayingLullabyId: String?,
    currentlyPlayingStoryId: String?,
    onNavigateToAudioPlayer: (LullabyDomainModel) -> Unit,
    onNavigateToStoryManager: (StoryDomainModel) -> Unit,
    onContentCategoryChange: (ExploreContentCategory) -> Unit,
    onFilterCategoryChange: (ExploreFilterCategory) -> Unit,
    onDownloadLullaby: (LullabyDomainModel) -> Unit,
    onToggleLullabyFavourite: (String) -> Unit,
    onToggleStoryFavourite: (String) -> Unit,
    contentBottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onLullabyAdClick: ((LullabyDomainModel) -> Unit)? = null,
    onStoryAdClick: ((StoryDomainModel) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()

    // Main content pager (Lullaby/Story)
    val contentPagerState = rememberPagerState(
        initialPage = when (contentState.contentCategory) {
            ExploreContentCategory.LULLABY -> 0
            ExploreContentCategory.STORY -> 1
        },
        pageCount = { 2 }
    )

    // Sync pager with state changes
    LaunchedEffect(contentState.contentCategory) {
        val targetPage = when (contentState.contentCategory) {
            ExploreContentCategory.LULLABY -> 0
            ExploreContentCategory.STORY -> 1
        }
        if (contentPagerState.currentPage != targetPage) {
            contentPagerState.animateScrollToPage(targetPage)
        }
    }

    // Update ViewModel when user swipes main tabs
    LaunchedEffect(contentPagerState.settledPage) {
        val newCategory = when (contentPagerState.settledPage) {
            0 -> ExploreContentCategory.LULLABY
            1 -> ExploreContentCategory.STORY
            else -> ExploreContentCategory.LULLABY
        }
        if (contentState.contentCategory != newCategory) {
            onContentCategoryChange(newCategory)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        // Main Category Buttons (Lullaby | Story)
        ExploreCategoryButtons(
            currentPage = contentPagerState.currentPage,
            onCategoryChange = { page ->
                scope.launch {
                    contentPagerState.animateScrollToPage(page)
                }
            }
        )

        // HorizontalPager for swipeable content
        HorizontalPager(
            state = contentPagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    // Lullaby Content with sub-tabs
                    LullabyContent(
                        contentState = contentState,
                        currentlyPlayingId = currentlyPlayingLullabyId,
                        onFilterCategoryChange = onFilterCategoryChange,
                        onLullabyClick = onNavigateToAudioPlayer,
                        onDownloadLullaby = onDownloadLullaby,
                        onToggleFavourite = onToggleLullabyFavourite,
                        contentBottomPadding = contentBottomPadding,
                        onAdButtonClick = onLullabyAdClick
                    )
                }
                1 -> {
                    // Story Content with sub-tabs
                    StoryContent(
                        contentState = contentState,
                        currentlyPlayingId = currentlyPlayingStoryId,
                        onFilterCategoryChange = onFilterCategoryChange,
                        onStoryClick = onNavigateToStoryManager,
                        onToggleFavourite = onToggleStoryFavourite,
                        contentBottomPadding = contentBottomPadding,
                        onAdButtonClick = onStoryAdClick
                    )
                }
            }
        }
    }
}

@Composable
private fun LullabyContent(
    contentState: ExploreUiState.Content,
    currentlyPlayingId: String?,
    onFilterCategoryChange: (ExploreFilterCategory) -> Unit,
    onLullabyClick: (LullabyDomainModel) -> Unit,
    onDownloadLullaby: (LullabyDomainModel) -> Unit,
    onToggleFavourite: (String) -> Unit,
    contentBottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onAdButtonClick: ((LullabyDomainModel) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()

    // Sub-category pager (All/Popular/Free)
    val filterPagerState = rememberPagerState(
        initialPage = when (contentState.filterCategory) {
            ExploreFilterCategory.ALL -> 0
            ExploreFilterCategory.POPULAR -> 1
            ExploreFilterCategory.FREE -> 2
        },
        pageCount = { 3 }
    )

    // Sync pager with filter category changes
    LaunchedEffect(contentState.filterCategory) {
        val targetPage = when (contentState.filterCategory) {
            ExploreFilterCategory.ALL -> 0
            ExploreFilterCategory.POPULAR -> 1
            ExploreFilterCategory.FREE -> 2
        }
        if (filterPagerState.currentPage != targetPage) {
            filterPagerState.animateScrollToPage(targetPage)
        }
    }

    // Update ViewModel when user swipes filter tabs
    LaunchedEffect(filterPagerState.settledPage) {
        val newFilterCategory = when (filterPagerState.settledPage) {
            0 -> ExploreFilterCategory.ALL
            1 -> ExploreFilterCategory.POPULAR
            2 -> ExploreFilterCategory.FREE
            else -> ExploreFilterCategory.ALL
        }
        if (contentState.filterCategory != newFilterCategory) {
            onFilterCategoryChange(newFilterCategory)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Category Buttons (All/Popular/Free)
        LullabyAndStoryCategoryButtons(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            currentPage = filterPagerState.currentPage,
            onCategoryChange = { page ->
                scope.launch {
                    filterPagerState.animateScrollToPage(page)
                }
            }
        )

        // HorizontalPager for filter categories
        HorizontalPager(
            state = filterPagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val lullabies = when (page) {
                0 -> contentState.allLullabies
                1 -> contentState.popularLullabies
                2 -> contentState.freeLullabies
                else -> contentState.allLullabies
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LullabyGrid(
                    uiState = contentState.toLullabyUiState(),
                    lullabies = lullabies,
                    contentBottomPadding = contentBottomPadding,
                    downloadOnClick = onDownloadLullaby,
                    onLullabyClick = onLullabyClick,
                    currentlyPlayingId = currentlyPlayingId,
                    onAdButtonClick = onAdButtonClick
                )
            }
        }
    }
}

@Composable
private fun StoryContent(
    contentState: ExploreUiState.Content,
    currentlyPlayingId: String?,
    onFilterCategoryChange: (ExploreFilterCategory) -> Unit,
    onStoryClick: (StoryDomainModel) -> Unit,
    onToggleFavourite: (String) -> Unit,
    contentBottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onAdButtonClick: ((StoryDomainModel) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Sub-category pager (All/Popular/Free)
    val filterPagerState = rememberPagerState(
        initialPage = when (contentState.filterCategory) {
            ExploreFilterCategory.ALL -> 0
            ExploreFilterCategory.POPULAR -> 1
            ExploreFilterCategory.FREE -> 2
        },
        pageCount = { 3 }
    )

    // Sync pager with filter category changes
    LaunchedEffect(contentState.filterCategory) {
        val targetPage = when (contentState.filterCategory) {
            ExploreFilterCategory.ALL -> 0
            ExploreFilterCategory.POPULAR -> 1
            ExploreFilterCategory.FREE -> 2
        }
        if (filterPagerState.currentPage != targetPage) {
            filterPagerState.animateScrollToPage(targetPage)
        }
    }

    // Update ViewModel when user swipes filter tabs
    LaunchedEffect(filterPagerState.settledPage) {
        val newFilterCategory = when (filterPagerState.settledPage) {
            0 -> ExploreFilterCategory.ALL
            1 -> ExploreFilterCategory.POPULAR
            2 -> ExploreFilterCategory.FREE
            else -> ExploreFilterCategory.ALL
        }
        if (contentState.filterCategory != newFilterCategory) {
            onFilterCategoryChange(newFilterCategory)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Category Buttons (All/Popular/Free)
        LullabyAndStoryCategoryButtons(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            currentPage = filterPagerState.currentPage,
            onCategoryChange = { page ->
                scope.launch {
                    filterPagerState.animateScrollToPage(page)
                }
            }
        )

        // HorizontalPager for filter categories
        HorizontalPager(
            state = filterPagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val stories = when (page) {
                0 -> contentState.allStories
                1 -> contentState.popularStories
                2 -> contentState.freeStories
                else -> contentState.allStories
            }

            Box(modifier = Modifier.fillMaxSize()) {
                MakeStoryList(
                    data = stories,
                    onStoryItemClick = { story ->
                        Log.d("ExploreScreen", "📚 Story clicked: ${story.storyName}")
                        onStoryClick(story)
                    },
                    contentBottomPadding = contentBottomPadding,
                    isCollapsed = true,
                    isBottomSheetVisible = false,
                    scrollEnabled = true,
                    currentlyPlayingId = currentlyPlayingId,
                    onAdButtonClick = onAdButtonClick,
                    // TODO: Add adUnlockedIds support when ExploreViewModel integrates SessionUnlockManager
                    adUnlockedIds = emptySet()
                )
            }
        }
    }
}

@Composable
private fun ExploreCategoryButtons(
    currentPage: Int,
    onCategoryChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimarySurfaceColor)
            .clip(RoundedCornerShape(100.dp))
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CategoryButton(
            text = stringResource(R.string.nav_lullaby),
            isSelected = currentPage == 0,
            onClick = { onCategoryChange(0) },
            modifier = Modifier.wrapContentWidth()
        )

        CategoryButton(
            text = stringResource(R.string.nav_story),
            isSelected = currentPage == 1,
            onClick = { onCategoryChange(1) },
            modifier = Modifier.wrapContentWidth()
        )
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
                text = "Loading...",
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

            androidx.compose.material3.Button(
                onClick = onRetry,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
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

/**
 * Extension function to convert ExploreUiState.Content to LullabyUiState.Content
 * This allows us to reuse the existing LullabyGrid component
 */
private fun ExploreUiState.Content.toLullabyUiState(): LullabyUiState.Content {
    return LullabyUiState.Content(
        lullabies = this.allLullabies,
        filteredLullabies = this.filteredLullabies,
        popularLullabies = this.popularLullabies,
        freeLullabies = this.freeLullabies,
        downloadingItems = this.downloadingItems,
        downloadedItems = this.downloadedItems,
        downloadProgress = this.downloadProgress,
        adState = this.adState
    )
}
