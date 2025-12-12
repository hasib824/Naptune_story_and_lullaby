# Naptune - Lullaby and Story App
## Complete Technical Documentation

---

## Project Overview

**Naptune** is a Lullaby and Bedtime Story application for children built using modern Android development practices. The app provides lullabies and bedtime stories with audio playback capabilities, multi-language support (7 languages), and premium subscription features.

| Property | Value |
|----------|-------|
| **Package** | `com.naptune.lullabyandstory` |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 35 (Android 15) |
| **Language** | Kotlin 100% |
| **UI Framework** | Jetpack Compose |
| **Architecture** | Clean Architecture + MVI |

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Project Structure](#2-project-structure)
3. [Data Layer](#3-data-layer)
4. [Domain Layer](#4-domain-layer)
5. [Presentation Layer](#5-presentation-layer)
6. [Dependency Injection](#6-dependency-injection)
7. [Backend Integration (Appwrite)](#7-backend-integration-appwrite)
8. [Push Notifications (FCM)](#8-push-notifications-fcm)
9. [Audio System (Media3)](#9-audio-system-media3)
10. [Monetization](#10-monetization)
11. [Multi-Language Support](#11-multi-language-support)
12. [Navigation](#12-navigation)
13. [Key Features](#13-key-features)
14. [Build Configuration](#14-build-configuration)
15. [Dependencies](#15-dependencies)

---

## 1. Architecture Overview

The project follows **Clean Architecture** with **MVI (Model-View-Intent)** pattern for the presentation layer.

```
+---------------------------------------------------------------------+
|                       PRESENTATION LAYER                             |
|  +-----------+   +-----------+   +------------+   +-------------+   |
|  |  Screen   | > |  Intent   | > |  ViewModel | > |   UiState   |   |
|  | (Compose) |   | (Actions) |   |   (MVI)    |   |   (State)   |   |
|  +-----------+   +-----------+   +------------+   +-------------+   |
+---------------------------------------------------------------------+
                                   |
                                   v
+---------------------------------------------------------------------+
|                         DOMAIN LAYER                                 |
|  +----------------+   +-----------------+   +-------------------+   |
|  |    UseCases    |   |   Repositories  |   |   Domain Models   |   |
|  | (Business Logic)|  |  (Interfaces)   |   |    (Entities)     |   |
|  +----------------+   +-----------------+   +-------------------+   |
+---------------------------------------------------------------------+
                                   |
                                   v
+---------------------------------------------------------------------+
|                          DATA LAYER                                  |
|  +-------------+  +-------------+  +---------------------------+    |
|  |    Room     |  |  DataStore  |  |        Appwrite           |    |
|  |  Database   |  |(Preferences)|  |    (Remote Backend)       |    |
|  +-------------+  +-------------+  +---------------------------+    |
|  +-------------+  +-------------+  +---------------------------+    |
|  |  Retrofit   |  |   Mappers   |  |      Data Sources         |    |
|  |(FCM Server) |  |             |  |    (Local/Remote)         |    |
|  +-------------+  +-------------+  +---------------------------+    |
+---------------------------------------------------------------------+
```

### Data Flow
```
User Action -> Intent -> ViewModel -> UseCase -> Repository -> DataSource -> Database/API
                                                      |
                    UI <- UiState <- ViewModel <- Flow<Data>
```

---

## 2. Project Structure

```
app/src/main/java/com/naptune/lullabyandstory/
|
+-- data/                              # DATA LAYER
|   +-- billing/
|   |   +-- BillingManager.kt          # Google Play Billing
|   |
|   +-- datastore/
|   |   +-- AppPreferences.kt          # Main preferences
|   |   +-- FcmPreferences.kt          # FCM token storage
|   |   +-- TimerPreferences.kt        # Sleep timer settings
|   |
|   +-- fcm/
|   |   +-- NaptuneMessagingService.kt # FCM handler
|   |   +-- TokenUploadWorker.kt       # Background token upload
|   |
|   +-- local/
|   |   +-- dao/                       # Room DAOs
|   |   +-- database/AppDatabase.kt    # Room database (version 6)
|   |   +-- entity/                    # Database entities
|   |   +-- LocalDataSource.kt         # Local data operations
|   |
|   +-- manager/
|   |   +-- SessionUnlockManager.kt    # Rewarded ad unlock tracking
|   |
|   +-- mapper/                        # Data <-> Domain mappers
|   +-- model/                         # Remote data models
|   |
|   +-- network/
|   |   +-- admob/                     # AdMob data sources
|   |   +-- appwrite/                  # Appwrite data sources
|   |   +-- fcm/                       # FCM API (Retrofit)
|   |   +-- prdownloader/              # Audio file downloader
|   |
|   +-- repository/                    # Repository implementations
|
+-- di/                                # DEPENDENCY INJECTION (Hilt)
|   +-- AppModule.kt
|   +-- DatabaseModule.kt
|   +-- RepositoryModule.kt
|   +-- AdMobModule.kt
|   +-- FcmModule.kt
|
+-- domain/                            # DOMAIN LAYER
|   +-- data/                          # Domain data classes
|   +-- manager/LanguageStateManager.kt
|   +-- model/                         # Domain models
|   +-- repository/                    # Repository interfaces
|   +-- usecase/                       # Business logic use cases
|       +-- admob/
|       +-- fcm/
|       +-- lullaby/
|       +-- story/
|       +-- translation/
|
+-- presentation/                      # PRESENTATION LAYER (MVI)
|   +-- components/                    # Reusable Compose components
|   |   +-- admob/
|   |   +-- common/
|   |   +-- language/
|   |   +-- lullaby/
|   |   +-- shimmer/
|   |   +-- story/
|   |
|   +-- explore/                       # Explore screen
|   +-- favourite/                     # Favourite screen
|   +-- fcm/                           # FCM management
|   +-- language/                      # Language selection
|   +-- lullaby/                       # Lullaby browse screen
|   +-- main/                          # Main/Home screen
|   +-- navigation/                    # Navigation setup
|   +-- player/                        # Audio player
|   |   +-- bottomsheet/               # Global audio player
|   |   +-- service/                   # MediaSessionService
|   |   +-- timermodal/                # Sleep timer
|   +-- premium/                       # Premium screen
|   +-- profile/                       # Profile screen
|   +-- settings/
|   +-- splash/
|   +-- story/
|   |   +-- storymanager/
|   |   +-- storyreader/
|   +-- sleepsounds/
|
+-- utils/
|   +-- LanguageManager.kt
|   +-- LocaleHelper.kt
|   +-- fcm/NotificationHelper.kt
|   +-- analytics/AnalyticsHelper.kt
|
+-- MainActivity.kt
+-- NaptuneApplication.kt              # @HiltAndroidApp
```

---

## 3. Data Layer

### 3.1 Room Database

**Database:** `lullaby_database` (Version 6)

#### Entities

```kotlin
// LullabyLocalEntity
@Entity(tableName = "lullabies")
data class LullabyLocalEntity(
    @PrimaryKey val documentId: String,
    val id: String,
    val musicName: String,
    val musicPath: String,
    val musicLocalPath: String?,
    val musicSize: Long,
    val imagePath: String,
    val musicLength: String,
    val isDownloaded: Boolean,
    val isFavourite: Boolean,
    val popularity_count: Int,
    val isFree: Boolean
)

// StoryLocalEntity
@Entity(tableName = "stories")
data class StoryLocalEntity(
    @PrimaryKey val documentId: String,
    val id: String,
    val storyName: String,
    val storyDescription: String,
    val imagePath: String,
    val storyAudioPath: String,
    val story_reading_time: String,
    val story_listen_time_in_millis: Long,
    val isFavourite: Boolean,
    val popularity_count: Int,
    val isFree: Boolean
)

// TranslationLocalEntity (Lullaby translations - 7 languages)
@Entity(tableName = "translations")
data class TranslationLocalEntity(
    @PrimaryKey val documentId: String,
    val lullabyId: String,
    val lullabyDocumentId: String,
    val en: String?,  // English
    val es: String?,  // Spanish
    val fr: String?,  // French
    val de: String?,  // German
    val pt: String?,  // Portuguese
    val hi: String?,  // Hindi
    val ar: String?   // Arabic
)

// FavouriteMetadataEntity (LIFO ordering)
@Entity(tableName = "favourite_metadata")
data class FavouriteMetadataEntity(
    @PrimaryKey val itemId: String,
    val itemType: String,
    val favouritedAt: Long  // Timestamp for LIFO ordering
)
```

### 3.2 DataStore Preferences

```kotlin
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val IS_CUSTOM_LANGUAGE_SELECTED = booleanPreferencesKey("is_custom_language_selected")
        val STORY_FONT_SIZE = floatPreferencesKey("story_font_size")
        val LAST_SYNC_TIME_LULLABY = longPreferencesKey("last_sync_time_lullaby")
        val LAST_SYNC_TIME_STORY = longPreferencesKey("last_sync_time_story")
    }

    // Sync interval: 24 hours
    suspend fun isSyncNeeded(isFromStory: Boolean): Boolean
    suspend fun saveLanguage(languageCode: String)
    suspend fun getLanguage(): String
    fun getLanguageSync(): String  // Synchronous for fast startup
}
```

### 3.3 Repository Pattern

```kotlin
// Repository Interface (Domain Layer)
interface LullabyRepository {
    suspend fun syncLullabiesFromRemote(): Flow<List<LullabyDomainModel>>
    suspend fun downloadLullaby(lullabyItem: LullabyDomainModel): Flow<DownloadLullabyResult>
    fun getAllLullabies(): Flow<List<LullabyDomainModel>>
    suspend fun getLullabyById(documentId: String): LullabyDomainModel?
    suspend fun toggleLullabyFavourite(lullabyId: String)
    fun checkIfLullabyIsFavourite(lullabyId: String): Flow<Boolean>
    fun getFavouriteLullabies(): Flow<List<LullabyDomainModel>>
}

// Repository Implementation - Parallel fetching
override suspend fun syncLullabiesFromRemote(): Flow<List<LullabyDomainModel>> {
    return flow {
        if (appPreferences.isSyncNeeded(false) || localDataSource.getLullabyCount() == 0) {
            coroutineScope {
                val lullabiesDeferred = async { lullabyRemoteDataSource.fetchLullabyData() }
                val translationsDeferred = async { lullabyRemoteDataSource.fetchTranslationData() }
                // Parallel insert after both complete
            }
        }
        emitAll(getReactiveLullabies())
    }
}
```

---

## 4. Domain Layer

### 4.1 Use Cases

```kotlin
// FetchLullabiesUseCase.kt
class FetchLullabiesUseCase @Inject constructor(
    private val lullabyRepository: LullabyRepository
) {
    suspend operator fun invoke(): Flow<List<LullabyDomainModel>> {
        return lullabyRepository.syncLullabiesFromRemote()
    }
}
```

### 4.2 Domain Models

```kotlin
data class LullabyDomainModel(
    val documentId: String,
    val id: String,
    val musicName: String,        // Language-aware name
    val musicPath: String,
    val musicLocalPath: String?,
    val musicSize: Long,
    val imagePath: String,
    val musicLength: String,
    val isDownloaded: Boolean?,
    val isFavourite: Boolean,
    val popularity_count: Int,
    val isFree: Boolean,
    val translation: TranslationDomainModel? = null
)
```

---

## 5. Presentation Layer

### 5.1 MVI Pattern Implementation

#### Intent (User Actions)
```kotlin
sealed class LullabyIntent {
    object FetchLullabies : LullabyIntent()
    data class DownloadLullabyItem(val lullabyItem: LullabyDomainModel) : LullabyIntent()
    data class ChangeCategory(val category: LullabyCategory) : LullabyIntent()
    object InitializeAds : LullabyIntent()
    data class LoadBannerAd(val adUnitId: String, val adSizeType: AdSizeType) : LullabyIntent()
    data class ShowRewardedAd(val adUnitId: String, val activity: Activity, val lullaby: LullabyDomainModel) : LullabyIntent()
}
```

#### State (UI State)
```kotlin
sealed class LullabyUiState {
    object IsLoading : LullabyUiState()
    data class Content(
        val lullabies: List<LullabyDomainModel>,
        val filteredLullabies: List<LullabyDomainModel>,
        val popularLullabies: List<LullabyDomainModel>,  // Pre-filtered
        val freeLullabies: List<LullabyDomainModel>,     // Pre-filtered
        val downloadingItems: Set<String>,
        val downloadedItems: Set<String>,
        val downloadProgress: Map<String, Int>,
        val currentCategory: LullabyCategory,
        val adUnlockedIds: Set<String>,                  // Rewarded ad unlocks
        val adState: AdUiState,
        val isPremium: Boolean                           // Single source of truth
    ) : LullabyUiState()
    data class Error(val message: String) : LullabyUiState()
}
```

#### ViewModel
```kotlin
@HiltViewModel
class LullabyViewModel @Inject constructor(
    private val fetchLullabiesUseCase: FetchLullabiesUseCase,
    private val sessionUnlockManager: SessionUnlockManager,
    private val billingManager: BillingManager
) : ViewModel() {

    // Combine base state + session unlocks + billing status
    val lullabyUiState: StateFlow<LullabyUiState> = combine(
        _lullabyUiState,
        sessionUnlockManager.unlockedItems,
        billingManager.isPurchased
    ) { baseState, unlockedIds, isPremium ->
        if (baseState is LullabyUiState.Content) {
            baseState.copy(adUnlockedIds = unlockedIds, isPremium = isPremium)
        } else baseState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LullabyUiState.IsLoading)

    fun handleIntent(intent: LullabyIntent) {
        when (intent) {
            is LullabyIntent.FetchLullabies -> fetchLullabyData()
            is LullabyIntent.DownloadLullabyItem -> downloadLullaby(intent.lullabyItem)
            is LullabyIntent.ChangeCategory -> changeCategory(intent.category)
            // ...
        }
    }
}
```

---

## 6. Dependency Injection

### Hilt Modules

```kotlin
// AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }
}

// DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "lullaby_database")
            .fallbackToDestructiveMigration()
            .build()
    }
}

// FcmModule.kt - With Qualifiers for separate Retrofit instance
@Module
@InstallIn(SingletonComponent::class)
object FcmModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class FcmRetrofit

    @Provides
    @Singleton
    @FcmRetrofit
    fun provideFcmRetrofit(@FcmOkHttpClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.FCM_SERVER_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

// RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindLullabyRepository(impl: LullabyRepositoryImpl): LullabyRepository
}
```

---

## 7. Backend Integration (Appwrite)

```kotlin
@Singleton
class AppwriteBaseClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val projectId = "671e0ca70034a1e99b3d"

    val client = Client(context)
        .setEndpoint("https://appwrite.taagidtech.com/v1")
        .setProject(projectId)

    val databases = Databases(client)

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
}
```

### Data Flow
```
Appwrite Backend -> RemoteDataSource -> Repository -> UseCase -> ViewModel -> UI
```

---

## 8. Push Notifications (FCM)

### Architecture
```
Personal FCM Server (notifier.appswave.xyz)
    -> Retrofit API Service
    -> FcmRemoteDataSource
    -> FcmRepositoryImpl
    -> NaptuneMessagingService (FirebaseMessagingService)
    -> NotificationHelper
    -> Android Notification
```

### FCM Service
```kotlin
@AndroidEntryPoint
class NaptuneMessagingService : FirebaseMessagingService() {

    @Inject lateinit var fcmRepository: FcmRepository
    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onMessageReceived(message: RemoteMessage) {
        val notificationPayload = extractNotificationPayload(message)
        displayNotification(notificationPayload)
    }

    override fun onNewToken(token: String) {
        serviceScope.launch {
            fcmRepository.registerToken(token)
        }
    }
}
```

---

## 9. Audio System (Media3)

### Components
1. **MusicService** - `MediaSessionService` for background playback
2. **MusicController** - ExoPlayer wrapper with state management
3. **GlobalAudioPlayerManager** - UI state coordination

### Media Service
```kotlin
@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject lateinit var exoPlayer: ExoPlayer
    @Inject lateinit var musicController: MusicController

    override fun onCreate() {
        super.onCreate()

        // Remove previous button from notification
        val customPlayerCommands = Player.Commands.Builder()
            .addAllCommands()
            .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
            .build()

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(mediaSessionCallback)
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Keep service alive if audio is loaded
        if (player?.currentMediaItem != null) {
            // Don't call super - keeps notification visible
        } else {
            super.onTaskRemoved(rootIntent)
            stopSelf()
        }
    }
}
```

### Audio Features
- **Source Awareness**: Lullabies loop, stories play once
- **Background Playback**: Foreground service with notification
- **Sleep Timer**: Auto-stop with AlarmManager
- **Offline Support**: Local file priority for downloaded content

---

## 10. Monetization

### Google Play Billing
```kotlin
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener, BillingClientStateListener {

    private val _isPurchased = MutableStateFlow(false)
    val isPurchased: StateFlow<Boolean> = _isPurchased.asStateFlow()

    private val _currentPurchaseType = MutableStateFlow<PurchaseType>(PurchaseType.None)
    val currentPurchaseType: StateFlow<PurchaseType> = _currentPurchaseType.asStateFlow()

    // Product IDs
    private val monthlySubscriptionId = "monthly_premium_subscription"
    private val yearlySubscriptionId = "yearly_premium_subscription"
    private val lifetimeProductId = "lifetime_premium_access"
}

enum class PurchaseType {
    None,                   // Free user - shows ads
    MonthlySubscription,    // Monthly subscriber
    YearlySubscription,     // Yearly subscriber
    Lifetime                // Lifetime purchase
}
```

### AdMob Integration
- **Banner Ads** - Shown on main screens for free users
- **Rewarded Ads** - Watch to unlock premium content temporarily
- **Session Unlock Manager** - Tracks ad-unlocked items per session

---

## 11. Multi-Language Support

### Supported Languages
| Code | Language | RTL |
|------|----------|-----|
| en | English | No |
| es | Spanish | No |
| fr | French | No |
| de | German | No |
| pt | Portuguese | No |
| hi | Hindi | No |
| ar | Arabic | Yes |

### Language Management
```kotlin
class LanguageManager @Inject constructor(
    val appPreferences: AppPreferences
) {
    companion object {
        const val DEFAULT_LANGUAGE = "en"
        val SUPPORTED_LANGUAGES = listOf("en", "es", "fr", "de", "pt", "hi", "ar")
    }
}
```

### Reactive Language Updates
```kotlin
// LanguageStateManager.kt
@Singleton
class LanguageStateManager @Inject constructor() {
    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()
}

// Repository - React to language changes
fun getReactiveLullabies(): Flow<List<LullabyDomainModel>> {
    return languageStateManager.currentLanguage.flatMapLatest { language ->
        localDataSource.getAllLullabiesWithLocalizedNames(language)
    }
}
```

---

## 12. Navigation

### Screen Definitions
```kotlin
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Main : Screen("main")
    object Favourite : Screen("favourite")
    object Profile : Screen("profile")
    object Premium : Screen("premium")
    object Settings : Screen("settings")
    object Debug : Screen("debug")
    object Explore : Screen("explore")
    object Lullaby : Screen("lullaby")
    object Story : Screen("story")
    object SleepSounds : Screen("sleep_sounds")
    object StoryManager : Screen("read_manager/{storyId}/{...}")
    object StoryReader : Screen("story_reader/{storyId}/{...}")
    object AudioPlayer : Screen("audio_player/{documentId}/{...}")
}
```

### Bottom Navigation
```kotlin
sealed class BottomNavItem(val route: String, val iconRes: Int, val title: String) {
    object Home : BottomNavItem("main", R.drawable.homenavic, "Home")
    object Favourite : BottomNavItem("favourite", R.drawable.favouritenavic, "Favourite")
    object Profile : BottomNavItem("profile", R.drawable.profilenavic, "Profile")
}
```

---

## 13. Key Features

| Feature | Description |
|---------|-------------|
| **Lullaby Playback** | Stream/download lullabies with loop mode |
| **Bedtime Stories** | Audio stories with reader mode |
| **Background Playback** | MediaSessionService with notification |
| **Sleep Timer** | Auto-stop playback after duration |
| **Offline Download** | PRDownloader with progress tracking |
| **Favourites** | LIFO ordering with metadata |
| **Multi-Language** | 7 languages with RTL support |
| **Premium Subscriptions** | Monthly/Yearly/Lifetime via Google Play |
| **Ad-Free Option** | Banner & Rewarded ads for free users |
| **Push Notifications** | FCM with custom server |
| **Analytics** | Firebase Analytics integration |

---

## 14. Build Configuration

```kotlin
android {
    namespace = "com.naptune.lullabyandstory"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.naptune.lullabyandstory"
        minSdk = 26
        targetSdk = 35

        resourceConfigurations += listOf("en", "es", "fr", "de", "pt", "hi", "ar")

        // FCM Server Configuration
        buildConfigField("String", "FCM_SERVER_URL", "\"https://notifier.appswave.xyz/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}
```

---

## 15. Dependencies

### Core Dependencies
```kotlin
// Core Android
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.lifecycle.runtime.ktx)
implementation(libs.androidx.activity.compose)

// Compose
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.ui)
implementation(libs.androidx.material3)
implementation(libs.navigation.compose)

// Hilt
implementation(libs.hilt.android)
implementation(libs.hilt.navigation.compose)
ksp(libs.hilt.compiler)

// Room
implementation(libs.bundles.room)
ksp(libs.androidx.room.compiler)

// DataStore
implementation(libs.androidx.datastore.preferences)

// Appwrite
implementation(libs.sdk.appwrite)

// Coil (Image Loading)
implementation(libs.coil.compose)

// ExoPlayer (Media3)
implementation(libs.bundles.exoplayer)

// PRDownloader
implementation(libs.prdownloader)

// AdMob
implementation(libs.play.services.ads)

// Google Play Billing
implementation(libs.billing.ktx)

// Firebase
implementation("com.google.firebase:firebase-analytics-ktx")
implementation("com.google.firebase:firebase-messaging-ktx")

// Retrofit (FCM)
implementation("com.squareup.retrofit2:retrofit:3.0.0")
implementation("com.squareup.retrofit2:converter-gson:3.0.0")

// WorkManager
implementation("androidx.work:work-runtime-ktx")
implementation("androidx.hilt:hilt-work")
```

---

## Summary

**Naptune** demonstrates modern Android development with:

- **Clean Architecture** - Clear separation of concerns
- **MVI Pattern** - Predictable state management
- **Jetpack Compose** - Declarative UI
- **Hilt** - Dependency injection
- **Room** - Local database with reactive Flows
- **Appwrite** - Backend-as-a-Service
- **Media3** - Professional audio playback
- **Multi-language** - 7 languages including RTL
- **Google Play Billing** - Premium subscriptions
- **FCM** - Push notifications with custom server

---

*Documentation Version: 2.0*
*Last Updated: December 2024*
*Total Kotlin Files: ~180+*
