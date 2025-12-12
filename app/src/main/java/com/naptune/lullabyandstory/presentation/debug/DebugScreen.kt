package com.naptune.lullabyandstory.presentation.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naptune.lullabyandstory.presentation.main.MainViewModel
import com.naptune.lullabyandstory.presentation.main.MainUiState
import com.naptune.lullabyandstory.presentation.components.common.responsive.*

@Composable
fun DebugScreen(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val dimensionManager = rememberScreenDimensionManager()
    
    // Responsive layout container
    ResponsiveContent(
        dimensionManager = dimensionManager,
        compact = {
            CompactDebugLayout(
                uiState = uiState,
                dimensionManager = dimensionManager,
                mainViewModel = mainViewModel
            )
        },
        medium = {
            MediumDebugLayout(
                uiState = uiState,
                dimensionManager = dimensionManager,
                mainViewModel = mainViewModel
            )
        },
        expanded = {
            ExpandedDebugLayout(
                uiState = uiState,
                dimensionManager = dimensionManager,
                mainViewModel = mainViewModel
            )
        }
    )
}

@Composable
private fun CompactDebugLayout(
    uiState: MainUiState,
    dimensionManager: ScreenDimensionManager,
    mainViewModel: MainViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .responsivePadding(
                dimensionManager = dimensionManager,
                horizontal = ResponsivePaddingSize.MEDIUM,
                vertical = ResponsivePaddingSize.MEDIUM
            ),
        verticalArrangement = Arrangement.spacedBy(ResponsiveSpacing.medium(dimensionManager))
    ) {
        
        item {
            // Header with screen info
            Column {
                Text(
                    text = "🛠️ Neptune Debug Panel",
                    style = ResponsiveTextStyles.headlineMedium(dimensionManager),
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "📱 ${dimensionManager.deviceType} • ${dimensionManager.screenSize}",
                    style = ResponsiveTextStyles.bodySmall(dimensionManager),
                    color = Color.Gray
                )
            }
        }
        
        item {
            // Screen Info Card
            ResponsiveDebugCard(
                title = "📏 Screen Information",
                dimensionManager = dimensionManager
            ) {
                Text(
                    text = dimensionManager.getDebugInfo(),
                    style = ResponsiveTextStyles.bodySmall(dimensionManager),
                    color = Color.DarkGray
                )
            }
        }
        
        item {
            // Current State Card
            ResponsiveInfoCard(
                title = "📊 Current State",
                dimensionManager = dimensionManager
            ) {
                StateContent(uiState, dimensionManager)
            }
        }
        
        item {
            // Action Buttons Card
            ResponsiveActionCard(
                title = "🛠️ Actions",
                dimensionManager = dimensionManager,
                actions = {
                    Button(
                        onClick = { mainViewModel.manualRefresh() },
                        modifier = Modifier
                            .weight(1f)
                            .height(dimensionManager.buttonHeight)
                    ) {
                        Text(
                            text = "🔄 Refresh",
                            style = ResponsiveTextStyles.bodyMedium(dimensionManager)
                        )
                    }
                    
                    Button(
                        onClick = { mainViewModel.manualRefresh() },
                        modifier = Modifier
                            .weight(1f)
                            .height(dimensionManager.buttonHeight)
                    ) {
                        Text(
                            text = "🔄 Force Sync",
                            style = ResponsiveTextStyles.bodyMedium(dimensionManager)
                        )
                    }
                }
            )
        }
        
        item {
            // Data Display
            if (uiState is MainUiState.Content) {
                DataDisplayCard(
                    uiState = uiState,
                    dimensionManager = dimensionManager,
                    compact = true
                )
            }
        }
        
        item {
            // Instructions Card
            InstructionsCard(dimensionManager)
        }
    }
}

