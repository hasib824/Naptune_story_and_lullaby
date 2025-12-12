package com.naptune.lullabyandstory

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.google.android.gms.ads.MobileAds
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import com.naptune.lullabyandstory.domain.manager.LanguageStateManager
import com.naptune.lullabyandstory.presentation.player.bottomsheet.GlobalAudioPlayerManager
import com.naptune.lullabyandstory.utils.LanguageManager
import com.naptune.lullabyandstory.utils.LocaleHelper
import com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import io.appwrite.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
// LANGUAGE_SUPPORT_IMPLEMENTATION_PLAN.md
@HiltAndroidApp
class NaptuneApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var languageStateManager: LanguageStateManager

    @Inject
    lateinit var globalAudioPlayerManager: GlobalAudioPlayerManager

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    // ✅ Add application-level coroutine scope for non-blocking initialization
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // ✅ Store ImageLoader reference for cleanup
    private var imageLoader: ImageLoader? = null

    override fun onCreate() {
        super.onCreate()

        Log.d("NaptuneApplication", "🚀 Application starting...")

        // ✅ Initialize language settings without blocking (FIXED)
        initializeLanguage()

        // ✅ Existing PRDownloader setup
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .setReadTimeout(30_000)
            .setConnectTimeout(30_000)
            .build()

        PRDownloader.initialize(applicationContext, config)
        
        // ✅ Initialize AdMob SDK (following official guide)
        MobileAds.initialize(this) { initializationStatus ->
            Log.d("NaptuneApplication", "🚀 AdMob initialized successfully")
            Log.d("NaptuneApplication", "📊 Initialization status: ${initializationStatus.adapterStatusMap}")

            // Log adapter initialization status for debugging
            initializationStatus.adapterStatusMap.forEach { (className, status) ->
                Log.d("NaptuneApplication", "Adapter: $className, Status: ${status.initializationState}, Description: ${status.description}")
            }
        }

        // ✅ Initialize Firebase Analytics with user properties
        initializeAnalytics()

        Log.d("NaptuneApplication", "🚀 Application initialized with optimized image loading, AdMob, language support, and analytics")



    }





    // ✅ NEW: Advanced Coil ImageLoader configuration
    override fun newImageLoader(): ImageLoader {
        // ✅ Store reference for cleanup
        if (imageLoader == null) {
            imageLoader = ImageLoader.Builder(this)
            // ✅ Memory Cache Configuration (30% of app heap)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.30) // Use 30% of available heap
                    .strongReferencesEnabled(true) // Keep strong references for better performance
                    .weakReferencesEnabled(true) // Allow GC when memory is low
                    .build()
            }
            // ✅ Disk Cache Configuration (150MB)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(150 * 1024 * 1024) // 150MB disk cache
                    .build()
            }
            // ✅ Network optimizations with OkHttp
            .okHttpClient {
                OkHttpClient.Builder()
                    .callTimeout(20, TimeUnit.SECONDS) // Total timeout
                    .connectTimeout(10, TimeUnit.SECONDS) // Connection timeout
                    .readTimeout(15, TimeUnit.SECONDS) // Read timeout
                    .writeTimeout(10, TimeUnit.SECONDS) // Write timeout
                    // ✅ Enable response caching
                    .cache(
                        okhttp3.Cache(
                            directory = cacheDir.resolve("http_cache"),
                            maxSize = 50 * 1024 * 1024 // 50MB HTTP cache
                        )
                    )
                    .build()
            }
            // ✅ Default cache policies for better performance
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            // ✅ Component registry for additional formats (Updated API)
            .components {
                add(SvgDecoder.Factory()) // SVG support
            }
            // ✅ Enable crossfade by default
            .crossfade(300) // 300ms crossfade animation
            // ✅ Enable logging in debug builds only
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            // ✅ Respect system settings
            .respectCacheHeaders(false) // Don't respect cache headers from server
            .allowHardware(true) // Enable hardware bitmaps for better performance
            .allowRgb565(true) // Allow RGB565 for smaller memory usage
            .build()

            Log.d("NaptuneApplication", "✅ Coil ImageLoader created and cached")
        }

        return imageLoader!!
    }

    /**
     * ✅ Initialize Firebase Analytics with user properties
     */
    private fun initializeAnalytics() {
        try {
            // Get device information
            val deviceType = if (resources.configuration.screenLayout and
                android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK >=
                android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE) {
                "tablet"
            } else {
                "phone"
            }

            val language = languageManager.appPreferences.getLanguageSync() ?: "en"
            val osVersion = "Android ${Build.VERSION.RELEASE}"
            val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"

            // Set user properties
            analyticsHelper.setUserProperty("device_type", deviceType)
            analyticsHelper.setUserProperty("app_language", language)
            analyticsHelper.setUserProperty("os_version", osVersion)
            analyticsHelper.setUserProperty("device_model", deviceModel)

            // Log app open event
            analyticsHelper.logAppOpen()

            Log.d("NaptuneApplication", "🔥 Firebase Analytics initialized | Device: $deviceType | Language: $language")
        } catch (e: Exception) {
            Log.e("NaptuneApplication", "❌ Analytics initialization failed: ${e.message}")
        }
    }

    /**
     * ✅ FIXED: Initialize language settings with hybrid device/manual language detection
     * Now uses non-blocking approach for fast startup
     */
    private fun initializeLanguage() {
        // ✅ Get cached language synchronously for fast initial setup
        val cachedLanguage = try {
            languageManager.appPreferences.getLanguageSync() ?: LanguageManager.DEFAULT_LANGUAGE
        } catch (e: Exception) {
            Log.e("NaptuneApplication", "❌ Language cache read failed: ${e.message}")
            LanguageManager.DEFAULT_LANGUAGE
        }

        // Apply immediately for fast startup
        LocaleHelper.setLocale(this@NaptuneApplication, cachedLanguage)
        Log.d("NaptuneApplication", "🌍 Language applied (cached): $cachedLanguage")

        // ✅ Launch background initialization for proper detection (non-blocking)
        applicationScope.launch {
            try {
                val detectedLanguage = if (languageManager.shouldFollowDeviceLanguage()) {
                    val isFirstLaunch = languageManager.isFirstLaunch()

                    if (isFirstLaunch) {
                        // First launch - detect and set device language
                        val deviceLanguage = languageManager.detectDeviceLanguagePublic()
                        val languageToUse = if (languageManager.isLanguageSupported(deviceLanguage)) {
                            deviceLanguage
                        } else {
                            LanguageManager.DEFAULT_LANGUAGE
                        }

                        languageManager.setLanguage(languageToUse, isManualChange = false)

                        // Show first launch toast notification
                        showFirstLaunchToast(languageToUse)

                        Log.d("NaptuneApplication", "🆕 First launch: Device language '$deviceLanguage' → Using '$languageToUse'")
                        languageToUse
                    } else {
                        // Not first launch - check if device language changed
                        val currentSavedLanguage = languageManager.getLanguage()
                        val currentDeviceLanguage = languageManager.detectDeviceLanguagePublic()

                        if (currentDeviceLanguage != currentSavedLanguage &&
                            languageManager.isLanguageSupported(currentDeviceLanguage)) {
                            // Device language changed and is supported - update automatically
                            languageManager.setLanguage(currentDeviceLanguage, isManualChange = false)
                            Log.d("NaptuneApplication", "📱 Device language auto-changed: $currentSavedLanguage → $currentDeviceLanguage")
                            currentDeviceLanguage
                        } else {
                            // No device language change or not supported - keep current
                            Log.d("NaptuneApplication", "📱 Following device language: $currentSavedLanguage")
                            currentSavedLanguage
                        }
                    }
                } else {
                    // 🔒 User manually selected language - respect user choice
                    val savedLanguage = languageManager.getLanguage()
                    Log.d("NaptuneApplication", "🔒 Custom language selected by user: $savedLanguage")
                    savedLanguage
                }

                // Mark first launch as complete
                if (languageManager.isFirstLaunch()) {
                    languageManager.appPreferences.saveBoolean(LanguageManager.KEY_FIRST_LAUNCH, false)
                }

                // Update if different from cached value
                if (detectedLanguage != cachedLanguage) {
                    Log.d("NaptuneApplication", "🔄 Language changed: $cachedLanguage → $detectedLanguage")
                    LocaleHelper.setLocale(this@NaptuneApplication, detectedLanguage)
                }

                Log.d("NaptuneApplication", "✅ Language detection completed: $detectedLanguage")
            } catch (e: Exception) {
                Log.e("NaptuneApplication", "❌ Background language detection failed: ${e.message}")
            }
        }
    }

    /**
     * Show toast notification for first launch language detection
     */
    private fun showFirstLaunchToast(languageCode: String) {
        try {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                val languageName = languageManager.getLanguageDisplayName(languageCode)
                val toastMessage = when (languageCode) {
                    "en" -> "Language set to $languageName (change anytime from Profile)"
                    "es" -> "Idioma establecido en $languageName (cambiar en cualquier momento desde Perfil)"
                    "fr" -> "Langue définie sur $languageName (changer à tout moment depuis le Profil)"
                    "de" -> "Sprache auf $languageName gesetzt (jederzeit im Profil ändern)"
                    "pt" -> "Idioma definido para $languageName (alterar a qualquer momento no Perfil)"
                    "hi" -> "भाषा $languageName पर सेट की गई (प्रोफाइल से कभी भी बदलें)"
                    "ar" -> "تم تعيين اللغة إلى $languageName (يمكن التغيير في أي وقت من الملف الشخصي)"
                    else -> "Language set to $languageName (change anytime from Profile)"
                }

                android.widget.Toast.makeText(this@NaptuneApplication, toastMessage, android.widget.Toast.LENGTH_LONG).show()
                Log.d("NaptuneApplication", "🍞 First launch toast shown: $toastMessage")
            }
        } catch (e: Exception) {
            Log.e("NaptuneApplication", "❌ First launch toast failed: ${e.message}")
        }
    }

    override fun attachBaseContext(base: android.content.Context) {
        // Don't apply language here - let initializeLanguage() handle automatic detection
        // This prevents interference with device language change detection
        super.attachBaseContext(base)
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)

        // ✅ Use cached value instead of blocking
        val languageCode = try {
            languageManager.appPreferences.getLanguageSync() ?: "en"
        } catch (e: Exception) {
            Log.e("NaptuneApplication", "❌ Config change language read failed: ${e.message}")
            "en"
        }

        LocaleHelper.setLocale(this, languageCode)

        // ✅ Refresh in background if needed
        applicationScope.launch {
            try {
                // Verify and update if needed
                val currentLanguage = languageManager.getLanguage()
                if (currentLanguage != languageCode) {
                    Log.d("NaptuneApplication", "🔄 Language refreshed: $languageCode → $currentLanguage")
                    LocaleHelper.setLocale(this@NaptuneApplication, currentLanguage)
                }
            } catch (e: Exception) {
                Log.e("NaptuneApplication", "❌ Language refresh failed: ${e.message}")
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        Log.d("NaptuneApplication", "🧹 Application terminating - cleaning up resources")

        // ✅ Clean up singleton resources
        globalAudioPlayerManager.cleanup()
        languageStateManager.cleanup()

        // ✅ CRITICAL FIX: Shutdown Coil ImageLoader
        imageLoader?.let { loader ->
            try {
                loader.shutdown()
                Log.d("NaptuneApplication", "✅ Coil ImageLoader shutdown successfully")
            } catch (e: Exception) {
                Log.e("NaptuneApplication", "❌ Failed to shutdown ImageLoader: ${e.message}")
            }
        }
        imageLoader = null

        // ✅ Cancel application scope
        applicationScope.cancel()

        Log.d("NaptuneApplication", "✅ Application terminated - all resources cleaned up")
    }
}
