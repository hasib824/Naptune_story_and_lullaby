package com.naptune.lullabyandstory.presentation.main

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import com.naptune.lullabyandstory.data.manager.AdManager
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.domain.data.DownloadLullabyResult
import com.naptune.lullabyandstory.domain.model.ContentInfo
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.domain.usecase.lullaby.DownloaLullabyUsecase
import com.naptune.lullabyandstory.domain.usecase.lullaby.FetchLullabiesUseCase
import com.naptune.lullabyandstory.domain.usecase.lullaby.GetFavouriteLullabiesUseCase
import com.naptune.lullabyandstory.domain.usecase.story.FetchStoriesUsecase
import com.naptune.lullabyandstory.domain.usecase.story.GetFavouriteStoriesUseCase
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.utils.InternetConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import com.naptune.lullabyandstory.presentation.player.service.MusicController
import com.naptune.lullabyandstory.utils.LanguageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * ViewModel for Main/Home screen.
 * REFACTORED: Now follows Single Responsibility Principle (SRP).
 * Ad management logic delegated to unified AdManager.
 *
 * Responsibilities:
 * - Lullaby and Story data fetching and management
 * - Download management
 * - Home screen state coordination
 * - Music playback coordination
 *
 * Ad management delegated to: AdManager (shared across all ViewModels)
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fetchLullabiesUseCase: FetchLullabiesUseCase,
    private val fetchStoriesUseCase: FetchStoriesUsecase,
    private val getFavouriteLullabiesUseCase: GetFavouriteLullabiesUseCase,
    private val getFavouriteStoriesUseCase: GetFavouriteStoriesUseCase,
    private val downloaLullabyUsecase: DownloaLullabyUsecase,
    private val internetConnectionManager: InternetConnectionManager,
    // ✅ SRP FIX: Single unified ad manager instead of 6 ad use cases
    private val adManager: AdManager,
    // ✅ Session unlock manager for UI state observation
    private val sessionUnlockManager: com.naptune.lullabyandstory.data.manager.SessionUnlockManager,
    private val appPreferences: AppPreferences,
    private val musicController: MusicController,
    val languageManager: LanguageManager,
    // ✅ Analytics
    private val analyticsHelper: com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper,
    // ✅ Billing - Premium status and purchase management
    private val billingManager: com.naptune.lullabyandstory.data.billing.BillingManager
) : ViewModel() {

    // ✅ Flag to prevent concurrent data fetching - Initialize before init block
    private val _isFetchingData = MutableStateFlow(false)

    private val _uiState = MutableStateFlow<MainUiState>(
        MainUiState.Loading
    )

    // ✅ ViewModel's single source of truth for premium status
    private val _isPremium = MutableStateFlow(false)

    init {
        Log.d("MainViewModel", "🚀 ViewModel initialized")



        // ✅ Start monitoring network changes for automatic data sync
        monitorNetworkForDataSync()

        // ✅ BEST: Fetch data immediately for faster UI (Option 3)
        fetchHomeData()

        // ✅ Single place that manages premium status and ads
        managePremiumStatusAndAds()

        // ✅ Track screen view Analytics
        trackScreenView()


    }

    // ✅ Expose network state for UI
    val isNetworkAvailable: StateFlow<Boolean> = internetConnectionManager.isNetworkAvailable

    // ✅ MVI FIX: Combine base state + session unlocks + billing status + ad state into single state
    val uiState: StateFlow<MainUiState> = combine(
        _uiState.trackChanges("BaseState"),
        sessionUnlockManager.unlockedItems.trackChanges("UnlockedIds"),
        _isPremium.trackChanges("IsPremium"),  // ✅ Use local premium state
        adManager.adState.trackChanges("AdState")  // ✅ Get ad state from manager
    ) { baseState, unlockedIds, isPremium, adState ->
        // ✅ Debug: Log state combination
        Log.d("MainViewModel uiState", "🔄 State combine - UnlockedIds: $unlockedIds, isPremium: $isPremium")

        // ✅ If base state is Content, merge with unlocked IDs AND premium status AND ad state
        if (baseState is MainUiState.Content) {
            val newState = baseState.copy(
                adUnlockedIds = unlockedIds,
                isPremium = isPremium,  // ✅ MVI: Single source of truth
                adState = adState  // ✅ Use ad state from manager
            )
            Log.d(
                "MainViewModel",
                "✅ Updated state: ${unlockedIds.size} unlocked, premium=$isPremium"
            )
            newState
        } else {
            Log.d("MainViewModel", "⚠️ Base state is not Content: ${baseState::class.simpleName}")
            baseState
        }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState.Loading
    )

    // ❌ REMOVED: Navigation events - ViewModels should not handle navigation
    // private val _navigationEvents = MutableStateFlow<MainNavigationEvent?>(null)
    // val navigationEvents: StateFlow<MainNavigationEvent?> = _navigationEvents.asStateFlow()

    // ✅ ENHANCED: Show border immediately when preparing OR playing (combines isPreparing + isActuallyPlaying)
    val currentlyPlayingLullabyId: StateFlow<String?> = combine(
        musicController.currentAudioItem,
        musicController.isActuallyPlaying,
        musicController.isPreparing
    ) { currentAudio, isActuallyPlaying, isPreparing ->
        // Show border if audio is preparing OR actually playing
        if ((isActuallyPlaying || isPreparing) && currentAudio != null && !currentAudio.isFromStory) {
            currentAudio.documentId
        } else {
            null
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    /**
     * ✅ Centralized premium status and ad management
     * - Single collector for billingManager.isPurchased
     * - Updates local _isPremium state for combine block
     * - Manages ad lifecycle based on premium status
     */
    private fun managePremiumStatusAndAds() {
        viewModelScope.launch {
            var adsInitialized = false
            var bannerAdLoaded = false
            var rewardedAdPreloaded = false

            // ✅ Single place that collects from billingManager
            billingManager.isPurchased.collect { isPremium ->
                Log.d("MainViewModel", "💳 Premium status: $isPremium")

                // ✅ Update local state (used by combine block)
                _isPremium.value = isPremium

                if (!isPremium) {
                    // Free user - initialize and load ads
                    if (!adsInitialized) {
                        Log.d("MainViewModel", "📢 Initializing ads for free user")
                        initializeAds()
                        adsInitialized = true
                    }

                    // Load banner ad once
                    if (!bannerAdLoaded && internetConnectionManager.isCurrentlyConnected()) {
                        Log.d("MainViewModel", "📢 Loading banner ad")
                        loadBannerAd(
                            adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                            adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
                        )
                        bannerAdLoaded = true
                    }

                    // Preload rewarded ad once
                    if (!rewardedAdPreloaded && internetConnectionManager.isCurrentlyConnected()) {
                        Log.d("MainViewModel", "🎁 Preloading rewarded ad")
                        loadRewardedAd(AdMobDataSource.TEST_REWARDED_AD_UNIT_ID)
                        rewardedAdPreloaded = true
                    }
                } else {
                    Log.d("MainViewModel", "🏆 Premium user - No ads needed")
                    // Mark as initialized to prevent future ad loading
                    adsInitialized = true
                    bannerAdLoaded = true
                    rewardedAdPreloaded = true
                }
            }
        }
    }

    fun <T> Flow<T>.trackChanges(name: String): Flow<T> =
        this.distinctUntilChanged()
            .onEach { value ->
                Log.d("MainViewModel uiState", "🔄 State combine $name changed to")
            }

    val currentlyPlayingStoryId: StateFlow<String?> = combine(
        musicController.currentAudioItem,
        musicController.isActuallyPlaying,
        musicController.isPreparing
    ) { currentAudio, isActuallyPlaying, isPreparing ->
        // Show border if audio is preparing OR actually playing
        if ((isActuallyPlaying || isPreparing) && currentAudio != null && currentAudio.isFromStory) {
            currentAudio.documentId
        } else {
            null
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    // ✅ Premium status from BillingManager
    // Used to hide ads and show PRO badges for premium users
    /*    val isPremium: StateFlow<Boolean> = billingManager.isPremium.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false  // Default to non-premium until billing initializes
        )*/

    // ✅ Current purchase type (None, Monthly, Yearly, Lifetime)
    val currentPurchaseType = billingManager.currentPurchaseType.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        com.naptune.lullabyandstory.data.billing.PurchaseType.None
    )

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.FetchHomeData -> fetchHomeData()
            is MainIntent.OnPageChanged -> updateCurrentPage(intent.page)
            // ❌ REMOVED: Navigation intents - handled by callbacks now
            // is MainIntent.OnLullabyClick -> navigateToAudioPlayer(intent.lullaby)
            // is MainIntent.OnStoryClick -> navigateToAudioPlayer(intent.story)
            is MainIntent.OnLullabyDownloadClick -> {
                Log.d("MainViewModel", "📥 Download lullaby: ${intent.lullaby.musicName}")
                downloadLullaby(intent.lullaby)
            }
            // AdMob intents
            is MainIntent.InitializeAds -> initializeAds()
            is MainIntent.LoadBannerAd -> loadBannerAd(intent.adUnitId, intent.adSizeType)
            is MainIntent.DestroyBannerAd -> destroyBannerAd(intent.adUnitId)
            // Rewarded Ad intents
            is MainIntent.LoadRewardedAd -> loadRewardedAd(intent.adUnitId)
            is MainIntent.ShowRewardedAdForStory -> showRewardedAdForStory(
                intent.adUnitId,
                intent.activity,
                intent.story
            )

            is MainIntent.ShowRewardedAdForLullaby -> showRewardedAdForLullaby(
                intent.adUnitId,
                intent.activity,
                intent.lullaby
            )
        }
    }

    private fun downloadLullaby(lullabyItem: LullabyDomainModel) {
        // ✅ CHECK: Internet connection before download
        if (!internetConnectionManager.checkNetworkAndShowToast()) {
            Log.d(
                "MainViewModel",
                "❌ No internet connection - Download cancelled for: ${lullabyItem.documentId}"
            )
            return
        }

        // ✅ CHECK: Prevent re-download if already downloading
        val currentState = _uiState.value as? MainUiState.Content
        if (currentState != null && lullabyItem.documentId in currentState.downloadingItems) {
            Log.d("MainViewModel", "⚠️ Download already in progress for: ${lullabyItem.documentId}")
            return // Don't start duplicate download
        }

        // ✅ Analytics: Track download started
        val downloadStartTime = System.currentTimeMillis()

        analyticsHelper.logDownloadStarted(
            contentType = "lullaby",
            contentId = lullabyItem.documentId,
            contentName = lullabyItem.musicName,
            sourceScreen = "main"
        )

        // ✅ IMMEDIATELY update state for instant UI feedback
        if (currentState != null) {
            _uiState.value = currentState.copy(
                downloadingItems = currentState.downloadingItems + lullabyItem.documentId,
                downloadProgress = currentState.downloadProgress + (lullabyItem.documentId to 5)
            )
            Log.d(
                "MainViewModel",
                "✅ Instant state update for: ${lullabyItem.documentId} - Now downloading with 5% progress"
            )
        }

        viewModelScope.launch {
            downloaLullabyUsecase(lullabyItem = lullabyItem).collect { downloadResult ->
                when (downloadResult) {
                    is DownloadLullabyResult.Completed -> {
                        val value = _uiState.value
                        if (value is MainUiState.Content) {
                            _uiState.value = value.copy(
                                downloadedItems = value.downloadedItems + lullabyItem.documentId
                            )
                        }

                        // ✅ Analytics: Track download completed
                        val downloadDuration = System.currentTimeMillis() - downloadStartTime
                        analyticsHelper.logDownloadCompleted(
                            contentType = "lullaby",
                            contentId = lullabyItem.documentId,
                            contentName = lullabyItem.musicName,
                            downloadTimeMs = downloadDuration
                        )

                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.toast_download_success,
                                lullabyItem.musicName
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is DownloadLullabyResult.Error -> {
                        // ✅ Analytics: Track download failed
                        analyticsHelper.logDownloadFailed(
                            contentType = "lullaby",
                            contentId = lullabyItem.documentId,
                            errorMessage = downloadResult.toString()
                        )
                    }

                    is DownloadLullabyResult.Progress -> {
                        val value = _uiState.value
                        if (value is MainUiState.Content) {
                            _uiState.value = value.copy(
                                downloadProgress = value.downloadProgress + (lullabyItem.documentId to downloadResult.progressPercentige)
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * ✅ OPTIMIZED: Monitor network changes and automatically fetch data when connection is restored
     * Now uses WhileSubscribed to stop monitoring when no UI observers
     */
    private fun monitorNetworkForDataSync() {
        viewModelScope.launch {
            try {
                internetConnectionManager.isNetworkAvailable
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = false
                    )
                    .collect { isConnected ->
                        Log.d("MainViewModel", "🌐 Network state changed: $isConnected")
                        Log.d("MainViewModel", "📊 Debug info:")
                        Log.d("MainViewModel", "  - Network connected: $isConnected")

                        // ✅ Safe access to datastore with error handling
                        val isDataSynced = try {
                            appPreferences.isHomeDataSynced()
                        } catch (e: Exception) {
                            Log.e("MainViewModel", "Error checking data sync status: ${e.message}")
                            false
                        }

                        // ✅ Safe access to state flow with null check
                        val isFetching = _isFetchingData.value

                        Log.d("MainViewModel", "  - Data synced: $isDataSynced")
                        Log.d("MainViewModel", "  - Is fetching: $isFetching")
                        // ✅ Only fetch if network is available AND data is not synced yet
                        if (isConnected && !isDataSynced) {
                            Log.d("MainViewModel", "🔄 Network restored - Auto-fetching home data")
                            fetchHomeData()
                        } else if (isConnected && isDataSynced) {
                            Log.d(
                                "MainViewModel",
                                "✅ Network available but data already synced - no fetch needed"
                            )
                        } else if (isConnected) {
                            Log.d(
                                "MainViewModel",
                                "✅ Network available but other conditions not met"
                            )
                        } else {
                            Log.d("MainViewModel", "❌ Network not available")
                        }

                        // ✅ Handle banner ad loading based on network state
                        handleBannerAdNetworkState(isConnected)
                    }
            } catch (e: Exception) {
                Log.e("MainViewModel", "❌ Error in network monitoring: ${e.message}", e)
            }
        }
    }

    /**
     * ✅ Handle banner ad network state monitoring
     * ⚠️ IMPORTANT: Does NOT load or destroy ads
     * - UI layer handles visibility based on network state
     * - Init block handles all ad loading logic
     * - This just monitors for debugging purposes
     */
    private fun handleBannerAdNetworkState(isConnected: Boolean) {
        Log.d("MainViewModel", "📢 Network state changed: $isConnected")

        val currentState = _uiState.value as? MainUiState.Content
        if (currentState != null) {
            val bannerAd = currentState.adState.bannerAd
            if (isConnected) {
                Log.d(
                    "MainViewModel",
                    "✅ Network available - Banner ad state: loaded=${bannerAd?.isLoaded}, loading=${bannerAd?.isLoading}"
                )
            } else {
                Log.d(
                    "MainViewModel",
                    "❌ Network unavailable - Banner ad preserved in state, UI will hide it"
                )
            }
        }
    }

    /**
     * Orders favourites according to business rules (max 4 items total):
     * Shows LIFO (Last In First Out) - Latest favourites first
     * 1. If 2+ lullabies AND 2+ stories -> 2 lullabies + 2 stories (4 items)
     * 2. If 2+ lullabies AND 1 story -> up to 3 lullabies + 1 story (3-4 items)
     * 3. If 1 lullaby AND 1 story -> 1 lullaby + 1 story (2 items)
     * 4. If 1 lullaby AND 2+ stories -> 1 lullaby + 3 stories (4 items)
     * 5. If only lullabies -> up to 4 lullabies
     * 6. If only stories -> up to 4 stories
     * 7. If no favourites -> empty lists (section hidden)
     */
    private fun orderFavourites(
        lullabies: List<LullabyDomainModel>,
        stories: List<StoryDomainModel>
    ): Pair<List<LullabyDomainModel>, List<StoryDomainModel>> {
        // ✅ Lists are already ordered by favourited_at DESC (LIFO) from database queries
        // The FavouriteMetadataEntity table handles LIFO ordering at database level

        return when {
            // Case 1: 2+ lullabies AND 2+ stories -> Show 2 lullabies + 2 stories (total 4)
            lullabies.size >= 2 && stories.size >= 2 -> {
                Log.d(
                    "MainViewModel",
                    "📊 Favourites (LIFO): 2+ lullabies & 2+ stories -> showing latest 2+2"
                )
                Pair(lullabies.take(2), stories.take(2))
            }

            // Case 2: 2+ lullabies AND 1 story -> Show up to 3 lullabies + 1 story (max 4 total)
            lullabies.size >= 2 && stories.size == 1 -> {
                val lullabiesToShow = minOf(3, lullabies.size)
                Log.d(
                    "MainViewModel",
                    "📊 Favourites (LIFO): ${lullabies.size} lullabies & 1 story -> showing latest $lullabiesToShow+1"
                )
                Pair(lullabies.take(lullabiesToShow), stories)
            }

            // Case 3: 1 lullaby AND 1 story -> Show lullaby first, then story
            lullabies.size == 1 && stories.size == 1 -> {
                Log.d(
                    "MainViewModel",
                    "📊 Favourites (LIFO): 1 lullaby & 1 story -> showing latest 1+1"
                )
                Pair(lullabies.take(1), stories.take(1))
            }

            // Case 4: 1 lullaby AND 2+ stories -> Show 1 lullaby + 3 stories (max 4 total)
            lullabies.size == 1 && stories.size > 1 -> {
                Log.d(
                    "MainViewModel",
                    "📊 Favourites (LIFO): 1 lullaby & ${stories.size} stories -> showing latest 1+3"
                )
                Pair(lullabies.take(1), stories.take(3))
            }

            // Case 5: Only lullabies (no stories) -> Show up to 4 lullabies
            lullabies.isNotEmpty() && stories.isEmpty() -> {
                Log.d(
                    "MainViewModel",
                    "📊 Favourites (LIFO): ${lullabies.size} lullabies only -> showing latest ${
                        minOf(
                            4,
                            lullabies.size
                        )
                    }"
                )
                Pair(lullabies.take(4), emptyList())
            }

            // Case 6: Only stories (no lullabies) -> Show up to 4 stories
            lullabies.isEmpty() && stories.isNotEmpty() -> {
                Log.d(
                    "MainViewModel",
                    "📊 Favourites (LIFO): ${stories.size} stories only -> showing latest ${
                        minOf(
                            4,
                            stories.size
                        )
                    }"
                )
                Pair(emptyList(), stories.take(4))
            }

            // Case 7: No favourites -> Return empty lists (section will be hidden)
            else -> {
                Log.d("MainViewModel", "📊 Favourites (LIFO): No favourites -> hiding section")
                Pair(emptyList(), emptyList())
            }
        }
    }

    private fun fetchHomeData() {
        viewModelScope.launch {
            try {
                // ✅ Prevent concurrent fetching
                if (_isFetchingData.value) {
                    Log.d("MainViewModel", "⚠️ Data fetching already in progress - skipping")
                    return@launch
                }

                if (!appPreferences.isHomeDataSynced() && !internetConnectionManager.checkNetworkAndShowToast()) {
                    Log.d("MainViewModel", "❌ No internet connection - Fetching cancelled")
                    return@launch
                }

                Log.d("MainViewModel", "🚀 Fetching home data...")
                _isFetchingData.value = true
                // _uiState.value = MainUiState.Loading

                // ✅ IMPROVED: Fetch both lullabies and stories simultaneously
                val lullabiesFlow = fetchLullabiesUseCase()
                val storiesFlow = fetchStoriesUseCase()
                val favouriteLullabiesFlow = getFavouriteLullabiesUseCase()
                val favouriteStoriesFlow = getFavouriteStoriesUseCase()

                if (lullabiesFlow != null && storiesFlow != null) {
                    appPreferences.setHomeDataSynced(true)
                }

                Log.d("isinparallel", " Yeah Comes here")

                // ✅ Combine all flows to update UI when any changes
                combine(
                    lullabiesFlow,
                    storiesFlow,
                    favouriteLullabiesFlow,
                    favouriteStoriesFlow
                ) { lullabies, stories, favLullabies, favStories ->
                    Log.d(
                        "MainViewModel update",
                        "🔄 Data updated - Lullabies: ${lullabies.size}, Stories: ${stories.size}, Fav Lullabies: ${favLullabies.size}, Fav Stories: ${favStories.size}"
                    )

                    // ✅ Apply ordering logic for favourites (max 4 items)
                    val (orderedFavLullabies, orderedFavStories) = orderFavourites(
                        favLullabies,
                        favStories
                    )

                    // ✅ Preserve current ad state when updating data
                    val currentState = _uiState.value as? MainUiState.Content

                    MainUiState.Content(
                        todaysPickLullabies = lullabies.take(2),
                        todaysPickStory = stories.firstOrNull(),
                        popularLullabies = lullabies.take(4),
                        popularStories = stories.take(4),
                        favouriteLullabies = orderedFavLullabies,
                        favouriteStories = orderedFavStories,
                        currentTodaysPickPage = currentState?.currentTodaysPickPage ?: 0,
                        adState = currentState?.adState ?: AdUiState()
                    )
                }.collect { newState ->
                    _uiState.value = newState
                    Log.d("MainViewModel", "✅ UI state updated successfully")
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "❌ Error in fetchHomeData: ${e.message}", e)
                _uiState.value = MainUiState.Error("Failed to load data: ${e.message}")
            } finally {
                // ✅ Reset fetching flag when done (success or error)
                _isFetchingData.value = false
                Log.d("MainViewModel", "🏁 Data fetching completed - flag reset")
            }
        }
    }

    private fun updateCurrentPage(page: Int) {
        val currentState = _uiState.value
        if (currentState is MainUiState.Content) {
            _uiState.value = currentState.copy(currentTodaysPickPage = page)
            Log.d("MainViewModel", "📱 Page changed to: $page")
        }
    }

    /**
     * ✅ Track screen view
     */
    private fun trackScreenView() {
        analyticsHelper.logScreenView(
            screenName = "Main",
            screenClass = "MainScreen"
        )
    }

    // ❌ REMOVED: All navigation methods - handled by callbacks now
    // private fun navigateToAudioPlayer(lullaby: LullabyDomainModel) { ... }
    // private fun navigateToAudioPlayer(story: StoryDomainModel) { ... }
    // fun clearNavigationEvent() { ... }

    /**
     * ✅ Manual refresh for testing
     */
    fun manualRefresh() {
        Log.d("MainViewModel", "🔄 Manual refresh triggered")
        fetchHomeData()
    }

    /**
     * ✅ Debug method to check data sources
     */
    fun debugDataSources() {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "🔍 Debug: Checking lullabies...")
                fetchLullabiesUseCase().collect { lullabies ->
                    Log.d("MainViewModel", "🎵 Debug lullabies: ${lullabies.size}")
                    lullabies.forEach { lullaby ->
                        Log.d("MainViewModel", "  - ${lullaby.musicName}")
                    }
                }

                Log.d("MainViewModel", "🔍 Debug: Checking stories...")
                fetchStoriesUseCase().collect { stories ->
                    Log.d("MainViewModel", "📚 Debug stories: ${stories.size}")
                    stories.forEach { story ->
                        Log.d("MainViewModel", "  - ${story.storyName}")
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "💥 Debug error: ${e.message}", e)
            }
        }
    }

    /**
     * ✅ Debug method to test network reconnection scenario
     * Call this when you want to force re-sync data after network connection
     */
    fun testNetworkReconnectionScenario() {
        viewModelScope.launch {
            Log.d("MainViewModel", "🧪 Testing network reconnection scenario")
            Log.d("MainViewModel", "📊 Current state BEFORE reset:")
            Log.d("MainViewModel", "  - Is data synced: ${appPreferences.isHomeDataSynced()}")
            Log.d("MainViewModel", "  - Is fetching: ${_isFetchingData.value}")
            Log.d(
                "MainViewModel",
                "  - Network available: ${internetConnectionManager.isCurrentlyConnected()}"
            )

            Log.d("MainViewModel", "🔄 Resetting data sync for test...")
            appPreferences.setHomeDataSynced(false)

            Log.d("MainViewModel", "📊 State AFTER reset:")
            Log.d("MainViewModel", "  - Is data synced: ${appPreferences.isHomeDataSynced()}")
            Log.d(
                "MainViewModel",
                "  - Network available: ${internetConnectionManager.isCurrentlyConnected()}"
            )

            Log.d("MainViewModel", "✅ Data sync reset complete")
            Log.d(
                "MainViewModel",
                "⏳ If network is available, monitoring should trigger fetch automatically..."
            )

            // ✅ Manual trigger if network is already available
            if (internetConnectionManager.isCurrentlyConnected()) {
                Log.d("MainViewModel", "🚀 Network already available - triggering fetch manually")
                fetchHomeData()
            }
        }
    }

    /**
     * ✅ Debug method to test banner ad network behavior
     */
    fun testBannerAdNetworkBehavior() {
        viewModelScope.launch {
            Log.d("MainViewModel", "🧪 Testing banner ad network behavior")
            Log.d("MainViewModel", "📊 Current banner ad state:")

            val currentState = _uiState.value as? MainUiState.Content
            if (currentState != null) {
                val bannerAd = currentState.adState.bannerAd
                Log.d("MainViewModel", "  - Banner ad exists: ${bannerAd != null}")
                Log.d("MainViewModel", "  - Banner ad loaded: ${bannerAd?.isLoaded ?: false}")
                Log.d("MainViewModel", "  - Banner ad loading: ${bannerAd?.isLoading ?: false}")
                Log.d(
                    "MainViewModel",
                    "  - Network available: ${internetConnectionManager.isCurrentlyConnected()}"
                )

                // ✅ Force trigger network state handling
                Log.d("MainViewModel", "🔄 Force triggering network state handling...")
                handleBannerAdNetworkState(internetConnectionManager.isCurrentlyConnected())
            } else {
                Log.d("MainViewModel", "❌ No content state available")
            }
        }
    }

    /**
     * ✅ Debug method to test smooth banner ad animations
     * Useful for testing network on/off transitions
     */
    fun testSmoothBannerAdTransitions() {
        viewModelScope.launch {
            Log.d("MainViewModel", "🎬 Testing smooth banner ad transitions")

            val currentNetworkState = internetConnectionManager.isCurrentlyConnected()
            Log.d("MainViewModel", "📊 Current network state: $currentNetworkState")

            if (currentNetworkState) {
                Log.d(
                    "MainViewModel",
                    "✅ Network available - Banner ad animations should be smooth"
                )
                Log.d("MainViewModel", "📱 Watch for:")
                Log.d("MainViewModel", "  - Space always reserved (60dp height)")
                Log.d("MainViewModel", "  - Smooth fade in/out transitions")
                Log.d("MainViewModel", "  - No content jumping")
                Log.d("MainViewModel", "  - Loading → Ad transition smooth")
            } else {
                Log.d("MainViewModel", "❌ Network not available - Banner ad should be hidden")
                Log.d("MainViewModel", "📱 Expected behavior:")
                Log.d("MainViewModel", "  - Space still reserved (60dp)")
                Log.d("MainViewModel", "  - Content slides out smoothly")
                Log.d("MainViewModel", "  - No layout shift")
            }
        }
    }

    // ✅ SRP FIX: Ad functionality delegated to AdManager
    private fun initializeAds() {
        adManager.initializeAds()
    }

    private fun loadBannerAd(
        adUnitId: String,
        adSizeType: com.naptune.lullabyandstory.domain.model.AdSizeType
    ) {
        adManager.loadBannerAd(adUnitId, adSizeType, placement = "main_screen")
    }

    private fun destroyBannerAd(adUnitId: String) {
        adManager.destroyBannerAd(adUnitId)
    }

    // Rewarded ad methods
    private fun loadRewardedAd(adUnitId: String) {
        adManager.loadRewardedAd(adUnitId)
    }

    private fun showRewardedAdForStory(
        adUnitId: String,
        activity: Activity,
        story: StoryDomainModel
    ) {
        // ✅ SRP FIX: Delegate to AdManager with ContentInfo
        adManager.showRewardedAd(
            adUnitId = adUnitId,
            activity = activity,
            content = ContentInfo.fromStory(story),
            sourceScreen = "main_screen"
        )
    }

    private fun showRewardedAdForLullaby(
        adUnitId: String,
        activity: Activity,
        lullaby: LullabyDomainModel
    ) {
        // ✅ SRP FIX: Delegate to AdManager with ContentInfo
        adManager.showRewardedAd(
            adUnitId = adUnitId,
            activity = activity,
            content = ContentInfo.fromLullaby(lullaby),
            sourceScreen = "main_screen"
        )
    }

    override fun onCleared() {
        super.onCleared()
        // ⚠️ IMPORTANT: Do NOT destroy banner ad here!
        // Banner ads use shared adUnitId stored in AdMobDataSource singleton.
        // Destroying here would also destroy other screens' banner ads,
        // causing them to vanish when user navigates between screens.
        // The ad will be recreated when user navigates to this screen again.
        Log.d("MainViewModel", "🧹 ViewModel cleared - Banner ad preserved (shared across screens)")
    }

    // ═══════════════════════════════════════════════════════════
    // 📊 ANALYTICS TRACKING
    // ═══════════════════════════════════════════════════════════

    /**
     * Track lullaby selection from Main screen
     */
    fun trackLullabySelected(lullaby: LullabyDomainModel) {
        try {
            val isDownloaded = lullaby.isDownloaded ?: false

            analyticsHelper.logLullabySelected(
                lullabyId = lullaby.documentId,
                lullabyName = lullaby.musicName,
                category = "lullaby",
                sourceScreen = "main",
                isPremium = !lullaby.isFree,
                isDownloaded = isDownloaded
            )

            // ✅ Analytics: Track offline content usage
            if (isDownloaded) {
                analyticsHelper.logDownloadedContentPlayed(
                    contentType = "lullaby",
                    contentId = lullaby.documentId,
                    contentName = lullaby.musicName,
                    sourceScreen = "main"
                )
            }

            Log.d(
                "MainViewModel",
                "📊 Tracked lullaby selection: ${lullaby.musicName} (Downloaded: $isDownloaded)"
            )
        } catch (e: Exception) {
            Log.e("MainViewModel", "❌ Analytics error: ${e.message}")
        }
    }

    /**
     * Track story selection from Main screen
     */
    fun trackStorySelected(story: StoryDomainModel) {
        try {
            analyticsHelper.logStorySelected(
                storyId = story.documentId,
                storyName = story.storyName,
                category = "story",
                sourceScreen = "main",
                isPremium = !story.isFree,
                interactionType = "listen"
            )
            Log.d("MainViewModel", "📊 Tracked story selection: ${story.storyName}")
        } catch (e: Exception) {
            Log.e("MainViewModel", "❌ Analytics error: ${e.message}")
        }
    }
}

// ❌ REMOVED: Navigation Events - using callbacks now
// sealed class MainNavigationEvent {
//     data class NavigateToAudioPlayer(...) : MainNavigationEvent()
// }
