package com.naptune.lullabyandstory.presentation.components.common

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.ui.theme.FilterTextColor

/**
 * Reusable FilterHeader component for LullabyScreen and StoryScreen
 *
 * @param contentType The type of content being filtered (e.g., "Lullabies", "Stories")
 * @param isShowingPopular Whether currently showing popular items or all items
 * @param onFilterClick Callback when filter button is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
fun FilterHeader(
    contentType: String,
    isShowingPopular: Boolean,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Title
        Text(
            text = if (isShowingPopular) "Popular $contentType" else "All $contentType",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            ),
            color = Color.White
        )

        // Right: Filter Button
        Row(
            modifier = Modifier
                .clickable(
                    indication = null, // ✅ Remove ripple effect
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onFilterClick()
                    // ✅ Show toast when filter changes
                    val message = if (isShowingPopular) {
                        "Showing All $contentType"
                    } else {
                        "Showing Popular $contentType"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Fixed width for text to prevent button shifting
            Text(
                text = if (isShowingPopular) "Popular" else "Recent",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = FilterTextColor
                ),
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_filter_lullaby_story),
                contentDescription = "Filter",
                tint = Color.Unspecified,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
