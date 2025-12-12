package com.naptune.lullabyandstory.presentation.story

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.naptune.lullabyandstory.R

/**
 * 🎯 High Performance Story Screen with ConstraintLayout
 * 
 * Benefits:
 * - Single layout pass = Better performance
 * - Flat hierarchy = Less memory usage
 * - Complex layouts in one container
 * - Hardware acceleration optimized
 */
@Composable
fun ConstraintLayoutStoryScreen(
    storyTitle: String = "The turtle & hair",
    storyDescription: String = "In a cozy little house nestled in a bustling town, there lived a young couple eagerly awaiting the arrival of their first baby. They had spent months preparing the nursery, picking out adorable little clothes..",
    storyDuration: String = "5 minutes story",
    storyImageUrl: String = "",
    onBackClick: () -> Unit = {},
    onReadClick: () -> Unit = {},
    onListenClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        // Create references for all components
        val (
            backgroundImage,
            gradientOverlay,
            backButton,
            favoriteButton,
            contentCard,
            titleText,
            descriptionText,
            durationRow,
            buttonRow
        ) = createRefs()
        
        // ✅ 1. Background Image (1/3 of screen height)
        AsyncImage(
            model = storyImageUrl.ifEmpty { "https://images.pexels.com/photos/1287145/pexels-photo-1287145.jpeg" },
            contentDescription = "Story Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.constrainAs(backgroundImage) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
                height = Dimension.value(screenHeight * 0.33f)
            }
        )
        
        // ✅ 2. Gradient Overlay (for better text visibility)
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
                .constrainAs(gradientOverlay) {
                    top.linkTo(backgroundImage.top)
                    bottom.linkTo(backgroundImage.bottom)
                    start.linkTo(backgroundImage.start)
                    end.linkTo(backgroundImage.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
        )
        
        // ✅ 3. Back Button (Top-left on image)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    CircleShape
                )
                .constrainAs(backButton) {
                    top.linkTo(parent.top, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // ✅ 4. Favorite Button (Top-right on image)
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    CircleShape
                )
                .constrainAs(favoriteButton) {
                    top.linkTo(parent.top, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_fav_big),
                contentDescription = "Favorite",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // ✅ 5. Content Card (Below image with overlap)
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A1A4A)
            ),
            modifier = Modifier.constrainAs(contentCard) {
                top.linkTo(backgroundImage.bottom, margin = (-24).dp) // Overlap
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            }
        ) {
            // Content inside card managed by inner ConstraintLayout
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                val (title, description, duration, buttons) = createRefs()
                
                // Story Title
                Text(
                    text = storyTitle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 34.sp,
                    modifier = Modifier.constrainAs(title) {
                        top.linkTo(parent.top, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                )
                
                // Story Description
                Text(
                    text = storyDescription,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 24.sp,
                    modifier = Modifier.constrainAs(description) {
                        top.linkTo(title.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                )
                
                // Duration Row
                Row(
                    modifier = Modifier.constrainAs(duration) {
                        bottom.linkTo(buttons.top, margin = 16.dp)
                        start.linkTo(parent.start)
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_timer),
                        contentDescription = "Duration",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = storyDuration,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.constrainAs(buttons) {
                        bottom.linkTo(parent.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                ) {
                    // Read Button
                    OutlinedButton(
                        onClick = onReadClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, Color.White.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_book),
                            contentDescription = "Read",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.story_read),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Listen Button
                    Button(
                        onClick = onListenClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volume),
                            contentDescription = "Listen",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.story_listen),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * 🎯 Advanced ConstraintLayout with Chains
 */
@Composable
fun AdvancedConstraintLayoutStoryScreen(
    storyTitle: String = "The turtle & hair",
    onBackClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        val (image, backBtn, favBtn, content) = createRefs()
        
        // Create horizontal chain for top buttons
        createHorizontalChain(
            backBtn, favBtn,
            chainStyle = androidx.constraintlayout.compose.ChainStyle.SpreadInside
        )
        
        // Background Image
        AsyncImage(
            model = "https://images.pexels.com/photos/1287145/pexels-photo-1287145.jpeg",
            contentDescription = "Story Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.constrainAs(image) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
                height = Dimension.value(screenHeight * 0.33f)
            }
        )
        
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                .constrainAs(backBtn) {
                    top.linkTo(parent.top, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "Back",
                tint = Color.White
            )
        }
        
        // Favorite Button
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                .constrainAs(favBtn) {
                    top.linkTo(parent.top, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_fav_big),
                contentDescription = "Favorite",
                tint = Color.White
            )
        }
        
        // Content
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1A4A)),
            modifier = Modifier.constrainAs(content) {
                top.linkTo(image.bottom, margin = (-24).dp)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = storyTitle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                // Add more content...
            }
        }
    }
}

/**
 * 🚀 Performance Optimized ConstraintLayout
 */
@Composable
fun OptimizedConstraintLayoutStoryScreen(
    storyTitle: String = "The turtle & hair",
    onBackClick: () -> Unit = {}
) {
    BoxWithConstraints { // Auto-sizing based on available space
        val maxHeight = maxHeight
        val imageHeight = maxHeight * 0.33f
        
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
        ) {
            val (image, appbar, content) = createRefs()
            
            // Background Image
            AsyncImage(
                model = "https://images.pexels.com/photos/1287145/pexels-photo-1287145.jpeg",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.value(imageHeight)
                }
            )
            
            // Transparent AppBar
            Row(
                modifier = Modifier.constrainAs(appbar) {
                    top.linkTo(parent.top, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    width = Dimension.fillToConstraints
                }
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.background(
                        Color.Black.copy(alpha = 0.6f),
                        CircleShape
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = { },
                    modifier = Modifier.background(
                        Color.Black.copy(alpha = 0.6f),
                        CircleShape
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_fav_big),
                        contentDescription = "Favorite",
                        tint = Color.White
                    )
                }
            }
            
            // Content
            Text(
                text = storyTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.constrainAs(content) {
                    top.linkTo(image.bottom, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    width = Dimension.fillToConstraints
                }
            )
        }
    }
}