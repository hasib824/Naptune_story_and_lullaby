package com.naptune.lullabyandstory.presentation.explore

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.domain.data.DownloadLullabyResult
import com.naptune.lullabyandstory.domain.usecase.lullaby.FetchLullabiesUseCase
import com.naptune.lullabyandstory.domain.usecase.lullaby.DownloaLullabyUsecase
import com.naptune.lullabyandstory.domain.usecase.lullaby.ToggleLullabyFavouriteUseCase
import com.naptune.lullabyandstory.domain.usecase.story.FetchStoriesUsecase
import com.naptune.lullabyandstory.domain.usecase.story.ToogleStoryFavouriteUseCase
import com.naptune.lullabyandstory.presentation.player.service.MusicController
import com.naptune.lullabyandstory.utils.InternetConnectionManager
import com.naptune.lullabyandstory.domain.usecase.admob.*
import com.naptune.lullabyandstory.domain.model.*
import com.naptune.lullabyandstory.presentation.main.AdUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    // Lullaby use cases
    private val fetchLullabiesUseCase: FetchLullabiesUseCase,
    private val downloadLullabyUseCase: DownloaLullabyUsecase,
    private val toggleLullabyFavouriteUseCase: ToggleLullabyFavouriteUseCase,
    // Story use cases
    private val fetchStoriesUseCase: FetchStoriesUsecase,
    private val toggleStoryFavouriteUseCase: ToogleStoryFavouriteUseCase,
    // Music controller
    private val musicController: MusicController,
    // Internet connection
    private val internetConnectionManager: InternetConnectionManager,
    // AdMob use cases
    private val initializeAdMobUseCase: InitializeAdMobUseCase,
    private val loadBannerAdUseCase: LoadBannerAdUseCase,
    private val destroyBannerAdUseCase: DestroyBannerAdUseCase,
    private val loadRewardedAdUseCase: LoadRewardedAdUseCase,
    private val showRewardedAdUseCase: ShowRewardedAdUseCase,
    private val checkRewardedAdAvailabilityUseCase: CheckRewardedAdAvailabilityUseCase,
    // ✅ Analytics
    private val analyticsHelper: com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper
) : ViewModel() {

    companion object {
        private const val TAG = "ExploreViewModel"
    }

    init {
        // ✅ Track screen view
        trackScreenView()
    }

    /**
     * ✅ Track screen view
     */
    private fun trackScreenView() {
        /*analyticsHelper.tatlogScreenView(
            screenName = "Explore",
            screenClass = "ExploreScreen"
        )*/
    }

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.IsLoading)
    val uiState: StateFlow<ExploreUiState> = _uiState
        .onStart {
            loadData()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExploreUiState.IsLoading
        )

    // Expose network state for banner ads
    val isNetworkAvailable: StateFlow<Boolean> = internetConnectionManager.isNetworkAvailable

    // Currently playing lullaby ID
    val currentlyPlayingLullabyId: StateFlow<String?> = combine(
        musicController.currentAudioItem,
        musicController.isPlaying
    ) { audioItem, _ ->
        if (audioItem?.isFromStory == false) {
            audioItem.documentId
        } else {
            null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Currently playing story ID
    val currentlyPlayingStoryId: StateFlow<String?> = combine(
        musicController.currentAudioItem,
        musicController.isPlaying
    ) { audioItem, _ ->
        if (audioItem?.isFromStory == true) {
            audioItem.documentId
        } else {
            null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        Log.d(TAG, "🚀 ExploreViewModel initialized")
        monitorNetworkForAdLoading()
    }

    fun handleIntent(intent: ExploreIntent) {
        when (intent) {
            is ExploreIntent.LoadData -> loadData()
            is ExploreIntent.ChangeContentCategory -> changeContentCategory(intent.category)
            is ExploreIntent.ChangeFilterCategory -> changeFilterCategory(intent.category)
            is ExploreIntent.DownloadLullaby -> downloadLullaby(intent.lullaby)
            is ExploreIntent.ToggleLullabyFavourite -> toggleLullabyFavourite(intent.lullabyId)
            is ExploreIntent.ToggleStoryFavourite -> toggleStoryFavourite(intent.storyId)
            // AdMob intents
            ExploreIntent.InitializeAds -> initializeAds()
            is ExploreIntent.LoadBannerAd -> loadBannerAd(intent.adUnitId, intent.adSizeType)
            ExploreIntent.DestroyBannerAd -> destroyBannerAd()
            is ExploreIntent.LoadRewardedAd -> loadRewardedAd(intent.adUnitId)
            is ExploreIntent.ShowRewardedAdForLullaby -> showRewardedAdForLullaby(
                intent.adUnitId, intent.activity, intent.lullaby
            )
            is ExploreIntent.ShowRewardedAdForStory -> showRewardedAdForStory(
                intent.adUnitId, intent.activity, intent.story
            )
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = ExploreUiState.IsLoading

            try {
                // Fetch both lullabies and stories
                combine(
                    fetchLullabiesUseCase(),
                    fetchStoriesUseCase()
                ) { lullabies, stories ->
                    Pair(lullabies, stories)
                }.collect { (lullabies, stories) ->
                    // Filter lullabies
                    val allLullabies = lullabies
                    val popularLullabies = lullabies.filter { it.popularity_count > 0 }
                    val freeLullabies = lullabies.filter { it.isFree }

                    // Filter stories
                    val allStories = stories
                    val popularStories = stories.filter { it.popularity_count > 0 }
                    val freeStories = stories.filter { it.isFree }

                    val currentState = _uiState.value as? ExploreUiState.Content
                    _uiState.value = ExploreUiState.Content(
                        contentCategory = currentState?.contentCategory ?: ExploreContentCategory.LULLABY,
                        filterCategory = currentState?.filterCategory ?: ExploreFilterCategory.ALL,
                        allLullabies = allLullabies,
                        popularLullabies = popularLullabies,
                        freeLullabies = freeLullabies,
                        allStories = allStories,
                        popularStories = popularStories,
                        freeStories = freeStories,
                        adState = currentState?.adState ?: AdUiState()
                    )

                    Log.d(TAG, "✅ Data loaded - Lullabies: ${allLullabies.size}, Stories: ${allStories.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading data: ${e.message}")
                _uiState.value = ExploreUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun changeContentCategory(category: ExploreContentCategory) {
        val currentState = _uiState.value as? ExploreUiState.Content ?: return
        _uiState.value = currentState.copy(contentCategory = category)
        Log.d(TAG, "📂 Content category changed to: $category")
    }

    private fun changeFilterCategory(category: ExploreFilterCategory) {
        val currentState = _uiState.value as? ExploreUiState.Content ?: return
        _uiState.value = currentState.copy(filterCategory = category)
        Log.d(TAG, "🔍 Filter category changed to: $category")
    }

    private fun downloadLullaby(lullaby: LullabyDomainModel) {
        viewModelScope.launch {
            downloadLullabyUseCase(lullaby).collect { result ->
                when (result) {
                    is DownloadLullabyResult.Progress -> {
                        val currentState = _uiState.value as? ExploreUiState.Content ?: return@collect
                        val updatedDownloadingItems = currentState.downloadingItems + lullaby.documentId
                        val updatedProgress = currentState.downloadProgress + (lullaby.documentId to result.progressPercentige)
                        _uiState.value = currentState.copy(
                            downloadingItems = updatedDownloadingItems,
                            downloadProgress = updatedProgress
                        )
                    }
                    is DownloadLullabyResult.Completed -> {
                        val currentState = _uiState.value as? ExploreUiState.Content ?: return@collect
                        val updatedDownloadingItems = currentState.downloadingItems - lullaby.documentId
                        val updatedProgress = currentState.downloadProgress - lullaby.documentId
                        val updatedDownloadedItems = currentState.downloadedItems + lullaby.documentId
                        _uiState.value = currentState.copy(
                            downloadingItems = updatedDownloadingItems,
                            downloadProgress = updatedProgress,
                            downloadedItems = updatedDownloadedItems
                        )
                        Log.d(TAG, "✅ Lullaby downloaded: ${lullaby.musicName}")
                    }
                    is DownloadLullabyResult.Error -> {
                        val currentState = _uiState.value as? ExploreUiState.Content ?: return@collect
                        val updatedDownloadingItems = currentState.downloadingItems - lullaby.documentId
                        val updatedProgress = currentState.downloadProgress - lullaby.documentId
                        _uiState.value = currentState.copy(
                            downloadingItems = updatedDownloadingItems,
                            downloadProgress = updatedProgress
                        )
                        Log.e(TAG, "❌ Download error: ${result.message}")
                    }
                }
            }
        }
    }

    private fun toggleLullabyFavourite(lullabyId: String) {
        viewModelScope.launch {
            try {
                toggleLullabyFavouriteUseCase(lullabyId)
                Log.d(TAG, "❤️ Lullaby favourite toggled: $lullabyId")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error toggling lullaby favourite: ${e.message}")
            }
        }
    }

    private fun toggleStoryFavourite(storyId: String) {
        viewModelScope.launch {
            try {
                toggleStoryFavouriteUseCase(storyId)
                Log.d(TAG, "❤️ Story favourite toggled: $storyId")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error toggling story favourite: ${e.message}")
            }
        }
    }

    // ========== AdMob Methods ==========

    private fun initializeAds() {
        viewModelScope.launch {
            initializeAdMobUseCase()
            Log.d(TAG, "🎯 AdMob initialized")
        }
    }

    private fun loadBannerAd(adUnitId: String, adSizeType: AdSizeType) {
        viewModelScope.launch {
            loadBannerAdUseCase(adUnitId, adSizeType).collect { result ->
                val currentState = _uiState.value as? ExploreUiState.Content ?: return@collect
                when (result) {
                    is AdLoadResult.Loading -> {
                        val defaultHeight = when (adSizeType) {
                            AdSizeType.ANCHORED_ADAPTIVE_BANNER -> 90
                            AdSizeType.LARGE_BANNER -> 100
                            else -> 50
                        }
                        val loadingAd = currentState.adState.bannerAd?.copy(isLoading = true, error = null)
                            ?: BannerAdDomainModel(
                                adUnitId = adUnitId,
                                adSize = AdSize(width = 320, height = defaultHeight, type = adSizeType),
                                isLoading = true,
                                isLoaded = false,
                                error = null
                            )
                        _uiState.value = currentState.copy(
                            adState = currentState.adState.copy(
                                bannerAd = loadingAd,
                                isAdInitialized = true
                            )
                        )
                        Log.d(TAG, "⏳ Banner ad loading...")
                    }
                    is AdLoadResult.Success -> {
                        val updatedState = _uiState.value as? ExploreUiState.Content ?: return@collect
                        _uiState.value = updatedState.copy(
                            adState = updatedState.adState.copy(
                                bannerAd = result.bannerAd,
                                isAdInitialized = true
                            )
                        )
                        Log.d(TAG, "✅ Banner ad loaded successfully")
                    }
                    is AdLoadResult.Error -> {
                        val updatedState = _uiState.value as? ExploreUiState.Content ?: return@collect
                        val defaultHeight = when (adSizeType) {
                            AdSizeType.ANCHORED_ADAPTIVE_BANNER -> 90
                            AdSizeType.LARGE_BANNER -> 100
                            else -> 50
                        }
                        val errorAd = updatedState.adState.bannerAd?.copy(
                            isLoading = false,
                            isLoaded = false,
                            error = result.message
                        ) ?: BannerAdDomainModel(
                            adUnitId = adUnitId,
                            adSize = AdSize(width = 320, height = defaultHeight, type = adSizeType),
                            isLoading = false,
                            isLoaded = false,
                            error = result.message
                        )
                        _uiState.value = updatedState.copy(
                            adState = updatedState.adState.copy(bannerAd = errorAd)
                        )
                        Log.e(TAG, "❌ Banner ad load error: ${result.message}")
                    }
                }
            }
        }
    }

    private fun destroyBannerAd() {
        // ✅ Use GlobalScope for cleanup that must complete (viewModelScope is cancelled in onCleared)
        GlobalScope.launch(Dispatchers.Main.immediate) {
            try {
                destroyBannerAdUseCase("explore_banner")
                val currentState = _uiState.value as? ExploreUiState.Content ?: return@launch
                _uiState.value = currentState.copy(adState = AdUiState())
                Log.d(TAG, "🗑️ Banner ad destroyed")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error during cleanup: ${e.message}")
            }
        }
    }

    private fun loadRewardedAd(adUnitId: String) {
        viewModelScope.launch {
            loadRewardedAdUseCase(adUnitId).collect { result ->
                when (result) {
                    is RewardedAdLoadResult.Loading -> {
                        Log.d(TAG, "⏳ Rewarded ad loading...")
                    }
                    is RewardedAdLoadResult.Success -> {
                        Log.d(TAG, "✅ Rewarded ad loaded successfully")
                    }
                    is RewardedAdLoadResult.Error -> {
                        Log.e(TAG, "❌ Rewarded ad load error: ${result.message}")
                    }
                }
            }
        }
    }

    private fun showRewardedAdForLullaby(adUnitId: String, activity: android.app.Activity, lullaby: LullabyDomainModel) {
        viewModelScope.launch {
            showRewardedAdUseCase(adUnitId, activity).collect { result ->
                when (result) {
                    is RewardedAdShowResult.Loading -> {
                        Log.d(TAG, "⏳ Showing rewarded ad...")
                    }
                    is RewardedAdShowResult.Success -> {
                        Log.d(TAG, "✅ Rewarded ad shown - unlocking lullaby: ${lullaby.musicName}")
                        // TODO: Implement unlock logic
                    }
                    is RewardedAdShowResult.Dismissed -> {
                        Log.d(TAG, "⚠️ Rewarded ad dismissed: ${result.reason}")
                    }
                    is RewardedAdShowResult.Error -> {
                        Log.e(TAG, "❌ Rewarded ad show error: ${result.message}")
                    }
                }
            }
        }
    }

    private fun showRewardedAdForStory(adUnitId: String, activity: android.app.Activity, story: StoryDomainModel) {
        viewModelScope.launch {
            showRewardedAdUseCase(adUnitId, activity).collect { result ->
                when (result) {
                    is RewardedAdShowResult.Loading -> {
                        Log.d(TAG, "⏳ Showing rewarded ad...")
                    }
                    is RewardedAdShowResult.Success -> {
                        Log.d(TAG, "✅ Rewarded ad shown - unlocking story: ${story.storyName}")
                        // TODO: Implement unlock logic
                    }
                    is RewardedAdShowResult.Dismissed -> {
                        Log.d(TAG, "⚠️ Rewarded ad dismissed: ${result.reason}")
                    }
                    is RewardedAdShowResult.Error -> {
                        Log.e(TAG, "❌ Rewarded ad show error: ${result.message}")
                    }
                }
            }
        }
    }

    private fun monitorNetworkForAdLoading() {
        viewModelScope.launch {
            isNetworkAvailable.collect { isAvailable ->
                Log.d(TAG, "🌐 Network state changed: $isAvailable")
                // Auto-reload banner ad when network becomes available
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        destroyBannerAd()
        Log.d(TAG, "🧹 ViewModel cleared")
    }
}
