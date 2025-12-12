package com.naptune.lullabyandstory.presentation.components.common.responsive

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naptune.lullabyandstory.domain.model.AdSizeType

/**
 * Responsive Modifier Extensions
 */

/**
 * Apply responsive padding based on screen size
 */


data class ResponsiveStylesOfAudioplayerScreen(
    val adSizeType: AdSizeType,
    val imageSize: Float,
    val nowPlayingTopMargin: Dp = 60.dp,
    val categoryTitleBottomMargin: Dp = 48.dp,
    val volumePorionMarginBottom: Dp = 32.dp,
    val favouritePortionMarginBottom: Dp = 40.dp,
    val playPauseIconSize: Dp = 0.dp,
    val nextPrevIconSize: Dp = 0.dp,
    val favouritePortionIconsSize: Dp = 0.dp,
    val nowPlayingTextSize: TextUnit = 20.sp,
    val nowPlayingBackIconSize: Dp = 0.dp,
    val pregressAndTotalDurationFontSize: TextUnit = 16.sp,

    )

@Composable
fun getAudioplayerScreenResponisiveSizes(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): ResponsiveStylesOfAudioplayerScreen {
    val combineDeviceAndDensity = dimensionManager.combineDeviceAndDensity()
    println("DeviceProfile Combine Density ${combineDeviceAndDensity}")
    when (combineDeviceAndDensity) {
        /* DeviceProfile.PHONE_LDPI -> TODO()
         DeviceProfile.PHONE_MDPI -> TODO() */
        DeviceProfile.PHONE_HDPI -> {
            println("DeviceProfile DeviceProfile.PHONE_HDPI ${dimensionManager.screenHeightDp}")
            return ResponsiveStylesOfAudioplayerScreen(
                AdSizeType.ANCHORED_ADAPTIVE_BANNER,
                0.34f,
                40.dp,
                24.dp,
                24.dp,
                28.dp,
                playPauseIconSize = 44.dp,
                nextPrevIconSize = 32.dp,
                favouritePortionIconsSize = 36.dp,
                nowPlayingTextSize = 18.sp,
                nowPlayingBackIconSize = 28.dp,
                14.sp
            )
        }

        DeviceProfile.PHONE_XHDPI -> {
            println("DeviceProfile DeviceProfile.PHONE_XHDPI  ${dimensionManager.screenHeightDp}")
            if (dimensionManager.screenHeightDp < 750) {
                return ResponsiveStylesOfAudioplayerScreen(
                    AdSizeType.ANCHORED_ADAPTIVE_BANNER,
                    0.40f,
                    pregressAndTotalDurationFontSize = 14.sp,
                    favouritePortionMarginBottom = 32.dp,
                    playPauseIconSize = 48.dp,
                    nextPrevIconSize = 36.dp,
                    favouritePortionIconsSize = 40.dp,
                    nowPlayingTextSize = 18.sp,
                    nowPlayingBackIconSize = 30.dp,
                )
            } else if (dimensionManager.screenHeightDp < 800) {
                return ResponsiveStylesOfAudioplayerScreen(
                    AdSizeType.MEDIUM_RECTANGLE,
                    0.30f,
                    favouritePortionMarginBottom = 32.dp
                )
            } else {
                return ResponsiveStylesOfAudioplayerScreen(AdSizeType.MEDIUM_RECTANGLE, 0.30f)
            }

        }

        DeviceProfile.PHONE_XXHDPI -> {

            println("DeviceProfile DeviceProfile.PHONE_XXHDPI  ${dimensionManager.screenHeightDp}")
            if (dimensionManager.screenHeightDp < 750) {
                return ResponsiveStylesOfAudioplayerScreen(
                    AdSizeType.ANCHORED_ADAPTIVE_BANNER,
                    0.36f,
                    favouritePortionMarginBottom = 32.dp
                )
            } else if (dimensionManager.screenHeightDp < 800) {
                return ResponsiveStylesOfAudioplayerScreen(
                    AdSizeType.MEDIUM_RECTANGLE,
                    0.30f,
                    favouritePortionMarginBottom = 32.dp
                )
            } else {
                return ResponsiveStylesOfAudioplayerScreen(AdSizeType.MEDIUM_RECTANGLE, 0.32f)
            }
        }

        else -> return ResponsiveStylesOfAudioplayerScreen(AdSizeType.MEDIUM_RECTANGLE, 0.32f)
        /* DeviceProfile.PHONE_XXHDPI -> TODO()
         DeviceProfile.PHONE_XXXHDPI -> TODO()
         DeviceProfile.TABLET_MDPI -> TODO()
         DeviceProfile.TABLET_HDPI -> TODO()
         DeviceProfile.TABLET_XHDPI -> TODO()
         DeviceProfile.TABLET_XXHDPI -> TODO()
         DeviceProfile.TABLET_XXXHDPI -> TODO()
         DeviceProfile.FOLDABLE_HDPI -> TODO()
         DeviceProfile.FOLDABLE_XHDPI -> TODO()
         DeviceProfile.FOLDABLE_XXHDPI -> TODO()*/
    }
}

data class ResponsiveStylesOfTimerModal(
    val radioButtonSize: Dp = 24.dp,
    val titleFontSize: TextUnit = 24.sp,
    val fontSize: TextUnit = 16.sp,
    val spacing: Dp = 16.dp,
    val itemMarginTop: Dp = 20.dp,
    val setBtnMarginTop: Dp = 32.dp,
    val setBtnHeight: Dp = 48.dp,
    val contentHorizontalPadding: Dp = 32.dp,
    val contentVerticalPadding: Dp = 20.dp,
    val wheelTimePickerContentHeight: Dp = 180.dp,
    val hhMMPaddingTop: Dp = 32.dp
)

