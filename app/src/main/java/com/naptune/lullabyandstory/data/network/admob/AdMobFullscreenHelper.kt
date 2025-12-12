package com.naptune.lullabyandstory.data.network.admob

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Helper class to handle fullscreen AdMob ads with proper system UI management
 * Fixes issues with status bar visibility and topbar displacement
 */
object AdMobFullscreenHelper {
    
    private const val TAG = "AdMobFullscreenHelper"
    
    // Store original window flags to restore later
    private var originalSystemUiVisibility: Int = 0
    private var originalStatusBarColor: Int = 0
    private var originalNavigationBarColor: Int = 0
    private var wasLightStatusBar: Boolean = false
    private var wasLightNavigationBar: Boolean = false
    
    /**
     * Prepare activity for fullscreen ad display
     * Call this WHEN ad actually starts showing (onAdShowedFullScreenContent)
     */
    fun prepareForFullscreenAd(activity: Activity) {
        try {
          /*  Log.d(TAG, "🎬 Preparing activity for fullscreen ad...")
            
            val window = activity.window
            val decorView = window.decorView
            val insetsController = WindowCompat.getInsetsController(window, decorView)
            
            // Store original values
            originalStatusBarColor = window.statusBarColor
            originalNavigationBarColor = window.navigationBarColor
            wasLightStatusBar = insetsController.isAppearanceLightStatusBars
            wasLightNavigationBar = insetsController.isAppearanceLightNavigationBars
            
            // ✅ Aggressive fullscreen approach for AdMob
            window.apply {
                // Set flags for true fullscreen
                addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                
                // Make bars transparent
                statusBarColor = Color.TRANSPARENT
                navigationBarColor = Color.TRANSPARENT
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Android 11+ - More aggressive approach
                    insetsController.hide(
                        WindowInsetsCompat.Type.statusBars() 
                        or WindowInsetsCompat.Type.navigationBars()
                        or WindowInsetsCompat.Type.systemGestures()
                    )
                    insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    
                    // Additional API 30+ specific flags
                    setDecorFitsSystemWindows(false)
                } else {
                    // Legacy approach - very aggressive
                    @Suppress("DEPRECATION")
                    decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LOW_PROFILE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
                }
            }
            
            Log.d(TAG, "✅ Activity prepared for fullscreen ad with aggressive flags")*/
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error preparing for fullscreen ad: ${e.message}", e)
        }
    }
    
    /**
     * Restore activity to original state
     * Call this AFTER ad is dismissed/completed
     */
    fun restoreFromFullscreenAd(activity: Activity) {
        try {
           /* Log.d(TAG, "🔄 Restoring activity from fullscreen ad...")
            
            val window = activity.window
            val decorView = window.decorView
            val insetsController = WindowCompat.getInsetsController(window, decorView)
            
            // ✅ Clear all fullscreen flags first
            window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            
            // Restore original colors
            window.statusBarColor = originalStatusBarColor
            window.navigationBarColor = originalNavigationBarColor
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ - Show system bars properly
                window.setDecorFitsSystemWindows(false)
                
                insetsController.show(
                    WindowInsetsCompat.Type.statusBars() 
                    or WindowInsetsCompat.Type.navigationBars()
                )
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                
                // Restore appearance
                insetsController.isAppearanceLightStatusBars = wasLightStatusBar
                insetsController.isAppearanceLightNavigationBars = wasLightNavigationBar
            } else {
                // Legacy - restore to edge-to-edge with transparent status bar
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
            }
            
            // ✅ Small delay to ensure proper restoration
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                resetToEdgeToEdge(activity)
            }, 100)
            
            Log.d(TAG, "✅ Activity restored from fullscreen ad")*/
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error restoring from fullscreen ad: ${e.message}", e)
        }
    }
    
    /**
     * Reset to edge-to-edge while preserving current status bar color
     * Call this to ensure proper edge-to-edge after ad completion
     */
    fun resetToEdgeToEdge(activity: Activity) {
        try {
          /*  Log.d(TAG, "🔧 Resetting to edge-to-edge mode...")
            
            val window = activity.window
            val decorView = window.decorView
            val insetsController = WindowCompat.getInsetsController(window, decorView)
            
            // ✅ Preserve current status bar color instead of forcing transparent
            val currentStatusBarColor = window.statusBarColor
            Log.d(TAG, "🎨 Preserving current status bar color: $currentStatusBarColor")
            
            // Only set navigation bar transparent for edge-to-edge
            window.navigationBarColor = Color.TRANSPARENT
            
            // Enable edge-to-edge layout
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // ✅ Don't force light status bar icons - let the current screen decide
            // insetsController.isAppearanceLightStatusBars = false
            
            Log.d(TAG, "✅ Edge-to-edge mode restored with preserved status bar color")*/
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error resetting to edge-to-edge: ${e.message}", e)
        }
    }
}