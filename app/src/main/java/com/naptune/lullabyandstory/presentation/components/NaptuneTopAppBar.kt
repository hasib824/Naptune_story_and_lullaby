package com.naptune.lullabyandstory.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.data.billing.BillingManager
import com.naptune.lullabyandstory.presentation.navigation.Screen
import com.naptune.lullabyandstory.ui.theme.AccentColor
import com.naptune.lullabyandstory.ui.theme.DarkItemColor
import com.naptune.lullabyandstory.ui.theme.PremiumButtonTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaptuneTopAppBar(
    onPremiumClick: () -> Unit,
    currentScreen: String,
    isPurchased: Boolean,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.Transparent) // Transparent to show gradient
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Naptune title with Winky Sans font
            Text(
                text = if (currentScreen.equals(Screen.Story.route)) "Stories"
                else if (currentScreen.equals(Screen.Lullaby.route)) "Lullabies"
                else if (!currentScreen.equals(Screen.Main.route)) currentScreen.replaceFirstChar { it.uppercase() }
                else stringResource(R.string.app_title),
                style = if (currentScreen.equals(Screen.Main.route)) MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp
                )
                else MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), // Uses Winky Sans from theme
                color = Color.White // White text on gradient background
            )

            // Right side - Premium button with Nunito Regular

            if (!isPurchased) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(5.dp)) // First clip
                        .clickable { onPremiumClick() } // Then clickable
                        .background(
                            AccentColor
                            /* Brush.horizontalGradient(
                                 colors = listOf(AccentColorGradientLeft, AccentColorGradientRight)
                             )*/
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
                {
                    Text(
                        text = stringResource(R.string.get_pro),
                        style = PremiumButtonTextStyle.copy(fontWeight = FontWeight.SemiBold), // Uses Nunito Regular
                        color = DarkItemColor, // White text on gradient background
                        modifier = Modifier
                            .padding(end = 8.dp)

                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_get_pro),
                        contentDescription = "get pro Icon",
                        tint = DarkItemColor
                    )
                }
            }

        }
    }
}
