package com.naptune.lullabyandstory.presentation.story.storyreader
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.presentation.components.admob.SmoothBannerAdSection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naptune.lullabyandstory.ui.theme.StoryManagerBackground
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.ui.theme.StoryFontColor
import com.naptune.lullabyandstory.ui.theme.Typography
import com.naptune.lullabyandstory.ui.theme.PrimaryColor
import com.naptune.lullabyandstory.ui.theme.PrimarySurfaceColor
import kotlinx.coroutines.flow.distinctUntilChanged

// ✅ Performance optimization: Single data class for all animation values
data class AnimationValues(
    val scrollProgress: Float,
    val translationY: Float,
    val toolbarHeight: Float,
    val alpha: Float,
    val smallFontSize: Float,
    val largeFontSize: Float
)

@Composable
fun StoryReaderScreen(
    story: StoryDomainModel?,
    viewModel: StoryReaderViewModel = hiltViewModel(),
    contentBottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onBackClick: () -> Unit = {}
) {



    // ✅ Handle null story case
    if (story == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.story_reader_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        return
    }

    // ✅ MVI state management with ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()
    // ✅ FIX: Collect premium status to hide ads
    val isPurchased by viewModel.isPurchased.collectAsStateWithLifecycle()

    // ✅ ARCHITECTURE FIX: Ad initialization moved to ViewModel init block
    // Ads are only initialized for free users in StoryReaderViewModel
    
    // ✅ Handle loading and error states
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // Handle error display (you can add SnackBar here if needed)
            viewModel.clearError()
        }
    }
    
    // ✅ Show loading state while font preferences are being loaded
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StoryManagerBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        return
    }

    // ✅ Maximum performance: GPU-only direct calculations with scroll direction detection
    val scrollState = rememberScrollState()
    
    // ✅ Track scroll direction and toolbar visibility state
    var lastScrollValue by remember { mutableFloatStateOf(0f) }
    var toolbarVisibility by remember { mutableFloatStateOf(1f) } // 0f = hidden, 1f = visible
    var isScrollingUp by remember { mutableStateOf(false) }

    // ✅ PERFORMANCE FIX: Use snapshotFlow instead of LaunchedEffect(scrollState.value) (Issue #14)
    LaunchedEffect(Unit) {
        snapshotFlow { scrollState.value }
            .distinctUntilChanged() // Only emit when value actually changes
            .collect { currentScroll ->
                val currentScrollFloat = currentScroll.toFloat()
                val scrollDelta = currentScrollFloat - lastScrollValue

                // Only update if significant change (>5 pixels)
                if (kotlin.math.abs(scrollDelta) > 5) {
                    when {
                        // Scrolling Up (hiding toolbar)
                        scrollDelta > 5f && toolbarVisibility > 0f -> {
                            isScrollingUp = false
                            toolbarVisibility = (toolbarVisibility - (scrollDelta / 200f)).coerceIn(0f, 1f)
                        }
                        // Scrolling down (showing toolbar)
                        scrollDelta < -5f && toolbarVisibility < 1f -> {
                            isScrollingUp = true
                            toolbarVisibility = (toolbarVisibility + (-scrollDelta / 150f)).coerceIn(0f, 1f)
                        }
                    }
                    lastScrollValue = currentScrollFloat
                }

                // At top, always show toolbar
                if (currentScrollFloat <= 20f) {
                    toolbarVisibility = 1f
                }
            }
    }
    
    // ✅ Animation calculations based on toolbar visibility instead of scroll position
    val animationState = remember {
        derivedStateOf {
            val progress = 1f - toolbarVisibility // Invert: 1f = hidden, 0f = visible
            val dividerProgress = if (scrollState.value <= 20f) 0f else progress
            
            // ✅ All calculations based on visibility state
            Triple(
                first = progress, // Main animation progress
                second = 1f - dividerProgress, // Divider alpha 
                third = AnimationValues(
                    scrollProgress = progress,
                    translationY = -80f * progress,
                    toolbarHeight = 80f * toolbarVisibility, // Direct visibility mapping
                    alpha = toolbarVisibility, // Direct visibility mapping
                    smallFontSize = 12f + (4f * toolbarVisibility), // 12-16sp range
                    largeFontSize = 14f + (4f * toolbarVisibility)   // 14-18sp range
                )
            )
        }
    }

    // ✅ Extract optimized values in single operation (no intermediate animations)
    val (mainProgress, dividerAlpha, animValues) = animationState.value
    
    // ✅ Direct GPU calculations - no Compose animation overhead
    val translationYy = animValues.translationY
    val toolbarHeight = animValues.toolbarHeight.dp
    val toolbarAlpha = animValues.alpha
    val dynamicSmallFontSize = animValues.smallFontSize.sp
    val dynamicLargeFontSize = animValues.largeFontSize.sp

    // ✅ Animated title visibility based on scroll
    val titleVisibility by animateFloatAsState(
        targetValue = if (scrollState.value > 100) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "title_visibility"
    )

    // ✅ Truncate story name to 20 characters with "..."
    val truncatedTitle = remember(story.storyName) {
        if (story.storyName.length > 20) {
            story.storyName.take(20) + "..."
        } else {
            story.storyName
        }
    }

    // ✅ Font size controls visibility state
    var showFontControls by remember { mutableStateOf(false) }

    // ✅ Track previous scroll value to detect scroll changes
    var previousScrollValue by remember { mutableIntStateOf(0) }

    // ✅ Close dropdown when user scrolls
    LaunchedEffect(scrollState.value) {
        if (showFontControls && scrollState.value != previousScrollValue) {
            // Scroll value changed, close the dropdown
            showFontControls = false
        }
        previousScrollValue = scrollState.value
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
        // ✅ Top spacing for custom app bar
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp) // Height of custom top app bar
        )

        // ✅ Scrollable Story Content with Full-Width Ad Support
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Text(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 0.dp),
                style = Typography.titleMedium.copy( fontWeight = FontWeight.Bold, fontSize = 24.sp),
                text = story.storyName, maxLines = 2)
            // Add some top spacing with margin
            Spacer(modifier = Modifier.height(16.dp))

            // Story Content with Smart Layout (handles margins internally)
            StoryContentWithInlineAd(
                story = story,
                uiState = uiState,
                isNetworkAvailable = isNetworkAvailable,
                modifier = Modifier.fillMaxWidth(),
                contentBottomPadding = contentBottomPadding,
                isPurchased = isPurchased
            )

            // Bottom spacing for better readability
            Spacer(modifier = Modifier.height(32.dp))
        }
        }

        // ✅ Dynamic Top App Bar Overlay
        StoryReaderTopAppBar(
            title = truncatedTitle,
            titleVisibility = titleVisibility,
            onBackClick = onBackClick,
            showFontControls = showFontControls,
            onToggleFontControls = { showFontControls = !showFontControls },
            uiState = uiState,
            onIncreaseFontSize = { viewModel.handleIntent(StoryReaderIntent.IncreaseFontSize) },
            onDecreaseFontSize = { viewModel.handleIntent(StoryReaderIntent.DecreaseFontSize) },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(10f)
        )
    }
}

