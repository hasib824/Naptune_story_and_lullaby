package com.naptune.lullabyandstory.presentation.components.common.responsive

import android.content.res.Configuration
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive Design System for Neptune App
 *
 * This system provides adaptive UI components that automatically adjust to different screen sizes,
 * densities, and orientations following Material Design guidelines.
 *
 * FUTURE UPGRADE: To use modern WindowSizeClass API, add this dependency:
 * implementation("androidx.compose.material3:material3-window-size-class:$version")
 *
 * Then replace rememberScreenDimensionManager() with WindowSizeClass-based implementation.
 */

/**
 * Screen Size Categories
 */
enum class ScreenSize {
    COMPACT,    // Phones in portrait mode (< 600dp)
    MEDIUM,     // Small tablets (600dp - 840dp)
    EXPANDED    // Large tablets (> 840dp)
}

/**
 * Screen Density Categories (Official Android Documentation)
 * Reference: https://developer.android.com/training/multiscreen/screendensities
 * Scaling ratio: 3:4:6:8:12:16
 */
enum class ScreenDensity {
    LDPI,       // ~120 dpi (Low density)
    MDPI,       // ~160 dpi (Medium density - baseline)
    TVDPI,      // ~213 dpi (TV density - between mdpi and hdpi)
    HDPI,       // ~240 dpi (High density)
    XHDPI,      // ~320 dpi (Extra-high density)
    XXHDPI,     // ~480 dpi (Extra-extra-high density)
    XXXHDPI     // ~640 dpi (Extra-extra-extra-high density)
}

/**
 * Screen Orientation
 */
enum class ScreenOrientation {
    PORTRAIT,
    LANDSCAPE
}

/**
 * Device Type based on screen characteristics
 */
enum class DeviceType {
    PHONE,
    TABLET,
    FOLDABLE
}

/**
 * Enhanced Device Profile combining Device Type + Screen Density
 * This provides precise control for different device+density combinations
 */
enum class DeviceProfile {
    // Phone profiles
    PHONE_LDPI,     // Old phones, basic devices (~120 dpi)
    PHONE_MDPI,     // Standard phones (~160 dpi)
    PHONE_HDPI,     // Modern phones (~240 dpi)
    PHONE_XHDPI,    // High-end phones (~320 dpi)
    PHONE_XXHDPI,   // Premium phones (~480 dpi)
    PHONE_XXXHDPI,  // Flagship phones (~640+ dpi)

    // Tablet profiles
    TABLET_MDPI,    // Basic tablets (~160 dpi)
    TABLET_HDPI,    // Standard tablets (~240 dpi)
    TABLET_XHDPI,   // High-res tablets (~320 dpi)
    TABLET_XXHDPI,  // Premium tablets (~480 dpi)
    TABLET_XXXHDPI, // Flagship tablets (~640+ dpi)

    // Foldable profiles
    FOLDABLE_HDPI,  // Standard foldables (~240 dpi)
    FOLDABLE_XHDPI, // High-res foldables (~320 dpi)
    FOLDABLE_XXHDPI // Premium foldables (~480+ dpi)
}

/**
 * Adaptive dimensions based on device profile
 */
data class AdaptiveDimensions(
    val paddingSmall: Dp,
    val paddingMedium: Dp,
    val paddingLarge: Dp,
    val textSizeSmall: Dp,
    val textSizeMedium: Dp,
    val textSizeLarge: Dp,
    val iconSizeSmall: Dp,
    val iconSizeMedium: Dp,
    val iconSizeLarge: Dp,
    val cardCornerRadius: Dp,
    val gridColumns: Int
)

/**
 * Main Screen Dimension Manager
 * Provides adaptive dimensions for different screen sizes and densities
 *
 * Note: This uses Configuration for compatibility. For modern apps, consider adding
 * androidx.compose.material3:material3-window-size-class dependency and using WindowSizeClass
 */