@Composable
fun getTimerModalResponsiveSizes(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): ResponsiveStylesOfTimerModal {
    val combineDeviceAndDensity = dimensionManager.combineDeviceAndDensity()

    when (combineDeviceAndDensity) {
        DeviceProfile.PHONE_LDPI -> TODO()
        DeviceProfile.PHONE_MDPI -> TODO()
        DeviceProfile.PHONE_HDPI -> {
            return ResponsiveStylesOfTimerModal(
                20.dp, titleFontSize = 20.sp, fontSize = 14.sp, 10.dp,
                itemMarginTop = 16.dp, setBtnMarginTop = 24.dp, setBtnHeight = 36.dp, 24.dp, 16.dp,
                128.dp, hhMMPaddingTop = 24.dp
            )
        }


        DeviceProfile.PHONE_XHDPI -> {
            return ResponsiveStylesOfTimerModal(
                22.dp, titleFontSize = 20.sp, fontSize = 16.sp, 16.dp,
                itemMarginTop = 20.dp, setBtnMarginTop = 24.dp, setBtnHeight = 40.dp, 32.dp, 20.dp,
                160.dp, hhMMPaddingTop = 24.dp
            )
        }

        else -> return ResponsiveStylesOfTimerModal()
        /*  DeviceProfile.PHONE_XXHDPI -> TODO()
          DeviceProfile.PHONE_XXXHDPI -> TODO()
          DeviceProfile.TABLET_MDPI -> TODO()
          DeviceProfile.TABLET_HDPI -> TODO()
          DeviceProfile.TABLET_XHDPI -> TODO()
          DeviceProfile.TABLET_XXHDPI -> TODO()
          DeviceProfile.TABLET_XXXHDPI -> TODO()
          DeviceProfile.FOLDABLE_HDPI -> TODO()
          DeviceProfile.FOLDABLE_XHDPI -> TODO()
          DeviceProfile.FOLDABLE_XXHDPI -> TODO()*/
    }

}


data class ResponsiveStylesOfStoryManagerScreen(
    val imageSize: Float,
    val titleTopMargin: Dp,
    val horizontalDeviderBottomMargin: Dp,
    val limitedStorySize: Int,
    val btnPadding: Dp = 12.dp,
    val storyTitleFontSize: TextUnit = 20.sp,
    val storyBodyFontSize: TextUnit = 16.sp,
    val horizontalLinePadding: Dp = 8.dp,
    val miniAudioControllerSize: Dp = 64.dp,
    val readStoryMarginBottom: Dp = 104.dp

)

@Composable
fun getAudioManagerScreenResponisiveSizes(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): ResponsiveStylesOfStoryManagerScreen {
    val combineDeviceAndDensity = dimensionManager.combineDeviceAndDensity()
    when (combineDeviceAndDensity) {
        /* DeviceProfile.PHONE_LDPI -> TODO()
         DeviceProfile.PHONE_MDPI -> TODO()*/
        DeviceProfile.PHONE_HDPI -> {
            return ResponsiveStylesOfStoryManagerScreen(
                0.24f,
                0.dp,
                16.dp,
                152,
                8.dp,
                18.sp,
                14.sp,
                horizontalLinePadding = 6.dp,
                miniAudioControllerSize = 52.dp,
                readStoryMarginBottom = 82.dp
            )
        }

        DeviceProfile.PHONE_XHDPI -> {

            if (dimensionManager.screenHeightDp < 700) {
                return ResponsiveStylesOfStoryManagerScreen(
                    0.26f, 0.dp,
                    24.dp, 165, 10.dp, readStoryMarginBottom = 100.dp
                )
            } else {
                return ResponsiveStylesOfStoryManagerScreen(0.30f, 16.dp, 40.dp, 210)
            }
        }

        DeviceProfile.PHONE_XXHDPI -> {
            if (dimensionManager.screenHeightDp < 750) {
                return ResponsiveStylesOfStoryManagerScreen(
                    0.26f, 0.dp,
                    24.dp, 165, 10.dp, readStoryMarginBottom = 100.dp
                )
            } else return ResponsiveStylesOfStoryManagerScreen(0.32f, 16.dp, 40.dp, 210)
        }

        else -> return ResponsiveStylesOfStoryManagerScreen(0.32f, 16.dp, 40.dp, 210)
        /* DeviceProfile.PHONE_XXXHDPI -> TODO()
         DeviceProfile.TABLET_MDPI -> TODO()
         DeviceProfile.TABLET_HDPI -> TODO()
         DeviceProfile.TABLET_XHDPI -> TODO()
         DeviceProfile.TABLET_XXHDPI -> TODO()
         DeviceProfile.TABLET_XXXHDPI -> TODO()
         DeviceProfile.FOLDABLE_HDPI -> TODO()
         DeviceProfile.FOLDABLE_XHDPI -> TODO()
         DeviceProfile.FOLDABLE_XXHDPI -> TODO()*/
    }
}


data class ResponsiveStylesOfProfileScreen(
    val pageTitleFontSize: TextUnit = 20.sp,
    val pageTitleBottomMargin: Dp = 24.dp,
    val userNameFontSize: TextUnit = 20.sp,
    val unlockPremiumFontSize: TextUnit = 20.sp,

    val imageSize: Dp = 72.dp,
    val horizontalDevidertopMargin: Dp = 24.dp,
    val firstItemTopMargin: Dp = 16.dp,
    val profileitemHorizontalPadding: Dp = 8.dp,
    val profileitemVerticalPadding: Dp = 18.dp
)

