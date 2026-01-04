package com.naptune.lullabyandstory.presentation.navigation

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.naptune.lullabyandstory.presentation.components.common.NaptuneGradientBackground
import com.naptune.lullabyandstory.presentation.components.NaptuneTopAppBar
import com.naptune.lullabyandstory.presentation.debug.DebugScreen
import com.naptune.lullabyandstory.presentation.favourite.FavouriteScreen
import com.naptune.lullabyandstory.presentation.lullaby.LullabyScreen
import com.naptune.lullabyandstory.presentation.main.MainScreen
import com.naptune.lullabyandstory.presentation.player.bottomsheet.AudioPlayerBottomSheetContent
import com.naptune.lullabyandstory.presentation.player.bottomsheet.GlobalAudioPlayerManager
import com.naptune.lullabyandstory.presentation.player.bottomsheet.MiniAudioControllerContainer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.gson.Gson
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.data.billing.BillingManager
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.presentation.components.common.BackTopAppBar
import com.naptune.lullabyandstory.presentation.components.common.SetStatusBarColor
import com.naptune.lullabyandstory.presentation.components.common.responsive.getProfileScreenResponsiveStyles
import com.naptune.lullabyandstory.presentation.components.common.responsive.rememberScreenDimensionManager
import com.naptune.lullabyandstory.presentation.language.LanguageViewModel
import com.naptune.lullabyandstory.presentation.premium.PremiumScreen
import com.naptune.lullabyandstory.presentation.profile.ProfileScreen
import com.naptune.lullabyandstory.presentation.settings.SettingsScreen
import com.naptune.lullabyandstory.presentation.sleepsounds.SleepSoundsScreen
import com.naptune.lullabyandstory.presentation.splash.SplashScreen
import com.naptune.lullabyandstory.presentation.story.StoryScreen
import com.naptune.lullabyandstory.presentation.story.storymanager.StoryManagerScreen
import com.naptune.lullabyandstory.presentation.story.storyreader.StoryReaderScreen
import kotlin.text.ifEmpty

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaptuneNavigation(
    navController: NavHostController = rememberNavController(),
    startFromNotification: Boolean = false,
    shouldShowSplash: Boolean = true,// ✅ NEW: Control splash screen (separate from language first launch),
    musicControllerVM: MusicControllerAccessViewModel = hiltViewModel(),
    billingManager: BillingManager
) {
    val globalAudioPlayerManager: GlobalAudioPlayerManager = remember { GlobalAudioPlayerManager() }
    
    // ✅ Get MusicController using dedicated ViewModel
    val musicControllerVM: MusicControllerAccessViewModel = hiltViewModel()
    
    // ✅ Register MusicController for state sync (fixes notification sync issue)
    LaunchedEffect(Unit) {
        globalAudioPlayerManager.registerMusicController(musicControllerVM.musicController)
    }

    val isPurchased  by  billingManager.isPurchased.collectAsState()

    var isStoryReaderScreen = navController.currentBackStackEntryAsState().value?.destination?.route == Screen.StoryReader.route
    var isPremiumScreen = navController.currentBackStackEntryAsState().value?.destination?.route == Screen.Premium.route


    Log.e("AudioPlayerScreenContainer", "On Naptune Navigation : " + startFromNotification)
    var startFromNotificationCopy by remember { mutableStateOf(startFromNotification) }

    // ✅ Observe global audio player state
    val isAudioPlayerVisible by globalAudioPlayerManager.isVisible.collectAsState()
    val audioPlayerState by globalAudioPlayerManager.bottomSheetState.collectAsState()

    // ✅ Alpha animation state for 100ms fadeout
    var bottomSheetAlpha by remember { mutableStateOf(1f) }

    // ✅ Animated alpha for smooth 100ms transition
    val animatedBottomSheetAlpha by animateFloatAsState(
        targetValue = bottomSheetAlpha,
        animationSpec = tween(durationMillis = 50), // Match your 100ms delay
        label = "bottom_sheet_alpha"
    )

    // ✅ Coroutine scope for fade-then-navigate logic
    val scope = rememberCoroutineScope()


    // Apply gradient to entire navigation - no blink!
    NaptuneGradientBackground(isStoryReaderScreen, isPremiumScreen) {
        Box {
            NavHost(
                navController = navController,
                // ✅ Show splash only if not shown before AND not from notification
                startDestination = if (shouldShowSplash && !startFromNotification) Screen.Splash.route else Screen.Main.route
            ) {
                // Splash Screen (no gradient)
                composable(Screen.Splash.route) {
                    // Reset to transparent background for splash
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White) // White background for splash
                    ) {
                        SplashScreen(
                            onNavigateToNext = {
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.Splash.route) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }
                }

                // Main content with top bar and bottom navigation
                composable(Screen.Main.route) {
                    MainLayout(
                        navController = navController,
                        currentScreen = Screen.Main.route,
                        isPurchased = isPurchased,
                        globalAudioPlayerManager = globalAudioPlayerManager,
                        isBottomSheetVisible = isAudioPlayerVisible,
                        content = { contentBottomPadding ->
                            // ✅ Clean callback-based navigation
                            MainScreen(
                                onNavigateToLullaby = {
                                    navController.navigate(Screen.Lullaby.route)
                                },
                                onNavigateToStoryScreen = {
                                    navController.navigate(Screen.Story.route)
                                },
                                onNavigateToSleepSounds = {
                                    navController.navigate(Screen.SleepSounds.route)
                                },
                                onNavigateToFavourite = {
                                    navController.navigate(Screen.Favourite.route)
                                },
                                // ✅ NEW: Direct navigation callbacks via GlobalAudioPlayerManager
                                onNavigateToAudioPlayer = { lullaby ->
                                    globalAudioPlayerManager.showAudioPlayer(
                                        audioId = lullaby.id,
                                        isFromStory = false,
                                        musicPath = lullaby.musicPath,
                                        musicName = lullaby.musicName,
                                        imagePath = lullaby.imagePath,
                                        documentId = lullaby.documentId,
                                        musicLocalPath = lullaby.musicLocalPath
                                    )
                                },
                                onNavigateToStoryAudioPlayer = { story ->
                                    globalAudioPlayerManager.showAudioPlayer(
                                        audioId = story.id.ifEmpty { "story_${System.currentTimeMillis()}" },
                                        isFromStory = true,
                                        musicPath = story.storyAudioPath,
                                        musicName = story.storyName,
                                        imagePath = story.imagePath,
                                        documentId = story.documentId,
                                        musicLocalPath = null,
                                        story_listen_time_in_millis = story.story_listen_time_in_millis
                                    )
                                },
                                onNavigateToStoryManager = { storyItem ->
                                    val route = Screen.StoryManager.createJsonRoute(
                                        storyItem
                                    )
                                    navController.navigate(route)
                                },
                                // ✅ NEW: Pass dynamic content padding
                                contentBottomPadding = contentBottomPadding
                            )
                        },
                    )
                }

                // ✅ NEW: Explore Screen with Lullaby/Story tabs
     /*           composable(Screen.Explore.route) {
                    MainLayout(
                        navController = navController,
                        currentScreen = Screen.Explore.route,
                        isPurchased = isPurchased,
                        globalAudioPlayerManager = globalAudioPlayerManager,
                        isBottomSheetVisible = isAudioPlayerVisible,
                        content = { contentBottomPadding ->
                            com.naptune.lullabyandstory.presentation.explore.ExploreScreen(
                                onNavigateToAudioPlayer = { lullaby ->
                                    globalAudioPlayerManager.showAudioPlayer(
                                        audioId = lullaby.id,
                                        isFromStory = false,
                                        musicPath = lullaby.musicPath,
                                        musicName = lullaby.musicName,
                                        imagePath = lullaby.imagePath,
                                        documentId = lullaby.documentId,
                                        musicLocalPath = lullaby.musicLocalPath
                                    )
                                },
                                onNavigateToStoryManager = { storyItem ->
                                    val route = Screen.StoryManager.createRoute(
                                        storyId = storyItem.id,
                                        storyName = storyItem.storyName,
                                        storyDescription = storyItem.storyDescription,
                                        imagePath = storyItem.imagePath,
                                        documentId = storyItem.documentId,
                                        storyAudioPath = storyItem.storyAudioPath,
                                        storyLength = storyItem.story_reading_time
                                    )
                                    navController.navigate(route)
                                },
                                contentBottomPadding = contentBottomPadding
                            )
                        }
                    )
                }*/

                composable(Screen.Favourite.route) {
                    MainLayout(
                        navController = navController,
                        currentScreen = Screen.Favourite.route,
                        isPurchased = isPurchased,
                        globalAudioPlayerManager = globalAudioPlayerManager,
                        isBottomSheetVisible = isAudioPlayerVisible,
                        backTopAppbarTitle = stringResource(R.string.nav_favourite), // ✅ Add title
                        shouldShowBottomNav = false, // ✅ Hide bottom nav when opened from Profile
                        content = { contentBottomPadding ->
                            FavouriteScreen(
                                // ✅ NEW: Direct navigation callbacks via GlobalAudioPlayerManager
                                onNavigateToAudioPlayer = { lullaby ->
                                    globalAudioPlayerManager.showAudioPlayer(
                                        audioId = lullaby.id,
                                        isFromStory = false,
                                        musicPath = lullaby.musicPath,
                                        musicName = lullaby.musicName,
                                        imagePath = lullaby.imagePath,
                                        documentId = lullaby.documentId,
                                        musicLocalPath = lullaby.musicLocalPath
                                    )
                                },
                                onNavigateToStoryAudioPlayer = { story ->
                                    globalAudioPlayerManager.showAudioPlayer(
                                        audioId = story.id.ifEmpty { "story_${System.currentTimeMillis()}" },
                                        isFromStory = true,
                                        musicPath = story.storyAudioPath,
                                        musicName = story.storyName,
                                        imagePath = story.imagePath,
                                        documentId = story.documentId,
                                        musicLocalPath = null,
                                        story_listen_time_in_millis = story.story_listen_time_in_millis
                                    )
                                },
                                // ✅ NEW: Story manager navigation (same pattern as MainScreen)
                                onNavigateToStoryManager = { storyItem ->

                                    /*
                                    val route = Screen.StoryManager.createRoute(
                                        storyId = storyItem.id,
                                        storyName = storyItem.storyName,
                                        storyDescription = storyItem.storyDescription,
                                        imagePath = storyItem.imagePath,
                                        documentId = storyItem.documentId,
                                        storyAudioPath = storyItem.storyAudioPath,
                                        storyLength = storyItem.story_reading_time
                                    )
                                    */

                                    val route = Screen.StoryManager.createJsonRoute(storyItem)
                                    navController.navigate(route)
                                },
                                // ✅ NEW: Pass dynamic content padding
                                contentBottomPadding = contentBottomPadding
                            )
                        },
                    )
                }

                composable(Screen.Profile.route) {
                    var forceRecomposition by remember { mutableStateOf(0) }

                    MainLayout(
                        navController = navController,
                        currentScreen = Screen.Profile.route,
                        isPurchased = isPurchased,
                        isitProfileScreen = true,
                        globalAudioPlayerManager = globalAudioPlayerManager,
                        isBottomSheetVisible = isAudioPlayerVisible,
                        forceRecomposition = forceRecomposition
                    ) { contentBottomPadding ->
                        ProfileScreen(
                            contentBottomPadding = contentBottomPadding,
                            globalAudioPlayerManager = globalAudioPlayerManager,
                            onLanguageChanged = {
                                // Force bottom nav recomposition
                                forceRecomposition++
                            },
                            onNavigateToSettings = {
                                navController.navigate(Screen.Settings.route)
                            },
                            onNavigateToFavourite = {
                                // ✅ Navigate to Favourite screen without bottom nav
                                navController.navigate(Screen.Favourite.route)
                            }
                        )
                    }
                }

                // ✅ NEW: Debug Screen
                composable(Screen.Debug.route) {
                    MainLayout(
                        navController = navController,
                        isPurchased = isPurchased,
                        currentScreen = Screen.Debug.route,
                        globalAudioPlayerManager = globalAudioPlayerManager,
                        isBottomSheetVisible = isAudioPlayerVisible,
                        content = { contentBottomPadding ->
                            DebugScreen()
                        },
                    )
                }

                // ✅ NEW: Settings Screen
                composable(Screen.Settings.route) {
                    MainLayout(
                        navController = navController,
                        currentScreen = Screen.Settings.route,
                        isPurchased = isPurchased,
                        globalAudioPlayerManager = globalAudioPlayerManager,
                        isBottomSheetVisible = isAudioPlayerVisible,
                        backTopAppbarTitle = stringResource(R.string.profile_settings_title),
                        shouldShowBottomNav = false,
                        content = { contentBottomPadding ->
                            SettingsScreen(
                                contentBottomPadding = contentBottomPadding
                            )
                        },
                    )
                }

                // Content screens with back navigation
                composable(Screen.Lullaby.route) {


                    MainLayout(
                        navController = navController,
                        currentScreen = Screen.Lullaby.route,
                        isPurchased = isPurchased,
                        globalAudioPlayerManager = globalAudioPlayerManager,
                        isBottomSheetVisible = isAudioPlayerVisible,
                        backTopAppbarTitle = null,
                        shouldShowBottomNav = true, // ✅ Show bottom nav when navigating to Lullaby
                        content = { contentBottomPadding ->
                            LullabyScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                    true
                                },
                                onLullabyClick = { lullaby ->
                                    // ✅ Show audio player via GlobalAudioPlayerManager
                                    Log.d(
                                        "LullabyScreen",
                                        "🎵 Opening audio player for: ${lullaby.musicName}"
                                    )

                                    globalAudioPlayerManager.showAudioPlayer(
                                        audioId = lullaby.id,
                                        isFromStory = false,
                                        musicPath = lullaby.musicPath,
                                        musicName = lullaby.musicName,
                                        imagePath = lullaby.imagePath,
                                        documentId = lullaby.documentId,
                                        musicLocalPath = lullaby.musicLocalPath
                                    )
                                },
                                contentBottomPadding = contentBottomPadding
                            )
                        },
                    )

                    // }
                }


                composable(Screen.Story.route) {
                    MainLayout(
                        navController = navController,
                        currentScreen = Screen.Story.route,
                        isPurchased = isPurchased,
                        globalAudioPlayerManager = globalAudioPlayerManager,
                        isBottomSheetVisible = isAudioPlayerVisible,
                        backTopAppbarTitle = null,
                        shouldShowBottomNav = true, // ✅ Show bottom nav when navigating to Story
                        content = { contentBottomPadding ->
                            StoryScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                navController = navController,
                                onStroyItemClick = { storyItem ->

                                   /* val route = Screen.StoryManager.createRoute(
                                        storyId = storyItem.id,
                                        storyName = storyItem.storyName,
                                        storyDescription = storyItem.storyDescription,
                                        imagePath = storyItem.imagePath,
                                        documentId = storyItem.documentId,
                                        storyAudioPath = storyItem.storyAudioPath,
                                        storyLength = storyItem.story_reading_time,
                                    )*/

                                    val route = Screen.StoryManager.createJsonRoute(storyItem)
                                    navController.navigate(route)
                                },
                                contentBottomPadding = contentBottomPadding
                            )
                        },
                    )
                }


                composable(
                    Screen.StoryManager.route, arguments = listOf(
                        navArgument("storyJson") { type = NavType.StringType },
                       /* navArgument("storyName") { type = NavType.StringType },
                        navArgument("storyDescription") { type = NavType.StringType },
                        navArgument("imagePath") { type = NavType.StringType },
                        navArgument("documentId") { type = NavType.StringType },
                        navArgument("storyAudioPath") { type = NavType.StringType },
                        navArgument("storyLength") { type = NavType.StringType },*/
                    )
                ) { backStackEntry ->
                   /* val storyId = backStackEntry.arguments?.getString("storyId") ?: "1"
                    val storyName = backStackEntry.arguments?.getString("storyName") ?: ""
                    val storyDescription =
                        backStackEntry.arguments?.getString("storyDescription") ?: ""
                    val imagePath = backStackEntry.arguments?.getString("imagePath") ?: ""
                    val documentId = backStackEntry.arguments?.getString("documentId") ?: ""
                    val storyAudioPath = backStackEntry.arguments?.getString("storyAudioPath") ?: ""
                    val storyLength = backStackEntry.arguments?.getString("storyLength") ?: ""


                     val currentStory = StoryDomainModel(
                     id = java.net.URLDecoder.decode(storyId, "UTF-8"),
                     storyName = java.net.URLDecoder.decode(storyName, "UTF-8"),
                     storyDescription = java.net.URLDecoder.decode(storyDescription, "UTF-8"),
                     imagePath = java.net.URLDecoder.decode(imagePath, "UTF-8"),
                     storyAudioPath = java.net.URLDecoder.decode(storyAudioPath, "UTF-8"),
                     documentId = java.net.URLDecoder.decode(documentId, "UTF-8"),
                     story_reading_time = java.net.URLDecoder.decode(storyLength, "UTF-8"),
                     )*/

                    val currentStory = backStackEntry.arguments?.getString("storyJson")?.let { json ->
                       Gson().fromJson(json, StoryDomainModel::class.java)
                    }

                    SetStatusBarColor(
                        color = Color.Transparent,
                    )

                    MainLayout(
                        navController = navController,
                        currentScreen = Screen.Story.route,
                        globalAudioPlayerManager = globalAudioPlayerManager,
                        isBottomSheetVisible = isAudioPlayerVisible,
                        shouldShowBottomNav = false,
                        showTopAppBar = false,
                        content = {
                            StoryManagerScreen(
                                storyDomainModel = currentStory,
                                onBackClick = {
                                    navController.popBackStack()
                                    true
                                },
                                onReadStoryClick = { storyItem ->
                                    // ✅ Navigate to story reader screen
                                   /* val route = Screen.StoryReader.createRoute(
                                        storyId = storyItem.id,
                                        storyName = storyItem.storyName,
                                        storyDescription = storyItem.storyDescription,
                                        imagePath = storyItem.imagePath,
                                        documentId = storyItem.documentId,
                                        isFavourite = storyItem.isFavourite
                                    )*/

                                    val route = Screen.StoryReader.createJsonRoute(storyItem)
                                    navController.navigate(route)

                                },
                                onPlayStoryClick = { storyItem ->
                                    if (storyItem != null) {
                                        // ✅ Show audio player via GlobalAudioPlayerManager
                                        Log.d(
                                            "StoryManagerScreen",
                                            "🎵 Opening audio player for story: ${storyItem.storyName}"
                                        )

                                        globalAudioPlayerManager.showAudioPlayer(
                                            audioId = storyItem.id.ifEmpty { "story_${System.currentTimeMillis()}" },
                                            isFromStory = true,
                                            musicPath = storyItem.storyAudioPath,
                                            musicName = storyItem.storyName,
                                            imagePath = storyItem.imagePath,
                                            documentId = storyItem.documentId,
                                            musicLocalPath = null,
                                            story_listen_time_in_millis = storyItem.story_listen_time_in_millis
                                        )
                                    } else {
                                        Log.e("StoryItem", "StoryItem is null")
                                    }
                                }
                            )
                        },
                    )
                }

                composable(
                    Screen.StoryReader.route,
                    arguments = listOf(
                        navArgument("storyJson") { type = NavType.StringType },
                       /* navArgument("storyName") { type = NavType.StringType },
                        navArgument("storyDescription") { type = NavType.StringType },
                        navArgument("imagePath") { type = NavType.StringType },
                        navArgument("documentId") { type = NavType.StringType },
                        navArgument("isFavourite") { type = NavType.BoolType }*/
                    )
                ) { backStackEntry ->

                    val storyDoamainModel = backStackEntry.arguments?.getString("storyJson")?.let {
                        json ->
                        Gson().fromJson(json, StoryDomainModel::class.java)
                    }
                   /* val storyId = backStackEntry.arguments?.getString("storyId") ?: ""
                    val storyName = backStackEntry.arguments?.getString("storyName") ?: ""
                    val storyDescription =
                        backStackEntry.arguments?.getString("storyDescription") ?: ""
                    val imagePath = backStackEntry.arguments?.getString("imagePath") ?: ""
                    val documentId = backStackEntry.arguments?.getString("documentId") ?: ""
                    val isFavourite = backStackEntry.arguments?.getBoolean("isFavourite") ?: false*/

                    // ✅ Create StoryDomainModel from URL parameters
                    val storyFromUrl = storyDoamainModel

                    MainLayout(
                        navController = navController,
                        currentScreen = Screen.StoryReader.route,
                        globalAudioPlayerManager = globalAudioPlayerManager,
                        isBottomSheetVisible = isAudioPlayerVisible,
                        shouldShowBottomNav = false,
                        showTopAppBar = false, // ✅ Hide default top app bar for custom one
                        content = { contentBottomPadding ->
                            SetStatusBarColor(
                                color = Color.Transparent,
                                darkIcons = false
                            )

                            StoryReaderScreen(
                                story = storyFromUrl,  // ✅ Direct from URL parameters
                                contentBottomPadding = contentBottomPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        },
                    )
                }



                composable(Screen.SleepSounds.route) {
                    MainLayout(
                        navController = navController,
                        currentScreen = Screen.SleepSounds.route,
                        globalAudioPlayerManager = globalAudioPlayerManager,
                        isBottomSheetVisible = isAudioPlayerVisible,
                        content = { contentBottomPadding ->
                            SleepSoundsScreen(
                                // ✅ NEW: Pass dynamic content padding
                                contentBottomPadding = contentBottomPadding
                            )
                        },
                    )
                }

                // ✅ REMOVED: AudioPlayer route - now handled by global bottom sheet

                // Premium screen (full screen, no bottom nav)
                composable(Screen.Premium.route) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent, // Transparent to show gradient
                        topBar = {
                         /*   NaptuneTopAppBar(
                                onPremiumClick = {
                                    // Already on premium screen
                                }
                            )*/
                        }
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize()
                        ) {
                            PremiumScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                } // End of Premium composable
            } // End of NavHost
            
            // ✅ Global Audio Player Bottom Sheet
            if (isAudioPlayerVisible && audioPlayerState != null) {
                val currentState = audioPlayerState!!
                
                // ✅ Force status bar to stay light with DisposableEffect
                val view = LocalView.current
                DisposableEffect(isAudioPlayerVisible) {
                    Log.d("StatusBarDebug", "🟡 DisposableEffect: Bottom sheet opened")
                    
                    val activity = view.context as? ComponentActivity
                    val window = activity?.window
                    val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
                    
                    // Set light status bar immediately
                    insetsController?.isAppearanceLightStatusBars = false
                    Log.d("StatusBarDebug", "🟢 Forced light status bar on open")
                    
                    // Keep forcing it every frame to override Material 3
                    val runnable = object : Runnable {
                        override fun run() {
                            insetsController?.isAppearanceLightStatusBars = false
                            view.post(this)
                        }
                    }
                    view.post(runnable)
                    
                    onDispose {
                        Log.d("StatusBarDebug", "🔴 DisposableEffect: Bottom sheet closed")
                        view.removeCallbacks(runnable)
                        // Restore original status bar when bottom sheet closes
                        insetsController?.isAppearanceLightStatusBars = false
                    }
                }
                
                ModalBottomSheet(
                    onDismissRequest = {
                        globalAudioPlayerManager.hideAudioPlayer()
                    },
                    dragHandle = null, // No drag handle for fullscreen experience
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(animatedBottomSheetAlpha), // ✅ Apply 100ms fade animation
                    sheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true // Force fullscreen expansion
                    ),
                    containerColor = Color.Transparent, // Transparent to show gradient
                    contentColor = Color.White
                ) {
                    // Apply gradient background to the bottom sheet content
                    NaptuneGradientBackground {

                        AudioPlayerBottomSheetContent(
                            state = currentState.copy(
                                onNavigateToStoryReader = { story ->
                                    // ✅ Simple approach: Navigate first, then hide after delay
                                 /*   val route = Screen.StoryReader.createRoute(
                                        storyId = story.id,
                                        storyName = story.storyName,
                                        storyDescription = story.storyDescription,
                                        imagePath = story.imagePath,
                                        documentId = story.documentId,
                                        isFavourite = story.isFavourite
                                    )

                                    navController.navigate(route)*/

                                    // ✅ Fade out animation + hide after 100ms
                                    scope.launch {
                                        // Start fade out immediately after navigation
                                        delay(75)
                                        bottomSheetAlpha = 0f

                                        delay(50)
                                        // Wait for 100ms fade animation to complete
                                        // Hide bottom sheet after fade is done
                                        globalAudioPlayerManager.hideAudioPlayer()
                                        
                                        // Reset alpha for next time
                                        bottomSheetAlpha = 1f
                                    }
                                }
                            ),
                            onbackClick = { globalAudioPlayerManager.hideAudioPlayer() },
                            globalAudioPlayerManager = globalAudioPlayerManager
                        )
                    }
                }
            }
            
        } // End of Box
    } // End of NaptuneGradientBackground
} // End of NaptuneNavigation function

