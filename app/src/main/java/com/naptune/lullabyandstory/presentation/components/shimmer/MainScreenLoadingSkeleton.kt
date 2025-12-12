package com.naptune.lullabyandstory.presentation.components.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.presentation.components.common.SectionHeader

/**
 * Loading skeleton for MainScreen with shimmer effect
 * Replicates the exact structure of MainScreen when loading
 *
 * Shows shimmer for:
 * - Today's Pick (ViewPager with 2 lullabies)
 * - Popular Lullabies (4 items in 2x2 grid)
 * - Popular Stories (4 items)
 *
 * Section headers are visible (no shimmer)
 * NO shimmer for Favourites section (only shows when data exists)
 */
@Composable
fun MainScreenLoadingSkeleton(
    contentBottomPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp), // Same as MainScreen
        contentPadding = PaddingValues(bottom = contentBottomPadding)
    ) {
        // Today's Pick Section with shimmer
        item {
            TodaysPickShimmer()
        }

        // Popular Lullabies Section
        item {
            PopularLullabiesShimmer()
        }

        // Popular Stories Section
        item {
            PopularStoriesShimmer()
        }

        // NO Favourites section in loading state
    }
}

/**
 * Today's Pick section shimmer
 * Shows 2 lullaby shimmer items in a row
 */
@Composable
private fun TodaysPickShimmer() {
    Column(
        modifier = Modifier.padding(horizontal = 4.dp) // ✅ Match actual TodaysPickSection padding
    ) {
        // Section title "Today's Pick" - visible, no shimmer
        SectionHeader(
            title = stringResource(R.string.section_todays_pick),
            showSeeAll = false // ✅ Adds 20.dp start padding internally (total: 4+20=24.dp from left)
        )

        // ✅ No extra Spacer - SectionHeader already has 16.dp bottom padding

        // 2 lullaby items in a row (same as TodaysPickLullabyPage)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), // Same as original
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Same spacing
        ) {
            // First lullaby shimmer
            LullabyItemShimmer(
                modifier = Modifier.weight(1f)
            )

            // Second lullaby shimmer
            LullabyItemShimmer(
                modifier = Modifier.weight(1f)
            )
        }

        // ✅ Page Indicators (Dots) - Same as actual TodaysPickSection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(2) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == 0)
                                Color.White // First dot active
                            else
                                Color.White.copy(alpha = 0.3f) // Second dot inactive
                        )
                )
                if (index < 1) {
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }
    }
}

/**
 * Popular Lullabies section shimmer
 * Shows 4 lullaby items in 2x2 grid
 */
@Composable
private fun PopularLullabiesShimmer() {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) { // ✅ 20.dp padding
        // Section header - visible, no shimmer
        SectionHeader(
            title = stringResource(R.string.section_popular_lullaby),
            showSeeAll = true, // ✅ Adds 0.dp start padding (total: 20+0=20.dp from left edge)
            onSeeAllClick = null // ✅ Don't show "See All" button in loading state
        )

        // 2x2 grid (same structure as PopularLullabySection)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp) // ✅ Same spacing
        ) {
            // First row - 2 items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LullabyItemShimmer(modifier = Modifier.weight(1f))
                LullabyItemShimmer(modifier = Modifier.weight(1f))
            }

            // Second row - 2 items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LullabyItemShimmer(modifier = Modifier.weight(1f))
                LullabyItemShimmer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Popular Stories section shimmer
 * Shows 4 story items vertically
 */
@Composable
private fun PopularStoriesShimmer() {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) { // ✅ 20.dp padding
        // Section header - visible, no shimmer
        SectionHeader(
            title = stringResource(R.string.section_popular_story),
            showSeeAll = true, // ✅ Adds 0.dp start padding (total: 20+0=20.dp from left edge)
            onSeeAllClick = null // ✅ Don't show "See All" button in loading state
        )

        // 4 story items in column (same as PopularStorySection)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp) // Same spacing
        ) {
            repeat(4) {
                StoryItemShimmer()
            }
        }
    }
}