fun getProfileScreenResponsiveStyles(dimensionManager: ScreenDimensionManager): ResponsiveStylesOfProfileScreen {
    val combineDeviceAndDensity = dimensionManager.combineDeviceAndDensity()

    when (combineDeviceAndDensity) {
        /*DeviceProfile.PHONE_LDPI -> {}
        DeviceProfile.PHONE_MDPI -> {}*/
        DeviceProfile.PHONE_HDPI -> {
            return ResponsiveStylesOfProfileScreen(
                pageTitleFontSize = 18.sp, 20.dp, 16.sp,
                16.sp, 72.dp, 16.dp, 12.dp, 8.dp, 18.dp
            )
        }

        DeviceProfile.PHONE_XHDPI -> {
            return ResponsiveStylesOfProfileScreen(
                pageTitleFontSize = 18.sp, 24.dp, 18.sp,
                18.sp, 72.dp, 20.dp, 14.dp, 8.dp, 18.dp
            )
        }

        DeviceProfile.PHONE_XXHDPI -> {
            println("densityDpi xhdpi enter")
            if (dimensionManager.screenHeightDp < 750) {
                return ResponsiveStylesOfProfileScreen(
                    pageTitleFontSize = 18.sp, 24.dp, 18.sp,
                    18.sp, 72.dp, 20.dp, 14.dp, 8.dp, 18.dp
                )
            }
            return ResponsiveStylesOfProfileScreen()
        }

        else -> return ResponsiveStylesOfProfileScreen()
        /* DeviceProfile.PHONE_XXXHDPI -> {  return ResponsiveStylesOfProfileScreen() }
         DeviceProfile.TABLET_MDPI -> {}
         DeviceProfile.TABLET_HDPI -> {}
         DeviceProfile.TABLET_XHDPI -> {}
         DeviceProfile.TABLET_XXHDPI -> {}
         DeviceProfile.TABLET_XXXHDPI -> {}
         DeviceProfile.FOLDABLE_HDPI -> {}
         DeviceProfile.FOLDABLE_XHDPI -> {}
         DeviceProfile.FOLDABLE_XXHDPI -> {}*/

    }
}

@Composable
fun Modifier.responsivePadding(
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager(),
    horizontal: ResponsivePaddingSize = ResponsivePaddingSize.MEDIUM,
    vertical: ResponsivePaddingSize = ResponsivePaddingSize.MEDIUM
): Modifier = this.padding(
    horizontal = horizontal.getDp(dimensionManager),
    vertical = vertical.getDp(dimensionManager)
)

/**
 * Apply responsive padding with individual sides
 */
@Composable
fun Modifier.responsivePadding(
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager(),
    start: ResponsivePaddingSize = ResponsivePaddingSize.NONE,
    top: ResponsivePaddingSize = ResponsivePaddingSize.NONE,
    end: ResponsivePaddingSize = ResponsivePaddingSize.NONE,
    bottom: ResponsivePaddingSize = ResponsivePaddingSize.NONE
): Modifier = this.padding(
    start = start.getDp(dimensionManager),
    top = top.getDp(dimensionManager),
    end = end.getDp(dimensionManager),
    bottom = bottom.getDp(dimensionManager)
)

/**
 * Apply responsive width constraint for tablets
 */
@Composable
fun Modifier.responsiveWidth(
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()
): Modifier = when {
    dimensionManager.maxContentWidth != Dp.Unspecified -> this.widthIn(max = dimensionManager.maxContentWidth)
    else -> this.fillMaxWidth()
}

/**
 * Apply responsive height based on screen size
 */
@Composable
fun Modifier.responsiveHeight(
    compactHeight: Dp,
    mediumHeight: Dp = compactHeight * 1.2f,
    expandedHeight: Dp = compactHeight * 1.5f,
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()
): Modifier = this.height(
    when (dimensionManager.screenSize) {
        ScreenSize.COMPACT -> compactHeight
        ScreenSize.MEDIUM -> mediumHeight
        ScreenSize.EXPANDED -> expandedHeight
    }
)

/**
 * Apply responsive size (both width and height)
 */
@Composable
fun Modifier.responsiveSize(
    compactSize: Dp,
    mediumSize: Dp = compactSize * 1.2f,
    expandedSize: Dp = compactSize * 1.5f,
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()
): Modifier = this.size(
    when (dimensionManager.screenSize) {
        ScreenSize.COMPACT -> compactSize
        ScreenSize.MEDIUM -> mediumSize
        ScreenSize.EXPANDED -> expandedSize
    }
)

/**
 * Responsive Padding Size Enum
 */
enum class ResponsivePaddingSize {
    NONE,
    SMALL,
    MEDIUM,
    LARGE;

    @Composable
    fun getDp(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (this) {
            NONE -> 0.dp
            SMALL -> dimensionManager.basePaddingSmall
            MEDIUM -> dimensionManager.basePaddingMedium
            LARGE -> dimensionManager.basePaddingLarge
        }
}

/**
 * Comprehensive Responsive Text Styles with TextUnit support
 */
object ResponsiveTextStyles {

    // ============ MATERIAL 3 TEXT STYLES ============

    @Composable
    fun displayLarge(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 57.sp
            ScreenSize.MEDIUM -> 64.sp
            ScreenSize.EXPANDED -> 72.sp
        }
        return MaterialTheme.typography.displayLarge.copy(fontSize = baseSize)
    }