/*@Composable
private fun SubLayout(
    navController: NavHostController,
    currentScreen: String,
    globalAudioPlayerManager: GlobalAudioPlayerManager? = null,
    isBottomSheetVisible: Boolean = false,
    title: String,
    content: @Composable (contentBottomPadding: androidx.compose.ui.unit.Dp) -> Unit,

) {

    val context = LocalContext.current
    val appPreferences = remember { AppPreferences(context) }
    val coroutineScope = rememberCoroutineScope()
    // ✅ Observe mini controller visibility for dynamic padding
    val isAudioRunning by globalAudioPlayerManager?.isAudioRunning?.collectAsState() ?: remember { mutableStateOf(false) }
    val currentAudioInfo by globalAudioPlayerManager?.currentAudioInfo?.collectAsState() ?: remember { mutableStateOf(null) }

    // Calculate if mini controller should be shown
    val shouldShowMiniController = isAudioRunning && !isBottomSheetVisible && currentAudioInfo != null

    // ✅ FIXED: Show controller when audio is ACTIVE (playing OR paused) but bottom sheet is hidden
    val hasActiveAudio by globalAudioPlayerManager?.hasActiveAudio?.collectAsState() ?: remember { mutableStateOf(false) }
    val shouldShow = hasActiveAudio && !isBottomSheetVisible && currentAudioInfo != null

    // ✅ Dynamic content padding: 80dp when mini controller visible, 0dp when hidden
    val contentBottomPadding by animateDpAsState(
        targetValue = if (shouldShow) 66.dp else 0.dp,
        animationSpec = tween(260),
        label = "content_padding"
    )
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Transparent to show gradient
        topBar = {
            BackTopAppBar(
                onBackClick = {
                    navController.popBackStack()
                },
                title = title,
                currentScreen = currentScreen
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            content(contentBottomPadding)
            MiniController(globalAudioPlayerManager, isBottomSheetVisible, Modifier.align(Alignment.BottomCenter).padding())
        }
    }
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainLayout(
    navController: NavHostController,
    currentScreen: String="",
    isPurchased: Boolean = false,
    globalAudioPlayerManager: GlobalAudioPlayerManager? = null,
    isBottomSheetVisible: Boolean = false,
    backTopAppbarTitle: String? = null,
    shouldShowBottomNav: Boolean = true,
    showTopAppBar: Boolean = true,
    isitProfileScreen: Boolean= false,
    forceRecomposition: Int = 0,
    content: @Composable (contentBottomPadding: Dp) -> Unit,


    ) {
    // Get AppPreferences through Hilt injection using LocalContext
    val context = LocalContext.current
    val appPreferences = remember { AppPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    // Observe language change for bottom nav recomposition
    val languageViewModel: LanguageViewModel = hiltViewModel()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    // ✅ Observe mini controller visibility for dynamic padding
    val isAudioRunning by globalAudioPlayerManager?.isAudioRunning?.collectAsState() ?: remember { mutableStateOf(false) }
    val currentAudioInfo by globalAudioPlayerManager?.currentAudioInfo?.collectAsState() ?: remember { mutableStateOf(null) }
    
    // Calculate if mini controller should be shown
    val shouldShowMiniController = isAudioRunning && !isBottomSheetVisible && currentAudioInfo != null

    // ✅ FIXED: Show controller when audio is ACTIVE (playing OR paused) but bottom sheet is hidden
    val hasActiveAudio by globalAudioPlayerManager?.hasActiveAudio?.collectAsState() ?: remember { mutableStateOf(false) }
    val shouldShow = hasActiveAudio && !isBottomSheetVisible && currentAudioInfo != null
    
    // ✅ Dynamic content padding: 80dp when mini controller visible, 0dp when hidden
    val contentBottomPadding by animateDpAsState(
        targetValue = if (shouldShow) 66.dp else 0.dp,
        animationSpec = tween(260),
        label = "content_padding"
    )

    val screenDimensionManager = rememberScreenDimensionManager()
    val profileScreenResponsiveSizes = getProfileScreenResponsiveStyles(screenDimensionManager)

    if(showTopAppBar)
    {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent, // Transparent to show gradient
            topBar = {
                if(showTopAppBar ) // && !isitProfileScreen
                {
                    if(backTopAppbarTitle!=null)
                    {
                        BackTopAppBar(
                            onBackClick = {
                                navController.popBackStack()
                            },
                            title = backTopAppbarTitle,
                            currentScreen = currentScreen
                        )
                    }
                    else{
                        NaptuneTopAppBar(
                            onPremiumClick = {
                                navController.navigate(Screen.Premium.route)
                            },
                            currentScreen = currentScreen,
                            isPurchased = isPurchased
                        )
                    }

                }
                else if(isitProfileScreen)
                {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.nav_profile),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = profileScreenResponsiveSizes.pageTitleFontSize
                                ),
                                modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }

            },
            bottomBar = {
                if(shouldShowBottomNav)
                {
                    // Force recomposition when forceRecomposition changes
                    key(forceRecomposition) {
                        NaptuneBottomNavigation(navController = navController)
                    }
                }
            }
        ) { paddingValues ->

            // Save bottom padding to AppPreferences
            Box(
                modifier = Modifier
                    .then(
                        if (showTopAppBar) Modifier.padding(top = paddingValues.calculateTopPadding(), bottom = paddingValues.calculateBottomPadding(), start = 0.dp, end = 0.dp)
                        else Modifier.padding( bottom = paddingValues.calculateBottomPadding())
                    )
                    .fillMaxSize()
            ) {

                // ✅ Content with dynamic bottom padding for mini controller
                content(contentBottomPadding)


                MiniController(
                    globalAudioPlayerManager,
                    isBottomSheetVisible,
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
                )

            }
        }
    }
    else
    {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            content(contentBottomPadding)
            val windowInsets = WindowInsets.systemBars
            val bottomPadding = with(LocalDensity.current) {
                val toDp = windowInsets.getBottom(this).toDp()
                toDp
            }
            MiniController(globalAudioPlayerManager, isBottomSheetVisible, Modifier.align(Alignment.BottomCenter).padding(bottom = bottomPadding+4.dp))
        }
    }

}

@Composable
fun MiniController(
    globalAudioPlayerManager: GlobalAudioPlayerManager?,
    isBottomSheetVisible: Boolean,
    modifier: Modifier
)
{
    if (globalAudioPlayerManager != null) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                  // 2dp above bottom nav

            ) {
                MiniAudioControllerContainer(
                    globalAudioPlayerManager = globalAudioPlayerManager,
                    isBottomSheetVisible = isBottomSheetVisible,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
    }
}

