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
import com.naptune.lullabyandstory.domain.usecase.admob.InitializeAdMobUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.LoadBannerAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.DestroyBannerAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.LoadRewardedAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.ShowRewardedAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.CheckRewardedAdAvailabilityUseCase
import com.naptune.lullabyandstory.domain.model.BannerAdDomainModel
import com.naptune.lullabyandstory.domain.model.RewardedAdLoadResult
import com.naptune.lullabyandstory.domain.model.RewardedAdShowResult
import com.naptune.lullabyandstory.domain.model.AdLoadResult
import android.util.Log
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
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

@HiltViewModel
class FavouriteViewModel @Inject constructor(
    private val getFavouriteLullabiesUseCase: GetFavouriteLullabiesUseCase,
    private val getFavouriteStoriesUseCase: GetFavouriteStoriesUseCase,
    private val toggleLullabyFavouriteUseCase: ToggleLullabyFavouriteUseCase,
    private val toggleStoryFavouriteUseCase: ToogleStoryFavouriteUseCase,
    // ✅ NEW: Inject MusicController to track playing state
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
    private val sessionUnlockManager: com.naptune.lullabyandstory.data.manager.SessionUnlockManager,
    // ✅ Firebase Analytics helper
    private val analyticsHelper: com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper,
    // ✅ Billing - Premium status
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

    // ✅ NEW: Combine base state with session unlock manager
    val uiState: StateFlow<FavouriteUiState> = combine(
        _uiState,
        sessionUnlockManager.unlockedItems
    ) { baseState, unlockedIds ->
        Log.d("FavouriteViewModel", "🔄 State combine triggered - UnlockedIds: $unlockedIds")
        if (baseState is FavouriteUiState.Content) {
            val newState = baseState.copy(adUnlockedIds = unlockedIds)
            Log.d("FavouriteViewModel", "✅ Updated Content state with ${unlockedIds.size} unlocked items")
            newState
        } else {
            baseState
        }
    }.onStart {
        initializeAds()
        if (internetConnectionManager.isCurrentlyConnected()) {
            // ✅ Preload rewarded ad for story/lullaby unlock on start
            Log.d("FavouriteViewModel", "🎁 Preloading rewarded ad on start")
            loadRewardedAd(AdMobDataSource.TEST_REWARDED_AD_UNIT_ID)
        }
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
        Log.d("FavouriteViewModel", "🎯 Initializing AdMob...")
        viewModelScope.launch {
            try {
                initializeAdMobUseCase()
                Log.d("FavouriteViewModel", "✅ AdMob initialized successfully")

                // Update UI state to reflect initialization
                val currentState = _uiState.value as? FavouriteUiState.Content
                if (currentState != null) {
                    _uiState.value = currentState.copy(isAdInitialized = true)
                }
            } catch (e: Exception) {
                Log.e("FavouriteViewModel", "❌ AdMob initialization failed: ${e.message}", e)
            }
        }
    }
    
    private fun loadBannerAd(adUnitId: String, adSizeType: com.naptune.lullabyandstory.domain.model.AdSizeType) {
        // 🏆 Skip ad loading for premium users
        if (isPurchased.value) {
            Log.d("FavouriteViewModel", "🏆 Premium user - Skipping banner ad load")
            return
        }

        Log.d("FavouriteViewModel", "🎯 Loading banner ad: $adUnitId")
        viewModelScope.launch {
            try {
                loadBannerAdUseCase(adUnitId, adSizeType).collect { result ->
                    val currentState = _uiState.value as? FavouriteUiState.Content
                    if (currentState != null) {
                        when (result) {
                            is AdLoadResult.Loading -> {
                                Log.d("FavouriteViewModel", "⏳ Banner ad loading...")
                                val defaultHeight = when (adSizeType) {
                                    com.naptune.lullabyandstory.domain.model.AdSizeType.ANCHORED_ADAPTIVE_BANNER -> 90
                                    com.naptune.lullabyandstory.domain.model.AdSizeType.LARGE_BANNER -> 100
                                    else -> 50
                                }
                                
                                val loadingAd = currentState.adState.bannerAd?.copy(isLoading = true, error = null)
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
                                Log.d("FavouriteViewModel", "✅ Banner ad loaded successfully")
                                _uiState.value = currentState.copy(
                                    adState = currentState.adState.copy(bannerAd = result.bannerAd)
                                )
                            }
                            is AdLoadResult.Error -> {
                                Log.e("FavouriteViewModel", "❌ Banner ad load failed: ${result.message}")
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
                Log.e("FavouriteViewModel", "❌ Banner ad loading failed: ${e.message}", e)
                
                // Update UI with error state
                val currentState = _uiState.value as? FavouriteUiState.Content
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
        Log.d("FavouriteViewModel", "🎯 Destroying banner ad: $adUnitId")
        viewModelScope.launch {
            try {
                destroyBannerAdUseCase(adUnitId)
                Log.d("FavouriteViewModel", "✅ Banner ad destroyed successfully")
                
                // Clear ad from UI state
                val currentState = _uiState.value as? FavouriteUiState.Content
                if (currentState != null) {
                    _uiState.value = currentState.copy(
                        adState = currentState.adState.copy(bannerAd = null)
                    )
                }
            } catch (e: Exception) {
                Log.e("FavouriteViewModel", "❌ Banner ad destruction failed: ${e.message}", e)
            }
        }
    }
    
    // Rewarded ad methods
    private fun loadRewardedAd(adUnitId: String) {
        // 🏆 Skip ad loading for premium users
        if (isPurchased.value) {
            Log.d("FavouriteViewModel", "🏆 Premium user - Skipping rewarded ad load")
            return
        }

        Log.d("FavouriteViewModel", "🎬 Loading rewarded ad - Unit: $adUnitId")

        viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                val currentState = _uiState.value as? FavouriteUiState.Content ?: return@launch
                _uiState.value = currentState.copy(
                    adState = currentState.adState.copy(
                        isLoadingRewardedAd = true,
                        rewardedAdError = null
                    )
                )

                loadRewardedAdUseCase(adUnitId).collect { result ->
                    val state = _uiState.value as? FavouriteUiState.Content ?: return@collect
                    when (result) {
                        is RewardedAdLoadResult.Loading -> {
                            Log.d("FavouriteViewModel", "⏳ Rewarded ad loading...")
                            _uiState.value = state.copy(
                                adState = state.adState.copy(
                                    isLoadingRewardedAd = true,
                                    rewardedAdError = null
                                )
                            )
                        }
                        is RewardedAdLoadResult.Success -> {
                            Log.d("FavouriteViewModel", "✅ Rewarded ad loaded successfully")

                            // ✅ Analytics: Track ad loaded
                            val loadTime = System.currentTimeMillis() - startTime
                            analyticsHelper.logRewardedAdLoaded(adUnitId, loadTime)

                            _uiState.value = state.copy(
                                adState = state.adState.copy(
                                    rewardedAd = result.rewardedAd,
                                    isLoadingRewardedAd = false,
                                    rewardedAdError = null
                                )
                            )
                        }
                        is RewardedAdLoadResult.Error -> {
                            Log.e("FavouriteViewModel", "❌ Rewarded ad load failed: ${result.message}")

                            // ✅ Analytics: Track ad load failed
                            val networkType = if (internetConnectionManager.isCurrentlyConnected()) "connected" else "disconnected"
                            analyticsHelper.logRewardedAdLoadFailed(
                                adUnitId = adUnitId,
                                errorCode = "load_error",
                                errorMessage = result.message,
                                networkType = networkType
                            )

                            _uiState.value = state.copy(
                                adState = state.adState.copy(
                                    isLoadingRewardedAd = false,
                                    rewardedAdError = result.message
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FavouriteViewModel", "💥 Rewarded ad loading exception: ${e.message}", e)
                val currentState = _uiState.value as? FavouriteUiState.Content
                if (currentState != null) {
                    _uiState.value = currentState.copy(
                        adState = currentState.adState.copy(
                            isLoadingRewardedAd = false,
                            rewardedAdError = "Failed to load ad: ${e.message}"
                        )
                    )
                }
            }
        }
    }

    private fun showRewardedAdForStory(adUnitId: String, activity: Activity, story: StoryDomainModel) {
        Log.d("FavouriteViewModel", "🎬 Showing rewarded ad for story: ${story.storyName}")

        // ✅ Analytics: Track ad requested
        analyticsHelper.logRewardedAdRequested(
            contentType = "story",
            contentId = story.documentId,
            contentName = story.storyName,
            isPremium = !story.isFree,
            sourceScreen = "favourites",
            adUnitId = adUnitId
        )

        // ✅ Check if ad is available
        if (!checkRewardedAdAvailabilityUseCase(adUnitId)) {
            Log.w("FavouriteViewModel", "❌ Rewarded ad not available, loading new ad...")
            loadRewardedAd(adUnitId)
            Toast.makeText(activity, "Loading ad, please try again in a moment", Toast.LENGTH_SHORT).show()
            return
        }

        val adStartTime = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                showRewardedAdUseCase(adUnitId, activity).collect { result ->
                    when (result) {
                        is RewardedAdShowResult.Loading -> {
                            Log.d("FavouriteViewModel", "⏳ Rewarded ad loading for story: ${story.storyName}")

                            // ✅ Analytics: Track ad started
                            analyticsHelper.logRewardedAdStarted(adUnitId, story.documentId)
                        }
                        is RewardedAdShowResult.Success -> {
                            Log.d("FavouriteViewModel", "🎉 Reward earned for story: ${story.storyName} - ${result.reward.amount} ${result.reward.type}")
                            Log.d("FavouriteViewModel", "📋 Story documentId: ${story.documentId}")

                            val watchDuration = ((System.currentTimeMillis() - adStartTime) / 1000).toInt()

                            // ✅ Analytics: Track ad completed
                            analyticsHelper.logRewardedAdCompleted(
                                adUnitId = adUnitId,
                                contentType = "story",
                                contentId = story.documentId,
                                contentName = story.storyName,
                                rewardAmount = result.reward.amount,
                                rewardType = result.reward.type,
                                watchDurationSeconds = watchDuration
                            )

                            // ✅ NEW: Unlock story for current session via SessionUnlockManager
                            sessionUnlockManager.unlockItem(
                                itemId = story.documentId,
                                itemType = com.naptune.lullabyandstory.data.manager.UnlockType.Story
                            )

                            // ✅ Analytics: Track content unlocked via ad
                            analyticsHelper.logContentUnlockedViaAd(
                                contentType = "story",
                                contentId = story.documentId,
                                contentName = story.storyName,
                                category = "story",
                                sessionUnlockCount = sessionUnlockManager.getUnlockCount(),
                                timeSpentBeforeUnlock = (System.currentTimeMillis() - adStartTime) / 1000
                            )

                            // ✅ Analytics: Track session unlock used
                            analyticsHelper.logSessionUnlockUsed(
                                contentType = "story",
                                contentId = story.documentId,
                                contentName = story.storyName,
                                minutesSinceUnlock = 0L // Just unlocked
                            )

                            // ✅ Debug: Verify unlock
                            val isNowUnlocked = sessionUnlockManager.isItemUnlocked(story.documentId)
                            Log.d("FavouriteViewModel", "🔓 Story unlock verified: $isNowUnlocked")
                            Log.d("FavouriteViewModel", "📊 Total unlocked items: ${sessionUnlockManager.getUnlockCount()}")

                            // Show success toast
                            Toast.makeText(
                                activity,
                                "🎁 ${story.storyName} unlocked for this session!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // ✅ Preload next rewarded ad
                            loadRewardedAd(adUnitId)
                        }
                        is RewardedAdShowResult.Dismissed -> {
                            Log.d("FavouriteViewModel", "📱 Rewarded ad dismissed for story: ${story.storyName} - ${result.reason}")

                            // ✅ Analytics: Track ad closed early (if not completed)
                            if (result.reason != "completed") {
                                val watchedSeconds = ((System.currentTimeMillis() - adStartTime) / 1000).toInt()
                                analyticsHelper.logRewardedAdClosedEarly(
                                    adUnitId = adUnitId,
                                    contentId = story.documentId,
                                    watchedSeconds = watchedSeconds,
                                    requiredSeconds = 30 // Typical rewarded ad duration
                                )
                            }

                            // ✅ Preload next rewarded ad
                            loadRewardedAd(adUnitId)
                        }
                        is RewardedAdShowResult.Error -> {
                            Log.e("FavouriteViewModel", "❌ Failed to show rewarded ad for story: ${result.message}")
                            val currentState = _uiState.value as? FavouriteUiState.Content
                            if (currentState != null) {
                                _uiState.value = currentState.copy(
                                    adState = currentState.adState.copy(
                                        rewardedAdError = result.message
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FavouriteViewModel", "💥 Show rewarded ad exception for story: ${e.message}", e)
            }
        }
    }

    private fun showRewardedAdForLullaby(adUnitId: String, activity: Activity, lullaby: LullabyDomainModel) {
        Log.d("FavouriteViewModel", "🎬 Showing rewarded ad for lullaby: ${lullaby.musicName}")

        // ✅ Analytics: Track ad requested
        analyticsHelper.logRewardedAdRequested(
            contentType = "lullaby",
            contentId = lullaby.documentId,
            contentName = lullaby.musicName,
            isPremium = !lullaby.isFree,
            sourceScreen = "favourites",
            adUnitId = adUnitId
        )

        // ✅ Check if ad is available
        if (!checkRewardedAdAvailabilityUseCase(adUnitId)) {
            Log.w("FavouriteViewModel", "❌ Rewarded ad not available, loading new ad...")
            loadRewardedAd(adUnitId)
            Toast.makeText(activity, "Loading ad, please try again in a moment", Toast.LENGTH_SHORT).show()
            return
        }

        val adStartTime = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                showRewardedAdUseCase(adUnitId, activity).collect { result ->
                    when (result) {
                        is RewardedAdShowResult.Loading -> {
                            Log.d("FavouriteViewModel", "⏳ Rewarded ad loading for lullaby: ${lullaby.musicName}")

                            // ✅ Analytics: Track ad started
                            analyticsHelper.logRewardedAdStarted(adUnitId, lullaby.documentId)
                        }
                        is RewardedAdShowResult.Success -> {
                            Log.d("FavouriteViewModel", "🎉 Reward earned for lullaby: ${lullaby.musicName} - ${result.reward.amount} ${result.reward.type}")
                            Log.d("FavouriteViewModel", "📋 Lullaby documentId: ${lullaby.documentId}")

                            val watchDuration = ((System.currentTimeMillis() - adStartTime) / 1000).toInt()

                            // ✅ Analytics: Track ad completed
                            analyticsHelper.logRewardedAdCompleted(
                                adUnitId = adUnitId,
                                contentType = "lullaby",
                                contentId = lullaby.documentId,
                                contentName = lullaby.musicName,
                                rewardAmount = result.reward.amount,
                                rewardType = result.reward.type,
                                watchDurationSeconds = watchDuration
                            )

                            // ✅ NEW: Unlock lullaby for current session via SessionUnlockManager
                            sessionUnlockManager.unlockItem(
                                itemId = lullaby.documentId,
                                itemType = com.naptune.lullabyandstory.data.manager.UnlockType.Lullaby
                            )

                            // ✅ Analytics: Track content unlocked via ad
                            analyticsHelper.logContentUnlockedViaAd(
                                contentType = "lullaby",
                                contentId = lullaby.documentId,
                                contentName = lullaby.musicName,
                                category = "lullaby",
                                sessionUnlockCount = sessionUnlockManager.getUnlockCount(),
                                timeSpentBeforeUnlock = (System.currentTimeMillis() - adStartTime) / 1000
                            )

                            // ✅ Analytics: Track session unlock used
                            analyticsHelper.logSessionUnlockUsed(
                                contentType = "lullaby",
                                contentId = lullaby.documentId,
                                contentName = lullaby.musicName,
                                minutesSinceUnlock = 0L // Just unlocked
                            )

                            // ✅ Debug: Verify unlock
                            val isNowUnlocked = sessionUnlockManager.isItemUnlocked(lullaby.documentId)
                            Log.d("FavouriteViewModel", "🔓 Lullaby unlock verified: $isNowUnlocked")
                            Log.d("FavouriteViewModel", "📊 Total unlocked items: ${sessionUnlockManager.getUnlockCount()}")

                            // Show success toast
                            Toast.makeText(
                                activity,
                                "🎁 ${lullaby.musicName} unlocked for this session!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // ✅ Preload next rewarded ad
                            loadRewardedAd(adUnitId)
                        }
                        is RewardedAdShowResult.Dismissed -> {
                            Log.d("FavouriteViewModel", "📱 Rewarded ad dismissed for lullaby: ${lullaby.musicName} - ${result.reason}")

                            // ✅ Analytics: Track ad closed early (if not completed)
                            if (result.reason != "completed") {
                                val watchedSeconds = ((System.currentTimeMillis() - adStartTime) / 1000).toInt()
                                analyticsHelper.logRewardedAdClosedEarly(
                                    adUnitId = adUnitId,
                                    contentId = lullaby.documentId,
                                    watchedSeconds = watchedSeconds,
                                    requiredSeconds = 30 // Typical rewarded ad duration
                                )
                            }

                            // ✅ Preload next rewarded ad
                            loadRewardedAd(adUnitId)
                        }
                        is RewardedAdShowResult.Error -> {
                            Log.e("FavouriteViewModel", "❌ Failed to show rewarded ad for lullaby: ${result.message}")
                            val currentState = _uiState.value as? FavouriteUiState.Content
                            if (currentState != null) {
                                _uiState.value = currentState.copy(
                                    adState = currentState.adState.copy(
                                        rewardedAdError = result.message
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FavouriteViewModel", "💥 Show rewarded ad exception for lullaby: ${e.message}", e)
            }
        }
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
