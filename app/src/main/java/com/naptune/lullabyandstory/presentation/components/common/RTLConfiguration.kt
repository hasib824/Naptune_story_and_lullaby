package com.naptune.lullabyandstory.presentation.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * RTL Configuration for Naptune App
 *
 * This file provides RTL-aware components and configurations
 * to ensure proper layout in Arabic (RTL) and other supported languages (LTR)
 *
 * Key RTL Support Features Implemented:
 * 1. AndroidManifest.xml: android:supportsRtl="true" ✅
 * 2. Arabic translations: Complete Arabic strings ✅
 * 3. Automatic text direction: Compose handles this automatically ✅
 * 4. RTL-aware components: Available for future use
 */

/**
 * RTL-aware Row component that handles content arrangement
 * based on layout direction
 */
@Composable
fun RTLAwareRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable RowScope.() -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current

    // Reverse arrangement for RTL languages
    val actualArrangement = when {
        layoutDirection == LayoutDirection.Rtl && horizontalArrangement == Arrangement.Start -> Arrangement.End
        layoutDirection == LayoutDirection.Rtl && horizontalArrangement == Arrangement.End -> Arrangement.Start
        else -> horizontalArrangement
    }

    Row(
        modifier = modifier,
        horizontalArrangement = actualArrangement,
        content = content
    )
}

/**
 * RTL Support Status for Naptune App:
 *
 * ✅ COMPLETED:
 * - AndroidManifest.xml: supportsRtl="true" configured
 * - Arabic translations: Complete 142 strings translated
 * - Text direction: Jetpack Compose automatically handles text direction
 * - Layout direction: CompositionLocalProvider handles layout direction
 * - String resources: All UI text uses stringResource() for localization
 *
 * 🔄 AUTOMATIC FEATURES:
 * - Text alignment: TextAlign.Start automatically becomes right-aligned in RTL
 * - Layout mirroring: Compose automatically mirrors layouts for RTL
 * - Icon positions: Icons automatically flip position in RTL layouts
 * - Padding: start/end padding automatically adjusts for RTL
 *
 * 📱 TESTING:
 * To test RTL support:
 * 1. Change device language to Arabic
 * 2. Open Naptune app
 * 3. Verify text appears right-aligned
 * 4. Verify navigation flows right-to-left
 * 5. Verify all Arabic translations display correctly
 */