@Composable
private fun MediumDebugLayout(
    uiState: MainUiState,
    dimensionManager: ScreenDimensionManager,
    mainViewModel: MainViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .responsivePadding(
                dimensionManager = dimensionManager,
                horizontal = ResponsivePaddingSize.LARGE,
                vertical = ResponsivePaddingSize.MEDIUM
            ),
        verticalArrangement = Arrangement.spacedBy(ResponsiveSpacing.large(dimensionManager))
    ) {
        
        item {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🛠️ Neptune Debug Panel",
                        style = ResponsiveTextStyles.headlineLarge(dimensionManager),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "📱 ${dimensionManager.deviceType} • ${dimensionManager.screenSize} • ${dimensionManager.orientation}",
                        style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                        color = Color.Gray
                    )
                }
                
                // Quick actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ResponsiveSpacing.small(dimensionManager))
                ) {
                    Button(
                        onClick = { mainViewModel.manualRefresh() }
                    ) {
                        Text("🔄 Refresh")
                    }
                }
            }
        }
        
        item {
            // Two column layout for medium screens
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ResponsiveSpacing.medium(dimensionManager))
            ) {
                // Left Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(ResponsiveSpacing.medium(dimensionManager))
                ) {
                    ResponsiveDebugCard(
                        title = "📏 Screen Information",
                        dimensionManager = dimensionManager
                    ) {
                        Text(
                            text = dimensionManager.getDebugInfo(),
                            style = ResponsiveTextStyles.bodySmall(dimensionManager),
                            color = Color.DarkGray
                        )
                    }
                    
                    ResponsiveInfoCard(
                        title = "📊 Current State",
                        dimensionManager = dimensionManager
                    ) {
                        StateContent(uiState, dimensionManager)
                    }
                }
                
                // Right Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(ResponsiveSpacing.medium(dimensionManager))
                ) {
                    if (uiState is MainUiState.Content) {
                        DataDisplayCard(
                            uiState = uiState,
                            dimensionManager = dimensionManager,
                            compact = false
                        )
                    }
                    
                    InstructionsCard(dimensionManager)
                }
            }
        }
    }
}

@Composable
private fun ExpandedDebugLayout(
    uiState: MainUiState,
    dimensionManager: ScreenDimensionManager,
    mainViewModel: MainViewModel
) {
    // Three column layout for large tablets
    Column(
        modifier = Modifier
            .fillMaxSize()
            .responsivePadding(
                dimensionManager = dimensionManager,
                horizontal = ResponsivePaddingSize.LARGE,
                vertical = ResponsivePaddingSize.LARGE
            )
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🛠️ Neptune Debug Panel",
                    style = ResponsiveTextStyles.headlineLarge(dimensionManager),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "📱 ${dimensionManager.screenWidthDp} x ${dimensionManager.screenHeightDp} dp • ${dimensionManager.deviceType} • ${dimensionManager.screenDensity}",
                    style = ResponsiveTextStyles.titleSmall(dimensionManager),
                    color = Color.Gray
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(ResponsiveSpacing.medium(dimensionManager))
            ) {
                Button(onClick = { mainViewModel.manualRefresh() }) {
                    Text("🔄 Manual Refresh")
                }
                Button(onClick = { mainViewModel.manualRefresh() }) {
                    Text("🔄 Force Sync")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(ResponsiveSpacing.large(dimensionManager)))
        
        // Three column content
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ResponsiveSpacing.large(dimensionManager))
        ) {
            // Left Column - System Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ResponsiveSpacing.medium(dimensionManager))
            ) {
                ResponsiveDebugCard(
                    title = "📏 Screen Details",
                    dimensionManager = dimensionManager
                ) {
                    Text(
                        text = dimensionManager.getDebugInfo(),
                        style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                        color = Color.DarkGray
                    )
                }
                
                ResponsiveInfoCard(
                    title = "📊 App State",
                    dimensionManager = dimensionManager
                ) {
                    StateContent(uiState, dimensionManager)
                }
            }
            
            // Middle Column - Data
            Column(
                modifier = Modifier.weight(1.5f),
                verticalArrangement = Arrangement.spacedBy(ResponsiveSpacing.medium(dimensionManager))
            ) {
                if (uiState is MainUiState.Content) {
                    DataDisplayCard(
                        uiState = uiState,
                        dimensionManager = dimensionManager,
                        compact = false
                    )
                }
            }
            
            // Right Column - Instructions
            Column(
                modifier = Modifier.weight(1f)
            ) {
                InstructionsCard(dimensionManager)
            }
        }
    }
}

