package com.naptune.lullabyandstory.presentation.story.storymanager

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naptune.lullabyandstory.presentation.components.admob.SmoothBannerAdSection
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import com.naptune.lullabyandstory.presentation.components.common.responsive.getAudioManagerScreenResponisiveSizes
import com.naptune.lullabyandstory.presentation.components.common.responsive.rememberScreenDimensionManager
import com.naptune.lullabyandstory.ui.theme.AccentColor
import com.naptune.lullabyandstory.ui.theme.PrimaryColor
import com.naptune.lullabyandstory.ui.theme.StoryFontColor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryManagerScreen(
    storyDomainModel: StoryDomainModel?,
    viewModel: StoryManagerViewModel = hiltViewModel(),
    onBackClick: () -> Boolean,
    onReadStoryClick: (StoryDomainModel) -> Unit,
    onPlayStoryClick: (StoryDomainModel?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()
    // ✅ FIX: Collect premium status to hide ads
    val isPurchased by viewModel.isPurchased.collectAsStateWithLifecycle()

    val screenDimensionManager = rememberScreenDimensionManager()

    val responsiveSizes =
        getAudioManagerScreenResponisiveSizes(screenDimensionManager)

    // ✅ ARCHITECTURE FIX: Ad initialization moved to ViewModel init block
    // Ads are only initialized for free users in StoryManagerViewModel
    // ✅ Handle null story case to prevent empty state flash
    if (storyDomainModel == null) {
        // Show loading indicator instead of empty content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
           // .background(StoryManagerBackground)
    ) {

        // "https://images.pexels.com/photos/1287145/pexels-photo-1287145.jpeg"

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 0.dp)
        ) {
            // 1. Fullscreen Image
            AsyncImage(
                model = storyDomainModel?.imagePath,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(responsiveSizes.imageSize)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
                contentScale = ContentScale.Crop,
                contentDescription = "",
                alpha = 0.40f
            )

            TopAppBar(
                modifier = Modifier.padding(start = 12.dp),
                title = { Text("") },
                navigationIcon = {

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Transparent, CircleShape)
                            .clickable
                            { onBackClick() }

                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu_back), // তোমার কাস্টম আইকন
                            contentDescription = "Back",
                            tint = Color.Unspecified,
                            modifier = Modifier.padding(0.dp)
                        )
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                )
            )
        }

        // ✅ Reusable Smooth Banner Ad Component with story-themed loading
        // ✅ FIX: Only show ads for free users
        if (!isPurchased) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = if (isNetworkAvailable) 16.dp else 0.dp)
            )
            {
                SmoothBannerAdSection(
                    isNetworkAvailable = isNetworkAvailable,
                    adState = uiState.adState,
                    loadingText = "Loading Story Ad...", // Custom loading text for StoryManagerScreen
                    enableDebugLogging = true // Enable debug logging for StoryManagerScreen
                )
            }
        }

        ConstraintLayout(
            Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
        )
        {
            val (storyHeadlineRef, storeDescRef, readStoryBtnRef, playStoryBtnRef, spacerRef, hl1Ref, hl2Ref, timerIcRef, timerTextRef) = createRefs()

            Text(
                text = storyDomainModel?.storyName ?: "hi ",
                modifier = Modifier.padding(bottom = 10.dp).constrainAs(storyHeadlineRef)
                {
                    top.linkTo(
                        parent.top,
                        responsiveSizes.titleTopMargin
                    ) // Direct from parent top since banner ad is outside ConstraintLayout
                    start.linkTo(parent.start)

                },
                style = MaterialTheme.typography.titleMedium.copy( fontWeight = FontWeight.Bold, fontSize = 24.sp),
                fontSize = responsiveSizes.storyTitleFontSize
            )

            Text(
                text = getLimitedText(
                    storyDomainModel?.storyDescription ?: "Empty Stroy",
                    responsiveSizes.limitedStorySize
                ),
                modifier = Modifier.constrainAs(storeDescRef)
                {
                    top.linkTo(storyHeadlineRef.bottom, 8.dp)
                    start.linkTo(storyHeadlineRef.start)

                },
                style = MaterialTheme.typography.bodyLarge.copy( color = StoryFontColor),
                fontSize = responsiveSizes.storyBodyFontSize
            )

            HorizontalDivider(
                modifier = Modifier
                    .constrainAs(hl2Ref) {
                        bottom.linkTo(timerTextRef.top, responsiveSizes.horizontalLinePadding)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(horizontal = 8.dp),
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.40f)
            )

            Icon(
                modifier = Modifier
                    .constrainAs(timerIcRef) {
                        bottom.linkTo(timerTextRef.bottom)
                        top.linkTo(timerTextRef.top)

                    }
                    .padding(end = 8.dp),
                painter = painterResource(R.drawable.ic_timer_read_story),
                contentDescription = "",
                tint = Color.Unspecified
            )

            Text(
                text = run {
                    // Try to parse as integer first
                    val minutes = storyDomainModel.story_reading_time.toIntOrNull()
                    if (minutes != null) {
                        stringResource(R.string.story_duration_format, minutes)
                    } else {
                        // Fallback: use the string as is
                        "${storyDomainModel.story_reading_time} minutes story"
                    }
                },
                style = MaterialTheme.typography.bodyLarge.copy( color = StoryFontColor),
                fontSize = responsiveSizes.storyBodyFontSize,
                modifier = Modifier.constrainAs(timerTextRef) {

                    bottom.linkTo(hl1Ref.top, responsiveSizes.horizontalLinePadding)

                })

            createHorizontalChain(timerIcRef, timerTextRef, chainStyle = ChainStyle.Packed)

            HorizontalDivider(
                modifier = Modifier
                    .constrainAs(hl1Ref) {
                        bottom.linkTo(
                            readStoryBtnRef.top,
                            responsiveSizes.horizontalDeviderBottomMargin
                        )
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(horizontal = 8.dp),
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.40f)
            )


            Row(
                Modifier
                    .constrainAs(readStoryBtnRef)
                    {
                        bottom.linkTo(parent.bottom, responsiveSizes.readStoryMarginBottom) // 48.dp
                        end.linkTo(playStoryBtnRef.start, margin = 16.dp)
                        width = Dimension.fillToConstraints

                    }
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.Transparent)
                    .border(2.dp, AccentColor, RoundedCornerShape(100.dp))
                    .clickable { onReadStoryClick(storyDomainModel) }
                    .padding(responsiveSizes.btnPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            )
            {
                Text(stringResource(R.string.story_read), style = MaterialTheme.typography.bodyLarge)
                Icon(
                    painter = painterResource(R.drawable.ic_read_for_btn),
                    contentDescription = "",
                    tint = Color.Unspecified
                )
            }

            Spacer(Modifier
                .width(16.dp)
                .constrainAs(spacerRef) {
                    start.linkTo(readStoryBtnRef.end)
                    end.linkTo(playStoryBtnRef.start)
                    top.linkTo(readStoryBtnRef.top)
                    bottom.linkTo(readStoryBtnRef.bottom)
                })

            Row(
                Modifier
                    .constrainAs(playStoryBtnRef)
                    {
                        top.linkTo(readStoryBtnRef.top)
                        bottom.linkTo(readStoryBtnRef.bottom)
                        start.linkTo(readStoryBtnRef.end, margin = 16.dp)
                        width = Dimension.fillToConstraints
                    }
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = AccentColor,
                        shape = RoundedCornerShape(100.dp)
                    )
                    .clickable
                    {
                        // ✅ Check network connection before playing story
                        viewModel.handleIntent(
                            StoryManagerIntent.CheckNetworkForStoryStream(
                                story = storyDomainModel,
                                onSuccess = { validatedStory ->
                                    onPlayStoryClick(validatedStory)
                                    Log.e("StoryItem Manager", validatedStory.toString())
                                }
                            )
                        )
                    }
                    .padding(responsiveSizes.btnPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            )
            {
                Icon(
                    painter = painterResource(R.drawable.ic_volume_for_btn),
                    contentDescription = "",
                    tint = Color.Unspecified
                )
                Text(stringResource(R.string.story_listen), style = MaterialTheme.typography.bodyLarge)
            }

            createHorizontalChain(
                readStoryBtnRef,
                playStoryBtnRef,
                chainStyle = ChainStyle.SpreadInside
            )


        }

    }
}

fun getLimitedText(input: String, textsize: Int = 210): String {
    val displayText = if (input.length > textsize) {
        input.take(textsize) + ".."
    } else input
    return displayText
}