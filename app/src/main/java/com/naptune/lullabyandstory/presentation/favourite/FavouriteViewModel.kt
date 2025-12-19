package com.naptune.lullabyandstory.presentation.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.domain.usecase.lullaby.GetFavouriteLullabiesUseCase
import com.naptune.lullabyandstory.domain.usecase.lullaby.ToggleLullabyFavouriteUseCase
import com.naptune.lullabyandstory.domain.usecase.story.GetFavouriteStoriesUseCase
import com.naptune.lullabyandstory.domain.usecase.story.ToogleStoryFavouriteUseCase
import com.naptune.lullabyandstory.presentation.player.service.MusicController
import com.naptune.lullabyandstory.utils.InternetConnectionManager
import android.app.Activity
import android.widget.Toast
import android.util.Log
import com.naptune.lullabyandstory.data.manager.AdManager
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.model.ContentInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * ViewModel for Favourite screen.
 * REFACTORED: Now follows Single Responsibility Principle (SRP).
 * Ad management logic delegated to unified AdManager.
 *
 * Responsibilities:
 * - Favourite lullabies and stories data management
 * - Toggle favourite status
 * - Music playback coordination
 *
 * Ad management delegated to: AdManager (shared across all ViewModels)
 */
@HiltViewModel
class FavouriteViewModel @Inject constructor(
    private val getFavouriteLullabiesUseCase: GetFavouriteLullabiesUseCase,
    private val getFavouriteStoriesUseCase: GetFavouriteStoriesUseCase,
    private val toggleLullabyFavouriteUseCase: ToggleLullabyFavouriteUseCase,
    private val toggleStoryFavouriteUseCase: ToogleStoryFavouriteUseCase,
    private val musicController: MusicController,
    private val internetConnectionManager: InternetConnectionManager,
    // ✅ SRP FIX: Single unified ad manager instead of 6 ad use cases
    private val adManager: AdManager,
    // ✅ Session unlock manager for UI state observation
    private val sessionUnlockManager: com.naptune.lullabyandstory.data.manager.SessionUnlockManager,
    private val analyticsHelper: com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper,
    private val billingManager: com.naptune.lullabyandstory.data.billing.BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavouriteUiState>(FavouriteUiState.isLoading)

    init {
        // ✅ Track screen view
        trackScreenView()
    }

    /**
     * ✅ Track screen view
     */
    private fun trackScreenView() {
        analyticsHelper.logScreenView(
            screenName = "Favourites",
            screenClass = "FavouriteScreen"
        )
    }

    // ✅ MVI FIX: Combine base state + session unlocks + ad state into single state
    val uiState: StateFlow<FavouriteUiState> = combine(
        _uiState,
        sessionUnlockManager.unlockedItems,
        adManager.adState  // ✅ Get ad state from manager
    ) { baseState, unlockedIds, adState ->
        Log.d("FavouriteViewModel", "🔄 State combine - UnlockedIds: $unlockedIds")

        if (baseState is FavouriteUiState.Content) {
            val newState = baseState.copy(
                adUnlockedIds = unlockedIds,
                adState = adState  // ✅ Use ad state from manager
            )
            Log.d("FavouriteViewModel", "✅ Updated state: ${unlockedIds.size} unlocked")
            newState
        } else {
            baseState
        }
    }.onStart {
        // ✅ SRP FIX: Delegate ad initialization to manager
        adManager.initializeAds()
        adManager.loadBannerAd(
            adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
            adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER,
            placement = "favourite_screen"
        )

        // Preload rewarded ad for content unlock
        adManager.loadRewardedAd(AdMobDataSource.TEST_REWARDED_AD_UNIT_ID)

        loadFavourites()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavouriteUiState.isLoading)

    // ✅ NEW: Expose network state for UI like MainViewModel
    val isNetworkAvailable: StateFlow<Boolean> = internetConnectionManager.isNetworkAvailable

    // ❌ REMOVED: Navigation events
    // private val _navigationEvents = MutableStateFlow<FavouriteNavigationEvent?>(null)
    // val navigationEvents: StateFlow<FavouriteNavigationEvent?> = _navigationEvents.asStateFlow()

    // ✅ NEW: Expose currently playing lullaby ID from MusicController
    val currentlyPlayingLullabyId: StateFlow<String?> = combine(
        musicController.currentAudioItem,
        musicController.isPlaying
    ) { audioItem, isPlaying ->
        // ✅ Show border if audio is loaded (regardless of playing/paused state)
        // Border will disappear only when audio is stopped (currentAudioItem = null)
        if (audioItem?.isFromStory == false) {
            audioItem.documentId // ✅ Show border for lullabies (playing OR paused)
        } else {
            null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // ✅ NEW: Expose currently playing story ID from MusicController
    val currentlyPlayingStoryId: StateFlow<String?> = combine(
        musicController.currentAudioItem,
        musicController.isPlaying
    ) { audioItem, isPlaying ->
        // ✅ Show border if audio is loaded (regardless of playing/paused state)
        // Border will disappear only when audio is stopped (currentAudioItem = null)
        if (audioItem?.isFromStory == true) {
            audioItem.documentId // ✅ Show border for stories (playing OR paused)
        } else {
            null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // ✅ Premium status from BillingManager
    val isPurchased: StateFlow<Boolean> = billingManager.isPurchased.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    init {
        Log.d("FavouriteViewModel", "🚀 ViewModel initialized")

        // ✅ FIX: Initialize ads ONLY after billing status is known and ONLY if NOT premium
        viewModelScope.launch {
            var adsInitialized = false  // ✅ FIX: Prevent infinite ad loading loop
            isPurchased.collect { isPremiumUser ->
                Log.d("FavouriteViewModel", "💳 Billing status received - isPremium: $isPremiumUser")

                if (!isPremiumUser) {
                    // ✅ Initialize AdMob SDK once
                    if (!adsInitialized) {
                        Log.d("FavouriteViewModel", "📢 Free user - Initializing ads")
                        initializeAds()
                        // Start monitoring network for ad loading
                        monitorNetworkForAdLoading()
                        adsInitialized = true  // ✅ Mark as initialized
                    }

                    // ✅ Reload banner ad if missing (handles back navigation)
                    val currentState = uiState.value
                    if (currentState is FavouriteUiState.Content) {
                        val bannerAd = currentState.adState.bannerAd
                        if (internetConnectionManager.isCurrentlyConnected() &&
                            (bannerAd == null || (!bannerAd.isLoaded && !bannerAd.isLoading))) {
                            Log.d("FavouriteViewModel", "🔄 Banner ad missing - Reloading")
                            loadBannerAd(
                                adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                                adSizeType = com.naptune.lullabyandstory.domain.model.AdSizeType.ANCHORED_ADAPTIVE_BANNER
                            )
                        }
                    }
                } else {
                    Log.d("FavouriteViewModel", "🏆 Premium user - Skipping all ad initialization")
                    adsInitialized = true  // ✅ Mark as initialized even for premium
                }
            }
        }

        handleIntent(FavouriteIntent.LoadFavourites)
    }

    fun handleIntent(intent: FavouriteIntent) {
        when (intent) {
            is FavouriteIntent.LoadFavourites -> loadFavourites()
            is FavouriteIntent.ChangeCategory -> changeCategory(intent.category)
            // ❌ REMOVED: Navigation intents
            // is FavouriteIntent.OnLullabyClick -> navigateToAudioPlayer(intent.lullaby)
            // is FavouriteIntent.OnStoryClick -> navigateToAudioPlayer(intent.story)
            is FavouriteIntent.ToggleLullabyFavourite -> toggleLullabyFavourite(intent.lullabyId)
            is FavouriteIntent.ToggleStoryFavourite -> toggleStoryFavourite(intent.storyId)
            // AdMob intents
            FavouriteIntent.InitializeAds -> initializeAds()
            is FavouriteIntent.LoadBannerAd -> loadBannerAd(intent.adUnitId, intent.adSizeType)
            is FavouriteIntent.DestroyBannerAd -> destroyBannerAd(intent.adUnitId)
            // Rewarded Ad intents
            is FavouriteIntent.LoadRewardedAd -> loadRewardedAd(intent.adUnitId)
            is FavouriteIntent.ShowRewardedAdForStory -> showRewardedAdForStory(intent.adUnitId, intent.activity, intent.story)
            is FavouriteIntent.ShowRewardedAdForLullaby -> showRewardedAdForLullaby(intent.adUnitId, intent.activity, intent.lullaby)
        }
    }

    private fun loadFavourites() {
        // ✅ Analytics: Track favorites screen viewed
        analyticsHelper.logFavoritesScreenViewed()

        viewModelScope.launch {
            _uiState.value = FavouriteUiState.isLoading


                combine(
                    getFavouriteLullabiesUseCase(),
                    getFavouriteStoriesUseCase()
                ) { lullabies, stories ->
                    Pair(lullabies, stories)
                }.catch {
                    _uiState.value = FavouriteUiState.Error(it.message ?: "Unknown error")
                }.collect { (lullabies, stories) ->
                    val currentState = _uiState.value as? FavouriteUiState.Content
                    _uiState.value = FavouriteUiState.Content(
                        currentCategory = FavouriteCategory.LULLABY,
                        favouriteLullabies = lullabies,
                        favouriteStories = stories,
                        // Preserve existing ad state during data updates
                        bannerAd = currentState?.bannerAd,
                        isAdInitialized = currentState?.isAdInitialized ?: false,
                        // ✅ Preserve rewarded ad state
                        rewardedAd = currentState?.rewardedAd,
                        isLoadingRewardedAd = currentState?.isLoadingRewardedAd ?: false,
                        rewardedAdError = currentState?.rewardedAdError
                    )
                }


        }
    }

    private fun changeCategory(category: FavouriteCategory) {

        val value = _uiState.value

        if ( value is FavouriteUiState.Content)
        {
            _uiState.value = value.copy(currentCategory = category)

        }
    }

    // ❌ REMOVED: All navigation methods
    // private fun navigateToAudioPlayer(lullaby: LullabyDomainModel) { ... }
    // private fun navigateToAudioPlayer(story: StoryDomainModel) { ... }

    private fun toggleLullabyFavourite(lullabyId: String) {
        viewModelScope.launch {
            try {
                toggleLullabyFavouriteUseCase(lullabyId)
            } catch (e: Exception) {
                _uiState.value = FavouriteUiState.Error(
                    "Failed to update favourite: ${e.message}"
                )
            }
        }
    }

    private fun toggleStoryFavourite(storyId: String) {
        viewModelScope.launch {
            try {
                toggleStoryFavouriteUseCase(storyId)
            } catch (e: Exception) {
                _uiState.value = FavouriteUiState.Error(
                 "Failed to update favourite: ${e.message}"
                )
            }
        }
    }

    // ❌ REMOVED: clearNavigationEvent()
    // fun clearNavigationEvent() { ... }

    fun clearErrorMessage() {
        _uiState.value =  FavouriteUiState.Error(
            ""
        )
    }
    
    // AdMob functionality
    private fun initializeAds() {
        adManager.initializeAds()
    }
    
    private fun loadBannerAd(adUnitId: String, adSizeType: com.naptune.lullabyandstory.domain.model.AdSizeType) {
        adManager.loadBannerAd(adUnitId, adSizeType, placement = "favourite_screen")
    }
    
    private fun destroyBannerAd(adUnitId: String) {
        adManager.destroyBannerAd(adUnitId)
    }
    
    // Rewarded ad methods
    private fun loadRewardedAd(adUnitId: String) {
        adManager.loadRewardedAd(adUnitId)
    }

    private fun showRewardedAdForStory(adUnitId: String, activity: Activity, story: StoryDomainModel) {
        // ✅ SRP FIX: Delegate to AdManager with ContentInfo
        adManager.showRewardedAd(
            adUnitId = adUnitId,
            activity = activity,
            content = ContentInfo.fromStory(story),
            sourceScreen = "favourite_screen"
        )
    }

    private fun showRewardedAdForLullaby(adUnitId: String, activity: Activity, lullaby: LullabyDomainModel) {
        // ✅ SRP FIX: Delegate to AdManager with ContentInfo
        adManager.showRewardedAd(
            adUnitId = adUnitId,
            activity = activity,
            content = ContentInfo.fromLullaby(lullaby),
            sourceScreen = "favourite_screen"
        )
    }

    /**
     * ✅ Monitor network changes and automatically handle banner ad loading/hiding
     */
    private fun monitorNetworkForAdLoading() {
        viewModelScope.launch {
            internetConnectionManager.isNetworkAvailable.collect { isConnected ->
                Log.d("FavouriteViewModel", "🌐 Network state changed: $isConnected")
                
                // ✅ Handle banner ad loading based on network state
                handleBannerAdNetworkState(isConnected)
            }
        }
    }

    /**
     * ✅ Handle banner ad loading/hiding based on network connectivity
     */
    private fun handleBannerAdNetworkState(isConnected: Boolean) {
        Log.d("FavouriteViewModel", "📢 Handling banner ad network state: $isConnected")
        
        val currentState = _uiState.value as? FavouriteUiState.Content
        if (currentState != null) {
            if (isConnected) {
                // ✅ Network available - check if ad needs to be loaded
                val bannerAd = currentState.adState.bannerAd
                if (bannerAd == null || (!bannerAd.isLoaded && !bannerAd.isLoading)) {
                    Log.d("FavouriteViewModel", "🚀 Network available - Starting banner ad load")
                    loadBannerAd(
                        adUnitId = com.naptune.lullabyandstory.data.network.admob.AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                        adSizeType = com.naptune.lullabyandstory.domain.model.AdSizeType.ANCHORED_ADAPTIVE_BANNER
                    )
                } else {
                    Log.d("FavouriteViewModel", "✅ Banner ad already loaded or loading")
                }
            } else {
                // ✅ Network not available - clear ad state
                Log.d("FavouriteViewModel", "❌ Network not available - Clearing banner ad state")
                _uiState.value = currentState.copy(
                    adState = currentState.adState.copy(bannerAd = null)
                )
            }
        }
    }

    // ✅ Firebase Analytics: Track lullaby played from favorites
    fun trackLullabyPlayedFromFavourites(lullaby: LullabyDomainModel) {
        try {
            analyticsHelper.logContentPlayedFromFavourites(
                contentType = "lullaby",
                contentId = lullaby.id,
                contentName = lullaby.musicName,
                category = "lullaby"
            )
            Log.d("FavouriteViewModel", "🔥 Analytics: Lullaby played from favourites - ${lullaby.musicName}")
        } catch (e: Exception) {
            Log.e("FavouriteViewModel", "❌ Analytics error: ${e.message}")
        }
    }

    // ✅ Firebase Analytics: Track story played from favorites
    fun trackStoryPlayedFromFavourites(story: StoryDomainModel) {
        try {
            analyticsHelper.logContentPlayedFromFavourites(
                contentType = "story",
                contentId = story.id,
                contentName = story.storyName,
                category = "story"
            )
            Log.d("FavouriteViewModel", "🔥 Analytics: Story played from favourites - ${story.storyName}")
        } catch (e: Exception) {
            Log.e("FavouriteViewModel", "❌ Analytics error: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        // ⚠️ IMPORTANT: Do NOT destroy banner ad here!
        // Banner ads use shared adUnitId stored in AdMobDataSource singleton.
        // Destroying here would also destroy MainScreen's banner ad,
        // causing it to vanish when user navigates back to Home.
        // The ad will be recreated when user navigates to this screen again.
        Log.d("FavouriteViewModel", "🧹 ViewModel cleared - Banner ad preserved (shared with MainScreen)")
    }
}

// ❌ REMOVED: Navigation Events - using callbacks now
// sealed class FavouriteNavigationEvent {
//     data class NavigateToAudioPlayer(...) : FavouriteNavigationEvent()
// }
