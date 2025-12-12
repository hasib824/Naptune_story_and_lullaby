package com.naptune.lullabyandstory.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.presentation.components.common.responsive.ResponsiveStylesOfProfileScreen
import com.naptune.lullabyandstory.presentation.components.common.responsive.getProfileScreenResponsiveStyles
import com.naptune.lullabyandstory.presentation.components.common.responsive.rememberScreenDimensionManager

@Composable
fun SettingsScreen(
    contentBottomPadding: Dp = 0.dp
) {
    val rememberScreenDimensionManager = rememberScreenDimensionManager()
    val responsiveStyles = getProfileScreenResponsiveStyles(rememberScreenDimensionManager)

    // Settings items
    val settingsItems = listOf(
        SettingsItem(
            title = stringResource(R.string.profile_privacy_policy),
            icon = R.drawable.ic_privacy_policy,
            onClick = {
                // TODO: Handle Privacy Policy click
            }
        ),
        SettingsItem(
            title = stringResource(R.string.profile_acknowledgement),
            icon = R.drawable.ic_acknowledgement,
            onClick = {
                // TODO: Handle Acknowledgement click
            }
        ),
        SettingsItem(
            title = stringResource(R.string.profile_restore_purchase),
            icon = R.drawable.ic_restore_purchase,
            onClick = {
                // TODO: Handle Restore Purchase click
            }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 20.dp) // ✅ Pre-computed (20-8)
    ) {
        // Settings items in LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp, bottom = contentBottomPadding) // ✅ Pre-computed (40-18)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(settingsItems) { item ->
                SettingsItemComponent(
                    modifier = Modifier.fillMaxWidth(),
                    title = item.title,
                    icon = item.icon,
                    onClick = item.onClick,
                    responsiveStyles = responsiveStyles
                )
            }
        }
    }
}

// Data class for settings items
private data class SettingsItem(
    val title: String,
    val icon: Int,
    val onClick: () -> Unit
)

@Composable
private fun SettingsItemComponent(
    modifier: Modifier = Modifier,
    title: String,
    icon: Int,
    onClick: () -> Unit = {},
    responsiveStyles: ResponsiveStylesOfProfileScreen
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(
                start = responsiveStyles.profileitemHorizontalPadding,
                end = responsiveStyles.profileitemHorizontalPadding,
                top = responsiveStyles.profileitemVerticalPadding,
                bottom = responsiveStyles.profileitemVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            tint = Color.Unspecified,
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            color = Color.Unspecified
        )
    }
}