@Composable
fun rememberScreenDimensionManager(): ScreenDimensionManager {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Use configuration values but optimize the remember dependencies
    // This addresses the IDE warning while maintaining compatibility
    return remember(
        configuration.screenWidthDp,
        configuration.screenHeightDp,
        configuration.orientation,
        configuration.densityDpi
    ) {
        ScreenDimensionManager(
            screenWidthDp = configuration.screenWidthDp,
            screenHeightDp = configuration.screenHeightDp,
            densityDpi = configuration.densityDpi,
            density = density.density,
            orientation = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ScreenOrientation.LANDSCAPE
            } else {
                ScreenOrientation.PORTRAIT
            }
        )
    }
}

/**
 * Screen Dimension Manager Class
 */
data class ScreenDimensionManager(
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val densityDpi: Int,
    val density: Float,
    val orientation: ScreenOrientation
) {

    /**
     * Get screen size category based on Material Design breakpoints
     * - Compact: < 600dp (phones)
     * - Medium: 600-840dp (small tablets, large phones in landscape)
     * - Expanded: > 840dp (large tablets)
     */
    val screenSize: ScreenSize = when {
        screenWidthDp < 600 -> ScreenSize.COMPACT
        screenWidthDp < 840 -> ScreenSize.MEDIUM
        else -> ScreenSize.EXPANDED
    }

    /**
     * Get screen density category based on official Android ranges
     * Reference: https://developer.android.com/training/multiscreen/screendensities
     */
    val screenDensity: ScreenDensity = when {

        densityDpi <= 140 -> {
            println("densityDpi: $densityDpi")
            ScreenDensity.LDPI
        }     // ~120 dpi
        densityDpi <= 200 -> {
            println("densityDpi: $densityDpi mdpi")
            ScreenDensity.MDPI
        }    // ~160 dpi
        densityDpi <= 280 -> {
            println("densityDpi: $densityDpi hdpi")
            ScreenDensity.HDPI
        }    // ~240 dpi
        densityDpi <= 400 -> {
            println("densityDpi: $densityDpi x")
            ScreenDensity.XHDPI
        }    // ~320 dpi
        densityDpi <= 560 -> {
            println("densityDpi: $densityDpi  xx  hey")
            ScreenDensity.XXHDPI
        }   // ~480 dpi
        else -> ScreenDensity.XXXHDPI
        // ~640 dpi and above
    }

    /**
     * Enhanced device type detection with multiple factors
     */
    val deviceType: DeviceType = detectDeviceType()

    /**
     * Combined device profile (Device Type + Screen Density)
     */
    val deviceProfile: DeviceProfile = combineDeviceAndDensity()

    /**
     * Adaptive dimensions based on device profile
     */
    val adaptiveDimensions: AdaptiveDimensions = createAdaptiveDimensions()

    /**
     * Better device detection with multiple factors
     */
    private fun detectDeviceType(): DeviceType {
        val smallestWidth = minOf(screenWidthDp, screenHeightDp)
        val largestWidth = maxOf(screenWidthDp, screenHeightDp)
        val aspectRatio = largestWidth.toFloat() / smallestWidth

        return when {
            // Phone: Width < 600dp
            screenWidthDp < 600 -> {
                Log.e("ScreenDimensionManager", "Phone 1")
                DeviceType.PHONE }

            // Tablet: Width >= 600dp
            screenWidthDp >= 600 -> {
                // Check for foldable characteristics
                val aspectRatio = if (orientation == ScreenOrientation.PORTRAIT) {
                    screenHeightDp.toFloat() / screenWidthDp.toFloat()
                } else {

                    screenWidthDp.toFloat() / screenHeightDp.toFloat()
                }

                // Foldables typically have extreme aspect ratios when unfolded
                if (aspectRatio > 2.1f && screenWidthDp > 600) {
                    Log.e("ScreenDimensionManager", "FOLDABLE")
                    DeviceType.FOLDABLE
                } else {
                    Log.e("ScreenDimensionManager", "Tablet")
                    DeviceType.TABLET
                }
            }

            else -> {
                Log.e("ScreenDimensionManager", "Phone")
                DeviceType.PHONE
            }
        }
    }

    /**
     * Combine device type + density for precise profiling
     */
     fun combineDeviceAndDensity(): DeviceProfile {
        val profile = when (deviceType) {
            DeviceType.PHONE -> when (screenDensity) {
                ScreenDensity.LDPI -> DeviceProfile.PHONE_LDPI
                ScreenDensity.MDPI -> DeviceProfile.PHONE_MDPI
                ScreenDensity.TVDPI, ScreenDensity.HDPI -> DeviceProfile.PHONE_HDPI
                ScreenDensity.XHDPI -> DeviceProfile.PHONE_XHDPI
                ScreenDensity.XXHDPI -> DeviceProfile.PHONE_XXHDPI
                ScreenDensity.XXXHDPI -> DeviceProfile.PHONE_XXXHDPI
            }

            DeviceType.TABLET -> when (screenDensity) {
                ScreenDensity.LDPI, ScreenDensity.MDPI, ScreenDensity.TVDPI -> DeviceProfile.TABLET_MDPI
                ScreenDensity.HDPI -> DeviceProfile.TABLET_HDPI
                ScreenDensity.XHDPI -> DeviceProfile.TABLET_XHDPI
                ScreenDensity.XXHDPI -> DeviceProfile.TABLET_XXHDPI
                ScreenDensity.XXXHDPI -> DeviceProfile.TABLET_XXXHDPI
            }

            DeviceType.FOLDABLE -> when (screenDensity) {
                ScreenDensity.LDPI, ScreenDensity.MDPI, ScreenDensity.TVDPI, ScreenDensity.HDPI -> DeviceProfile.FOLDABLE_HDPI
                ScreenDensity.XHDPI -> DeviceProfile.FOLDABLE_XHDPI
                ScreenDensity.XXHDPI, ScreenDensity.XXXHDPI -> DeviceProfile.FOLDABLE_XXHDPI
            }
        }

        println("🎯 Device Profile: $ profile (${deviceType} + ${screenDensity})")
        return profile
    }

    /**
     * Create adaptive dimensions based on device profile
     */
    private fun createAdaptiveDimensions(): AdaptiveDimensions {
        return when (deviceProfile) {
            // Phone profiles - optimized for smaller screens
            DeviceProfile.PHONE_LDPI -> AdaptiveDimensions(
                paddingSmall = 4.dp, paddingMedium = 8.dp, paddingLarge = 12.dp,
                textSizeSmall = 10.dp, textSizeMedium = 14.dp, textSizeLarge = 20.dp,
                iconSizeSmall = 12.dp, iconSizeMedium = 18.dp, iconSizeLarge = 32.dp,
                cardCornerRadius = 6.dp,
                gridColumns = if (orientation == ScreenOrientation.PORTRAIT) 2 else 3
            )

            DeviceProfile.PHONE_MDPI -> AdaptiveDimensions(
                paddingSmall = 6.dp, paddingMedium = 12.dp, paddingLarge = 18.dp,
                textSizeSmall = 11.dp, textSizeMedium = 15.dp, textSizeLarge = 22.dp,
                iconSizeSmall = 14.dp, iconSizeMedium = 20.dp, iconSizeLarge = 36.dp,
                cardCornerRadius = 8.dp,
                gridColumns = if (orientation == ScreenOrientation.PORTRAIT) 2 else 3
            )

            DeviceProfile.PHONE_HDPI -> AdaptiveDimensions(
                paddingSmall = 8.dp, paddingMedium = 16.dp, paddingLarge = 24.dp,
                textSizeSmall = 12.dp, textSizeMedium = 16.dp, textSizeLarge = 24.dp,
                iconSizeSmall = 16.dp, iconSizeMedium = 24.dp, iconSizeLarge = 48.dp,
                cardCornerRadius = 8.dp,
                gridColumns = if (orientation == ScreenOrientation.PORTRAIT) 2 else 3
            )

            DeviceProfile.PHONE_XHDPI -> AdaptiveDimensions(
                paddingSmall = 8.dp, paddingMedium = 16.dp, paddingLarge = 24.dp,
                textSizeSmall = 12.dp, textSizeMedium = 16.dp, textSizeLarge = 24.dp,
                iconSizeSmall = 16.dp, iconSizeMedium = 24.dp, iconSizeLarge = 48.dp,
                cardCornerRadius = 8.dp,
                gridColumns = if (orientation == ScreenOrientation.PORTRAIT) 2 else 3
            )

            DeviceProfile.PHONE_XXHDPI -> AdaptiveDimensions(
                paddingSmall = 8.dp, paddingMedium = 16.dp, paddingLarge = 24.dp,
                textSizeSmall = 12.dp, textSizeMedium = 16.dp, textSizeLarge = 24.dp,
                iconSizeSmall = 16.dp, iconSizeMedium = 24.dp, iconSizeLarge = 48.dp,
                cardCornerRadius = 8.dp,
                gridColumns = if (orientation == ScreenOrientation.PORTRAIT) 2 else 4
            )

            DeviceProfile.PHONE_XXXHDPI -> AdaptiveDimensions(
                paddingSmall = 10.dp, paddingMedium = 18.dp, paddingLarge = 28.dp,
                textSizeSmall = 13.dp, textSizeMedium = 17.dp, textSizeLarge = 26.dp,
                iconSizeSmall = 18.dp, iconSizeMedium = 26.dp, iconSizeLarge = 52.dp,
                cardCornerRadius = 10.dp,
                gridColumns = if (orientation == ScreenOrientation.PORTRAIT) 2 else 4
            )

            // Tablet profiles - optimized for larger screens
            DeviceProfile.TABLET_MDPI -> AdaptiveDimensions(
                paddingSmall = 12.dp, paddingMedium = 20.dp, paddingLarge = 32.dp,
                textSizeSmall = 13.dp, textSizeMedium = 17.dp, textSizeLarge = 26.dp,
                iconSizeSmall = 18.dp, iconSizeMedium = 26.dp, iconSizeLarge = 52.dp,
                cardCornerRadius = 12.dp,
                gridColumns = if (orientation == ScreenOrientation.PORTRAIT) 3 else 4
            )

            DeviceProfile.TABLET_HDPI -> AdaptiveDimensions(
                paddingSmall = 16.dp, paddingMedium = 24.dp, paddingLarge = 40.dp,
                textSizeSmall = 14.dp, textSizeMedium = 18.dp, textSizeLarge = 28.dp,
                iconSizeSmall = 20.dp, iconSizeMedium = 28.dp, iconSizeLarge = 56.dp,
                cardCornerRadius = 12.dp,
                gridColumns = if (orientation == ScreenOrientation.PORTRAIT) 3 else 4
            )

            DeviceProfile.TABLET_XHDPI -> AdaptiveDimensions(
                paddingSmall = 16.dp, paddingMedium = 24.dp, paddingLarge = 40.dp,
                textSizeSmall = 14.dp, textSizeMedium = 18.dp, textSizeLarge = 28.dp,
                iconSizeSmall = 20.dp, iconSizeMedium = 28.dp, iconSizeLarge = 56.dp,
                cardCornerRadius = 12.dp,
                gridColumns = if (orientation == ScreenOrientation.PORTRAIT) 4 else 5
            )

            DeviceProfile.TABLET_XXHDPI -> AdaptiveDimensions(
                paddingSmall = 18.dp, paddingMedium = 28.dp, paddingLarge = 44.dp,
                textSizeSmall = 15.dp, textSizeMedium = 19.dp, textSizeLarge = 30.dp,
                iconSizeSmall = 22.dp, iconSizeMedium = 30.dp, iconSizeLarge = 60.dp,
                cardCornerRadius = 14.dp,
                gridColumns = if (orientation == ScreenOrientation.PORTRAIT) 4 else 5
            )

            DeviceProfile.TABLET_XXXHDPI -> AdaptiveDimensions(
                paddingSmall = 20.dp, paddingMedium = 32.dp, paddingLarge = 48.dp,
                textSizeSmall = 16.dp, textSizeMedium = 20.dp, textSizeLarge = 32.dp,
                iconSizeSmall = 24.dp, iconSizeMedium = 32.dp, iconSizeLarge = 64.dp,
                cardCornerRadius = 16.dp,
                gridColumns = if (orientation == ScreenOrientation.PORTRAIT) 4 else 6
            )

            // Foldable profiles - balanced approach
            DeviceProfile.FOLDABLE_HDPI -> AdaptiveDimensions(
                paddingSmall = 12.dp, paddingMedium = 20.dp, paddingLarge = 32.dp,
                textSizeSmall = 13.dp, textSizeMedium = 17.dp, textSizeLarge = 26.dp,
                iconSizeSmall = 18.dp, iconSizeMedium = 26.dp, iconSizeLarge = 52.dp,
                cardCornerRadius = 10.dp,
                gridColumns = 3
            )

            DeviceProfile.FOLDABLE_XHDPI -> AdaptiveDimensions(
                paddingSmall = 14.dp, paddingMedium = 22.dp, paddingLarge = 36.dp,
                textSizeSmall = 14.dp, textSizeMedium = 18.dp, textSizeLarge = 28.dp,
                iconSizeSmall = 20.dp, iconSizeMedium = 28.dp, iconSizeLarge = 56.dp,
                cardCornerRadius = 12.dp,
                gridColumns = 4
            )

            DeviceProfile.FOLDABLE_XXHDPI -> AdaptiveDimensions(
                paddingSmall = 16.dp, paddingMedium = 24.dp, paddingLarge = 40.dp,
                textSizeSmall = 15.dp, textSizeMedium = 19.dp, textSizeLarge = 30.dp,
                iconSizeSmall = 22.dp, iconSizeMedium = 30.dp, iconSizeLarge = 60.dp,
                cardCornerRadius = 14.dp,
                gridColumns = 5
            )
        }
    }

    /**
     * Responsive Dimensions
     */

    // Enhanced adaptive padding values based on device profile
    val basePaddingSmall: Dp = adaptiveDimensions.paddingSmall
    val basePaddingMedium: Dp = adaptiveDimensions.paddingMedium
    val basePaddingLarge: Dp = adaptiveDimensions.paddingLarge

    // Enhanced adaptive text sizes based on device profile
    val textSizeSmall: Dp = adaptiveDimensions.textSizeSmall
    val textSizeMedium: Dp = adaptiveDimensions.textSizeMedium
    val textSizeLarge: Dp = adaptiveDimensions.textSizeLarge

    // Card dimensions - adjusted for official density categories
    val cardElevation: Dp = when (screenDensity) {
        ScreenDensity.LDPI -> 1.dp
        ScreenDensity.MDPI -> 2.dp
        ScreenDensity.TVDPI -> 3.dp
        ScreenDensity.HDPI -> 4.dp
        ScreenDensity.XHDPI -> 5.dp
        ScreenDensity.XXHDPI -> 6.dp
        ScreenDensity.XXXHDPI -> 8.dp
    }

    val cardCornerRadius: Dp = adaptiveDimensions.cardCornerRadius

    // Button dimensions
    val buttonHeight: Dp = when (screenSize) {
        ScreenSize.COMPACT -> 48.dp
        ScreenSize.MEDIUM -> 52.dp
        ScreenSize.EXPANDED -> 56.dp
    }

    val buttonMinWidth: Dp = when (screenSize) {
        ScreenSize.COMPACT -> 64.dp
        ScreenSize.MEDIUM -> 80.dp
        ScreenSize.EXPANDED -> 96.dp
    }

    // Enhanced adaptive icon sizes based on device profile
    val iconSizeSmall: Dp = adaptiveDimensions.iconSizeSmall
    val iconSizeMedium: Dp = adaptiveDimensions.iconSizeMedium
    val iconSizeLarge: Dp = adaptiveDimensions.iconSizeLarge

    // Enhanced adaptive grid columns based on device profile
    val gridColumns: Int = adaptiveDimensions.gridColumns

    // Smart grid columns with device+density awareness
    val smartGridColumns: Int = gridColumns

    // Maximum content width (for tablets)
    val maxContentWidth: Dp = when (screenSize) {
        ScreenSize.COMPACT -> Dp.Unspecified
        ScreenSize.MEDIUM -> 720.dp
        ScreenSize.EXPANDED -> 960.dp
    }

    // Responsive spacing based on screen density
    // Uses official Android scaling ratio: 3:4:6:8:12:16
    fun scaledSize(baseDp: Dp): Dp = baseDp * densityMultiplier

    private val densityMultiplier: Float = when (screenDensity) {
        ScreenDensity.LDPI -> 0.75f      // 3/4 ratio
        ScreenDensity.MDPI -> 1.0f       // 4/4 baseline
        ScreenDensity.TVDPI -> 1.33f     // ~5.3/4 ratio
        ScreenDensity.HDPI -> 1.5f       // 6/4 ratio
        ScreenDensity.XHDPI -> 2.0f      // 8/4 ratio
        ScreenDensity.XXHDPI -> 3.0f     // 12/4 ratio
        ScreenDensity.XXXHDPI -> 4.0f    // 16/4 ratio
    }

    /**
     * Utility Functions
     */

    fun isCompact() = screenSize == ScreenSize.COMPACT
    fun isMedium() = screenSize == ScreenSize.MEDIUM
    fun isExpanded() = screenSize == ScreenSize.EXPANDED

    fun isPhone() = deviceType == DeviceType.PHONE
    fun isTablet() = deviceType == DeviceType.TABLET
    fun isFoldable() = deviceType == DeviceType.FOLDABLE

    // Device profile utility functions
    fun isPhoneLowDensity() = deviceProfile in listOf(DeviceProfile.PHONE_LDPI, DeviceProfile.PHONE_MDPI)
    fun isPhoneHighDensity() = deviceProfile in listOf(DeviceProfile.PHONE_XXHDPI, DeviceProfile.PHONE_XXXHDPI)
    fun isTabletHighRes() = deviceProfile in listOf(DeviceProfile.TABLET_XHDPI, DeviceProfile.TABLET_XXHDPI, DeviceProfile.TABLET_XXXHDPI)
    fun isPremiumDevice() = deviceProfile.name.contains("XXHDPI") || deviceProfile.name.contains("XXXHDPI")

    fun isPortrait() = orientation == ScreenOrientation.PORTRAIT
    fun isLandscape() = orientation == ScreenOrientation.LANDSCAPE

    /**
     * Debug information
     */
    fun getDebugInfo(): String = """
        📱 Enhanced Screen Info:
        - Size: $screenWidthDp x $screenHeightDp dp
        - Category: $screenSize
        - Device: $deviceType
        - Orientation: $orientation
        - Density: $densityDpi dpi ($screenDensity)
        - Device Profile: $deviceProfile
        - Detection: Multi-factor (Size + Density + Aspect Ratio)
        - Grid Columns: $gridColumns
        - Max Content Width: $maxContentWidth
        - Adaptive Padding: ${adaptiveDimensions.paddingSmall}/${adaptiveDimensions.paddingMedium}/${adaptiveDimensions.paddingLarge}
        - Adaptive Text: ${adaptiveDimensions.textSizeSmall}/${adaptiveDimensions.textSizeMedium}/${adaptiveDimensions.textSizeLarge}
        - Adaptive Icons: ${adaptiveDimensions.iconSizeSmall}/${adaptiveDimensions.iconSizeMedium}/${adaptiveDimensions.iconSizeLarge}
    """.trimIndent()
}