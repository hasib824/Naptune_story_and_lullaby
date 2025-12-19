package com.naptune.lullabyandstory.presentation.lullaby

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.domain.data.DownloadLullabyResult
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.presentation.main.AdUiState
import com.naptune.lullabyandstory.domain.usecase.lullaby.DownloaLullabyUsecase
import com.naptune.lullabyandstory.domain.usecase.lullaby.FetchLullabiesUseCase
import com.naptune.lullabyandstory.presentation.player.service.MusicController
import com.naptune.lullabyandstory.domain.usecase.admob.InitializeAdMobUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.LoadBannerAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.DestroyBannerAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.LoadRewardedAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.ShowRewardedAdUseCase
import com.naptune.lullabyandstory.domain.usecase.admob.CheckRewardedAdAvailabilityUseCase
import com.naptune.lullabyandstory.domain.model.AdLoadResult
import com.naptune.lullabyandstory.domain.model.RewardedAdLoadResult
import com.naptune.lullabyandstory.domain.model.RewardedAdShowResult
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.utils.InternetConnectionManager
import com.naptune.lullabyandstory.utils.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
class LullabyViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fetchLullabiesUseCase: FetchLullabiesUseCase,
    private val downloaLullabyUsecase: DownloaLullabyUsecase,
    private val internetConnectionManager: InternetConnectionManager,
    // ✅ Inject MusicController to track playing state
    private val musicController: MusicController,
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
    val languageManager: LanguageManager,
    // ✅ Analytics
    private val analyticsHelper: com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper,
    // ✅ Billing - Premium status and purchase management
    private val billingManager: com.naptune.lullabyandstory.data.billing.BillingManager
) : ViewModel() {

    private val _lullabyUiState = MutableStateFlow<LullabyUiState>(LullabyUiState.IsLoading)

    init {
        // ✅ Track screen view
        trackScreenView()
    }

    /**
     * ✅ Track screen view
     */
    private fun trackScreenView() {
        analyticsHelper.logScreenView(
            screenName = "Lullaby Browse",
            screenClass = "LullabyScreen"
        )
    }

    // ✅ MVI FIX: Combine base state + session unlocks + billing status into single state
    val lullabyUiState: StateFlow<LullabyUiState> = combine(
        _lullabyUiState,
        sessionUnlockManager.unlockedItems,
        billingManager.isPurchased  // ✅ Add premium status to state
    ) { baseState, unlockedIds, isPremium ->
        // ✅ Debug: Log state combination
        Log.d("LullabyViewModel", "🔄 State combine - UnlockedIds: $unlockedIds, isPremium: $isPremium")

        // ✅ If base state is Content, merge with unlocked IDs AND premium status
        if (baseState is LullabyUiState.Content) {
            val newState = baseState.copy(
                adUnlockedIds = unlockedIds,
                isPremium = isPremium  // ✅ MVI: Single source of truth
            )
            Log.d("LullabyViewModel", "✅ Updated state: ${unlockedIds.size} unlocked, premium=$isPremium")
            newState
        } else {
            Log.d("LullabyViewModel", "⚠️ Base state is not Content: ${baseState::class.simpleName}")
            baseState
        }
    }.onStart {
        initializeAds()

        loadBannerAd(
            adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
            adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
        )

        // Preload rewarded ad for lullaby unlock
        loadRewardedAd(AdMobDataSource.TEST_REWARDED_AD_UNIT_ID)

        fetchLullabyData()

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LullabyUiState.IsLoading)



    // ✅ NEW: Expose network state for UI like MainViewModel
    val isNetworkAvailable: StateFlow<Boolean> = internetConnectionManager.isNetworkAvailable

    // ✅ Expose currently playing lullaby ID from MusicController
    val currentlyPlayingLullabyId: StateFlow<String?> = combine(
        musicController.currentAudioItem,
        musicController.isPlaying
    ) { audioItem, isPlaying ->
        // ✅ UPDATED: Show border if audio is loaded (regardless of playing/paused state)
        // Border will disappear only when audio is stopped (currentAudioItem = null)
        val result = if (audioItem?.isFromStory == false) {
            audioItem.documentId // ✅ Show border for lullabies (playing OR paused)
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

    // ❌ REMOVED: Separate isPurchased StateFlow - now part of lullabyUiState
    // This follows pure MVI pattern: single source of truth

    init {
        Log.d("LullabyViewModel", "🚀 ViewModel initialized")

        // ✅ MVI FIX: Initialize ads based on combined state (wait for billing + content)
        viewModelScope.launch {
            var adsInitialized = false  // ✅ FIX: Prevent infinite ad loading loop
            lullabyUiState.collect { state ->
                if (state is LullabyUiState.Content) {
                    Log.d("LullabyViewModel", "💳 State updated - isPremium: ${state.isPremium}")

                    if (!state.isPremium) {
                        // ✅ Initialize AdMob SDK once
                        if (!adsInitialized) {
                            Log.d("LullabyViewModel", "📢 Free user - Initializing ads")
                            initializeAds()
                            // Start monitoring network for ad loading
                            monitorNetworkForAdLoading()
                            adsInitialized = true  // ✅ Mark as initialized
                        }

                        // ✅ Reload banner ad if missing (handles back navigation)
                        val bannerAd = state.adState.bannerAd
                        if (internetConnectionManager.isCurrentlyConnected() &&
                            (bannerAd == null || (!bannerAd.isLoaded && !bannerAd.isLoading))) {
                            Log.d("LullabyViewModel", "🔄 Banner ad missing - Reloading")
                            loadBannerAd(
                                adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                                adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
                            )
                        }
                    } else {
                        Log.d("LullabyViewModel", "🏆 Premium user - Skipping all ad initialization")
                        adsInitialized = true  // ✅ Mark as initialized even for premium
                    }
                }
            }
        }
    }

    fun handleIntent(lullabyIntent: LullabyIntent) {
        when (lullabyIntent) {
            is LullabyIntent.FetchLullabies -> fetchLullabyData()
            is LullabyIntent.DownloadLullabyItem -> downloadLullaby(
                lullabyIntent.lullabyItem
            )

            is LullabyIntent.ChangeCategory -> changeCategory(lullabyIntent.category)
            // AdMob intents
            is LullabyIntent.InitializeAds -> initializeAds()
            is LullabyIntent.LoadBannerAd -> loadBannerAd(
                lullabyIntent.adUnitId,
                lullabyIntent.adSizeType
            )

            is LullabyIntent.DestroyBannerAd -> destroyBannerAd(lullabyIntent.adUnitId)
            // Rewarded Ad intents  
            is LullabyIntent.LoadRewardedAd -> loadRewardedAd(lullabyIntent.adUnitId)
            is LullabyIntent.ShowRewardedAd -> showRewardedAd(
                lullabyIntent.adUnitId, 
                lullabyIntent.activity,
                lullabyIntent.lullaby
            )
        }
    }

    private fun downloadLullaby(
        lullabyItem: LullabyDomainModel
    ) {
        // ✅ CHECK: Internet connection before download
        if (!internetConnectionManager.checkNetworkAndShowToast()) {
            Log.d("LullabyViewModel", "❌ No internet connection - Download cancelled for: ${lullabyItem.documentId}")
            return
        }
        
        // ✅ CHECK: Prevent re-download if already downloading
        val currentState = _lullabyUiState.value as? LullabyUiState.Content
        if (currentState != null && lullabyItem.documentId in currentState.downloadingItems) {
            Log.d("LullabyViewModel", "⚠️ Download already in progress for: ${lullabyItem.documentId}")
            return // Don't start duplicate download
        }
        
        Log.e(
            "LullabyViewModel",
            "📁 Starting download for: ${lullabyItem.musicName} (ID: ${lullabyItem.documentId})"
        )

        // ✅ Analytics: Track download started
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

                    // ✅ Safe state update with proper copying
                    val currentState = _lullabyUiState.value as? LullabyUiState.Content ?: run {
                        Log.w(
                            "LullabyViewModel",
                            "Cannot update download state - UI not in Content state"
                        )
                        return@collect
                    }

                    val updatedState = when (result) {
                        is DownloadLullabyResult.Progress -> {
                            Log.e(
                                "LullabyViewModel",
                                "📈 Progress: ${result.progressPercentige}% for ${lullabyItem.documentId}"
                            )
                            currentState.copy(
                                downloadingItems = currentState.downloadingItems + lullabyItem.documentId,  // ✅ Create new set
                                downloadProgress = currentState.downloadProgress + (lullabyItem.documentId to result.progressPercentige)  // ✅ Create new map
                            )
                        }

                        is DownloadLullabyResult.Completed -> {
                            Log.e("LullabyViewModel", "✅ Download completed for: ${lullabyItem.documentId}")

                            // ✅ Analytics: Track download completed
                            val downloadDuration = System.currentTimeMillis() - downloadStartTime
                            analyticsHelper.logDownloadCompleted(
                                contentType = "lullaby",
                                contentId = lullabyItem.documentId,
                                contentName = lullabyItem.musicName,
                                downloadTimeMs = downloadDuration
                            )

                            // ✅ Show download completion toast
                            Toast.makeText(
                                context,
                                "✅ ${lullabyItem.musicName} downloaded successfully!",
                                Toast.LENGTH_SHORT
                            ).show()

                            currentState.copy(
                                downloadingItems = currentState.downloadingItems - lullabyItem.documentId,  // ✅ Remove from downloading
                                downloadedItems = currentState.downloadedItems + lullabyItem.documentId,    // ✅ Add to downloaded
                                downloadProgress = currentState.downloadProgress - lullabyItem.documentId   // ✅ Remove progress
                            )
                        }

                        is DownloadLullabyResult.Error -> {
                            Log.e(
                                "LullabyViewModel",
                                "❌ Download failed for ${lullabyItem.documentId}: ${result.message}"
                            )

                            // ✅ Analytics: Track download failed
                            analyticsHelper.logDownloadFailed(
                                contentType = "lullaby",
                                contentId = lullabyItem.documentId,
                                errorMessage = result.message
                            )

                            currentState.copy(
                                downloadingItems = currentState.downloadingItems - lullabyItem.documentId,  // ✅ Remove from downloading
                                downloadProgress = currentState.downloadProgress - lullabyItem.documentId,  // ✅ Remove progress
                                downloadError = currentState.downloadError + (lullabyItem.documentId to Throwable(
                                    result.message
                                ))  // ✅ Add error
                            )
                        }
                    }

                    _lullabyUiState.value = updatedState
                    Log.e(
                        "LullabyViewModel",
                        "🔄 State updated - Downloaded items: ${updatedState.downloadedItems}"
                    )
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
                    // ✅ Preserve current category when updating lullabies
                    val currentCategory =
                        (_lullabyUiState.value as? LullabyUiState.Content)?.currentCategory
                            ?: LullabyCategory.ALL

                    // ✅ Preserve current ad state when updating lullabies
                    val currentState = _lullabyUiState.value as? LullabyUiState.Content

                    // ✅ ULTRA OPTIMIZED: No translation processing needed - database handles it!
                    Log.d("LullabyViewModel", "🚀 Database-optimized lullabies received: ${response.size}")

                    // ✅ Pre-filter Popular and Free lists ONCE when data loads
                    val popularLullabies = response.filter { it.popularity_count > 0 }
                    val freeLullabies = response.filter { it.isFree }

                    Log.d("LullabyViewModel", "🚀 Pre-filtered lists created:")
                    Log.d("LullabyViewModel", "📊 Popular: ${popularLullabies.size} items")
                    Log.d("LullabyViewModel", "🆓 Free: ${freeLullabies.size} items")

                    // ✅ Set initial filteredLullabies based on current category
                    val initialFilteredLullabies = when (currentCategory) {
                        LullabyCategory.ALL -> response
                        LullabyCategory.POPULAR -> popularLullabies
                        LullabyCategory.FREE -> freeLullabies
                    }

                    _lullabyUiState.value = LullabyUiState.Content(
                        lullabies = response, // ✅ Direct use - already database-optimized
                        filteredLullabies = initialFilteredLullabies,
                        popularLullabies = popularLullabies,
                        freeLullabies = freeLullabies,
                        currentCategory = currentCategory,
                        // ✅ FIXED: Preserve AdState properly to prevent ad reload
                        adState = currentState?.adState ?: AdUiState(),
                        // ✅ DEPRECATED: Keep for backward compatibility
                        bannerAd = currentState?.bannerAd,
                        isAdInitialized = currentState?.isAdInitialized ?: false,
                        rewardedAd = currentState?.rewardedAd,
                        isLoadingRewardedAd = currentState?.isLoadingRewardedAd ?: false,
                        rewardedAdError = currentState?.rewardedAdError,
                        lastRewardedLullaby = currentState?.lastRewardedLullaby
                    )
                    Log.e("LullabyViewModel", "🎉 LullabyViewModel: SUCCESS! Data received!")
                    Log.e("LullabyViewModel", "📊 Response preview: ${response.take(100)}...")
                    Log.e(
                        "LullabyViewModel",
                        "✅ All layers working correctly - check detailed logs above!"
                    )
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

        // ✅ ULTRA FAST - Use pre-filtered lists (no filtering needed!)
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

        // ✅ Instant UI update - just reference switching!
        _lullabyUiState.value = currentState.copy(
            currentCategory = category,
            filteredLullabies = filteredLullabies
        )
    }

    // AdMob related methods
    private fun initializeAds() {
        Log.d("LullabyViewModel", "🚀 Initializing AdMob SDK...")
        viewModelScope.launch {
            try {
                initializeAdMobUseCase()

                val currentState = _lullabyUiState.value as? LullabyUiState.Content
                if (currentState != null) {
                    _lullabyUiState.value = currentState.copy(
                        adState = currentState.adState.copy(isAdInitialized = true)
                    )
                    Log.d("LullabyViewModel", "✅ AdMob initialized successfully")
                }
            } catch (e: Exception) {
                Log.e("LullabyViewModel", "❌ AdMob initialization failed: ${e.message}", e)
            }
        }
    }

    private fun loadBannerAd(
        adUnitId: String,
        adSizeType: com.naptune.lullabyandstory.domain.model.AdSizeType
    ) {
        // 🏆 Skip ad loading for premium users
        // ✅ MVI FIX: Get premium status from current state
        val currentState = lullabyUiState.value
        if (currentState is LullabyUiState.Content && currentState.isPremium) {
            Log.d("LullabyViewModel", "🏆 Premium user - Skipping banner ad load")
            return
        }

        Log.d("LullabyViewModel", "📢 Loading banner ad - Unit: $adUnitId, Size: $adSizeType")
        viewModelScope.launch {
            try {
                loadBannerAdUseCase(adUnitId, adSizeType).collect { result ->
                    val currentState = _lullabyUiState.value as? LullabyUiState.Content
                    if (currentState != null) {
                        when (result) {
                            is AdLoadResult.Loading -> {
                                Log.d("LullabyViewModel", "⏳ Banner ad loading...")
                                val defaultHeight = when (adSizeType) {
                                    com.naptune.lullabyandstory.domain.model.AdSizeType.ANCHORED_ADAPTIVE_BANNER -> 90 // Adaptive height
                                    com.naptune.lullabyandstory.domain.model.AdSizeType.LARGE_BANNER -> 100
                                    else -> 50
                                }

                                val loadingAd =
                                    currentState.adState.bannerAd?.copy(isLoading = true, error = null)
                                        ?: com.naptune.lullabyandstory.domain.model.BannerAdDomainModel(
                                            adUnitId = adUnitId,
                                            adSize = com.naptune.lullabyandstory.domain.model.AdSize(
                                                width = -1, // Full width for adaptive
                                                height = defaultHeight,
                                                type = adSizeType
                                            ),
                                            isLoading = true
                                        )
                                _lullabyUiState.value = currentState.copy(
                                    adState = currentState.adState.copy(bannerAd = loadingAd)
                                )
                            }

                            is AdLoadResult.Success -> {
                                Log.d("LullabyViewModel", "✅ Banner ad loaded successfully")
                                _lullabyUiState.value = currentState.copy(
                                    adState = currentState.adState.copy(bannerAd = result.bannerAd)
                                )
                            }

                            is AdLoadResult.Error -> {
                                Log.e(
                                    "LullabyViewModel",
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
                                        width = -1, // Full width for adaptive
                                        height = defaultHeight,
                                        type = adSizeType
                                    ),
                                    isLoading = false,
                                    isLoaded = false,
                                    error = result.message
                                )
                                _lullabyUiState.value = currentState.copy(
                                    adState = currentState.adState.copy(bannerAd = errorAd)
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LullabyViewModel", "💥 Banner ad loading exception: ${e.message}", e)
            }
        }
    }

    private fun destroyBannerAd(adUnitId: String) {
        Log.d("LullabyViewModel", "🗑️ Destroying banner ad - Unit: $adUnitId")
        viewModelScope.launch {
            try {
                destroyBannerAdUseCase(adUnitId)

                val currentState = _lullabyUiState.value as? LullabyUiState.Content
                if (currentState != null) {
                    _lullabyUiState.value = currentState.copy(
                        adState = currentState.adState.copy(bannerAd = null)
                    )
                    Log.d("LullabyViewModel", "✅ Banner ad destroyed successfully")
                }
            } catch (e: Exception) {
                Log.e("LullabyViewModel", "❌ Banner ad destruction failed: ${e.message}", e)
            }
        }
    }

    // Rewarded Ad functionality
    private fun loadRewardedAd(adUnitId: String) {
        // 🏆 Skip ad loading for premium users
        // ✅ MVI FIX: Get premium status from current state
        val currentState = lullabyUiState.value
        if (currentState is LullabyUiState.Content && currentState.isPremium) {
            Log.d("LullabyViewModel", "🏆 Premium user - Skipping rewarded ad load")
            return
        }

        Log.d("LullabyViewModel", "🎁 Loading rewarded ad: $adUnitId")
        viewModelScope.launch {
            try {
                loadRewardedAdUseCase(adUnitId).collect { result ->
                    val currentState = _lullabyUiState.value as? LullabyUiState.Content
                    if (currentState != null) {
                        when (result) {
                            is RewardedAdLoadResult.Loading -> {
                                Log.d("LullabyViewModel", "⏳ Rewarded ad loading...")
                                _lullabyUiState.value = currentState.copy(
                                    isLoadingRewardedAd = true,
                                    rewardedAdError = null
                                )
                            }
                            
                            is RewardedAdLoadResult.Success -> {
                                Log.d("LullabyViewModel", "✅ Rewarded ad loaded successfully")
                                _lullabyUiState.value = currentState.copy(
                                    rewardedAd = result.rewardedAd,
                                    isLoadingRewardedAd = false,
                                    rewardedAdError = null
                                )
                            }
                            
                            is RewardedAdLoadResult.Error -> {
                                Log.e("LullabyViewModel", "❌ Rewarded ad load failed: ${result.message}")
                                _lullabyUiState.value = currentState.copy(
                                    isLoadingRewardedAd = false,
                                    rewardedAdError = result.message
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LullabyViewModel", "💥 Exception loading rewarded ad: ${e.message}", e)
                val currentState = _lullabyUiState.value as? LullabyUiState.Content
                if (currentState != null) {
                    _lullabyUiState.value = currentState.copy(
                        isLoadingRewardedAd = false,
                        rewardedAdError = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    private fun showRewardedAd(adUnitId: String, activity: android.app.Activity, lullaby: LullabyDomainModel) {
        Log.d("LullabyViewModel", "🎬 Showing rewarded ad: $adUnitId for lullaby: ${lullaby.musicName}")

        // ✅ Check if ad is available
        if (!checkRewardedAdAvailabilityUseCase(adUnitId)) {
            Log.w("LullabyViewModel", "❌ Rewarded ad not available, loading new ad...")
            loadRewardedAd(adUnitId)
            Toast.makeText(activity, "Loading ad, please try again in a moment", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModelScope.launch {
            try {
                showRewardedAdUseCase(adUnitId, activity).collect { result ->
                    val currentState = _lullabyUiState.value as? LullabyUiState.Content
                    if (currentState != null) {
                        when (result) {
                            is RewardedAdShowResult.Loading -> {
                                Log.d("LullabyViewModel", "🎬 Showing rewarded ad...")
                                _lullabyUiState.value = currentState.copy(
                                    lastRewardedLullaby = lullaby
                                )
                            }
                            
                            is RewardedAdShowResult.Success -> {
                                Log.d("LullabyViewModel", "🎁 User earned reward: ${result.reward}")
                                _lullabyUiState.value = currentState.copy(
                                    lastRewardedLullaby = lullaby,
                                    rewardedAd = null // Ad consumed, need to reload
                                )
                                
                                // TODO: Handle reward logic here (unlock lullaby, grant premium access, etc.)
                                handleRewardEarned(lullaby, result.reward)
                                
                                // Preload next rewarded ad
                                loadRewardedAd(adUnitId)
                            }
                            
                            is RewardedAdShowResult.Dismissed -> {
                                Log.d("LullabyViewModel", "🚪 Rewarded ad dismissed: ${result.reason}")
                                _lullabyUiState.value = currentState.copy(
                                    rewardedAd = null // Ad consumed, need to reload
                                )
                                
                                // Preload next rewarded ad
                                loadRewardedAd(adUnitId)
                            }
                            
                            is RewardedAdShowResult.Error -> {
                                Log.e("LullabyViewModel", "❌ Rewarded ad show failed: ${result.message}")
                                _lullabyUiState.value = currentState.copy(
                                    rewardedAdError = result.message
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LullabyViewModel", "💥 Exception showing rewarded ad: ${e.message}", e)
                val currentState = _lullabyUiState.value as? LullabyUiState.Content
                if (currentState != null) {
                    _lullabyUiState.value = currentState.copy(
                        rewardedAdError = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    private fun handleRewardEarned(lullaby: LullabyDomainModel, reward: com.naptune.lullabyandstory.domain.model.RewardDomainModel) {
        Log.d("LullabyViewModel", "🎉 Processing reward for lullaby: ${lullaby.musicName}")
        Log.d("LullabyViewModel", "🎁 Reward: ${reward.type} - ${reward.amount}")
        Log.d("LullabyViewModel", "📋 Lullaby documentId: ${lullaby.documentId}")

        // ✅ NEW: Unlock lullaby for current session via SessionUnlockManager
        sessionUnlockManager.unlockItem(
            itemId = lullaby.documentId,
            itemType = com.naptune.lullabyandstory.data.manager.UnlockType.Lullaby
        )

        // ✅ Debug: Verify unlock was successful
        val isNowUnlocked = sessionUnlockManager.isItemUnlocked(lullaby.documentId)
        val totalUnlocked = sessionUnlockManager.getUnlockCount()
        Log.d("LullabyViewModel", "✅ Lullaby unlocked for session: ${lullaby.musicName}")
        Log.d("LullabyViewModel", "🔓 Unlock verified: $isNowUnlocked")
        Log.d("LullabyViewModel", "📊 Total unlocked items: $totalUnlocked")
        Log.d("LullabyViewModel", "📜 All unlocked IDs: ${sessionUnlockManager.getUnlockedItems()}")

        // Show success toast
        Toast.makeText(
            context,
            "🎁 ${lullaby.musicName} unlocked for this session!",
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
                Log.d("LullabyViewModel", "🌐 Network state changed: $isConnected")
                
                // ✅ Handle banner ad loading based on network state
                handleBannerAdNetworkState(isConnected)
            }
        }
    }

    /**
     * ✅ Handle banner ad loading/hiding based on network connectivity
     */
    private fun handleBannerAdNetworkState(isConnected: Boolean) {
        Log.d("LullabyViewModel", "📢 Handling banner ad network state: $isConnected")
        
        val currentState = _lullabyUiState.value as? LullabyUiState.Content
        if (currentState != null) {
            if (isConnected) {
                // ✅ Network available - check if ad needs to be loaded
                val bannerAd = currentState.adState.bannerAd
                if (bannerAd == null || (!bannerAd.isLoaded && !bannerAd.isLoading)) {
                    Log.d("LullabyViewModel", "🚀 Network available - Starting banner ad load")
                    loadBannerAd(
                        adUnitId = AdMobDataSource.TEST_BANNER_AD_UNIT_ID,
                        adSizeType = AdSizeType.ANCHORED_ADAPTIVE_BANNER
                    )
                } else {
                    Log.d("LullabyViewModel", "✅ Banner ad already loaded or loading")
                }
            } else {
                // ✅ Network not available - clear ad state
                Log.d("LullabyViewModel", "❌ Network not available - Clearing banner ad state")
                _lullabyUiState.value = currentState.copy(
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
        Log.d("LullabyViewModel", "🧹 ViewModel cleared - Banner ad preserved (shared with MainScreen)")
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

            // ✅ Analytics: Track offline content usage
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
