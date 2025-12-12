package com.naptune.lullabyandstory.presentation.components.admob

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.domain.model.AdSizeType

@Composable
fun AdLoadingUI(
    adSize: AdSizeType,
    backgroundColor: Color = Color.White.copy(alpha = 0.2f),
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    // ✅ Size-appropriate loading elements based on ad size
    val (iconSize, textStyle, spacing) = when (adSize) {
        AdSizeType.MEDIUM_RECTANGLE -> Triple(
            32.dp, 
            MaterialTheme.typography.bodyMedium, 
            12.dp
        )
        AdSizeType.LARGE_BANNER -> Triple(
            28.dp, 
            MaterialTheme.typography.bodyMedium, 
            10.dp
        )
        AdSizeType.ANCHORED_ADAPTIVE_BANNER -> Triple(
            24.dp, 
            MaterialTheme.typography.bodySmall, 
            8.dp
        )
        AdSizeType.INLINE_ADAPTIVE_BANNER -> Triple(
            24.dp, 
            MaterialTheme.typography.bodySmall, 
            8.dp
        )
        AdSizeType.BANNER -> Triple(
            20.dp, 
            MaterialTheme.typography.bodySmall, 
            6.dp
        )
        else -> Triple(
            22.dp, 
            MaterialTheme.typography.bodySmall, 
            8.dp
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(iconSize), 
                color = textColor,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.height(spacing))
            Text(
                text = stringResource(R.string.ad_loading),
                style = textStyle,
                color = textColor
            )
        }
    }
}

@Composable
fun AdErrorUI(
    adSize: AdSizeType,
    backgroundColor: Color = Color.Red.copy(alpha = 0.1f),
    textColor: Color = Color.Red.copy(alpha = 0.7f),
    modifier: Modifier = Modifier
) {
    val textStyle = when (adSize) {
        AdSizeType.MEDIUM_RECTANGLE -> MaterialTheme.typography.bodyMedium
        AdSizeType.LARGE_BANNER -> MaterialTheme.typography.bodyMedium  
        else -> MaterialTheme.typography.bodySmall
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.ad_failed),
            style = textStyle,
            color = textColor
        )
    }
}