package com.naptune.lullabyandstory.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naptune.lullabyandstory.ui.theme.PrimaryColor
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.ui.theme.SecondaryColor

/**
 * Reward Video Bottom Sheet Colors - Exact match from Figma design
 */
object RewardVideoColors {
    val backgroundColor = Color(0xFF2C3E50) // Dark blue-gray background
    val titleColor = Color(0xFFFFFFFF) // White title
    val descriptionColor = Color(0xFFE8E8E8) // Light gray description
    val closeButtonBg = Color(0xFF4A5568) // Gray close button background
    val closeIconColor = Color(0xFFFFFFFF) // White close icon
    val cancelButtonText = Color(0xFFFFFFFF) // White cancel text
    val watchButtonBg = Color(0xFF1E88E5) // Blue watch button
    val watchButtonText = Color(0xFFFFFFFF) // White watch text
}

/**
 * Reward Video Modal Bottom Sheet - Exact Figma Design
 *
 * @param onDismiss Callback when the bottom sheet is dismissed (Close or Cancel)
 * @param onWatchClick Callback when the Watch button is clicked
 * @param sheetState The state of the modal bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardVideoBottomSheet(
    onDismiss: () -> Unit,
    onWatchClick: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PrimaryColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null, // No drag handle for this design
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            // Header with Title and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title
                Text(
                    text = "Reward Video",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                // Close Button
                IconButton(
                    onClick = onDismiss,

                ) {
                    Icon(
                       painter = painterResource(R.drawable.ic_reward_close),
                        contentDescription = "Close",
                        tint = Color.Unspecified,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description Text
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Want to unlock this item?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                )
                Text(
                    text = "Watch a reward video!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(0.4f)
                        .height(44.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = RewardVideoColors.cancelButtonText
                    ),
                    shape = RoundedCornerShape(36.dp)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.sp
                    )
                }

                // Watch Button
                Button(
                    onClick = onWatchClick,
                    modifier = Modifier
                        .weight(0.36f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(36.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Text(
                        text = "Watch",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp
                    )
                }
            }
        }
    }
}

/**
 * Example Usage Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardVideoScreen() {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { showBottomSheet = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E88E5)
            )
        ) {
            Text("Show Reward Video")
        }
    }

    if (showBottomSheet) {
        RewardVideoBottomSheet(
            onDismiss = {
                showBottomSheet = false
            },
            onWatchClick = {
                // Handle watch video action
                showBottomSheet = false
                // Navigate to video or start video playback
            },
            sheetState = sheetState
        )
    }
}

/**
 * Alternative: With custom colors
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRewardVideoBottomSheet(
    onDismiss: () -> Unit,
    onWatchClick: () -> Unit,
    title: String = "Reward Video",
    description1: String = "Want to unlock this item?",
    description2: String = "Watch a reward video!",
    cancelText: String = "Cancel",
    watchText: String = "Watch",
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = RewardVideoColors.backgroundColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = RewardVideoColors.titleColor,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = RewardVideoColors.closeButtonBg,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = RewardVideoColors.closeIconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Description
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = description1,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = RewardVideoColors.descriptionColor,
                    lineHeight = 32.sp
                )
                Text(
                    text = description2,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = RewardVideoColors.descriptionColor,
                    lineHeight = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(0.4f)
                        .height(72.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = RewardVideoColors.cancelButtonText
                    ),
                    shape = RoundedCornerShape(36.dp)
                ) {
                    Text(
                        text = cancelText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onWatchClick,
                    modifier = Modifier
                        .weight(0.6f)
                        .height(72.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RewardVideoColors.watchButtonBg,
                        contentColor = RewardVideoColors.watchButtonText
                    ),
                    shape = RoundedCornerShape(36.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Text(
                        text = watchText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}