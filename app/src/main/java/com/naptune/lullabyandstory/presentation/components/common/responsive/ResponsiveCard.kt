package com.naptune.lullabyandstory.presentation.components.common.responsive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive Card that adapts to screen size and density
 */
@Composable
fun ResponsiveCard(
    modifier: Modifier = Modifier,
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager(),
    colors: CardColors = CardDefaults.cardColors(),
    elevation: Dp? = null,
    shape: Shape? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardElevation = elevation ?: dimensionManager.cardElevation
    val cardShape = shape ?: RoundedCornerShape(dimensionManager.cardCornerRadius)
    
    Card(
        modifier = modifier,
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        shape = cardShape,
        content = content
    )
}

/**
 * Responsive Info Card with predefined styling
 */
@Composable
fun ResponsiveInfoCard(
    title: String,
    modifier: Modifier = Modifier,
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager(),
    backgroundColor: Color = Color(0xFFE3F2FD),
    content: @Composable ColumnScope.() -> Unit
) {
    ResponsiveCard(
        modifier = modifier.fillMaxWidth(),
        dimensionManager = dimensionManager,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.responsivePadding(
                dimensionManager = dimensionManager,
                horizontal = ResponsivePaddingSize.MEDIUM,
                vertical = ResponsivePaddingSize.MEDIUM
            )
        ) {
            Text(
                text = title,
                style = ResponsiveTextStyles.titleMedium(dimensionManager),
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(ResponsiveSpacing.small(dimensionManager)))
            
            content()
        }
    }
}

/**
 * Responsive Debug Card specifically for debug information
 */
@Composable
fun ResponsiveDebugCard(
    title: String,
    modifier: Modifier = Modifier,
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager(),
    content: @Composable ColumnScope.() -> Unit
) {
    ResponsiveInfoCard(
        title = title,
        modifier = modifier,
        dimensionManager = dimensionManager,
        backgroundColor = Color(0xFFF5F5F5),
        content = content
    )
}

/**
 * Responsive Action Card with buttons
 */
@Composable
fun ResponsiveActionCard(
    title: String,
    modifier: Modifier = Modifier,
    dimensionManager: ScreenDimensionManager = rememberScreenDimensionManager(),
    actions: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    ResponsiveCard(
        modifier = modifier.fillMaxWidth(),
        dimensionManager = dimensionManager,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(
            modifier = Modifier.responsivePadding(
                dimensionManager = dimensionManager,
                horizontal = ResponsivePaddingSize.MEDIUM,
                vertical = ResponsivePaddingSize.MEDIUM
            )
        ) {
            Text(
                text = title,
                style = ResponsiveTextStyles.titleMedium(dimensionManager),
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(ResponsiveSpacing.small(dimensionManager)))
            
            content()
            
            Spacer(modifier = Modifier.height(ResponsiveSpacing.medium(dimensionManager)))
            
            // Actions row - responsive layout
            ResponsiveContent(
                dimensionManager = dimensionManager,
                compact = {
                    // Single column layout for compact screens
                    Column(
                        verticalArrangement = Arrangement.spacedBy(ResponsiveSpacing.small(dimensionManager))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ResponsiveSpacing.small(dimensionManager)),
                            content = actions
                        )
                    }
                },
                medium = {
                    // Row layout for medium screens
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ResponsiveSpacing.small(dimensionManager)),
                        content = actions
                    )
                },
                expanded = {
                    // Row layout with more spacing for large screens
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ResponsiveSpacing.medium(dimensionManager)),
                        content = actions
                    )
                }
            )
        }
    }
}