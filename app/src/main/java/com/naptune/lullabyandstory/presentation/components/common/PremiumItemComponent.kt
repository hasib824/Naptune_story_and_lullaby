package com.naptune.lullabyandstory.presentation.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naptune.lullabyandstory.ui.theme.NunitoFamily

@Composable
fun <T> PremiumItemComponent(
    onProButtonClick: (item: T) -> Unit,
    item: T,
    modifier: Modifier = Modifier
) {
    Box {
        Surface(
            modifier = modifier
                .align(Alignment.TopStart)
                .clickable {
                    // Log.d("AdComponent", "🎁 Rewarded ad button clicked for: $itemName")
                    onProButtonClick(item)
                },
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\uD83D\uDC51",
                    fontSize = 14.sp,
                    fontFamily = NunitoFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Pro",
                    fontSize = 14.sp,
                    fontFamily = NunitoFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }
        }
    }
}