    @Composable
    fun displayMedium(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 45.sp
            ScreenSize.MEDIUM -> 50.sp
            ScreenSize.EXPANDED -> 56.sp
        }
        return MaterialTheme.typography.displayMedium.copy(fontSize = baseSize)
    }

    @Composable
    fun displaySmall(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 36.sp
            ScreenSize.MEDIUM -> 40.sp
            ScreenSize.EXPANDED -> 44.sp
        }
        return MaterialTheme.typography.displaySmall.copy(fontSize = baseSize)
    }

    @Composable
    fun headlineLarge(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 32.sp
            ScreenSize.MEDIUM -> 36.sp
            ScreenSize.EXPANDED -> 40.sp
        }
        return MaterialTheme.typography.headlineLarge.copy(fontSize = baseSize)
    }

    @Composable
    fun headlineMedium(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 28.sp
            ScreenSize.MEDIUM -> 30.sp
            ScreenSize.EXPANDED -> 32.sp
        }
        return MaterialTheme.typography.headlineMedium.copy(fontSize = baseSize)
    }

    @Composable
    fun headlineSmall(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 24.sp
            ScreenSize.MEDIUM -> 26.sp
            ScreenSize.EXPANDED -> 28.sp
        }
        return MaterialTheme.typography.headlineSmall.copy(fontSize = baseSize)
    }

    @Composable
    fun titleLarge(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 22.sp
            ScreenSize.MEDIUM -> 24.sp
            ScreenSize.EXPANDED -> 26.sp
        }
        return MaterialTheme.typography.titleLarge.copy(fontSize = baseSize)
    }

    @Composable
    fun titleMedium(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 16.sp
            ScreenSize.MEDIUM -> 18.sp
            ScreenSize.EXPANDED -> 20.sp
        }
        return MaterialTheme.typography.titleMedium.copy(fontSize = baseSize)
    }

    @Composable
    fun titleSmall(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 14.sp
            ScreenSize.MEDIUM -> 15.sp
            ScreenSize.EXPANDED -> 16.sp
        }
        return MaterialTheme.typography.titleSmall.copy(fontSize = baseSize)
    }

    @Composable
    fun labelLarge(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 14.sp
            ScreenSize.MEDIUM -> 16.sp
            ScreenSize.EXPANDED -> 18.sp
        }
        return MaterialTheme.typography.labelLarge.copy(fontSize = baseSize)
    }

    @Composable
    fun labelMedium(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 12.sp
            ScreenSize.MEDIUM -> 14.sp
            ScreenSize.EXPANDED -> 16.sp
        }
        return MaterialTheme.typography.labelMedium.copy(fontSize = baseSize)
    }

    @Composable
    fun labelSmall(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 11.sp
            ScreenSize.MEDIUM -> 12.sp
            ScreenSize.EXPANDED -> 13.sp
        }
        return MaterialTheme.typography.labelSmall.copy(fontSize = baseSize)
    }

    @Composable
    fun bodyLarge(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 16.sp
            ScreenSize.MEDIUM -> 17.sp
            ScreenSize.EXPANDED -> 18.sp
        }
        return MaterialTheme.typography.bodyLarge.copy(fontSize = baseSize)
    }

    @Composable
    fun bodyMedium(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 14.sp
            ScreenSize.MEDIUM -> 15.sp
            ScreenSize.EXPANDED -> 16.sp
        }
        return MaterialTheme.typography.bodyMedium.copy(fontSize = baseSize)
    }

    @Composable
    fun bodySmall(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 12.sp
            ScreenSize.MEDIUM -> 13.sp
            ScreenSize.EXPANDED -> 14.sp
        }
        return MaterialTheme.typography.bodySmall.copy(fontSize = baseSize)
    }

    // ============ CONVENIENCE TEXT SIZE METHODS ============

    @Composable
    fun size10(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 10.sp
            ScreenSize.MEDIUM -> 11.sp
            ScreenSize.EXPANDED -> 12.sp
        }
        return MaterialTheme.typography.bodySmall.copy(fontSize = baseSize)
    }

    @Composable
    fun size11(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 11.sp
            ScreenSize.MEDIUM -> 12.sp
            ScreenSize.EXPANDED -> 13.sp
        }
        return MaterialTheme.typography.bodySmall.copy(fontSize = baseSize)
    }

    @Composable
    fun size12(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 12.sp
            ScreenSize.MEDIUM -> 13.sp
            ScreenSize.EXPANDED -> 14.sp
        }
        return MaterialTheme.typography.bodySmall.copy(fontSize = baseSize)
    }

    @Composable
    fun size13(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 13.sp
            ScreenSize.MEDIUM -> 14.sp
            ScreenSize.EXPANDED -> 15.sp
        }
        return MaterialTheme.typography.bodySmall.copy(fontSize = baseSize)
    }

    @Composable
    fun size14(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 14.sp
            ScreenSize.MEDIUM -> 15.sp
            ScreenSize.EXPANDED -> 16.sp
        }
        return MaterialTheme.typography.bodyMedium.copy(fontSize = baseSize)
    }

    @Composable
    fun size15(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 15.sp
            ScreenSize.MEDIUM -> 16.sp
            ScreenSize.EXPANDED -> 17.sp
        }
        return MaterialTheme.typography.bodyMedium.copy(fontSize = baseSize)
    }

    @Composable
    fun size16(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 16.sp
            ScreenSize.MEDIUM -> 17.sp
            ScreenSize.EXPANDED -> 18.sp
        }
        return MaterialTheme.typography.bodyLarge.copy(fontSize = baseSize)
    }

    @Composable
    fun size18(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 18.sp
            ScreenSize.MEDIUM -> 19.sp
            ScreenSize.EXPANDED -> 20.sp
        }
        return MaterialTheme.typography.titleMedium.copy(fontSize = baseSize)
    }

    @Composable
    fun size20(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 20.sp
            ScreenSize.MEDIUM -> 21.sp
            ScreenSize.EXPANDED -> 22.sp
        }
        return MaterialTheme.typography.titleMedium.copy(fontSize = baseSize)
    }

    @Composable
    fun size22(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 22.sp
            ScreenSize.MEDIUM -> 23.sp
            ScreenSize.EXPANDED -> 24.sp
        }
        return MaterialTheme.typography.titleLarge.copy(fontSize = baseSize)
    }

    @Composable
    fun size24(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 24.sp
            ScreenSize.MEDIUM -> 25.sp
            ScreenSize.EXPANDED -> 26.sp
        }
        return MaterialTheme.typography.headlineSmall.copy(fontSize = baseSize)
    }

    @Composable
    fun size28(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 28.sp
            ScreenSize.MEDIUM -> 29.sp
            ScreenSize.EXPANDED -> 30.sp
        }
        return MaterialTheme.typography.headlineMedium.copy(fontSize = baseSize)
    }

    @Composable
    fun size32(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextStyle {
        val baseSize = when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 32.sp
            ScreenSize.MEDIUM -> 33.sp
            ScreenSize.EXPANDED -> 34.sp
        }
        return MaterialTheme.typography.headlineLarge.copy(fontSize = baseSize)
    }

    // ============ TEXT SIZE AS TEXTUNIT (for fontSize parameter) ============

    @Composable
    fun textSize10(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextUnit =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 10.sp
            ScreenSize.MEDIUM -> 11.sp
            ScreenSize.EXPANDED -> 12.sp
        }

    @Composable
    fun textSize11(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextUnit =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 11.sp
            ScreenSize.MEDIUM -> 12.sp
            ScreenSize.EXPANDED -> 13.sp
        }

    @Composable
    fun textSize12(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextUnit =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 12.sp
            ScreenSize.MEDIUM -> 13.sp
            ScreenSize.EXPANDED -> 14.sp
        }

    @Composable
    fun textSize13(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextUnit =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 13.sp
            ScreenSize.MEDIUM -> 14.sp
            ScreenSize.EXPANDED -> 15.sp
        }

    @Composable
    fun textSize14(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextUnit =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 14.sp
            ScreenSize.MEDIUM -> 15.sp
            ScreenSize.EXPANDED -> 16.sp
        }

    @Composable
    fun textSize15(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextUnit =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 15.sp
            ScreenSize.MEDIUM -> 16.sp
            ScreenSize.EXPANDED -> 17.sp
        }

    @Composable
    fun textSize16(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextUnit =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 16.sp
            ScreenSize.MEDIUM -> 17.sp
            ScreenSize.EXPANDED -> 18.sp
        }

    @Composable
    fun textSize18(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextUnit =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 18.sp
            ScreenSize.MEDIUM -> 19.sp
            ScreenSize.EXPANDED -> 20.sp
        }

    @Composable
    fun textSize20(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextUnit =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 20.sp
            ScreenSize.MEDIUM -> 21.sp
            ScreenSize.EXPANDED -> 22.sp
        }

    @Composable
    fun textSize22(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextUnit =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 22.sp
            ScreenSize.MEDIUM -> 23.sp
            ScreenSize.EXPANDED -> 24.sp
        }

    @Composable
    fun textSize24(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextUnit =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 24.sp
            ScreenSize.MEDIUM -> 25.sp
            ScreenSize.EXPANDED -> 26.sp
        }

    @Composable
    fun textSize28(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextUnit =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 28.sp
            ScreenSize.MEDIUM -> 29.sp
            ScreenSize.EXPANDED -> 30.sp
        }

    @Composable
    fun textSize32(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): TextUnit =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 32.sp
            ScreenSize.MEDIUM -> 33.sp
            ScreenSize.EXPANDED -> 34.sp
        }
}

