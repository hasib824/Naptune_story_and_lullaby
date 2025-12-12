package com.naptune.lullabyandstory.presentation.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.ui.theme.AccentColor
import com.naptune.lullabyandstory.ui.theme.DarkItemColor
import com.naptune.lullabyandstory.ui.theme.NunitoFamily
import com.naptune.lullabyandstory.ui.theme.PremmiumHeadlineStyle
import com.naptune.lullabyandstory.ui.theme.SecondaryColor

@Composable
fun PremiumScreen(
    onBackClick: () -> Unit = {},
    onProceedClick: (planType: PremiumPlan) -> Unit = {},
    onRestoreClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    viewModel: PremiumViewModel = hiltViewModel()
) {
    var selectedPlan by remember { mutableStateOf(PremiumPlan.YEARLY) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle UI state effects
    LaunchedEffect(uiState.showSuccessMessage) {
        if (uiState.showSuccessMessage) {
            onBackClick() // Close premium screen on successful purchase
        }
    }
    
    // Handle error messages with Snackbar
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
    }

    // Handle success messages
    LaunchedEffect(uiState.showAlreadyOwnedMessage) {
        if (uiState.showAlreadyOwnedMessage) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.premium_already_own),
                duration = SnackbarDuration.Short,

            )
            viewModel.clearMessages()
        }
    }

    // Handle restore messages
    uiState.showRestoreMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp ,top =16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_premium),
                        contentDescription = stringResource(R.string.content_desc_close),
                        tint = Color.White.copy(alpha = 1f),
                    )
                }
            }
        },
        snackbarHost = { 
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    val isError = snackbarData.visuals.message.contains("failed", ignoreCase = true) ||
                                  snackbarData.visuals.message.contains("error", ignoreCase = true) ||
                                  snackbarData.visuals.message.contains("not available", ignoreCase = true) ||
                                  snackbarData.visuals.message.contains("connection", ignoreCase = true)
                    
                    Snackbar(
                        snackbarData = snackbarData,
                        containerColor = if (isError) Color(0xFFE53E3E) else Color(0xFF4CAF50), // Red for errors, Green for success
                        contentColor = Color.White,
                        actionColor = Color.White
                    )
                }
            ) 
        },
        containerColor = Color.Transparent
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background( Color.Transparent
                )
                .padding(paddingValues)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

            Spacer(modifier = Modifier.height(0.dp))

            // Title
            Text(
                text = stringResource(R.string.naptune_premium),
                style = PremmiumHeadlineStyle,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.access_all_content),
                style = MaterialTheme.typography.titleMedium.copy( fontWeight = FontWeight.Medium),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Features list
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PremiumFeatureItem(
                    title = stringResource(R.string.premium_feature_access_content),
                    icon = Icons.Default.Check
                )
                PremiumFeatureItem(
                    title = stringResource(R.string.premium_feature_new_content),
                    icon = Icons.Default.Check
                )
                PremiumFeatureItem(
                    title = stringResource(R.string.premium_feature_ad_free),
                    icon = Icons.Default.Check
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pricing cards
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PricingCard(
                    plan = PremiumPlan.MONTHLY,
                    title = stringResource(R.string.premium_pricing_monthly),
                    subtitle = stringResource(R.string.premium_pricing_cancel_anytime),
                    price = "$3.99/mo",
                    isSelected = selectedPlan == PremiumPlan.MONTHLY,
                    onClick = { selectedPlan = PremiumPlan.MONTHLY }
                )

                PricingCard(
                    plan = PremiumPlan.YEARLY,
                    title = stringResource(R.string.premium_pricing_yearly),
                    subtitle = stringResource(R.string.premium_pricing_yearly_price),
                    price = "$2.99/mo",
                    badge = stringResource(R.string.premium_pricing_save_badge),
                    isSelected = selectedPlan == PremiumPlan.YEARLY,
                    onClick = { selectedPlan = PremiumPlan.YEARLY }
                )

                PricingCard(
                    plan = PremiumPlan.LIFETIME,
                    title = stringResource(R.string.premium_pricing_lifetime),
                    subtitle = stringResource(R.string.premium_pricing_lifetime_subtitle),
                    price = "$59",
                    isSelected = selectedPlan == PremiumPlan.LIFETIME,
                    onClick = { selectedPlan = PremiumPlan.LIFETIME }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Proceed button
            Button(
                onClick = { 
                    if (context is android.app.Activity) {
                        viewModel.purchasePremium(context, selectedPlan)
                    }
                    onProceedClick(selectedPlan) 
                },
                enabled = !uiState.isLoading && !uiState.isConnectionError,
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryColor // Bright green
                ),

            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = if (uiState.isPremium) stringResource(R.string.premium_button_already) else stringResource(R.string.premium_button_proceed),
                        fontSize = 18.sp,
                        fontFamily = NunitoFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.premium_restore),
                    fontSize = 16.sp,
                    fontFamily = NunitoFamily,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .clickable { 
                            viewModel.restorePurchases()
                            onRestoreClick() 
                        }
                        .padding(8.dp)
                )

                Text(
                    text = stringResource(R.string.text_bullet_separator),
                    fontSize = 16.sp,
                    fontFamily = NunitoFamily,
                    color = Color.White.copy(alpha = 0.5f)
                )

                Text(
                    text = stringResource(R.string.premium_privacy_terms),
                    fontSize = 16.sp,
                    fontFamily = NunitoFamily,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .clickable { onPrivacyClick() }
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        }
    }
}

@Composable
private fun PremiumFeatureItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Green checkmark icon
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_premium_list), // তোমার কাস্টম আইকন
                contentDescription = null,
                tint = Color.Unspecified,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            fontFamily = NunitoFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 1f),
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun PricingCard(
    plan: PremiumPlan,
    title: String,
    subtitle: String,
    price: String,
    badge: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .then(
                    if (isSelected) {
                        Modifier.border( color =AccentColor,
                       /*     brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF52B0FF),  // Purple
                                    Color(0xFFDCA4F8)   // Teal
                                )
                            ),*/
                            width = 2.dp,
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else {
                        Modifier.border(
                            width = 1.dp,
                            color = Color(0xFF667085),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontFamily = NunitoFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        fontFamily = NunitoFamily,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = price,
                    fontSize = 20.sp,
                    fontFamily = NunitoFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Badge for savings
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset((-16).dp, (-12).dp)
                    .background(
                        AccentColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badge,
                    fontFamily = NunitoFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkItemColor
                )
            }
        }
    }
}

enum class PremiumPlan {
    MONTHLY,
    YEARLY,
    LIFETIME
}