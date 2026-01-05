package com.naptune.lullabyandstory.presentation.story

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.data.manager.AdManager
import com.naptune.lullabyandstory.data.manager.SessionUnlockManager
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.domain.model.ContentInfo
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.domain.usecase.story.FetchStoriesUsecase
import com.naptune.lullabyandstory.presentation.player.service.MusicController
import com.naptune.lullabyandstory.utils.InternetConnectionManager
import com.naptune.lullabyandstory.domain.model.AdSizeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * ViewModel for Story browse screen.
 * REFACTORED: Now follows Single Responsibility Principle (SRP).
 * Ad management logic delegated to unified AdManager.
 *
 * Responsibilities:
 * - Story data fetching and management
 * - Category filtering
 * - Music playback coordination
 *
 * Ad management delegated to: AdManager (shared across all ViewModels)
 */
@HiltViewModel
class StoryViewModel @Inject constructor(
    private val fetchStoriesUsecase: FetchStoriesUsecase,
    // ✅ Inject MusicController to track playing state
    private val musicController: MusicController,
    // ✅ NEW: Inject InternetConnectionManager for network monitoring
    private val internetConnectionManager: InternetConnectionManager,
    // ✅ SRP FIX: Single unified ad manager instead of 6 ad use cases + sessionUnlockManager
    private val adManager: AdManager,
    // ✅ Session unlock manager for UI state observation
    private val sessionUnlockManager: SessionUnlockManager,
    // ✅ Analytics
    private val analyticsHelper: com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper,
    // ✅ Billing - Premium status
    private val billingManager: com.naptune.lullabyandstory.data.billing.BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<StoryUiState>(StoryUiState.IsLoading)

    init {
        // ✅ Track screen view
        trackScreenView()

        manageAdsLoading()
    }

    private fun manageAdsLoading() {
        viewModelScope.launch {
            var adsInitialized = false  // ✅ FIX: Prevent infinite ad loading loop
            billingManager.isPurchased.collect { isPurchased ->


                    if (!isPurchased) {
                        // ✅ Initialize AdMob SDK once
                        if (!adsInitialized) {
                            Log.d("StoryViewModel", "📢 Free user - Initializing ads")
                            initializeAds()
                            // Start monitoring network for ad loading
                            monitorNetworkForAdLoading()
                            adsInitialized = true  // ✅ Mark as initialized
                        }

                        // ✅ Reload banner ad if missing (handles back navigation)

                    } else {
                        Log.d("StoryViewModel", "🏆 Premium user - Skipping all ad initialization")
                        adsInitialized = true  // ✅ Mark as initialized even for premium
                    }

            }
        }
    }

    /**
     * ✅ Track screen view
     */
    private fun trackScreenView() {
        analyticsHelper.logScreenView(
            screenName = "Story Browse",
            screenClass = "StoryScreen"
        )
    }

    // ✅ MVI FIX: Combine base state + session unlocks + billing status + ad state into single state
    val uiState: StateFlow<StoryUiState> = combine(
        _uiState,
        sessionUnlockManager.unlockedItems,
        billingManager.isPurchased,  // ✅ Add premium status to state
        adManager.adState  // ✅ Get ad state from manager
    ) { baseState, unlockedIds, isPremium, adState ->
        // ✅ Debug: Log state combination
        Log.d("StoryViewModel", "🔄 State combine - UnlockedIds: $unlockedIds, isPremium: $isPremium")

        // ✅ If base state is Content, merge with unlocked IDs AND premium status AND ad state
        if (baseState is StoryUiState.Content) {
            val newState = baseState.copy(
                adUnlockedIds = unlockedIds,
                isPremium = isPremium,  // ✅ MVI: Single source of truth
                adState = adState  // ✅ Use ad state from manager
            )
            Log.d("StoryViewModel", "✅ Updated state: ${unlockedIds.size} unlocked, premium=$isPremium")
            newState
        } else {
            Log.d("StoryViewModel", "⚠️ Base state is not Content: ${baseState::class.simpleName}")
            baseState
        }
    }.onStart {
        // ✅ SRP FIX: Delegate ad initialization to manager
        fetchStories()

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StoryUiState.IsLoading)

    // ✅ Expose currently playing story ID from MusicController
    val currentlyPlayingStoryId: StateFlow<String?> = combine(
        musicController.currentAudioItem,
        musicController.isPlaying
    ) { audioItem, isPlaying ->
        val result = if (audioItem?.isFromStory == true) {
            audioItem.documentId
        } else {
            null
        }
        Log.d(
            "StoryViewModel",
            "📚 Currently playing state - Playing: $isPlaying, AudioItem: ${audioItem?.title}, IsStory: ${audioItem?.isFromStory}, Result: $result"
        )
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // ✅ NEW: Expose network state for UI like MainViewModel
    val isNetworkAvailable: StateFlow<Boolean> = internetConnectionManager.isNetworkAvailable

    // ❌ REMOVED: Separate isPremium StateFlow - now part of uiState
    // This follows pure MVI pattern: single source of truth

    fun onhandleIntent(storyIntent: StoryIntent) {
        Log.d("StoryViewModel", "🎯 Intent received: $storyIntent")
        when (storyIntent) {
            StoryIntent.FetchStories -> fetchStories()
            StoryIntent.DownloadStory -> downloadStoryItem()
            StoryIntent.toogleStoryFavourite -> downloadStoryItem()
            is StoryIntent.ChangeCategory -> changeCategory(storyIntent.category)
            // AdMob intents
            StoryIntent.InitializeAds -> initializeAds()
            is StoryIntent.LoadBannerAd -> loadBannerAd(
                storyIntent.adUnitId,
                storyIntent.adSizeType
            )

            is StoryIntent.DestroyBannerAd -> destroyBannerAd(storyIntent.adUnitId)
            // Rewarded Ad intents  
            is StoryIntent.LoadRewardedAd -> loadRewardedAd(storyIntent.adUnitId)
            is StoryIntent.ShowRewardedAd -> showRewardedAd(
                storyIntent.adUnitId, 
                storyIntent.activity,
                storyIntent.story
            )
        }
    }

    private fun downloadStoryItem() {
        Log.d("StoryViewModel", "📥 Download story item called")
        // TODO("Not yet implemented")
    }

    private fun fetchStories() {
        Log.d("StoryViewModel", "🔄 Starting fetch stories...")
        viewModelScope.launch {
            _uiState.value = StoryUiState.IsLoading
            Log.d("StoryViewModel", "⏳ State set to Loading")

            try {
                Log.d("StoryViewModel", "🌐 Calling use case...")
                fetchStoriesUsecase().collect { storyList ->
                    // ✅ Preserve current category when updating stories
                    val currentCategory = (_uiState.value as? StoryUiState.Content)?.currentCategory
                        ?: StoryCategory.ALL

                    // ✅ Pre-filter Popular and Free lists ONCE when data loads
                    val popularStories = storyList.filter { it.popularity_count > 0 }
                    val freeStories = storyList.filter { it.isFree }
                    
                    Log.d("StoryViewModel", "🚀 Pre-filtered lists created:")
                    Log.d("StoryViewModel", "📊 Popular: ${popularStories.size} items")
                    Log.d("StoryViewModel", "🆓 Free: ${freeStories.size} items")

                    // ✅ Set initial filteredStories based on current category
                    val initialFilteredStories = when (currentCategory) {
                        StoryCategory.ALL -> storyList
                        StoryCategory.POPULAR -> popularStories
                        StoryCategory.FREE -> freeStories
                    }

                    val currentState = _uiState.value as? StoryUiState.Content
                    _uiState.value = StoryUiState.Content(
                        storyList = storyList,
                        filteredStories = initialFilteredStories,
                        popularStories = popularStories,
                        freeStories = freeStories,
                        currentCategory = currentCategory,
                        // Preserve existing ad state during data updates
                        bannerAd = currentState?.bannerAd,
                        isAdInitialized = currentState?.isAdInitialized ?: false,
                        rewardedAd = currentState?.rewardedAd,
                        isLoadingRewardedAd = currentState?.isLoadingRewardedAd ?: false,
                        rewardedAdError = currentState?.rewardedAdError,
                        lastRewardedStory = currentState?.lastRewardedStory
                    )
                }

            } catch (e: Exception) {
                Log.e("StoryViewModel", "💥 Exception caught: ${e.message}", e)
                _uiState.value = StoryUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun changeCategory(category: StoryCategory) {
        Log.d("StoryViewModel", "🔄 Changing category to: $category")

        val currentState = _uiState.value as? StoryUiState.Content ?: run {
            Log.w("StoryViewModel", "Cannot change category - UI not in Content state")
            return
        }

        // ✅ ULTRA FAST - Use pre-filtered lists (no filtering needed!)
        val filteredStories = when (category) {
            StoryCategory.ALL -> {
                Log.d("StoryViewModel", "✅ ALL category - showing all ${currentState.storyList.size} stories")
                currentState.storyList
            }
            StoryCategory.POPULAR -> {
                Log.d("StoryViewModel", "📊 POPULAR category - showing ${currentState.popularStories.size} popular stories")
                currentState.popularStories
            }
            StoryCategory.FREE -> {
                Log.d("StoryViewModel", "🆓 FREE category - showing ${currentState.freeStories.size} free stories")
                currentState.freeStories
            }
        }

        // ✅ Instant UI update - just reference switching!
        _uiState.value = currentState.copy(
            currentCategory = category,
            filteredStories = filteredStories
        )
    }

    // ✅ SRP FIX: Ad functionality delegated to AdManager
    private fun initializeAds() {
        adManager.initializeAds()
    }

    private fun loadBannerAd(
        adUnitId: String,
        adSizeType: com.naptune.lullabyandstory.domain.model.AdSizeType
    ) {
        adManager.loadBannerAd(adUnitId, adSizeType, placement = "story_screen")
    }

    private fun destroyBannerAd(adUnitId: String) {
        adManager.destroyBannerAd(adUnitId)
    }

    // Rewarded Ad functionality
    private fun loadRewardedAd(adUnitId: String) {
        adManager.loadRewardedAd(adUnitId)
    }

    private fun showRewardedAd(adUnitId: String, activity: android.app.Activity, story: StoryDomainModel) {
        // ✅ SRP FIX: Delegate to AdManager with ContentInfo
        adManager.showRewardedAd(
            adUnitId = adUnitId,
            activity = activity,
            content = ContentInfo.fromStory(story),
            sourceScreen = "story_screen"
        )
    }

    // ✅ SRP FIX: Reward handling now managed by AdManager
    fun isRewardedAdAvailable(adUnitId: String = AdMobDataSource.TEST_REWARDED_AD_UNIT_ID): Boolean {
        return adManager.isRewardedAdAvailable(adUnitId)
    }

    /**
     * ✅ Monitor network changes and automatically handle banner ad loading/hiding
     */
    private fun monitorNetworkForAdLoading() {
        viewModelScope.launch {
            internetConnectionManager.isNetworkAvailable.collect { isConnected ->
                Log.d("StoryViewModel", "🌐 Network state changed: $isConnected")
                
                // ✅ Handle banner ad loading based on network state
                handleBannerAdNetworkState(isConnected)
            }
        }
    }

    /**
     * ✅ Handle banner ad loading/hiding based on network connectivity
     */
    private fun handleBannerAdNetworkState(isConnected: Boolean) {
        Log.d("StoryViewModel", "📢 Handling banner ad network state: $isConnected")

            if (isConnected) {
                // ✅ Network available - check if ad needs to be loaded

                    Log.d("StoryViewModel", "🚀 Network available - Starting banner ad load")
                    loadBannerAd(
                        adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                        adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
                    )
                    adManager.loadRewardedAd(AdMobDataSource.TEST_REWARDED_AD_UNIT_ID)

            } else {
                // ✅ Network not available - clear ad state
                Log.d("StoryViewModel", "❌ Network not available - Clearing banner ad state")

            }

    }

    override fun onCleared() {
        super.onCleared()
        // ⚠️ IMPORTANT: Do NOT destroy banner ad here!
        // Banner ads use shared adUnitId stored in AdMobDataSource singleton.
        // Destroying here would also destroy MainScreen's banner ad,
        // causing it to vanish when user navigates back to Home.
        // The ad will be recreated when user navigates to this screen again.
        Log.d("StoryViewModel", "🧹 ViewModel cleared - Banner ad preserved (shared with MainScreen)")
    }

    // ═══════════════════════════════════════════════════════════
    // 📊 ANALYTICS TRACKING
    // ═══════════════════════════════════════════════════════════

    /**
     * Track story selection from Story browse screen
     */
    fun trackStorySelected(story: StoryDomainModel, interactionType: String) {
        try {
            analyticsHelper.logStorySelected(
                storyId = story.documentId,
                storyName = story.storyName,
                category = "story",
                sourceScreen = "story_browse",
                isPremium = !story.isFree,
                interactionType = interactionType
            )
            Log.d("StoryViewModel", "📊 Tracked story selection: ${story.storyName} ($interactionType)")
        } catch (e: Exception) {
            Log.e("StoryViewModel", "❌ Analytics error: ${e.message}")
        }
    }
}