/**
 * Comprehensive Responsive Spacing Values
 */
object ResponsiveSpacing {

    @Composable
    fun none(): Dp = 0.dp

    @Composable
    fun xxSmall(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 1.dp
            ScreenSize.MEDIUM -> 1.dp
            ScreenSize.EXPANDED -> 2.dp
        }

    @Composable
    fun xSmall(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 2.dp
            ScreenSize.MEDIUM -> 3.dp
            ScreenSize.EXPANDED -> 4.dp
        }

    @Composable
    fun extraSmall(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 4.dp
            ScreenSize.MEDIUM -> 6.dp
            ScreenSize.EXPANDED -> 8.dp
        }

    @Composable
    fun small(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        dimensionManager.basePaddingSmall

    @Composable
    fun mediumSmall(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 12.dp
            ScreenSize.MEDIUM -> 14.dp
            ScreenSize.EXPANDED -> 16.dp
        }

    @Composable
    fun medium(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        dimensionManager.basePaddingMedium

    @Composable
    fun mediumLarge(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 20.dp
            ScreenSize.MEDIUM -> 24.dp
            ScreenSize.EXPANDED -> 28.dp
        }

    @Composable
    fun large(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        dimensionManager.basePaddingLarge

    @Composable
    fun extraLarge(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 32.dp
            ScreenSize.MEDIUM -> 40.dp
            ScreenSize.EXPANDED -> 48.dp
        }

    @Composable
    fun xxLarge(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 40.dp
            ScreenSize.MEDIUM -> 48.dp
            ScreenSize.EXPANDED -> 56.dp
        }

    @Composable
    fun xxxLarge(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 48.dp
            ScreenSize.MEDIUM -> 56.dp
            ScreenSize.EXPANDED -> 64.dp
        }

    // Specific size methods for exact values
    @Composable
    fun size1(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 1.dp
            ScreenSize.MEDIUM -> 1.dp
            ScreenSize.EXPANDED -> 1.dp
        }

    @Composable
    fun size2(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 2.dp
            ScreenSize.MEDIUM -> 2.dp
            ScreenSize.EXPANDED -> 3.dp
        }

    @Composable
    fun size3(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 3.dp
            ScreenSize.MEDIUM -> 3.dp
            ScreenSize.EXPANDED -> 4.dp
        }

    @Composable
    fun size4(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 4.dp
            ScreenSize.MEDIUM -> 5.dp
            ScreenSize.EXPANDED -> 6.dp
        }

    @Composable
    fun size5(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 5.dp
            ScreenSize.MEDIUM -> 6.dp
            ScreenSize.EXPANDED -> 7.dp
        }

    @Composable
    fun size6(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 6.dp
            ScreenSize.MEDIUM -> 7.dp
            ScreenSize.EXPANDED -> 8.dp
        }

    @Composable
    fun size8(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 8.dp
            ScreenSize.MEDIUM -> 9.dp
            ScreenSize.EXPANDED -> 10.dp
        }

    @Composable
    fun size10(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 10.dp
            ScreenSize.MEDIUM -> 11.dp
            ScreenSize.EXPANDED -> 12.dp
        }

    @Composable
    fun size12(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 12.dp
            ScreenSize.MEDIUM -> 13.dp
            ScreenSize.EXPANDED -> 14.dp
        }

    @Composable
    fun size14(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 14.dp
            ScreenSize.MEDIUM -> 15.dp
            ScreenSize.EXPANDED -> 16.dp
        }

    @Composable
    fun size16(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 16.dp
            ScreenSize.MEDIUM -> 18.dp
            ScreenSize.EXPANDED -> 20.dp
        }

    @Composable
    fun size18(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 18.dp
            ScreenSize.MEDIUM -> 20.dp
            ScreenSize.EXPANDED -> 22.dp
        }

    @Composable
    fun size20(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 20.dp
            ScreenSize.MEDIUM -> 22.dp
            ScreenSize.EXPANDED -> 24.dp
        }

    @Composable
    fun size24(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 24.dp
            ScreenSize.MEDIUM -> 26.dp
            ScreenSize.EXPANDED -> 28.dp
        }

    @Composable
    fun size28(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 28.dp
            ScreenSize.MEDIUM -> 30.dp
            ScreenSize.EXPANDED -> 32.dp
        }

    @Composable
    fun size30(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 30.dp
            ScreenSize.MEDIUM -> 32.dp
            ScreenSize.EXPANDED -> 34.dp
        }

    @Composable
    fun size32(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 32.dp
            ScreenSize.MEDIUM -> 34.dp
            ScreenSize.EXPANDED -> 36.dp
        }

    @Composable
    fun size36(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 36.dp
            ScreenSize.MEDIUM -> 38.dp
            ScreenSize.EXPANDED -> 40.dp
        }

    @Composable
    fun size40(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 40.dp
            ScreenSize.MEDIUM -> 42.dp
            ScreenSize.EXPANDED -> 44.dp
        }

    @Composable
    fun size48(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 48.dp
            ScreenSize.MEDIUM -> 50.dp
            ScreenSize.EXPANDED -> 52.dp
        }

    @Composable
    fun size50(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 50.dp
            ScreenSize.MEDIUM -> 52.dp
            ScreenSize.EXPANDED -> 54.dp
        }

    @Composable
    fun size56(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 56.dp
            ScreenSize.MEDIUM -> 58.dp
            ScreenSize.EXPANDED -> 60.dp
        }

    @Composable
    fun size60(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 60.dp
            ScreenSize.MEDIUM -> 62.dp
            ScreenSize.EXPANDED -> 64.dp
        }

    @Composable
    fun size64(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 64.dp
            ScreenSize.MEDIUM -> 66.dp
            ScreenSize.EXPANDED -> 68.dp
        }

    @Composable
    fun size72(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 72.dp
            ScreenSize.MEDIUM -> 74.dp
            ScreenSize.EXPANDED -> 76.dp
        }

    @Composable
    fun size80(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 80.dp
            ScreenSize.MEDIUM -> 84.dp
            ScreenSize.EXPANDED -> 88.dp
        }

    @Composable
    fun size100(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 100.dp
            ScreenSize.MEDIUM -> 105.dp
            ScreenSize.EXPANDED -> 110.dp
        }

    @Composable
    fun size120(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 120.dp
            ScreenSize.MEDIUM -> 126.dp
            ScreenSize.EXPANDED -> 132.dp
        }

    @Composable
    fun size150(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 150.dp
            ScreenSize.MEDIUM -> 157.dp
            ScreenSize.EXPANDED -> 165.dp
        }

    @Composable
    fun size200(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 200.dp
            ScreenSize.MEDIUM -> 210.dp
            ScreenSize.EXPANDED -> 220.dp
        }
}

/**
 * Responsive Icon Sizes - Enhanced with more options
 */
object ResponsiveIconSize {

    @Composable
    fun extraSmall(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 12.dp
            ScreenSize.MEDIUM -> 14.dp
            ScreenSize.EXPANDED -> 16.dp
        }

    @Composable
    fun small(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        dimensionManager.iconSizeSmall

    @Composable
    fun mediumSmall(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 18.dp
            ScreenSize.MEDIUM -> 20.dp
            ScreenSize.EXPANDED -> 22.dp
        }

    @Composable
    fun medium(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        dimensionManager.iconSizeMedium

    @Composable
    fun mediumLarge(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 28.dp
            ScreenSize.MEDIUM -> 30.dp
            ScreenSize.EXPANDED -> 32.dp
        }

    @Composable
    fun large(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        dimensionManager.iconSizeLarge

    @Composable
    fun extraLarge(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 32.dp
            ScreenSize.MEDIUM -> 36.dp
            ScreenSize.EXPANDED -> 40.dp
        }

    @Composable
    fun xxLarge(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 40.dp
            ScreenSize.MEDIUM -> 44.dp
            ScreenSize.EXPANDED -> 48.dp
        }

    @Composable
    fun xxxLarge(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 48.dp
            ScreenSize.MEDIUM -> 56.dp
            ScreenSize.EXPANDED -> 64.dp
        }

    // Convenience methods for specific sizes (only add where icons don't have size)
    @Composable
    fun size12(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 12.dp
            ScreenSize.MEDIUM -> 13.dp
            ScreenSize.EXPANDED -> 14.dp
        }

    @Composable
    fun size14(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 14.dp
            ScreenSize.MEDIUM -> 15.dp
            ScreenSize.EXPANDED -> 16.dp
        }

    @Composable
    fun size16(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 16.dp
            ScreenSize.MEDIUM -> 17.dp
            ScreenSize.EXPANDED -> 18.dp
        }

    @Composable
    fun size18(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 18.dp
            ScreenSize.MEDIUM -> 19.dp
            ScreenSize.EXPANDED -> 20.dp
        }

    @Composable
    fun size20(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 20.dp
            ScreenSize.MEDIUM -> 21.dp
            ScreenSize.EXPANDED -> 22.dp
        }

    @Composable
    fun size22(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 22.dp
            ScreenSize.MEDIUM -> 23.dp
            ScreenSize.EXPANDED -> 24.dp
        }

    @Composable
    fun size24(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 24.dp
            ScreenSize.MEDIUM -> 26.dp
            ScreenSize.EXPANDED -> 28.dp
        }

    @Composable
    fun size28(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 28.dp
            ScreenSize.MEDIUM -> 30.dp
            ScreenSize.EXPANDED -> 32.dp
        }

    @Composable
    fun size32(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 32.dp
            ScreenSize.MEDIUM -> 34.dp
            ScreenSize.EXPANDED -> 36.dp
        }

    @Composable
    fun size40(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 40.dp
            ScreenSize.MEDIUM -> 42.dp
            ScreenSize.EXPANDED -> 44.dp
        }

    @Composable
    fun size48(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Dp =
        when (dimensionManager.screenSize) {
            ScreenSize.COMPACT -> 48.dp
            ScreenSize.MEDIUM -> 50.dp
            ScreenSize.EXPANDED -> 52.dp
        }
}

/**
 * Responsive Grid Utilities
 */
object ResponsiveGrid {

    @Composable
    fun columns(dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()): Int =
        dimensionManager.gridColumns

    @Composable
    fun adaptiveColumns(
        minItemWidth: Dp,
        dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()
    ): Int {
        val availableWidth = dimensionManager.screenWidthDp.dp
        val spacing = dimensionManager.basePaddingMedium
        val columns = ((availableWidth - spacing) / (minItemWidth + spacing)).toInt()
        return (columns.coerceAtLeast(1)).coerceAtMost(6)
    }

    /**
     * Smart grid columns based on device profile
     */
    @Composable
    fun smartColumns(
        dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()
    ): Int = dimensionManager.smartGridColumns

    /**
     * Device-aware optimal columns
     */
    @Composable
    fun optimalColumns(
        forContent: ContentType,
        dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()
    ): Int = when (forContent) {
        ContentType.LULLABY_CARDS -> when (dimensionManager.deviceProfile) {
            DeviceProfile.PHONE_LDPI, DeviceProfile.PHONE_MDPI -> 1
            DeviceProfile.PHONE_HDPI, DeviceProfile.PHONE_XHDPI -> 2
            DeviceProfile.PHONE_XXHDPI, DeviceProfile.PHONE_XXXHDPI ->
                if (dimensionManager.orientation == ScreenOrientation.PORTRAIT) 2 else 3

            DeviceProfile.TABLET_MDPI, DeviceProfile.TABLET_HDPI ->
                if (dimensionManager.orientation == ScreenOrientation.PORTRAIT) 3 else 4

            DeviceProfile.TABLET_XHDPI, DeviceProfile.TABLET_XXHDPI ->
                if (dimensionManager.orientation == ScreenOrientation.PORTRAIT) 4 else 5

            DeviceProfile.TABLET_XXXHDPI ->
                if (dimensionManager.orientation == ScreenOrientation.PORTRAIT) 4 else 6

            else -> 3 // Foldables
        }

        ContentType.STORY_CARDS -> when (dimensionManager.deviceProfile) {
            DeviceProfile.PHONE_LDPI, DeviceProfile.PHONE_MDPI -> 1
            DeviceProfile.PHONE_HDPI, DeviceProfile.PHONE_XHDPI,
            DeviceProfile.PHONE_XXHDPI, DeviceProfile.PHONE_XXXHDPI -> 1

            DeviceProfile.TABLET_MDPI, DeviceProfile.TABLET_HDPI -> 2
            DeviceProfile.TABLET_XHDPI, DeviceProfile.TABLET_XXHDPI,
            DeviceProfile.TABLET_XXXHDPI -> 2

            else -> 2 // Foldables
        }

        ContentType.SMALL_ITEMS -> dimensionManager.gridColumns + 1
    }
}

enum class ContentType {
    LULLABY_CARDS,
    STORY_CARDS,
    SMALL_ITEMS
}

/**
 * Conditional Composable based on screen size
 */
@Composable
fun ResponsiveContent(
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager(),
    compact: @Composable () -> Unit = {},
    medium: @Composable () -> Unit = { compact() },
    expanded: @Composable () -> Unit = { medium() }
) {
    when (dimensionManager.screenSize) {
        ScreenSize.COMPACT -> compact()
        ScreenSize.MEDIUM -> medium()
        ScreenSize.EXPANDED -> expanded()
    }
}

/**
 * Conditional Composable based on device type
 */
@Composable
fun DeviceContent(
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager(),
    phone: @Composable () -> Unit = {},
    tablet: @Composable () -> Unit = { phone() }
) {
    when (dimensionManager.deviceType) {
        DeviceType.PHONE, DeviceType.FOLDABLE -> phone()
        DeviceType.TABLET -> tablet()
    }
}

/**
 * Conditional Composable based on orientation
 */
@Composable
fun OrientationContent(
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager(),
    portrait: @Composable () -> Unit = {},
    landscape: @Composable () -> Unit = { portrait() }
) {
    when (dimensionManager.orientation) {
        ScreenOrientation.PORTRAIT -> portrait()
        ScreenOrientation.LANDSCAPE -> landscape()
    }
}

/**
 * Enhanced Device Profile-Based Content
 * Provides content based on specific device+density combinations
 */
@Composable
fun DeviceProfileContent(
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager(),
    phoneLowDensity: @Composable () -> Unit = {},
    phoneStandard: @Composable () -> Unit = { phoneLowDensity() },
    phoneHighDensity: @Composable () -> Unit = { phoneStandard() },
    tabletStandard: @Composable () -> Unit = { phoneStandard() },
    tabletHighRes: @Composable () -> Unit = { tabletStandard() },
    foldable: @Composable () -> Unit = { tabletStandard() }
) {
    when (dimensionManager.deviceProfile) {
        DeviceProfile.PHONE_LDPI, DeviceProfile.PHONE_MDPI -> phoneLowDensity()
        DeviceProfile.PHONE_HDPI, DeviceProfile.PHONE_XHDPI -> phoneStandard()
        DeviceProfile.PHONE_XXHDPI, DeviceProfile.PHONE_XXXHDPI -> phoneHighDensity()
        DeviceProfile.TABLET_MDPI, DeviceProfile.TABLET_HDPI -> tabletStandard()
        DeviceProfile.TABLET_XHDPI, DeviceProfile.TABLET_XXHDPI, DeviceProfile.TABLET_XXXHDPI -> tabletHighRes()
        DeviceProfile.FOLDABLE_HDPI, DeviceProfile.FOLDABLE_XHDPI, DeviceProfile.FOLDABLE_XXHDPI -> foldable()
    }
}

/**
 * Enhanced Responsive Modifiers with Device Profile awareness
 */

/**
 * Apply device profile-aware padding
 */
@Composable
fun Modifier.adaptivePadding(
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager(),
    size: AdaptivePaddingSize = AdaptivePaddingSize.MEDIUM
): Modifier = this.padding(
    when (size) {
        AdaptivePaddingSize.SMALL -> dimensionManager.adaptiveDimensions.paddingSmall
        AdaptivePaddingSize.MEDIUM -> dimensionManager.adaptiveDimensions.paddingMedium
        AdaptivePaddingSize.LARGE -> dimensionManager.adaptiveDimensions.paddingLarge
    }
)

/**
 * Apply device profile-aware size
 */
@Composable
fun Modifier.adaptiveSize(
    baseSize: Dp,
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager()
): Modifier {
    val scaledSize = when (dimensionManager.deviceProfile) {
        DeviceProfile.PHONE_LDPI -> baseSize * 0.8f
        DeviceProfile.PHONE_MDPI -> baseSize * 0.9f
        DeviceProfile.PHONE_HDPI, DeviceProfile.PHONE_XHDPI -> baseSize
        DeviceProfile.PHONE_XXHDPI, DeviceProfile.PHONE_XXXHDPI -> baseSize * 1.1f
        DeviceProfile.TABLET_MDPI, DeviceProfile.TABLET_HDPI -> baseSize * 1.2f
        DeviceProfile.TABLET_XHDPI -> baseSize * 1.3f
        DeviceProfile.TABLET_XXHDPI, DeviceProfile.TABLET_XXXHDPI -> baseSize * 1.4f
        DeviceProfile.FOLDABLE_HDPI -> baseSize * 1.1f
        DeviceProfile.FOLDABLE_XHDPI -> baseSize * 1.2f
        DeviceProfile.FOLDABLE_XXHDPI -> baseSize * 1.3f
    }
    return this.size(scaledSize)
}

enum class AdaptivePaddingSize {
    SMALL,
    MEDIUM,
    LARGE
}

/**
 * Smart Content Layout based on device characteristics
 */
@Composable
fun SmartLayout(
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager(),
    content: @Composable SmartLayoutScope.() -> Unit
) {
    val scope = SmartLayoutScope(dimensionManager)
    scope.content()
}

class SmartLayoutScope(val dimensionManager: ScreenDimensionManager) {

    val isCompactPhone: Boolean
        get() = dimensionManager.deviceProfile in listOf(
            DeviceProfile.PHONE_LDPI,
            DeviceProfile.PHONE_MDPI,
            DeviceProfile.PHONE_HDPI
        )

    val isModernPhone: Boolean
        get() = dimensionManager.deviceProfile in listOf(
            DeviceProfile.PHONE_XHDPI,
            DeviceProfile.PHONE_XXHDPI,
            DeviceProfile.PHONE_XXXHDPI
        )

    val isTablet: Boolean
        get() = dimensionManager.deviceProfile.name.startsWith("TABLET")

    val isFoldable: Boolean
        get() = dimensionManager.deviceProfile.name.startsWith("FOLDABLE")

    val shouldUseCompactLayout: Boolean
        get() = isCompactPhone

    val shouldUseExpandedLayout: Boolean
        get() = isTablet || isFoldable

    val recommendedColumns: Int
        get() = dimensionManager.gridColumns
}