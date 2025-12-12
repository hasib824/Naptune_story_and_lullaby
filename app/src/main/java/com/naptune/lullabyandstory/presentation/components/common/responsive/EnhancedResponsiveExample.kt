package com.naptune.lullabyandstory.presentation.components.common.responsive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Enhanced Responsive Example showcasing Device+Density combination approach
 *
 * This demonstrates how the new DeviceProfile system works with different
 * device types and screen densities for optimal UI adaptation.
 */
@Composable
fun EnhancedResponsiveExampleScreen() {
    val dimensionManager = rememberScreenDimensionManager()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .adaptivePadding(dimensionManager, AdaptivePaddingSize.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(dimensionManager.adaptiveDimensions.paddingMedium)
    ) {
        item {
            // Enhanced header with device profile info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(dimensionManager.cardCornerRadius)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .adaptivePadding(dimensionManager, AdaptivePaddingSize.LARGE)
                ) {
                    Text(
                        text = "🚀 Enhanced Responsive System",
                        style = ResponsiveTextStyles.headlineMedium(dimensionManager),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(dimensionManager.adaptiveDimensions.paddingSmall))

                    Text(
                        text = "Device Profile: ${dimensionManager.deviceProfile}",
                        style = ResponsiveTextStyles.titleMedium(dimensionManager),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Text(
                        text = "${dimensionManager.deviceType} • ${dimensionManager.screenDensity} • ${dimensionManager.densityDpi} dpi",
                        style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        item {
            // Device Profile-based content demonstration
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensionManager.cardCornerRadius)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .adaptivePadding(dimensionManager, AdaptivePaddingSize.MEDIUM)
                ) {
                    Text(
                        text = "📱 Device-Specific Content",
                        style = ResponsiveTextStyles.titleLarge(dimensionManager),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(dimensionManager.adaptiveDimensions.paddingSmall))

                    // Device profile-based content
                    DeviceProfileContent(
                        dimensionManager = dimensionManager,
                        phoneLowDensity = {
                            Text(
                                text = "📱 Basic Phone Layout - Optimized for older/budget devices",
                                style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                                color = Color(0xFF4CAF50)
                            )
                        },
                        phoneStandard = {
                            Text(
                                text = "📱 Standard Phone Layout - Modern phone experience",
                                style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                                color = Color(0xFF2196F3)
                            )
                        },
                        phoneHighDensity = {
                            Text(
                                text = "📱 Premium Phone Layout - Flagship device features",
                                style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                                color = Color(0xFF9C27B0)
                            )
                        },
                        tabletStandard = {
                            Text(
                                text = "💻 Standard Tablet Layout - Multi-column experience",
                                style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                                color = Color(0xFFFF5722)
                            )
                        },
                        tabletHighRes = {
                            Text(
                                text = "💻 High-Resolution Tablet - Premium tablet features",
                                style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                                color = Color(0xFF795548)
                            )
                        },
                        foldable = {
                            Text(
                                text = "📱💻 Foldable Layout - Adaptive folding experience",
                                style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                                color = Color(0xFF607D8B)
                            )
                        }
                    )
                }
            }
        }

        item {
            // Adaptive grid demonstration
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensionManager.cardCornerRadius)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .adaptivePadding(dimensionManager, AdaptivePaddingSize.MEDIUM)
                ) {
                    Text(
                        text = "🎯 Smart Grid System",
                        style = ResponsiveTextStyles.titleLarge(dimensionManager),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(dimensionManager.adaptiveDimensions.paddingSmall))

                    Text(
                        text = "Optimal Columns: ${ResponsiveGrid.optimalColumns(ContentType.LULLABY_CARDS, dimensionManager)}",
                        style = ResponsiveTextStyles.bodyMedium(dimensionManager)
                    )

                    Spacer(modifier = Modifier.height(dimensionManager.adaptiveDimensions.paddingMedium))

                    // Adaptive grid layout
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(ResponsiveGrid.smartColumns(dimensionManager)),
                        modifier = Modifier.height(200.dp),
                        horizontalArrangement = Arrangement.spacedBy(dimensionManager.adaptiveDimensions.paddingSmall),
                        verticalArrangement = Arrangement.spacedBy(dimensionManager.adaptiveDimensions.paddingSmall)
                    ) {
                        items(12) { index ->
                            AdaptiveGridItem(
                                index = index,
                                dimensionManager = dimensionManager
                            )
                        }
                    }
                }
            }
        }

        item {
            // Smart layout demonstration
            SmartLayout(dimensionManager = dimensionManager) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionManager.cardCornerRadius)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .adaptivePadding(dimensionManager, AdaptivePaddingSize.MEDIUM)
                    ) {
                        Text(
                            text = "🧠 Smart Layout Detection",
                            style = ResponsiveTextStyles.titleLarge(dimensionManager),
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(dimensionManager.adaptiveDimensions.paddingSmall))

                        Column {
                            Row {
                                Text("Compact Phone: ", fontWeight = FontWeight.Medium)
                                Text(
                                    if (isCompactPhone) "✅ Yes" else "❌ No",
                                    color = if (isCompactPhone) Color(0xFF4CAF50) else Color(0xFF757575)
                                )
                            }

                            Row {
                                Text("Modern Phone: ", fontWeight = FontWeight.Medium)
                                Text(
                                    if (isModernPhone) "✅ Yes" else "❌ No",
                                    color = if (isModernPhone) Color(0xFF4CAF50) else Color(0xFF757575)
                                )
                            }

                            Row {
                                Text("Tablet: ", fontWeight = FontWeight.Medium)
                                Text(
                                    if (isTablet) "✅ Yes" else "❌ No",
                                    color = if (isTablet) Color(0xFF4CAF50) else Color(0xFF757575)
                                )
                            }

                            Row {
                                Text("Foldable: ", fontWeight = FontWeight.Medium)
                                Text(
                                    if (isFoldable) "✅ Yes" else "❌ No",
                                    color = if (isFoldable) Color(0xFF4CAF50) else Color(0xFF757575)
                                )
                            }

                            Spacer(modifier = Modifier.height(dimensionManager.adaptiveDimensions.paddingSmall))

                            Text(
                                text = "Recommended Columns: $recommendedColumns",
                                style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        item {
            // Debug information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(dimensionManager.cardCornerRadius)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .adaptivePadding(dimensionManager, AdaptivePaddingSize.MEDIUM)
                ) {
                    Text(
                        text = "🔧 Debug Information",
                        style = ResponsiveTextStyles.titleMedium(dimensionManager),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(dimensionManager.adaptiveDimensions.paddingSmall))

                    Text(
                        text = dimensionManager.getDebugInfo(),
                        style = ResponsiveTextStyles.bodySmall(dimensionManager),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AdaptiveGridItem(
    index: Int,
    dimensionManager: ScreenDimensionManager
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(dimensionManager.cardCornerRadius))
            .background(
                when (dimensionManager.deviceProfile) {
                    DeviceProfile.PHONE_LDPI, DeviceProfile.PHONE_MDPI -> Color(0xFFE8F5E8)
                    DeviceProfile.PHONE_HDPI, DeviceProfile.PHONE_XHDPI -> Color(0xFFE3F2FD)
                    DeviceProfile.PHONE_XXHDPI, DeviceProfile.PHONE_XXXHDPI -> Color(0xFFF3E5F5)
                    DeviceProfile.TABLET_MDPI, DeviceProfile.TABLET_HDPI -> Color(0xFFFFF3E0)
                    DeviceProfile.TABLET_XHDPI, DeviceProfile.TABLET_XXHDPI, DeviceProfile.TABLET_XXXHDPI -> Color(0xFFEFEBE9)
                    else -> Color(0xFFECEFF1) // Foldables
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = null,
                modifier = Modifier.adaptiveSize(
                    baseSize = 24.dp,
                    dimensionManager = dimensionManager
                ),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "${index + 1}",
                style = ResponsiveTextStyles.bodySmall(dimensionManager),
                fontWeight = FontWeight.Medium
            )
        }
    }
}