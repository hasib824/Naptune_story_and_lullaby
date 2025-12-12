package com.naptune.lullabyandstory.presentation.story

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.data.manager.SessionUnlockManager
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.domain.usecase.story.FetchStoriesUsecase
import com.naptune.lullabyandstory.presentation.player.service.MusicController
import com.naptune.lullabyandstory.utils.InternetConnectionManager
import com.naptune.lullabyandstory.domain.usecase.admob.InitializeAdMobUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.LoadBannerAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.DestroyBannerAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.LoadRewardedAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.ShowRewardedAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.CheckRewardedAdAvailabilityUseCase
import com.naptune.lullabyandstory.domain.model.BannerAdDomainModel
import com.naptune.lullabyandstory.domain.model.AdLoadResult
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.model.RewardedAdLoadResult
import com.naptune.lullabyandstory.domain.model.RewardedAdShowResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject


@HiltViewModel
class StoryViewModel @Inject constructor(
    private val fetchStoriesUsecase: FetchStoriesUsecase,
    // ✅ Inject MusicController to track playing state
    private val musicController: MusicController,
    // ✅ NEW: Inject InternetConnectionManager for network monitoring
    private val internetConnectionManager: InternetConnectionManager,
    // AdMob use cases
    private val initializeAdMobUseCase: InitializeAdMobUseCase,
    private val loadBannerAdUseCase: LoadBannerAdUseCase,
    private val destroyBannerAdUseCase: DestroyBannerAdUseCase,
    // Rewarded Ad use cases
    private val loadRewardedAdUseCase: LoadRewardedAdUseCase,
    private val showRewardedAdUseCase: ShowRewardedAdUseCase,
    private val checkRewardedAdAvailabilityUseCase: CheckRewardedAdAvailabilityUseCase,
    // ✅ NEW: Session unlock manager for rewarded ad unlocks
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

    // ✅ MVI FIX: Combine base state + session unlocks + billing status into single state
    val uiState: StateFlow<StoryUiState> = combine(
        _uiState,
        sessionUnlockManager.unlockedItems,
        billingManager.isPurchased  // ✅ Add premium status to state
    ) { baseState, unlockedIds, isPremium ->
        // ✅ Debug: Log state combination
        Log.d("StoryViewModel", "🔄 State combine - UnlockedIds: $unlockedIds, isPremium: $isPremium")

        // ✅ If base state is Content, merge with unlocked IDs AND premium status
        if (baseState is StoryUiState.Content) {
            val newState = baseState.copy(
                adUnlockedIds = unlockedIds,
                isPremium = isPremium  // ✅ MVI: Single source of truth
            )
            Log.d("StoryViewModel", "✅ Updated state: ${unlockedIds.size} unlocked, premium=$isPremium")
            newState
        } else {
            Log.d("StoryViewModel", "⚠️ Base state is not Content: ${baseState::class.simpleName}")
            baseState
        }
    }.onStart {
        initializeAds()

        loadBannerAd(
            adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
            adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
        )

        // Preload rewarded ad for story unlock
        loadRewardedAd(AdMobDataSource.TEST_REWARDED_AD_UNIT_ID)

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

    init {
        Log.d("StoryViewModel", "🚀 ViewModel initialized")

        // ✅ MVI FIX: Initialize ads based on combined state (wait for billing + content)
        viewModelScope.launch {
            var adsInitialized = false  // ✅ FIX: Prevent infinite ad loading loop
            uiState.collect { state ->
                if (state is StoryUiState.Content) {
                    Log.d("StoryViewModel", "💳 State updated - isPremium: ${state.isPremium}")

                    if (!state.isPremium) {
                        // ✅ Initialize AdMob SDK once
                        if (!adsInitialized) {
                            Log.d("StoryViewModel", "📢 Free user - Initializing ads")
                            initializeAds()
                            // Start monitoring network for ad loading
                            monitorNetworkForAdLoading()
                            adsInitialized = true  // ✅ Mark as initialized
                        }

                        // ✅ Reload banner ad if missing (handles back navigation)
                        val bannerAd = state.adState.bannerAd
                        if (internetConnectionManager.isCurrentlyConnected() &&
                            (bannerAd == null || (!bannerAd.isLoaded && !bannerAd.isLoading))) {
                            Log.d("StoryViewModel", "🔄 Banner ad missing - Reloading")
                            loadBannerAd(
                                adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                                adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
                            )
                        }
                    } else {
                        Log.d("StoryViewModel", "🏆 Premium user - Skipping all ad initialization")
                        adsInitialized = true  // ✅ Mark as initialized even for premium
                    }
                }
            }
        }
    }

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

    // AdMob functionality
    private fun initializeAds() {
        Log.d("StoryViewModel", "🎯 Initializing AdMob...")
        viewModelScope.launch {
            try {
                initializeAdMobUseCase()
                Log.d("StoryViewModel", "✅ AdMob initialized successfully")

                // Update UI state to reflect initialization
                val currentState = _uiState.value as? StoryUiState.Content
                if (currentState != null) {
                    _uiState.value = currentState.copy(
                        adState = currentState.adState.copy(isAdInitialized = true)
                    )
                }
            } catch (e: Exception) {
                Log.e("StoryViewModel", "❌ AdMob initialization failed: ${e.message}", e)
            }
        }
    }

    private fun loadBannerAd(
        adUnitId: String,
        adSizeType: com.naptune.lullabyandstory.domain.model.AdSizeType
    ) {
        // 🏆 Skip ad loading for premium users
        // ✅ MVI FIX: Get premium status from current state
        val currentState = uiState.value
        if (currentState is StoryUiState.Content && currentState.isPremium) {
            Log.d("StoryViewModel", "🏆 Premium user - Skipping banner ad load")
            return
        }

        Log.d("StoryViewModel", "🎯 Loading banner ad: $adUnitId")
        viewModelScope.launch {
            try {
                loadBannerAdUseCase(adUnitId, adSizeType).collect { result ->
                    val currentState = _uiState.value as? StoryUiState.Content
                    if (currentState != null) {
                        when (result) {
                            is AdLoadResult.Loading -> {
                                Log.d("StoryViewModel", "⏳ Banner ad loading...")
                                val defaultHeight = when (adSizeType) {
                                    com.naptune.lullabyandstory.domain.model.AdSizeType.ANCHORED_ADAPTIVE_BANNER -> 90
                                    com.naptune.lullabyandstory.domain.model.AdSizeType.LARGE_BANNER -> 100
                                    else -> 50
                                }

                                val loadingAd =
                                    currentState.adState.bannerAd?.copy(isLoading = true, error = null)
                                        ?: com.naptune.lullabyandstory.domain.model.BannerAdDomainModel(
                                            adUnitId = adUnitId,
                                            adSize = com.naptune.lullabyandstory.domain.model.AdSize(
                                                width = -1,
                                                height = defaultHeight,
                                                type = adSizeType
                                            ),
                                            isLoading = true
                                        )
                                _uiState.value = currentState.copy(
                                    adState = currentState.adState.copy(bannerAd = loadingAd)
                                )
                            }

                            is AdLoadResult.Success -> {
                                Log.d("StoryViewModel", "✅ Banner ad loaded successfully")
                                _uiState.value = currentState.copy(
                                    adState = currentState.adState.copy(bannerAd = result.bannerAd)
                                )
                            }

                            is AdLoadResult.Error -> {
                                Log.e(
                                    "StoryViewModel",
                                    "❌ Banner ad load failed: ${result.message}"
                                )
                                val defaultHeight = when (adSizeType) {
                                    com.naptune.lullabyandstory.domain.model.AdSizeType.ANCHORED_ADAPTIVE_BANNER -> 90
                                    com.naptune.lullabyandstory.domain.model.AdSizeType.LARGE_BANNER -> 100
                                    else -> 50
                                }

                                val errorAd = currentState.adState.bannerAd?.copy(
                                    isLoading = false,
                                    isLoaded = false,
                                    error = result.message
                                ) ?: com.naptune.lullabyandstory.domain.model.BannerAdDomainModel(
                                    adUnitId = adUnitId,
                                    adSize = com.naptune.lullabyandstory.domain.model.AdSize(
                                        width = -1,
                                        height = defaultHeight,
                                        type = adSizeType
                                    ),
                                    isLoading = false,
                                    isLoaded = false,
                                    error = result.message
                                )
                                _uiState.value = currentState.copy(
                                    adState = currentState.adState.copy(bannerAd = errorAd)
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("StoryViewModel", "❌ Banner ad loading failed: ${e.message}", e)

                // Update UI with error state
                val currentState = _uiState.value as? StoryUiState.Content
                if (currentState != null) {
                    val defaultHeight = when (adSizeType) {
                        com.naptune.lullabyandstory.domain.model.AdSizeType.ANCHORED_ADAPTIVE_BANNER -> 90
                        com.naptune.lullabyandstory.domain.model.AdSizeType.LARGE_BANNER -> 100
                        else -> 50
                    }

                    val errorAd = BannerAdDomainModel(
                        adUnitId = adUnitId,
                        adSize = com.naptune.lullabyandstory.domain.model.AdSize(
                            width = -1,
                            height = defaultHeight,
                            type = adSizeType
                        ),
                        isLoaded = false,
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                    _uiState.value = currentState.copy(
                        adState = currentState.adState.copy(bannerAd = errorAd)
                    )
                }
            }
        }
    }

    private fun destroyBannerAd(adUnitId: String) {
        Log.d("StoryViewModel", "🎯 Destroying banner ad: $adUnitId")
        viewModelScope.launch {
            try {
                destroyBannerAdUseCase(adUnitId)
                Log.d("StoryViewModel", "✅ Banner ad destroyed successfully")

                // Clear ad from UI state
                val currentState = _uiState.value as? StoryUiState.Content
                if (currentState != null) {
                    _uiState.value = currentState.copy(bannerAd = null)
                }
            } catch (e: Exception) {
                Log.e("StoryViewModel", "❌ Banner ad destruction failed: ${e.message}", e)
            }
        }
    }

    // Rewarded Ad functionality
    private fun loadRewardedAd(adUnitId: String) {
        // 🏆 Skip ad loading for premium users
        // ✅ MVI FIX: Get premium status from current state
        val currentState = uiState.value
        if (currentState is StoryUiState.Content && currentState.isPremium) {
            Log.d("StoryViewModel", "🏆 Premium user - Skipping rewarded ad load")
            return
        }

        Log.d("StoryViewModel", "🎁 Loading rewarded ad: $adUnitId")
        viewModelScope.launch {
            try {
                loadRewardedAdUseCase(adUnitId).collect { result ->
                    val currentState = _uiState.value as? StoryUiState.Content
                    if (currentState != null) {
                        when (result) {
                            is RewardedAdLoadResult.Loading -> {
                                Log.d("StoryViewModel", "⏳ Rewarded ad loading...")
                                _uiState.value = currentState.copy(
                                    isLoadingRewardedAd = true,
                                    rewardedAdError = null
                                )
                            }
                            
                            is RewardedAdLoadResult.Success -> {
                                Log.d("StoryViewModel", "✅ Rewarded ad loaded successfully")
                                _uiState.value = currentState.copy(
                                    rewardedAd = result.rewardedAd,
                                    isLoadingRewardedAd = false,
                                    rewardedAdError = null
                                )
                            }
                            
                            is RewardedAdLoadResult.Error -> {
                                Log.e("StoryViewModel", "❌ Rewarded ad load failed: ${result.message}")
                                _uiState.value = currentState.copy(
                                    isLoadingRewardedAd = false,
                                    rewardedAdError = result.message
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("StoryViewModel", "💥 Exception loading rewarded ad: ${e.message}", e)
                val currentState = _uiState.value as? StoryUiState.Content
                if (currentState != null) {
                    _uiState.value = currentState.copy(
                        isLoadingRewardedAd = false,
                        rewardedAdError = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    private fun showRewardedAd(adUnitId: String, activity: android.app.Activity, story: StoryDomainModel) {
        Log.d("StoryViewModel", "🎬 Showing rewarded ad: $adUnitId for story: ${story.storyName}")

        // ✅ Check if ad is available
        if (!checkRewardedAdAvailabilityUseCase(adUnitId)) {
            Log.w("StoryViewModel", "❌ Rewarded ad not available, loading new ad...")
            loadRewardedAd(adUnitId)
            Toast.makeText(activity, "Loading ad, please try again in a moment", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModelScope.launch {
            try {
                showRewardedAdUseCase(adUnitId, activity).collect { result ->
                    val currentState = _uiState.value as? StoryUiState.Content
                    if (currentState != null) {
                        when (result) {
                            is RewardedAdShowResult.Loading -> {
                                Log.d("StoryViewModel", "🎬 Showing rewarded ad...")
                                _uiState.value = currentState.copy(
                                    lastRewardedStory = story
                                )
                            }
                            
                            is RewardedAdShowResult.Success -> {
                                Log.d("StoryViewModel", "🎁 User earned reward: ${result.reward}")
                                _uiState.value = currentState.copy(
                                    lastRewardedStory = story,
                                    rewardedAd = null // Ad consumed, need to reload
                                )
                                
                                // TODO: Handle reward logic here (unlock story, grant premium access, etc.)
                                handleRewardEarned(story, result.reward, activity)
                                
                                // Preload next rewarded ad
                                loadRewardedAd(adUnitId)
                            }
                            
                            is RewardedAdShowResult.Dismissed -> {
                                Log.d("StoryViewModel", "🚪 Rewarded ad dismissed: ${result.reason}")
                                _uiState.value = currentState.copy(
                                    rewardedAd = null // Ad consumed, need to reload
                                )
                                
                                // Preload next rewarded ad
                                loadRewardedAd(adUnitId)
                            }
                            
                            is RewardedAdShowResult.Error -> {
                                Log.e("StoryViewModel", "❌ Rewarded ad show failed: ${result.message}")
                                _uiState.value = currentState.copy(
                                    rewardedAdError = result.message
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("StoryViewModel", "💥 Exception showing rewarded ad: ${e.message}", e)
                val currentState = _uiState.value as? StoryUiState.Content
                if (currentState != null) {
                    _uiState.value = currentState.copy(
                        rewardedAdError = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    private fun handleRewardEarned(story: StoryDomainModel, reward: com.naptune.lullabyandstory.domain.model.RewardDomainModel, activity: android.app.Activity) {
        Log.d("StoryViewModel", "🎉 Processing reward for story: ${story.storyName}")
        Log.d("StoryViewModel", "🎁 Reward: ${reward.type} - ${reward.amount}")
        Log.d("StoryViewModel", "📋 Story documentId: ${story.documentId}")

        // ✅ NEW: Unlock story for current session via SessionUnlockManager
        sessionUnlockManager.unlockItem(
            itemId = story.documentId,
            itemType = com.naptune.lullabyandstory.data.manager.UnlockType.Story
        )

        // ✅ Debug: Verify unlock was successful
        val isNowUnlocked = sessionUnlockManager.isItemUnlocked(story.documentId)
        val totalUnlocked = sessionUnlockManager.getUnlockCount()
        Log.d("StoryViewModel", "✅ Story unlocked for session: ${story.storyName}")
        Log.d("StoryViewModel", "🔓 Unlock verified: $isNowUnlocked")
        Log.d("StoryViewModel", "📊 Total unlocked items: $totalUnlocked")
        Log.d("StoryViewModel", "📜 All unlocked IDs: ${sessionUnlockManager.getUnlockedItems()}")

        // Show success toast
        Toast.makeText(
            activity,
            "🎁 ${story.storyName} unlocked for this session!",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun isRewardedAdAvailable(adUnitId: String = AdMobDataSource.TEST_REWARDED_AD_UNIT_ID): Boolean {
        return checkRewardedAdAvailabilityUseCase(adUnitId)
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
        
        val currentState = _uiState.value as? StoryUiState.Content
        if (currentState != null) {
            if (isConnected) {
                // ✅ Network available - check if ad needs to be loaded
                val bannerAd = currentState.adState.bannerAd
                if (bannerAd == null || (!bannerAd.isLoaded && !bannerAd.isLoading)) {
                    Log.d("StoryViewModel", "🚀 Network available - Starting banner ad load")
                    loadBannerAd(
                        adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                        adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
                    )
                } else {
                    Log.d("StoryViewModel", "✅ Banner ad already loaded or loading")
                }
            } else {
                // ✅ Network not available - clear ad state
                Log.d("StoryViewModel", "❌ Network not available - Clearing banner ad state")
                _uiState.value = currentState.copy(
                    adState = currentState.adState.copy(bannerAd = null)
                )
            }
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
