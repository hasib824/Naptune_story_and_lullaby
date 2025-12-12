package com.naptune.lullabyandstory.presentation.story

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.presentation.components.TransparentTopAppBar

/**
 * 🎭 Story Screen with Scaffold + Transparent TopAppBar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryScreenWithScaffold(
    storyTitle: String = "The turtle & hair",
    storyImageUrl: String = "",
    onBackClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val imageHeight = screenHeight * 0.33f
    
    Scaffold(
        topBar = {
            TransparentTopAppBar(
                title = storyTitle,
                showTitle = false, // Image এর উপরে title show করতে চাইলে true করো
                onBackClick = onBackClick,
                onMenuClick = onFavoriteClick,
                backgroundColor = Color.Transparent
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Fullscreen Image (Scaffold padding ignore করে)
            AsyncImage(
                model = storyImageUrl.ifEmpty { "https://images.pexels.com/photos/1287145/pexels-photo-1287145.jpeg" },
                contentDescription = "Story Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight + paddingValues.calculateTopPadding())
                    .offset(y = -paddingValues.calculateTopPadding()), // TopAppBar এর নিচে image যাওয়া prevent করে
                contentScale = ContentScale.Crop
            )
            
            // Content below image
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = imageHeight) // Image height এর পর content
            ) {
                // Story Content Card
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-24).dp), // Overlap with image
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A1A4A)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Story Title
                        Text(
                            text = storyTitle,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 34.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Story Description
                        Text(
                            text = stringResource(R.string.story_sample_text),
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Read")
                            }
                            
                            Button(
                                onClick = { },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF6366F1)
                                )
                            ) {
                                Text("Listen")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🎯 Method 3: Box Layout with Custom AppBar
 */
@Composable
fun StoryScreenWithBoxLayout(
    storyTitle: String = "The turtle & hair",
    storyImageUrl: String = "",
    onBackClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val imageHeight = screenHeight * 0.33f
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Fullscreen Image
        AsyncImage(
            model = storyImageUrl.ifEmpty { "https://images.pexels.com/photos/1287145/pexels-photo-1287145.jpeg" },
            contentDescription = "Story Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
            contentScale = ContentScale.Crop
        )
        
        // 2. Custom AppBar (Image এর উপরে)
        com.naptune.lullabyandstory.presentation.components.CustomAppBarRow(
            title = storyTitle,
            showTitle = false,
            onBackClick = onBackClick,
            onMenuClick = onFavoriteClick
        )
        
        // 3. Content below image
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = imageHeight + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                )
        ) {
            // Your content here
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-24).dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A1A4A)
                )
            ) {
                // Content
                Text(
                    text = storyTitle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}

/**
 * 🎯 Method 4: WindowInsets Approach (Advanced)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryScreenWithWindowInsets(
    storyTitle: String = "The turtle & hair",
    storyImageUrl: String = "",
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    TransparentTopAppBar(
                        onBackClick = onBackClick,
                        backgroundColor = Color.Transparent
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                windowInsets = WindowInsets(0) // Remove default insets
            )
        },
        contentWindowInsets = WindowInsets(0) // Remove content insets
    ) { _ ->
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Image section (ignoring scaffold padding)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f) // 40% of screen
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                AsyncImage(
                    model = storyImageUrl,
                    contentDescription = "Story Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Custom back button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Back icon
                }
            }
            
            // Content section
            // ... rest of your content
        }
    }
}