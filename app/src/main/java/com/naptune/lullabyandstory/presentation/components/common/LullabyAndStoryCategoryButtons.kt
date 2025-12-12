package com.naptune.lullabyandstory.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.naptune.lullabyandstory.R

@Composable
fun LullabyAndStoryCategoryButtons(
    currentPage: Int,
    onCategoryChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CategoryButton(
            text = stringResource(R.string.category_all),
            isSelected = currentPage == 0,
            onClick = { onCategoryChange(0) },
            modifier = Modifier.wrapContentWidth()
        )

        CategoryButton(
            text = stringResource(R.string.category_popular),
            isSelected = currentPage == 1,
            onClick = { onCategoryChange(1) },
            modifier = Modifier.wrapContentWidth()
        )

        CategoryButton(
            text = stringResource(R.string.category_free),
            isSelected = currentPage == 2,
            onClick = { onCategoryChange(2) },
            modifier = Modifier.wrapContentWidth()
        )
    }
}

@Composable
private fun CategoryButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(
                Color.White.copy(alpha = if (isSelected) 1.0f else 0.12f)
            )
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(32.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 0.dp),
            text = text,
            fontSize = 16.sp,
            style = MaterialTheme.typography.titleSmall.copy(
                brush = if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF392465), // Top
                            Color(0xFF6467D2)  // Bottom
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                } else {
                    null
                },
            ),
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}