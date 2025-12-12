package com.naptune.lullabyandstory.presentation.components.common

import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow                    // ✅ For shadow effect
import androidx.compose.ui.graphics.Brush               // ✅ For gradient (if needed)
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import coil.compose.AsyncImage
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.ui.theme.AccentColor
import com.naptune.lullabyandstory.ui.theme.NeutralColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanVolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(20.dp),

        // ✅ Hide ALL default components (এতে white dot যাবে)
        colors = SliderDefaults.colors(
            thumbColor = Color.Transparent,
            activeTrackColor = Color.Transparent,
            inactiveTrackColor = Color.Transparent,
            disabledThumbColor = Color.Transparent,
            disabledActiveTrackColor = Color.Transparent,
            disabledInactiveTrackColor = Color.Transparent
        ),

        // 🎨 Custom track - full width, no gaps
        track = { sliderState ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()  // ✅ Full width, no gaps
                    .height(4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(NeutralColor)
            ) {
                // Active white portion
                Box(
                    modifier = Modifier
                        .fillMaxWidth(sliderState.value)
                        .height(4.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(AccentColor)
                )
            }
        },

        // 🎯 Custom thumb
        thumb = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    )
}