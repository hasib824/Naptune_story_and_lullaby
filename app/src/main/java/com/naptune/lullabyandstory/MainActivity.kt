package com.naptune.lullabyandstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.activity.viewModels
import com.naptune.lullabyandstory.presentation.components.common.SetStatusBarColor
import com.naptune.lullabyandstory.presentation.fcm.FcmIntent
import com.naptune.lullabyandstory.presentation.fcm.FcmViewModel
import com.naptune.lullabyandstory.presentation.main.MainActivityViewModel
import com.naptune.lullabyandstory.presentation.navigation.NaptuneNavigation
import com.naptune.lullabyandstory.presentation.player.service.MusicController
import com.naptune.lullabyandstory.ui.theme.LullabyAndStoryTheme
import com.naptune.lullabyandstory.utils.fcm.DeepLinkRouter
import com.naptune.lullabyandstory.utils.fcm.FcmWorkScheduler
import com.naptune.lullabyandstory.utils.LanguageManager
import com.naptune.lullabyandstory.utils.LocaleHelper
import com.naptune.lullabyandstory.presentation.language.LanguageViewModel
import com.naptune.lullabyandstory.presentation.player.timermodal.operations.TimerAlarmManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import com.naptune.lullabyandstory.data.billing.BillingManager
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import com.naptune.lullabyandstory.data.datastore.getLanguageSync
import com.naptune.lullabyandstory.data.datastore.getSplashScreenShownSync
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    @Inject
    lateinit var musicController: MusicController

    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var appPreferences: AppPreferences

    // ✅ NEW: TimerAlarmManager for timer reset operations (injected via Hilt)
    @Inject
    lateinit var timerAlarmManager: TimerAlarmManager

    // 🆕 FCM: WorkScheduler for background token sync
    @Inject
    lateinit var fcmWorkScheduler: FcmWorkScheduler

    // ✅ ViewModel to handle timer reset on app launch
    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    // ✅ LanguageViewModel for toast display
    private val languageViewModel: LanguageViewModel by viewModels()

    // 🆕 FCM: ViewModel for FCM operations
    private val fcmViewModel: FcmViewModel by viewModels()

    // ✅ Flag to track if we came from notification
    private var isFromNotification = false

    // 🆕 FCM: Store navController for deep link routing
    private var navController: NavController? = null

    @Inject
    lateinit var billingManager: BillingManager

    // 🆕 FCM: Notification permission launcher (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("MainActivity", "📱 Notification permission: ${if (isGranted) "GRANTED" else "DENIED"}")
        fcmViewModel.updateNotificationPermission(isGranted)

        if (isGranted) {
            // Permission granted - initialize FCM
            initializeFcm()
        } else {
            Log.w("MainActivity", "⚠️ Notification permission denied - FCM notifications disabled")
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // ✅ Set proper window flags for AdMob compatibility
       /* window.apply {
            // Enable layout behind system bars for edge-to-edge
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(this, false)
            // Allow content to layout behind status/nav bars
            addFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }*/


        // ✅ Check if we came from notification click
        checkNotificationIntent(intent)

        Log.d("MainActivity", "🚀 MainActivity created")

        // ✅ Reset timer settings through TimerAlarmManager
        Log.d("MainActivity", "⏰ Timer settings reset called from MainActivity")

        // 🆕 FCM: Request notification permission and initialize FCM
        requestNotificationPermissionAndInitializeFcm()

        setContent {
            LullabyAndStoryTheme { // ✅ Original theme name restored

                SetStatusBarColor()

                val navControllerLocal = rememberNavController()

                // 🆕 FCM: Store navController for deep link routing
                LaunchedEffect(Unit) {
                    navController = navControllerLocal

                    // Handle deep link from notification if app was launched from notification
                    if (DeepLinkRouter.isFromNotification(intent)) {
                        DeepLinkRouter.handleNotificationIntent(intent, navControllerLocal)
                    }
                }

                // ✅ Fast synchronous check using shared DataStore extension
                val isSplashShown = remember { getSplashScreenShownSync() }
                Log.d("MainActivity", "🎬 Splash screen shown before: $isSplashShown")

                // ✅ Shows immediately, no waiting
                NaptuneNavigation(
                    navController = navControllerLocal,
                    startFromNotification = isFromNotification,
                    shouldShowSplash = !isSplashShown, // Show splash if NOT shown yet
                    billingManager = billingManager
                )
            }
        }

        lifecycleScope.launch {
            resetAppState()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun resetAppState() {
        // ✅ NEW: Reset timer settings using TimerAlarmManager DataStore
        timerAlarmManager.resetTimerSettings()
        timerAlarmManager.saveAlarmState(false, "", 0L)
        Log.d("MainActivity", "🔄 Timer settings reset using TimerAlarmManager DataStore")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "▶️ MainActivity onResume")
        // Check if language should be updated based on device changes (hybrid system)
        checkAndUpdateLanguageOnResume()
    }

    /**
     * ✅ FIXED: Check if device language changed while app was in background
     * Only update if user hasn't manually selected a language
     * Now uses lifecycleScope instead of runBlocking to avoid blocking main thread
     */
    private fun checkAndUpdateLanguageOnResume() {
        // ✅ Use lifecycleScope instead of runBlocking
        lifecycleScope.launch {
            try {
                if (languageManager.shouldFollowDeviceLanguage()) {
                    // ✅ Non-blocking suspend call
                    val currentAppLanguage = languageManager.getLanguage()
                    val currentDeviceLanguage = languageManager.detectDeviceLanguagePublic()

                    if (currentDeviceLanguage != currentAppLanguage &&
                        languageManager.isLanguageSupported(currentDeviceLanguage)) {

                        Log.d("MainActivity", "🔄 Device language changed: $currentAppLanguage → $currentDeviceLanguage")

                        // Update language (non-blocking)
                        languageManager.setLanguage(currentDeviceLanguage, isManualChange = false)

                        // Recreate activity on main thread
                        withContext(Dispatchers.Main) {
                            recreate()
                        }
                    } else {
                        Log.d("MainActivity", "✅ Language matches device: $currentAppLanguage")
                    }
                } else {
                    Log.d("MainActivity", "🔒 User has custom language selected - ignoring device changes")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "❌ Language check on resume failed: ${e.message}")
            }
        }
    }



    // ✅ Handle new intents (when app is already running)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "🔄 onNewIntent called")
        checkNotificationIntent(intent)
        setIntent(intent) // Update the activity's intent

        // 🆕 FCM: Handle deep link routing when app is already running
        navController?.let { nav ->
            if (DeepLinkRouter.isFromNotification(intent)) {
                DeepLinkRouter.handleNotificationIntent(intent, nav)
            }
        }
    }

    /**
     * ✅ Check if the intent came from notification click
     * Updated to support both old notification system and new FCM system
     */
    private fun checkNotificationIntent(intent: Intent?) {
        intent?.let {
            val action = it.action
            Log.d("MainActivity", "🎯 Intent action: $action")

            // Old notification system (audio player)
            if (action == "OPEN_AUDIO_PLAYER") {
                Log.d("MainActivity", "🔔 Notification click detected (audio player)")
                isFromNotification = true
                Log.d("MainActivity", "🎵 Will open Main screen with MiniController")
            }

            // 🆕 FCM: New notification system
            if (DeepLinkRouter.isFromNotification(it)) {
                Log.d("MainActivity", "🔔 FCM Notification click detected")
                val screenRoute = DeepLinkRouter.getScreenRoute(it)
                val contentId = DeepLinkRouter.getContentId(it)
                Log.d("MainActivity", "📍 FCM Deep link: route=$screenRoute, contentId=$contentId")
                isFromNotification = true
            }
        }
    }

    /**
     * 🆕 FCM: Request notification permission (Android 13+) and initialize FCM
     */
    private fun requestNotificationPermissionAndInitializeFcm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires runtime permission
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    Log.d("MainActivity", "✅ Notification permission already granted")
                    fcmViewModel.updateNotificationPermission(true)
                    initializeFcm()
                }
                else -> {
                    // Request permission
                    Log.d("MainActivity", "📱 Requesting notification permission...")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 12 and below - permission granted by default
            Log.d("MainActivity", "✅ Notification permission granted (Android < 13)")
            fcmViewModel.updateNotificationPermission(true)
            initializeFcm()
        }
    }

    /**
     * 🆕 FCM: Initialize FCM system
     * - Get FCM token
     * - Register with server
     * - Subscribe to global topic
     * - Schedule background sync
     */
    private fun initializeFcm() {
        Log.d("MainActivity", "════════════════════════════════════════")
        Log.d("MainActivity", "🔥 INITIALIZING FCM SYSTEM")
        Log.d("MainActivity", "════════════════════════════════════════")

        // Initialize FCM via ViewModel
        fcmViewModel.processIntent(FcmIntent.InitializeFcm)

        // Schedule periodic token sync (every 24 hours)
        fcmWorkScheduler.schedulePeriodicTokenSync()

        Log.d("MainActivity", "✅ FCM initialization started")
        Log.d("MainActivity", "⏰ Background sync scheduled (every 24h)")
        Log.d("MainActivity", "════════════════════════════════════════")
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        // Apply saved language setting to the activity context
        // ✅ Using shared DataStore extension - clean and consistent
        val languageCode = newBase.getLanguageSync()
        Log.d("MainActivity", "✅ Applying language from DataStore: $languageCode")
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }
}
