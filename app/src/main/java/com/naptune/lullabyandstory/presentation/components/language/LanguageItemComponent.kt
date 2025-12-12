package com.naptune.lullabyandstory.presentation.components.language

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.naptune.lullabyandstory.data.model.Language
import com.naptune.lullabyandstory.ui.theme.AccentColor
import com.naptune.lullabyandstory.ui.theme.LanguageItemBackgroundColor
import com.naptune.lullabyandstory.ui.theme.ModalStrokeColor
import com.naptune.lullabyandstory.R
@Composable
fun LanguageItemComponent(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(
                indication = null, // 👈 removes ripple effect
                interactionSource = remember { MutableInteractionSource() } // 👈 required when removing indication
            ) {
                onClick()

            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                LanguageItemBackgroundColor
            else
                Color.Transparent
        ),
       /* border = if (isSelected)
            BorderStroke(1.dp, AccentColor)
        else
            BorderStroke(1.dp, ModalStrokeColor),*/
        shape = RoundedCornerShape(0.dp) // Reduced from timer's 36dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ✅ Circular flag image (no Box needed!)
                AsyncImage(
                    model = language.flagImageUrl, // ✅ Flag image from CDN
                    contentDescription = "Flag of ${language.name}",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape), // Clip directly to circular shape
                    contentScale = ContentScale.Crop // Crop to fill the circle completely
                )

                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = language.nativeName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color =
                          if(isSelected) AccentColor else Color.White

                    )
                    Text(
                        text = language.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)

                    )
                }
            }

            // Selection Indicator
            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.ic_language_selected),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
            }
        }
    }
}