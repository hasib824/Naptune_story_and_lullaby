package com.naptune.lullabyandstory.presentation.components.common.responsive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/**
 * Example screen showing how to use the responsive design system
 */
@Composable
fun ResponsiveExampleScreen() {
    val dimensionManager = rememberScreenDimensionManager()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .responsivePadding(
                dimensionManager = dimensionManager,
                horizontal = ResponsivePaddingSize.MEDIUM,
                vertical = ResponsivePaddingSize.LARGE
            ),
        verticalArrangement = Arrangement.spacedBy(ResponsiveSpacing.medium(dimensionManager))
    ) {
        
        item {
            // Responsive Header
            Text(
                text = "📱 Responsive Design Demo",
                style = ResponsiveTextStyles.headlineLarge(dimensionManager),
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Device: ${dimensionManager.deviceType} • Screen: ${dimensionManager.screenSize}",
                style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                color = Color.Gray
            )
        }
        
        item {
            // Screen Information Card
            ResponsiveInfoCard(
                title = "📏 Screen Details",
                dimensionManager = dimensionManager
            ) {
                Text(
                    text = dimensionManager.getDebugInfo(),
                    style = ResponsiveTextStyles.bodySmall(dimensionManager)
                )
            }
        }
        
        item {
            // Responsive Button Examples
            ResponsiveActionCard(
                title = "🎛️ Responsive Buttons",
                dimensionManager = dimensionManager,
                actions = {
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .height(dimensionManager.buttonHeight)
                    ) {
                        Text("Button 1")
                    }
                    
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .height(dimensionManager.buttonHeight)
                    ) {
                        Text("Button 2")
                    }
                }
            ) {
                Text(
                    text = "Buttons automatically resize based on screen size.",
                    style = ResponsiveTextStyles.bodySmall(dimensionManager)
                )
            }
        }
        
        item {
            // Device-specific content
            DeviceContent(
                dimensionManager = dimensionManager,
                phone = {
                    ResponsiveCard(
                        dimensionManager = dimensionManager,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                    ) {
                        Column(
                            modifier = Modifier.responsivePadding(
                                dimensionManager = dimensionManager,
                                horizontal = ResponsivePaddingSize.MEDIUM,
                                vertical = ResponsivePaddingSize.MEDIUM
                            )
                        ) {
                            Text(
                                text = "📱 Phone Layout",
                                style = ResponsiveTextStyles.titleMedium(dimensionManager),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "This content is optimized for phone screens with single column layout.",
                                style = ResponsiveTextStyles.bodySmall(dimensionManager)
                            )
                        }
                    }
                },
                tablet = {
                    ResponsiveCard(
                        dimensionManager = dimensionManager,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8E8F5))
                    ) {
                        Column(
                            modifier = Modifier.responsivePadding(
                                dimensionManager = dimensionManager,
                                horizontal = ResponsivePaddingSize.MEDIUM,
                                vertical = ResponsivePaddingSize.MEDIUM
                            )
                        ) {
                            Text(
                                text = "🖥️ Tablet Layout",
                                style = ResponsiveTextStyles.titleMedium(dimensionManager),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "This content is optimized for tablet screens with multi-column layouts and larger text.",
                                style = ResponsiveTextStyles.bodySmall(dimensionManager)
                            )
                        }
                    }
                }
            )
        }
        
        item {
            // Orientation-specific content
            OrientationContent(
                dimensionManager = dimensionManager,
                portrait = {
                    ResponsiveInfoCard(
                        title = "📱 Portrait Mode",
                        dimensionManager = dimensionManager,
                        backgroundColor = Color(0xFFFFF3E0)
                    ) {
                        Text(
                            text = "Portrait orientation detected. Content is stacked vertically.",
                            style = ResponsiveTextStyles.bodyMedium(dimensionManager)
                        )
                    }
                },
                landscape = {
                    ResponsiveInfoCard(
                        title = "🔄 Landscape Mode",
                        dimensionManager = dimensionManager,
                        backgroundColor = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = "Landscape orientation detected. Content can be arranged horizontally.",
                            style = ResponsiveTextStyles.bodyMedium(dimensionManager)
                        )
                    }
                }
            )
        }
        
        item {
            // Grid example
            ResponsiveCard(
                dimensionManager = dimensionManager
            ) {
                Column(
                    modifier = Modifier.responsivePadding(
                        dimensionManager = dimensionManager,
                        horizontal = ResponsivePaddingSize.MEDIUM,
                        vertical = ResponsivePaddingSize.MEDIUM
                    )
                ) {
                    Text(
                        text = "📱 Responsive Grid (${ResponsiveGrid.columns(dimensionManager)} columns)",
                        style = ResponsiveTextStyles.titleMedium(dimensionManager),
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(ResponsiveSpacing.small(dimensionManager)))
                    
                    Text(
                        text = "Grid columns automatically adjust based on screen size and orientation.",
                        style = ResponsiveTextStyles.bodySmall(dimensionManager),
                        color = Color.Gray
                    )
                }
            }
        }
        
        item {
            // Different screen sizes showcase
            ResponsiveContent(
                dimensionManager = dimensionManager,
                compact = {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Column(
                            modifier = Modifier.padding(ResponsiveSpacing.medium(dimensionManager))
                        ) {
                            Text(
                                text = "📱 Compact Screen",
                                style = ResponsiveTextStyles.titleMedium(dimensionManager),
                                color = Color.Red.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Optimized for phones and small screens",
                                style = ResponsiveTextStyles.bodySmall(dimensionManager)
                            )
                        }
                    }
                },
                medium = {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
                    ) {
                        Column(
                            modifier = Modifier.padding(ResponsiveSpacing.medium(dimensionManager))
                        ) {
                            Text(
                                text = "📱 Medium Screen",
                                style = ResponsiveTextStyles.titleMedium(dimensionManager),
                                color = Color.Magenta.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Optimized for small tablets and large phones",
                                style = ResponsiveTextStyles.bodySmall(dimensionManager)
                            )
                        }
                    }
                },
                expanded = {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                    ) {
                        Column(
                            modifier = Modifier.padding(ResponsiveSpacing.medium(dimensionManager))
                        ) {
                            Text(
                                text = "🖥️ Expanded Screen",
                                style = ResponsiveTextStyles.titleMedium(dimensionManager),
                                color = Color.Green.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Optimized for large tablets and desktop displays",
                                style = ResponsiveTextStyles.bodySmall(dimensionManager)
                            )
                        }
                    }
                }
            )
        }
    }
}