@Composable
private fun StateContent(
    uiState: MainUiState,
    dimensionManager: ScreenDimensionManager
) {
    when (uiState) {
        is MainUiState.Loading -> {
            Text(
                text = "🔄 Loading...",
                style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                color = Color.Blue
            )
        }
        is MainUiState.Content -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(ResponsiveSpacing.extraSmall(dimensionManager))
            ) {
                Text(
                    text = "✅ Content Loaded",
                    style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                    color = Color.Green
                )
                Text(
                    text = "🎵 Lullabies: ${uiState.popularLullabies.size}",
                    style = ResponsiveTextStyles.bodySmall(dimensionManager)
                )
                Text(
                    text = "📚 Stories: ${uiState.popularStories.size}",
                    style = ResponsiveTextStyles.bodySmall(dimensionManager)
                )
                Text(
                    text = "🌟 Today's Pick: ${uiState.todaysPickLullabies.size}",
                    style = ResponsiveTextStyles.bodySmall(dimensionManager)
                )
                Text(
                    text = "📖 Featured Story: ${if(uiState.todaysPickStory != null) "Yes" else "No"}",
                    style = ResponsiveTextStyles.bodySmall(dimensionManager)
                )
            }
        }
        is MainUiState.Error -> {
            Text(
                text = "❌ Error: ${uiState.message}",
                style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                color = Color.Red
            )
        }
    }
}

@Composable
private fun DataDisplayCard(
    uiState: MainUiState.Content,
    dimensionManager: ScreenDimensionManager,
    compact: Boolean
) {
    ResponsiveCard(
        modifier = Modifier.fillMaxWidth(),
        dimensionManager = dimensionManager,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Column(
            modifier = Modifier.responsivePadding(
                dimensionManager = dimensionManager,
                horizontal = ResponsivePaddingSize.MEDIUM,
                vertical = ResponsivePaddingSize.MEDIUM
            )
        ) {
            Text(
                text = "🎵 Lullabies Data (${uiState.popularLullabies.size} items)",
                style = ResponsiveTextStyles.titleMedium(dimensionManager),
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(ResponsiveSpacing.small(dimensionManager)))
            
            if (compact) {
                // List view for compact screens
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(ResponsiveSpacing.extraSmall(dimensionManager))
                ) {
                    items(uiState.popularLullabies.take(5)) { lullaby ->
                        LullabyDebugItem(lullaby, dimensionManager, compact = true)
                    }
                }
            } else {
                // Grid view for larger screens
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(ResponsiveSpacing.extraSmall(dimensionManager)),
                    horizontalArrangement = Arrangement.spacedBy(ResponsiveSpacing.small(dimensionManager))
                ) {
                    items(uiState.popularLullabies.take(8)) { lullaby ->
                        LullabyDebugItem(lullaby, dimensionManager, compact = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun LullabyDebugItem(
    lullaby: com.naptune.lullabyandstory.domain.model.LullabyDomainModel,
    dimensionManager: ScreenDimensionManager,
    compact: Boolean
) {
    ResponsiveCard(
        modifier = Modifier.fillMaxWidth(),
        dimensionManager = dimensionManager,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.responsivePadding(
                dimensionManager = dimensionManager,
                horizontal = ResponsivePaddingSize.SMALL,
                vertical = ResponsivePaddingSize.SMALL
            )
        ) {
            Text(
                text = lullaby.musicName,
                style = ResponsiveTextStyles.bodyMedium(dimensionManager),
                fontWeight = FontWeight.Medium,
                maxLines = if (compact) 1 else 2
            )
            if (!compact) {
                Text(
                    text = "ID: ${lullaby.id}",
                    style = ResponsiveTextStyles.bodySmall(dimensionManager),
                    color = Color.Gray
                )
                Text(
                    text = "Doc: ${lullaby.documentId}",
                    style = ResponsiveTextStyles.bodySmall(dimensionManager),
                    color = Color.Gray
                )
            }
            Text(
                text = "Fav: ${if(lullaby.isFavourite) "❤️" else "🤍"}",
                style = ResponsiveTextStyles.bodySmall(dimensionManager),
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun InstructionsCard(
    dimensionManager: ScreenDimensionManager
) {
    ResponsiveCard(
        modifier = Modifier.fillMaxWidth(),
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
                text = "📋 Debug Instructions",
                style = ResponsiveTextStyles.titleMedium(dimensionManager),
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(ResponsiveSpacing.small(dimensionManager)))
            
            val instructions = listOf(
                "1. Check Logcat for sync logs",
                "2. Verify internet connection",
                "3. Check Appwrite configuration",
                "4. Use refresh buttons to retry",
                "5. Monitor download progress sync"
            )
            
            instructions.forEach { instruction ->
                Text(
                    text = instruction,
                    style = ResponsiveTextStyles.bodySmall(dimensionManager),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Add this to your navigation to access debug screen
 * You can add a hidden button in settings or use adb command
 */