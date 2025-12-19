package com.naptune.lullabyandstory.presentation.lullaby

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.data.billing.BillingManager
import com.naptune.lullabyandstory.data.manager.AdManager
import com.naptune.lullabyandstory.data.manager.SessionUnlockManager
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.domain.data.DownloadLullabyResult
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.model.ContentInfo
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.usecase.lullaby.DownloaLullabyUsecase
import com.naptune.lullabyandstory.domain.usecase.lullaby.FetchLullabiesUseCase
import com.naptune.lullabyandstory.presentation.main.AdUiState
import com.naptune.lullabyandstory.presentation.player.service.MusicController
import com.naptune.lullabyandstory.utils.InternetConnectionManager
import com.naptune.lullabyandstory.utils.LanguageManager
import com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Lullaby browse screen.
 * REFACTORED: Now follows Single Responsibility Principle (SRP).
 * Ad management logic extracted to unified AdManager.
 *
 * Responsibilities:
 * - Lullaby data fetching and management
 * - Download management
 * - Category filtering
 * - Music playback coordination
 *
 * Ad management delegated to: AdManager (shared across all ViewModels)
 */
@HiltViewModel
class LullabyViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fetchLullabiesUseCase: FetchLullabiesUseCase,
    private val downloaLullabyUsecase: DownloaLullabyUsecase,
    private val internetConnectionManager: InternetConnectionManager,
    private val musicController: MusicController,
    private val sessionUnlockManager: SessionUnlockManager,
    val languageManager: LanguageManager,
    private val analyticsHelper: AnalyticsHelper,
    private val billingManager: BillingManager,
    // ✅ SRP FIX: Single unified ad manager instead of 6 ad use cases
    private val adManager: AdManager
) : ViewModel() {

    private val _lullabyUiState = MutableStateFlow<LullabyUiState>(LullabyUiState.IsLoading)

    init {
        trackScreenView()
    }

    /**
     * Track screen view
     */
    private fun trackScreenView() {
        analyticsHelper.logScreenView(
            screenName = "Lullaby Browse",
            screenClass = "LullabyScreen"
        )
    }

    // ✅ MVI: Combine base state + session unlocks + billing status + ad state
    val lullabyUiState: StateFlow<LullabyUiState> = combine(
        _lullabyUiState,
        sessionUnlockManager.unlockedItems,
        billingManager.isPurchased,
        adManager.adState  // ✅ Get ad state from manager
    ) { baseState, unlockedIds, isPremium, adState ->
        Log.d("LullabyViewModel", "🔄 State combine - UnlockedIds: $unlockedIds, isPremium: $isPremium")

        if (baseState is LullabyUiState.Content) {
            val newState = baseState.copy(
                adUnlockedIds = unlockedIds,
                isPremium = isPremium,
                adState = adState  // ✅ Use ad state from manager
            )
            Log.d("LullabyViewModel", "✅ Updated state: ${unlockedIds.size} unlocked, premium=$isPremium")
            newState
        } else {
            Log.d("LullabyViewModel", "⚠️ Base state is not Content: ${baseState::class.simpleName}")
            baseState
        }
    }.onStart {
        // ✅ SRP FIX: Delegate ad initialization to manager
        adManager.initializeAds()
        adManager.loadBannerAd(
            adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
            adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER,
            placement = "lullaby_screen"
        )
        adManager.loadRewardedAd(AdMobDataSource.TEST_REWARDED_AD_UNIT_ID)

        fetchLullabyData()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LullabyUiState.IsLoading)

    // Expose network state for UI
    val isNetworkAvailable: StateFlow<Boolean> = internetConnectionManager.isNetworkAvailable

    // Expose currently playing lullaby ID from MusicController
    val currentlyPlayingLullabyId: StateFlow<String?> = combine(
        musicController.currentAudioItem,
        musicController.isPlaying
    ) { audioItem, isPlaying ->
        val result = if (audioItem?.isFromStory == false) {
            audioItem.documentId
        } else {
            null
        }
        Log.d(
            "LullabyViewModel",
            "🎵 Border state - Playing: $isPlaying, AudioItem: ${audioItem?.title}, IsStory: ${audioItem?.isFromStory}, Result: $result"
        )
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        Log.d("LullabyViewModel", "🚀 ViewModel initialized")

        // Monitor network for ad loading via manager
        viewModelScope.launch {
            var adsInitialized = false
            lullabyUiState.collect { state ->
                if (state is LullabyUiState.Content) {
                    Log.d("LullabyViewModel", "💳 State updated - isPremium: ${state.isPremium}")

                    if (!state.isPremium && !adsInitialized) {
                        Log.d("LullabyViewModel", "📢 Free user - Initializing ads via manager")
                        adManager.initializeAds()
                        monitorNetworkForAdLoading()
                        adsInitialized = true
                    } else if (state.isPremium) {
                        Log.d("LullabyViewModel", "🏆 Premium user - Skipping all ad initialization")
                        adsInitialized = true
                    }
                }
            }
        }
    }

    fun handleIntent(lullabyIntent: LullabyIntent) {
        when (lullabyIntent) {
            is LullabyIntent.FetchLullabies -> fetchLullabyData()
            is LullabyIntent.DownloadLullabyItem -> downloadLullaby(lullabyIntent.lullabyItem)
            is LullabyIntent.ChangeCategory -> changeCategory(lullabyIntent.category)

            // ✅ SRP FIX: Delegate ad intents to manager
            is LullabyIntent.InitializeAds -> adManager.initializeAds()
            is LullabyIntent.LoadBannerAd -> adManager.loadBannerAd(
                adUnitId = lullabyIntent.adUnitId,
                adSizeType = lullabyIntent.adSizeType,
                placement = "lullaby_screen"
            )
            is LullabyIntent.DestroyBannerAd -> adManager.destroyBannerAd(lullabyIntent.adUnitId)
            is LullabyIntent.LoadRewardedAd -> adManager.loadRewardedAd(lullabyIntent.adUnitId)
            is LullabyIntent.ShowRewardedAd -> adManager.showRewardedAd(
                adUnitId = lullabyIntent.adUnitId,
                activity = lullabyIntent.activity,
                content = ContentInfo.fromLullaby(lullabyIntent.lullaby),
                sourceScreen = "lullaby_screen"
            )
        }
    }

    private fun downloadLullaby(lullabyItem: LullabyDomainModel) {
        // CHECK: Internet connection before download
        if (!internetConnectionManager.checkNetworkAndShowToast()) {
            Log.d("LullabyViewModel", "❌ No internet connection - Download cancelled for: ${lullabyItem.documentId}")
            return
        }

        // CHECK: Prevent re-download if already downloading
        val currentState = _lullabyUiState.value as? LullabyUiState.Content
        if (currentState != null && lullabyItem.documentId in currentState.downloadingItems) {
            Log.d("LullabyViewModel", "⚠️ Download already in progress for: ${lullabyItem.documentId}")
            return
        }

        Log.e("LullabyViewModel", "📁 Starting download for: ${lullabyItem.musicName} (ID: ${lullabyItem.documentId})")

        // Analytics: Track download started
        val downloadStartTime = System.currentTimeMillis()
        analyticsHelper.logDownloadStarted(
            contentType = "lullaby",
            contentId = lullabyItem.documentId,
            contentName = lullabyItem.musicName,
            sourceScreen = "lullaby_browse"
        )

        viewModelScope.launch {
            try {
                downloaLullabyUsecase(lullabyItem).collect { result ->
                    Log.e("LullabyViewModel", "📊 Download result: $result")

                    val currentState = _lullabyUiState.value as? LullabyUiState.Content ?: run {
                        Log.w("LullabyViewModel", "Cannot update download state - UI not in Content state")
                        return@collect
                    }

                    val updatedState = when (result) {
                        is DownloadLullabyResult.Progress -> {
                            Log.e("LullabyViewModel", "📈 Progress: ${result.progressPercentige}% for ${lullabyItem.documentId}")
                            currentState.copy(
                                downloadingItems = currentState.downloadingItems + lullabyItem.documentId,
                                downloadProgress = currentState.downloadProgress + (lullabyItem.documentId to result.progressPercentige)
                            )
                        }

                        is DownloadLullabyResult.Completed -> {
                            Log.e("LullabyViewModel", "✅ Download completed for: ${lullabyItem.documentId}")

                            val downloadDuration = System.currentTimeMillis() - downloadStartTime
                            analyticsHelper.logDownloadCompleted(
                                contentType = "lullaby",
                                contentId = lullabyItem.documentId,
                                contentName = lullabyItem.musicName,
                                downloadTimeMs = downloadDuration
                            )

                            Toast.makeText(
                                context,
                                "✅ ${lullabyItem.musicName} downloaded successfully!",
                                Toast.LENGTH_SHORT
                            ).show()

                            currentState.copy(
                                downloadingItems = currentState.downloadingItems - lullabyItem.documentId,
                                downloadedItems = currentState.downloadedItems + lullabyItem.documentId,
                                downloadProgress = currentState.downloadProgress - lullabyItem.documentId
                            )
                        }

                        is DownloadLullabyResult.Error -> {
                            Log.e("LullabyViewModel", "❌ Download failed for ${lullabyItem.documentId}: ${result.message}")

                            analyticsHelper.logDownloadFailed(
                                contentType = "lullaby",
                                contentId = lullabyItem.documentId,
                                errorMessage = result.message
                            )

                            currentState.copy(
                                downloadingItems = currentState.downloadingItems - lullabyItem.documentId,
                                downloadProgress = currentState.downloadProgress - lullabyItem.documentId,
                                downloadError = currentState.downloadError + (lullabyItem.documentId to Throwable(result.message))
                            )
                        }
                    }

                    _lullabyUiState.value = updatedState
                    Log.e("LullabyViewModel", "🔄 State updated - Downloaded items: ${updatedState.downloadedItems}")
                }
            } catch (e: Exception) {
                Log.e("LullabyViewModel", "💥 Download coroutine failed: ${e.message}")
            }
        }
    }

    private fun fetchLullabyData() {
        Log.e("LullabyViewModel", "⚡ LullabyViewModel: Launching coroutine for data fetch...")

        viewModelScope.launch {
            Log.e("LullabyViewModel", "🌐 LullabyViewModel: Inside coroutine, calling UseCase...")

            fetchLullabiesUseCase()
                .collect { response ->
                    val currentCategory = (_lullabyUiState.value as? LullabyUiState.Content)?.currentCategory
                            ?: LullabyCategory.ALL

                    val currentState = _lullabyUiState.value as? LullabyUiState.Content

                    Log.d("LullabyViewModel", "🚀 Database-optimized lullabies received: ${response.size}")

                    // Pre-filter Popular and Free lists ONCE when data loads
                    val popularLullabies = response.filter { it.popularity_count > 0 }
                    val freeLullabies = response.filter { it.isFree }

                    Log.d("LullabyViewModel", "🚀 Pre-filtered lists created:")
                    Log.d("LullabyViewModel", "📊 Popular: ${popularLullabies.size} items")
                    Log.d("LullabyViewModel", "🆓 Free: ${freeLullabies.size} items")

                    val initialFilteredLullabies = when (currentCategory) {
                        LullabyCategory.ALL -> response
                        LullabyCategory.POPULAR -> popularLullabies
                        LullabyCategory.FREE -> freeLullabies
                    }

                    _lullabyUiState.value = LullabyUiState.Content(
                        lullabies = response,
                        filteredLullabies = initialFilteredLullabies,
                        popularLullabies = popularLullabies,
                        freeLullabies = freeLullabies,
                        currentCategory = currentCategory,
                        // ✅ SRP FIX: Ad state managed by AdManager
                        adState = currentState?.adState ?: AdUiState(),
                        // Backward compatibility fields
                        bannerAd = currentState?.bannerAd,
                        isAdInitialized = currentState?.isAdInitialized ?: false,
                        rewardedAd = currentState?.rewardedAd,
                        isLoadingRewardedAd = currentState?.isLoadingRewardedAd ?: false,
                        rewardedAdError = currentState?.rewardedAdError,
                        lastRewardedLullaby = currentState?.lastRewardedLullaby
                    )
                    Log.e("LullabyViewModel", "🎉 LullabyViewModel: SUCCESS! Data received!")
                    Log.e("LullabyViewModel", "📊 Response preview: ${response.take(100)}...")
                }
        }
    }

    fun retryFetch() {
        Log.e("LullabyViewModel", "🔄 LullabyViewModel: Manual retry triggered")
        fetchLullabyData()
    }

    private fun changeCategory(category: LullabyCategory) {
        Log.d("LullabyViewModel", "🔄 Changing category to: $category")

        val currentState = _lullabyUiState.value as? LullabyUiState.Content ?: run {
            Log.w("LullabyViewModel", "Cannot change category - UI not in Content state")
            return
        }

        // ULTRA FAST - Use pre-filtered lists
        val filteredLullabies = when (category) {
            LullabyCategory.ALL -> {
                Log.d("LullabyViewModel", "✅ ALL category - showing all ${currentState.lullabies.size} lullabies")
                currentState.lullabies
            }
            LullabyCategory.POPULAR -> {
                Log.d("LullabyViewModel", "📊 POPULAR category - showing ${currentState.popularLullabies.size} popular lullabies")
                currentState.popularLullabies
            }
            LullabyCategory.FREE -> {
                Log.d("LullabyViewModel", "🆓 FREE category - showing ${currentState.freeLullabies.size} free lullabies")
                currentState.freeLullabies
            }
        }

        _lullabyUiState.value = currentState.copy(
            currentCategory = category,
            filteredLullabies = filteredLullabies
        )
    }

    /**
     * Check if rewarded ad is available
     * ✅ SRP FIX: Delegate to manager
     */
    fun isRewardedAdAvailable(adUnitId: String = AdMobDataSource.TEST_REWARDED_AD_UNIT_ID): Boolean {
        return adManager.isRewardedAdAvailable(adUnitId)
    }

    /**
     * Monitor network changes and automatically handle banner ad loading
     * ✅ SRP FIX: Simplified - manager handles the details
     */
    private fun monitorNetworkForAdLoading() {
        viewModelScope.launch {
            internetConnectionManager.isNetworkAvailable.collect { isConnected ->
                Log.d("LullabyViewModel", "🌐 Network state changed: $isConnected")

                if (isConnected) {
                    // Network available - manager will handle loading
                    adManager.loadBannerAd(
                        adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                        adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER,
                        placement = "lullaby_screen"
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("LullabyViewModel", "🧹 ViewModel cleared - Ad manager handles cleanup")
    }

    // ═══════════════════════════════════════════════════════════
    // 📊 ANALYTICS TRACKING
    // ═══════════════════════════════════════════════════════════

    /**
     * Track lullaby selection from Lullaby browse screen
     */
    fun trackLullabySelected(lullaby: LullabyDomainModel) {
        try {
            val isDownloaded = lullaby.isDownloaded ?: false

            analyticsHelper.logLullabySelected(
                lullabyId = lullaby.documentId,
                lullabyName = lullaby.musicName,
                category = "lullaby",
                sourceScreen = "lullaby_browse",
                isPremium = !lullaby.isFree,
                isDownloaded = isDownloaded
            )

            if (isDownloaded) {
                analyticsHelper.logDownloadedContentPlayed(
                    contentType = "lullaby",
                    contentId = lullaby.documentId,
                    contentName = lullaby.musicName,
                    sourceScreen = "lullaby_browse"
                )
            }

            Log.d("LullabyViewModel", "📊 Tracked lullaby selection: ${lullaby.musicName} (Downloaded: $isDownloaded)")
        } catch (e: Exception) {
            Log.e("LullabyViewModel", "❌ Analytics error: ${e.message}")
        }
    }
}
