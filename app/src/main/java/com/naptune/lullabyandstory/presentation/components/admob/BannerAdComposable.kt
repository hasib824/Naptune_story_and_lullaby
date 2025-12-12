package com.naptune.lullabyandstory.presentation.components.admob

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdView
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.domain.model.BannerAdDomainModel
import com.naptune.lullabyandstory.domain.model.AdSizeType
import dagger.hilt.android.EntryPointAccessors

@Composable
fun BannerAdComposable(
    bannerAd: BannerAdDomainModel?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent, // Transparent for native look
    // ✅ NEW: Custom loading content slot
    loadingContent: @Composable (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Get AdMobDataSource from Hilt
    val adMobDataSource = remember {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AdMobEntryPoint::class.java
        )
        hiltEntryPoint.adMobDataSource()
    }
    
    // ✅ ENHANCED: Show space when loaded OR when loading (if custom loading provided)
    val shouldShowAdSpace = when {
        bannerAd?.isLoaded == true -> true
        loadingContent != null && bannerAd?.isLoading == true -> true
        else -> false
    }
    
    // ✅ Direct height - no animation, immediate space allocation
    val adHeight = if (shouldShowAdSpace) {
        when (bannerAd?.adSize?.type) {
            AdSizeType.ANCHORED_ADAPTIVE_BANNER -> bannerAd.adSize.height.dp.coerceAtLeast(50.dp)
            AdSizeType.INLINE_ADAPTIVE_BANNER -> bannerAd.adSize.height.dp // Google's automatic height
            AdSizeType.LARGE_BANNER -> 100.dp
            AdSizeType.BANNER -> 50.dp
            AdSizeType.MEDIUM_RECTANGLE -> 250.dp // 300x250 MREC size
            else -> 60.dp
        }
    } else {
        0.dp // FREE THE SPACE when not needed
    }
    
    // ✅ Only render the container when there's actually height to show
    if (adHeight > 0.dp) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(adHeight)
                .background(backgroundColor), // No corner radius - native flat appearance
            contentAlignment = Alignment.Center
        ) {
            // ✅ Enhanced display logic with loading UI support
            val ad = bannerAd ?: return@Box
            
            when {
                ad.isLoaded -> {
                    // ✅ Show actual loaded ad
                    AndroidView(
                        factory = { context ->
                            val adView = adMobDataSource.getAdView(ad.adUnitId)
                            if (adView != null) {
                                // 🔧 CRITICAL: Remove from parent first to prevent "child already has parent" error
                                try {
                                    (adView.parent as? android.view.ViewGroup)?.removeView(adView)
                                } catch (e: Exception) {
                                    Log.w("BannerAdComposable", "⚠️ Could not remove from parent: ${e.message}")
                                }
                                
                                Log.d("BannerAdComposable", "✅ Displaying loaded AdView for ${ad.adUnitId}")
                                Log.d("BannerAdComposable", "📐 AdView size: ${adView.adSize?.width}x${adView.adSize?.height}")
                                adView
                            } else {
                                Log.w("BannerAdComposable", "❌ AdView not found for ${ad.adUnitId}")
                                // Return empty view - we'll handle this gracefully
                                android.view.View(context)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        update = { adView ->
                            Log.d("BannerAdComposable", "🔄 AdView update called for ${ad.adUnitId}")
                            if (ad.adSize.type == AdSizeType.ANCHORED_ADAPTIVE_BANNER) {
                                Log.d("BannerAdComposable", "📱 Anchored adaptive banner displayed with full width")
                            }
                        }
                    )
                }
                loadingContent != null && ad.isLoading -> {
                    // ✅ Show custom loading UI provided by screen
                    loadingContent()
                }
                ad.error != null -> {
                    // ✅ Show error state
                    AdErrorUI(
                        adSize = ad.adSize.type,
                        backgroundColor = Color.Red.copy(alpha = 0.1f),
                        textColor = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    // ✅ Fallback empty state
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
    
    // ✅ Enhanced debug logging for space management
    LaunchedEffect(shouldShowAdSpace, adHeight, loadingContent) {
        when {
            bannerAd == null -> Log.d("BannerAdComposable", "🚫 No ad - FREE SPACE (0dp)")
            bannerAd.isLoading && loadingContent != null -> Log.d("BannerAdComposable", "⏳ Ad loading - SHOW CUSTOM LOADING UI (${adHeight})")
            bannerAd.isLoading && loadingContent == null -> Log.d("BannerAdComposable", "⏳ Ad loading - FREE SPACE (0dp) [No loading UI provided]")
            bannerAd.error != null -> Log.d("BannerAdComposable", "❌ Ad error - SHOW ERROR (${adHeight}): ${bannerAd.error}")
            bannerAd.isLoaded -> Log.d("BannerAdComposable", "✅ Ad loaded - SHOW AD (${adHeight})")
        }
    }
}

// Hilt entry point to access dependencies in Composable
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface AdMobEntryPoint {
    fun adMobDataSource(): AdMobDataSource
}