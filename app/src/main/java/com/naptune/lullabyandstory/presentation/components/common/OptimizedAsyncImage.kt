package com.naptune.lullabyandstory.presentation.components.common

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.RoundedCornersTransformation
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.ui.theme.PrimaryColor

/**
 * ✅ Optimized AsyncImage component with smart loading strategies
 */
@Composable
fun OptimizedAsyncImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    // ✅ Size-based optimization
    targetSize: ImageSize = ImageSize.Medium,
    // ✅ Corner radius for rounded images
    cornerRadius: Dp = 0.dp,
    // ✅ Custom placeholder and error handling
    showPlaceholder: Boolean = true,
    showLoadingIndicator: Boolean = true,
    placeholder: Painter? = null,
    error: Painter? = null,
    // ✅ Performance callbacks
    onSuccess: (() -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null
) {
    // ✅ Handle empty URLs
    if (imageUrl.isBlank()) {
        Log.w("OptimizedAsyncImage", "⚠️ Empty image URL provided")
        Box(
            modifier = modifier.background(
                color = PrimaryColor.copy(alpha = 0.3f),
                shape = if (cornerRadius > 0.dp) RoundedCornerShape(cornerRadius) else RoundedCornerShape(0.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🇼️",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }
        return
    }
    
    val context = LocalContext.current
    val density = LocalDensity.current
    
    // ✅ Smart URL optimization based on target size
    val optimizedUrl = remember(imageUrl, targetSize) {
        generateOptimizedUrl(imageUrl, targetSize)
    }
    
    // ✅ Calculate corner radius in pixels for Coil transformation
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }
    
    // ✅ Track loading state for analytics
    var loadStartTime by remember { mutableLongStateOf(0L) }
    var isLoading by remember { mutableStateOf(true) }
    
    // ✅ Default placeholder
    val defaultPlaceholder = placeholder ?: painterResource(R.drawable.ic_launcher_background)
    val defaultError = error ?: painterResource(R.drawable.ic_launcher_background)
    
    Box(modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(optimizedUrl)
                // ✅ Crossfade animation for smooth loading
                .crossfade(300)
                // ✅ Size optimization - let Coil handle sizing intelligently
                .size(
                    when (targetSize) {
                        ImageSize.Thumbnail -> Size(150, 150)
                        ImageSize.Small -> Size(200, 200)
                        ImageSize.Medium -> Size(300, 300)
                        ImageSize.Large -> Size(600, 600)
                        ImageSize.ExtraLarge -> Size(800, 800)
                        ImageSize.Original -> Size.ORIGINAL
                    }
                )
                // ✅ Cache optimization
                .memoryCacheKey("$optimizedUrl-${targetSize.name}")
                .diskCacheKey("$optimizedUrl-${targetSize.name}")
                // ✅ Corner radius transformation
                .apply {
                    if (cornerRadius > 0.dp) {
                        transformations(RoundedCornersTransformation(cornerRadiusPx))
                    }
                }
                // ✅ Performance monitoring
                .listener(
                    onStart = { 
                        loadStartTime = System.currentTimeMillis()
                        isLoading = true
                        Log.d("OptimizedAsyncImage", "🚀 Started loading: $optimizedUrl")
                    },
                    onSuccess = { _, _ ->
                        val loadTime = System.currentTimeMillis() - loadStartTime
                        isLoading = false
                        Log.d("OptimizedAsyncImage", "✅ Loaded in ${loadTime}ms: $optimizedUrl")
                        onSuccess?.invoke()
                    },
                    onError = { _, result ->
                        val loadTime = System.currentTimeMillis() - loadStartTime
                        isLoading = false
                        Log.e("OptimizedAsyncImage", "❌ Failed after ${loadTime}ms: $optimizedUrl", result.throwable)
                        onError?.invoke(result.throwable)
                    }
                )
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            placeholder = if (showPlaceholder) defaultPlaceholder else null,
            error = defaultError,
            onLoading = { 
                // Loading state handled by overlay
            }
        )
        
        // ✅ Custom loading indicator overlay (FIXED)
        if (showLoadingIndicator && isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = PrimaryColor.copy(alpha = 0.1f),
                        shape = if (cornerRadius > 0.dp) RoundedCornerShape(cornerRadius) else RoundedCornerShape(0.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = PrimaryColor,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

/**
 * ✅ Image size presets for different use cases
 */
enum class ImageSize {
    Thumbnail,    // 150x150 - For small previews
    Small,        // 200x200 - For small cards
    Medium,       // 300x300 - For grid items
    Large,        // 600x600 - For detailed views
    ExtraLarge,   // 800x800 - For full screen
    Original      // Original size - For zoom/download
}

/**
 * ✅ Generate optimized URL based on image size and format
 */
private fun generateOptimizedUrl(originalUrl: String, targetSize: ImageSize): String {
    // ✅ If URL already has query parameters, handle appropriately
    val baseUrl = originalUrl.split("?")[0]
    
    // ✅ Add optimization parameters
    val sizeParams = when (targetSize) {
        ImageSize.Thumbnail -> "w=150&h=150"
        ImageSize.Small -> "w=200&h=200"
        ImageSize.Medium -> "w=300&h=300"
        ImageSize.Large -> "w=600&h=600"
        ImageSize.ExtraLarge -> "w=800&h=800"
        ImageSize.Original -> return originalUrl // No optimization for original
    }
    
    // ✅ Add format and quality parameters
    val formatParams = "f=webp&q=85" // WebP format with 85% quality
    
    // ✅ Combine parameters
    val separator = if (originalUrl.contains("?")) "&" else "?"
    return "$baseUrl$separator$sizeParams&$formatParams"
}

/**
 * ✅ Convenience composables for common use cases
 */

@Composable
fun ThumbnailImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    OptimizedAsyncImage(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        targetSize = ImageSize.Thumbnail,
        cornerRadius = cornerRadius
    )
}

@Composable
fun GridItemImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp
) {
    OptimizedAsyncImage(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        targetSize = ImageSize.Medium,
        cornerRadius = cornerRadius
    )
}

@Composable
fun DetailImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 0.dp
) {
    OptimizedAsyncImage(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        targetSize = ImageSize.Large,
        cornerRadius = cornerRadius
    )
}