/**
 * Custom Top App Bar for Story Reader with animated title and font controls dropdown
 */
@Composable
private fun StoryReaderTopAppBar(
    title: String,
    titleVisibility: Float,
    onBackClick: () -> Unit,
    showFontControls: Boolean,
    onToggleFontControls: () -> Unit,
    uiState: StoryReaderUiState,
    onIncreaseFontSize: () -> Unit,
    onDecreaseFontSize: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .statusBarsPadding()
            .background(PrimaryColor) // ✅ Use PrimaryColor background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .clickable { onBackClick() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu_back),
                    contentDescription = "Back",
                    tint = Color.Unspecified
                )
            }

            // Animated Title (center)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center, // ✅ Center align the title
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        alpha = titleVisibility
                        translationY = (1f - titleVisibility) * -20f // Slide from top
                    }
            )

            // Settings Icon with Dropdown Menu
            Box {
                // Settings Icon
                IconButton(
                    onClick = { onToggleFontControls() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_story_reader_settings),
                        contentDescription = "Settings",
                        tint = Color.Unspecified
                    )
                }

                // Font Controls Dropdown Menu
                DropdownMenu(
                    expanded = showFontControls,
                    onDismissRequest = { onToggleFontControls() },
                    shape = RoundedCornerShape(8.dp),
                    containerColor = PrimarySurfaceColor
                ) {
                    // Custom content for font controls
                    Row(
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decrease Button
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .width(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onDecreaseFontSize() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aa",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.White
                            )
                        }

                        // Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(Color.White.copy(alpha = 0.5f))
                        )

                        // Increase Button
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .width(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onIncreaseFontSize() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aa",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper function to get story content
 * Uses storyDescription as the main story text content
 */
private fun getStoryContent(story: StoryDomainModel): String {
    return story.storyDescription.ifEmpty {
        // Fallback content if storyDescription is empty
        "Story content is not available at the moment. Please try again later."
    }
}

/**
 * Helper function to split story text into paragraphs for inline ad placement
 * Returns Pair(firstParagraph, remainingText)
 */
private fun splitStoryForInlineAd(storyText: String): Pair<String, String> {
    // ONLY split by double newline (\n\n) to detect paragraphs
    val paragraphs = storyText.split("\n\n").filter { it.isNotBlank() }
    
    return if (paragraphs.size > 1) {
        // If multiple paragraphs exist, return first paragraph and rest
        val firstParagraph = paragraphs[0].trim()
        val remainingText = paragraphs.drop(1).joinToString("\n\n").trim()
        Pair(firstParagraph, remainingText)
    } else {
        // NO fallback - if no \n\n found, don't show ad
        Pair(storyText, "")
    }
}

/**
 * Composable that displays story content with inline banner ad after first paragraph
 */
@Composable
private fun StoryContentWithInlineAd(
    story: StoryDomainModel,
    uiState: StoryReaderUiState,
    isNetworkAvailable: Boolean,
    modifier: Modifier = Modifier,
    contentBottomPadding: Dp = 0.dp,
    isPurchased: Boolean = false
) {
    val storyText = getStoryContent(story)
    val (firstParagraph, remainingText) = remember(storyText) {
        splitStoryForInlineAd(storyText)
    }
    
    // Debug logging
    LaunchedEffect(storyText, remainingText, uiState.adState.bannerAd) {
        Log.d("StoryContentDebug", "📖 Story text length: ${storyText.length}")
        Log.d("StoryContentDebug", "📑 First paragraph: ${firstParagraph.take(50)}...")
        Log.d("StoryContentDebug", "📄 Remaining text: ${remainingText.take(50)}...")
        Log.d("StoryContentDebug", "📱 Has remaining text: ${remainingText.isNotEmpty()}")
        Log.d("StoryContentDebug", "🎯 Inline banner ad: ${uiState.adState.bannerAd}")
        Log.d("StoryContentDebug", "⏳ Loading inline ad: ${uiState.adState.isBannerLoading}")
        Log.d("StoryContentDebug", "🌐 Network available: $isNetworkAvailable")
    }
    
    Column(modifier = modifier) {
        // First Paragraph with text margin
        Text(
            text = firstParagraph,
            fontSize = uiState.fontSize,
            lineHeight = (uiState.fontSize.value * 1.5f).sp,
            style = MaterialTheme.typography.bodyLarge.copy( color = StoryFontColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp) // Text margin
        )
        
        // Only show inline ad if there's remaining text (multiple paragraphs)
        // ✅ FIX: Only show ads for free users
        if (remainingText.isNotEmpty()) {
            // Spacer before ad


            // ✅ Use SmoothBannerAdSection instead of custom implementation

            if(!isPurchased)
            {
                Spacer(modifier = Modifier.height(16.dp))
                SmoothBannerAdSection(
                    isNetworkAvailable = isNetworkAvailable,
                    adState = uiState.adState,
                    loadingText = "Loading Reading Ad...", // Custom text for story reading
                    enableDebugLogging = true,
                    bannerHeight = 150.dp, // ✅ Loading height for INLINE_ADAPTIVE_BANNER
                    useAutoHeight = true, // ✅ Use natural ad height when loaded
                    modifier = Modifier.fillMaxWidth() // Full screen width for inline placement
                )
                Spacer(modifier = Modifier.height(16.dp))
            }



            // Spacer after ad

            
            // Remaining Text with text margin
            Text(
                text = remainingText + remainingText,
                fontSize = uiState.fontSize,
                lineHeight = (uiState.fontSize.value * 1.5f).sp,
                style = MaterialTheme.typography.bodyLarge.copy( color = StoryFontColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = contentBottomPadding) // Text margin
            )
        }
    }
}
