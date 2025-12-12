package com.naptune.lullabyandstory.presentation.player

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import com.naptune.lullabyandstory.presentation.components.common.responsive.rememberScreenDimensionManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import com.naptune.lullabyandstory.presentation.components.common.CleanVolumeSlider
import kotlinx.coroutines.launch
import java.time.LocalTime
import com.naptune.lullabyandstory.presentation.player.timermodal.*
import com.naptune.lullabyandstory.presentation.components.admob.BannerAdComposable
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naptune.lullabyandstory.presentation.components.common.responsive.DeviceProfile
import com.naptune.lullabyandstory.presentation.components.common.responsive.*

// ✅ Import extension function for time formatting
import com.naptune.lullabyandstory.presentation.player.formatTime
import com.naptune.lullabyandstory.ui.theme.NeutralColor
import com.naptune.lullabyandstory.ui.theme.SecondaryColor

// ✅ PERFORMANCE: Pre-computed colors to avoid allocations in composition
private val ShadowColor = Color.Black.copy(alpha = 0.5f)
private val ButtonShadowEnabled = Color.Black.copy(alpha = 0.50f)
private val ButtonShadowDisabled = Color.Black.copy(alpha = 0.0f)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreenNew(
    item: AudioItem,
    isFavourite: Boolean = false,
    isFromStory: Boolean = true,
    playerState: PlayerState = PlayerState(),
    onBackClick: () -> Unit = {},
    onPlayPause: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onFavouriteClick: () -> Unit = {},
    onReadStory: () -> Unit = {},
    onTimerClick: () -> Unit = {},
    onVolumeChange: (Float) -> Unit = {},
    onSeek: (Float) -> Unit = {},
    appPreferences: AppPreferences?,
    // ✅ NEW: Story navigation state
    storyNavigationState: StoryNavigationState = StoryNavigationState(),
    // ✅ NEW: Toast callbacks for disabled buttons
    onPreviousDisabledClick: () -> Unit = {},
    onNextDisabledClick: () -> Unit = {},
    // ✅ NEW: Navigation to StoryReaderScreen callback
    onNavigateToStoryReader: (AudioItem) -> Unit = {},
    // ✅ NEW: ViewModel for ad management
    viewModel: AudioPlayerViewModel = hiltViewModel()
) {
    val dimensionManager = rememberScreenDimensionManager()
    val screenHeight = dimensionManager.screenHeightDp

    if (dimensionManager.deviceProfile == DeviceProfile.TABLET_XHDPI) {
        Log.d("densityDpi AudioPlayerScreenNew", "Tablet XHDPI detected")
    }

    println("densityDpi in audio $screenHeight Device profile : ${dimensionManager.deviceProfile}");

    // ✅ Collect UI state for ad management
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ✅ FIX: Collect premium status to hide ads
    val isPurchased by viewModel.isPurchased.collectAsStateWithLifecycle()

    // ✅ NEW: Collect timer countdown state (immediate updates)
    val timerCountdown by viewModel.timerCountdownState.collectAsStateWithLifecycle()

    // ✅ SMART STATE: Track loading intention to prevent icon flash
    var wasLoadingNewAudio by remember { mutableStateOf(false) }

    // ✅ UPDATE: Smart loading state tracking with delay
    LaunchedEffect(playerState.isLoading, playerState.currentTime, playerState.isPlaying) {
        if (playerState.isLoading && (playerState.currentTime == "0:00" || playerState.totalTime == "0:00")) {
            wasLoadingNewAudio = true
        } else if (!playerState.isLoading && wasLoadingNewAudio) {
            // ✅ SMART DELAY: Wait a moment for playback to stabilize before resetting
            kotlinx.coroutines.delay(300) // Allow time for actual playback state to be confirmed
            wasLoadingNewAudio = false
        }
    }

    val responsiveSizes = getAudioplayerScreenResponisiveSizes(dimensionManager)

    // ✅ Initialize AdMob when screen is composed
    LaunchedEffect(Unit) {


        viewModel.handleIntent(AudioPlayerIntent.InitializeAds)
        viewModel.handleIntent(
            AudioPlayerIntent.LoadBannerAd(
                adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                adSizeType = responsiveSizes.adSizeType
            )
        )
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Background Image with rounded bottom corners


        val (backgroundImagRef, nowPlayingTextRef, nowPlayingBackIconRef, categoryTitleRef, musicTitleRef,
            advRef, icFavRef, icReadStoryRef, favSpacerRef, icTimerRef, sliderRef, volumeIcRef, icNextRef, icPlayPauseRef, icPreviousRef) = createRefs()

        // Additional refs for progress bar and timer countdown
        val progressBarRef = createRef()
        val currentTimeRef = createRef()
        val totalTimeRef = createRef()
        val timerCountdownTextRef = createRef() // ✅ NEW: Timer countdown text reference


        // ✅ NEW: Animated background image with smooth crossfade transition
        AnimatedContent(
            targetState = item.imagePath,
            transitionSpec = {
                // ✨ Soothing crossfade animation
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = FastOutSlowInEasing
                    )
                ) togetherWith fadeOut(
                    animationSpec = tween(
                        durationMillis = 600,
                        easing = FastOutLinearInEasing
                    )
                )
            },
            label = "background_image_transition",
            modifier = Modifier.constrainAs(backgroundImagRef) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) { imagePath ->
            AsyncImage(
                model = imagePath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(responsiveSizes.imageSize)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .graphicsLayer {
                        // ✨ Subtle scale animation for depth
                        scaleX = 1.02f
                        scaleY = 1.02f
                    },
                alpha = 0.40f
            )
        }

        Text(
            modifier = Modifier
                .constrainAs(nowPlayingTextRef) {
                    top.linkTo(backgroundImagRef.top, margin = responsiveSizes.nowPlayingTopMargin)
                },
            text = stringResource(R.string.audio_now_playing),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = responsiveSizes.nowPlayingTextSize),
        )

        Box(
            modifier = Modifier
                .constrainAs(nowPlayingBackIconRef) {
                    top.linkTo(nowPlayingTextRef.top)
                    bottom.linkTo(nowPlayingTextRef.bottom)
                    start.linkTo(nowPlayingTextRef.end)
                }
                .padding(12.dp)
                .clip(CircleShape)
                .background(Color.Transparent, CircleShape)
                .clickable(
                    onClick = { onBackClick() }
                )

        ) {
            Icon(
                modifier = Modifier.then(
                    if (responsiveSizes.nowPlayingBackIconSize > 0.dp) {
                        Modifier.size(responsiveSizes.nowPlayingBackIconSize)
                    } else {
                        Modifier
                    }
                ),
                painter = painterResource(R.drawable.icback),
                contentDescription = "Back",
                tint = Color.White,
            )
        }

        createHorizontalChain(
            nowPlayingTextRef,
            nowPlayingBackIconRef,
            chainStyle = ChainStyle.Packed
        )


        // ✅ NEW: Animated category with subtle fade transition
        AnimatedContent(
            targetState = if (isFromStory) stringResource(R.string.nav_story) else stringResource(R.string.nav_lullaby),
            transitionSpec = {
                // ✨ Gentle fade transition
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 500,
                        delayMillis = 200, // Slight delay for staggered effect
                        easing = LinearOutSlowInEasing
                    )
                ) togetherWith fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutLinearInEasing
                    )
                )
            },
            label = "category_transition",
            modifier = Modifier.constrainAs(categoryTitleRef) {
                bottom.linkTo(musicTitleRef.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) { categoryText ->
            Text(
                text = categoryText,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = responsiveSizes.nowPlayingTextSize),
            )
        }

        // ✅ NEW: Animated story title with smooth slide transition
        AnimatedContent(
            targetState = if (isFromStory) item.storyName else item.musicName,
            transitionSpec = {
                // ✨ Elegant slide + fade animation with stagger delay (PERFORMANCE FIX)
                (slideInVertically(
                    animationSpec = tween(
                        durationMillis = 600,
                        delayMillis = 300, // ✅ Stagger after category animation
                        easing = FastOutSlowInEasing
                    )
                ) { height -> height / 3 } + fadeIn(
                    animationSpec = tween(
                        durationMillis = 700,
                        delayMillis = 300, // ✅ Stagger after category animation
                        easing = FastOutSlowInEasing
                    )
                )) togetherWith (slideOutVertically(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutLinearInEasing
                    )
                ) { height -> -height / 3 } + fadeOut(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutLinearInEasing
                    )
                ))
            },
            label = "story_title_transition",
            modifier = Modifier.constrainAs(musicTitleRef) {
                bottom.linkTo(backgroundImagRef.bottom, responsiveSizes.categoryTitleBottomMargin)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) { titleText ->
            // ✨ Animated scale and alpha for extra smoothness
            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "title_scale"
            )

            Text(
                modifier = Modifier
                    .padding(top = 0.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                text = "~$titleText",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = responsiveSizes.nowPlayingTextSize),
            )
        }

        // ✅ MREC Ad Part - Medium Rectangle (300x250)
        // ✅ FIX: Only show ads for free users
        if (!isPurchased) {
            Box(
                modifier = Modifier
                    .constrainAs(advRef) {
                        top.linkTo(backgroundImagRef.bottom, if (screenHeight < 800) 24.dp else 32.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth()
                    .height(250.dp))
            {
                BannerAdComposable(
                    bannerAd = uiState.bannerAd,
                    loadingContent = {
                        // ✅ AudioPlayer custom loading UI - Large MREC size
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp), // Larger for MREC
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.player_loading_ad),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.player_please_wait),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                )
            }
        }


        // Lower Part

        // Favourite Icon
        Box(
            modifier = Modifier
                .constrainAs(icFavRef) {
                    bottom.linkTo(volumeIcRef.top, responsiveSizes.favouritePortionMarginBottom)
                }
                .then(
                    if (responsiveSizes.favouritePortionIconsSize > 0.dp) {
                        Modifier.size(responsiveSizes.favouritePortionIconsSize)
                    } else {
                        Modifier
                    }
                )
                .clip(CircleShape)
                .background(Color.Transparent, CircleShape)
                .clickable(
                    onClick = { onFavouriteClick() }
                )
        ) {
            // ✅ Crossfade animation for smooth favourite icon transition
            Crossfade(
                targetState = isFavourite,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                ),
                label = "favourite_icon_crossfade"
            ) { isFav ->
                Icon(
                    painter = painterResource(
                        if (isFav) R.drawable.ic_fav_big_fill else R.drawable.ic_fav_big
                    ),
                    contentDescription = "Favourite",
                    tint = Color.Unspecified
                )
            }
        }

        // ✅ UPDATED: StoryManager Icon - Navigate to StoryReaderScreen
        Box(
            modifier = Modifier
                .constrainAs(icReadStoryRef) {
                    bottom.linkTo(volumeIcRef.top, responsiveSizes.favouritePortionMarginBottom)
                    start.linkTo(icFavRef.end, 0.dp)
                }
                .padding(start = if (!isFromStory) 12.dp else 0.dp)
                .clip(CircleShape)
                .background(Color.Transparent, CircleShape)
                .size(if (!isFromStory) 0.dp else 0.dp)
                .clickable(
                    onClick = {
                        if (isFromStory) {
                            // ✅ NEW: Navigate to StoryReaderScreen
                            onNavigateToStoryReader(item)
                        }
                    },
                )
        ) {
            if (isFromStory) {
                /*Icon(
                    painter = painterResource(R.drawable.ic_book),
                    contentDescription = "Read Story",
                    tint = Color.White,
                )*/
            }
        }

        // Timer Icon

        val bottomSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        var isSheetVisible by remember { mutableStateOf(false) }

        // ✅ Get saved timer values from ViewModel (AppPreferences)
        val savedLocalTime by viewModel.getSavedTimerTime()
            .collectAsStateWithLifecycle(LocalTime.of(0, 0))
        val savedIndex by viewModel.getSavedTimerIndex().collectAsStateWithLifecycle(-1)

        var isSavedTimerButtonClicked by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()


        SlideUpTimerModal(

            bottomSheetState,
            isSheetVisible = isSheetVisible,
            onDismiss = { isSheetVisible = false },
            content = {
                SmartTimerModalContent(onClose = {
                    scope.launch {
                        bottomSheetState.hide()
                        isSheetVisible = false
                    }
                }, onSetTimer = { index, time ->

                    scope.launch {
                        Log.e("Selected Time 10:", "$time, $index")
                        bottomSheetState.hide()
                        isSheetVisible = false

                        // ✅ Use ViewModel to handle timer settings and scheduling following MVI pattern
                        viewModel.handleIntent(
                            AudioPlayerIntent.ScheduleTimer(time = time, index = index)
                        )

                        isSavedTimerButtonClicked = true
                    }

                }, onStopTimer = {

                    scope.launch {
                        bottomSheetState.hide()
                        isSheetVisible = false
                        viewModel.handleIntent(
                            AudioPlayerIntent.StopTimerAlarm
                        )
                    }
                }, savedLocalTime = savedLocalTime, savedSelectedIndex = savedIndex, isTimerRunning = timerCountdown.isTimerActive, isFromStory = isFromStory)

            })

        Spacer(Modifier
            .constrainAs(favSpacerRef) { centerTo(volumeIcRef) }
            .width(if (!isFromStory) 12.dp else 24.dp))

        Box(
            modifier = Modifier
                .constrainAs(icTimerRef) {
                    bottom.linkTo(volumeIcRef.top, responsiveSizes.favouritePortionMarginBottom)
                    start.linkTo(icReadStoryRef.end)
                }
                .then(
                    if (responsiveSizes.favouritePortionIconsSize > 0.dp) {
                        Modifier.size(responsiveSizes.favouritePortionIconsSize)
                    } else {
                        Modifier
                    }
                )
                .clip(CircleShape)
                .background(Color.Transparent, CircleShape)
                .clickable(
                    onClick = { isSheetVisible = true   /*onTimerClick() */ }
                )
        ) {
            // ✅ Crossfade animation for smooth icon transition
            Crossfade(
                targetState = timerCountdown.isTimerActive,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                ),
                label = "timer_icon_crossfade"
            ) { isTimerActive ->
                Icon(
                    painter = painterResource(
                        if (isTimerActive) R.drawable.ic_timer_fill else R.drawable.ic_timer
                    ),
                    contentDescription = "Timer",
                    tint = Color.Unspecified,
                )
            }
        }

        // ✅ NEW: Timer Countdown Text with smooth fade animation
        AnimatedVisibility(
            visible = timerCountdown.isVisible,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutLinearInEasing
                )
            ),
            modifier = Modifier
                .constrainAs(timerCountdownTextRef) {
                    top.linkTo(icTimerRef.top)
                    bottom.linkTo(icTimerRef.bottom)
                    start.linkTo(icTimerRef.end, 16.dp) // 24dp right of timer icon
                }
        ) {
            // ✅ Conditional font: Nunito Regular for "End of story", SansSerif for countdown
            val isEndOfStoryText = timerCountdown.remainingTimeText == "End of story"

            Text(
                text = timerCountdown.remainingTimeText,
                color = Color.White.copy(alpha = 0.5f), // 50% opacity white
                fontSize = 16.sp,
                fontFamily = if (isEndOfStoryText) {
                    com.naptune.lullabyandstory.ui.theme.NunitoFamily // Nunito Regular for "End of story"
                } else {
                    androidx.compose.ui.text.font.FontFamily.SansSerif // SansSerif for countdown numbers
                },
                fontWeight = if (isEndOfStoryText) {
                    FontWeight.Normal // Nunito Regular
                } else {
                    null // Default weight for SansSerif
                }
            )
        }

        createHorizontalChain(
            icFavRef,
            icReadStoryRef,
            favSpacerRef,
            icTimerRef,
            chainStyle = ChainStyle.Packed
        )



        Icon(
            painter = painterResource(R.drawable.ic_volume),
            contentDescription = "Volume",
            tint = Color.White,
            modifier = Modifier
                .constrainAs(volumeIcRef) {
                    bottom.linkTo(icPlayPauseRef.top, responsiveSizes.volumePorionMarginBottom)
                }
                .padding(start = 48.dp, end = 8.dp)
        )

        // পুরাণো Slider এর জায়গায় এটা use করুন:
        CleanVolumeSlider(
            value = playerState.volume,
            onValueChange = onVolumeChange,
            modifier = Modifier
                .constrainAs(sliderRef) {
                    top.linkTo(volumeIcRef.top)
                    bottom.linkTo(volumeIcRef.bottom)
                    start.linkTo(volumeIcRef.end)
                    width = Dimension.fillToConstraints
                    end.linkTo(parent.end)
                }
                .padding(end = 48.dp)
        )



        if (isFromStory) {
            // ✅ UPDATED: Previous Button with enabled/disabled state and animation
            val canGoPrevious = storyNavigationState.canGoToPrevious

            // ✨ Subtle pulse animation when button becomes enabled
            val buttonScale by animateFloatAsState(
                targetValue = if (canGoPrevious) 1f else 0.9f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "previous_button_scale"
            )

            val buttonAlpha by animateFloatAsState(
                targetValue = if (canGoPrevious) 1f else 0.4f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                ),
                label = "previous_button_alpha"
            )

            Row(
                modifier = Modifier
                    .then(
                        if (responsiveSizes.nextPrevIconSize > 0.dp) {
                            Modifier.size(responsiveSizes.nextPrevIconSize)
                        } else {
                            Modifier
                        }
                    )
                    .constrainAs(icPreviousRef) {
                        top.linkTo(icPlayPauseRef.top)
                        bottom.linkTo(icPlayPauseRef.bottom)
                        end.linkTo(icPlayPauseRef.start)
                    }

                   /* .graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                        alpha = buttonAlpha
                    }*/
                    .shadow(
                        elevation = if (canGoPrevious) 4.dp else 0.dp,
                        shape = CircleShape,
                        spotColor = if (canGoPrevious) ButtonShadowEnabled else ButtonShadowDisabled
                    )
                    .clip(CircleShape)
                    .background(Color.Transparent, CircleShape)
                    .clickable {
                        if (canGoPrevious) {
                            onPrevious()
                        } else {
                            onPreviousDisabledClick()
                        }
                    },
            ) {
                Icon(
                    modifier = Modifier.alpha(if(canGoPrevious) 1f else 0.35f),
                    painter = painterResource(R.drawable.ic_prev),
                    contentDescription = "Previous",
                    tint =  Color.Unspecified ,
                )
            }
        }



        // ✅ OPTIMIZED: Performance-enhanced play/pause button with memory leak prevention
        Box(
            modifier = Modifier
                .constrainAs(icPlayPauseRef) {
                    bottom.linkTo(
                        if (isFromStory) progressBarRef.top else parent.bottom,
                        if (isFromStory) 40.dp else 60.dp
                    )
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(start = 24.dp, end = 24.dp)
                // ✅ GPU-OPTIMIZED: Use graphicsLayer for shadow instead of Modifier.shadow
                .graphicsLayer {
                    if (!playerState.isLoading) {
                        shadowElevation = 4.dp.toPx()
                        shape = CircleShape
                        clip = true
                        // ✅ PERFORMANCE: Pre-computed shadow color (zero allocations)
                        spotShadowColor = ShadowColor
                    } else {
                        shadowElevation = 0f
                    }
                }
                .clip(CircleShape)
                .background(Color.Transparent, CircleShape)
                // ✅ OPTIMIZED: Use indication with ripple for better touch feedback
                .clickable(
                    enabled = !playerState.isLoading,
                    onClick = onPlayPause,
                    // ✅ PERFORMANCE: Remove default indication to avoid extra layers
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {

            // ✅ SMART CROSSFADE: Prevent flash with sophisticated state tracking
            Crossfade(
                targetState = when {
                    // ✅ CASE 1: Currently loading new audio → predict pause (will auto-play)
                    wasLoadingNewAudio && playerState.isLoading -> true

                    // ✅ CASE 2: Just finished loading new audio → stay with pause prediction briefly
                    wasLoadingNewAudio && !playerState.isLoading -> true

                    // ✅ CASE 3: Normal play/pause states → use actual state
                    else -> playerState.isPlaying
                },
                animationSpec = tween(
                    durationMillis = 250, // ✅ FASTER: Reduced from 300ms for snappier feel
                    easing = FastOutSlowInEasing
                ),
                label = "play_pause_crossfade" // ✅ SHORTER: Optimized label
            ) { isPlaying ->
                // ✅ PERFORMANCE: Pre-compute values outside composition
                val iconResource = if (!isPlaying) R.drawable.ic_play else R.drawable.ic_pause
                val contentDesc = if (isPlaying) "Pause" else "Play"

                Icon(
                    painter = painterResource(iconResource),
                    modifier = Modifier
                        // ✅ SMART VISIBILITY: Prevent blink during play/pause transitions
                        .graphicsLayer {
                            // ✅ FIXED: Only hide icon for real loading (new audio), not play/pause
                            val shouldHideIcon = playerState.isLoading &&
                                (playerState.currentTime.isEmpty() || playerState.currentTime.startsWith("0:00") ||
                                 playerState.totalTime.isEmpty() || playerState.totalTime.startsWith("0:00"))
                            alpha = if (!shouldHideIcon) 1f else 0f
                            // ✅ PERFORMANCE: Hardware-accelerated alpha
                            renderEffect = null // Ensure no extra effects
                        }
                        .then(
                            if (responsiveSizes.playPauseIconSize > 0.dp) {
                                Modifier.size(responsiveSizes.playPauseIconSize)
                            } else {
                                Modifier
                            }
                        ),
                    contentDescription = contentDesc,
                    tint = Color.Unspecified,
                )
            }
            // ✅ SMART LOADING: Only show for real loading, not play/pause transitions
            AnimatedVisibility(
                visible = playerState.isLoading &&
                    (playerState.currentTime.isEmpty() || playerState.currentTime.startsWith("0:00") ||
                     playerState.totalTime.isEmpty() || playerState.totalTime.startsWith("0:00")),
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 200, // ✅ FAST: Quick appearance
                        easing = LinearOutSlowInEasing
                    )
                ),
                exit = fadeOut(
                    animationSpec = tween(
                        durationMillis = 150, // ✅ FASTER: Quick disappearance
                        easing = FastOutLinearInEasing
                    )
                )
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        // ✅ PERFORMANCE: Use graphicsLayer for better hardware acceleration
                        .graphicsLayer {
                            renderEffect = null // No extra effects
                        },
                    color = Color.White,
                    strokeWidth = 3.dp,
                    // ✅ OPTIMIZATION: Reduce segments for better performance
                    trackColor = Color.Transparent
                )
            }


            /* if (!playerState.isLoading) {


            } else {

            }*/
        }

        if (isFromStory) {
            // ✅ UPDATED: Next Button with enabled/disabled state and animation
            val canGoNext = storyNavigationState.canGoToNext

            // ✨ Subtle pulse animation when button becomes enabled
            val buttonScale by animateFloatAsState(
                targetValue = if (canGoNext) 1f else 0.9f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "next_button_scale"
            )

            val buttonAlpha by animateFloatAsState(
                targetValue = if (canGoNext) 1f else 0.4f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                ),
                label = "next_button_alpha"
            )

            Box(
                modifier = Modifier
                    .constrainAs(icNextRef) {
                        top.linkTo(icPlayPauseRef.top)
                        bottom.linkTo(icPlayPauseRef.bottom)
                        start.linkTo(icPlayPauseRef.end)
                    }
                    .then(
                        if (responsiveSizes.nextPrevIconSize > 0.dp) {
                            Modifier.size(responsiveSizes.nextPrevIconSize)
                        } else {
                            Modifier
                        }
                    )
                  /*  .graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                        alpha = buttonAlpha
                    }*/
                    .shadow(
                        elevation = if (canGoNext) 4.dp else 0.dp,
                        shape = CircleShape,
                        spotColor = if (canGoNext) ButtonShadowEnabled else ButtonShadowDisabled
                    )
                    .clip(CircleShape)
                    .background(Color.Transparent, CircleShape)
                    .clickable {
                        if (canGoNext) {
                            onNext()
                        } else {
                            onNextDisabledClick()
                        }
                    }
            ) {
                Icon(
                    modifier = Modifier.alpha(if(canGoNext) 1f else 0.35f),
                    painter = painterResource(R.drawable.ic_next),
                    contentDescription = "Next",
                    tint =  Color.Unspecified,
                )
            }
        }

        // Progress Bar Section (only for stories)
        if (isFromStory) {
            // Current Time Text
            Text(
                text = playerState.currentTime + "m",
                color = NeutralColor,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = responsiveSizes.pregressAndTotalDurationFontSize
                ),
                modifier = Modifier
                    .constrainAs(currentTimeRef) {
                        bottom.linkTo(progressBarRef.top, 6.dp)
                        start.linkTo(parent.start, 8.dp)
                    }
            )

            // Total Duration Text
            Text(
                text = if (item.story_listen_time_in_millis > 0) {
                    val formattedTime = item.story_listen_time_in_millis.formatTime()
                    Log.d(
                        "AudioPlayerScreenNew",
                        "🎵 Using DB duration: ${item.story_listen_time_in_millis}ms = ${formattedTime}m"
                    )
                    "${formattedTime}m"
                } else {
                    Log.d(
                        "AudioPlayerScreenNew",
                        "🎵 Using MediaPlayer duration: ${playerState.totalTime}m"
                    )
                    "${playerState.totalTime}m" // Fallback to MediaPlayer
                },
                color = NeutralColor,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = responsiveSizes.pregressAndTotalDurationFontSize
                ),
                modifier = Modifier
                    .constrainAs(totalTimeRef) {
                        bottom.linkTo(progressBarRef.top, 6.dp)
                        end.linkTo(parent.end, 8.dp)
                    }
            )

            // Custom Progress Bar (fully flat with no rounded edges)
            Box(
                modifier = Modifier
                    .constrainAs(progressBarRef) {
                        bottom.linkTo(parent.bottom, 0.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth()
                    .height(4.dp)
                    .drawBehind {
                        // Background track
                        drawRect(
                            color = NeutralColor,
                            size = size
                        )

                        // Progress fill (perfectly flat edges)
                        val progressWidth = size.width * playerState.progress
                        if (progressWidth > 0) {
                            drawRect(
                                color = SecondaryColor,
                                size = androidx.compose.ui.geometry.Size(
                                    width = progressWidth,
                                    height = size.height
                                )
                            )
                        }
                    }
            )
        }

    }
}