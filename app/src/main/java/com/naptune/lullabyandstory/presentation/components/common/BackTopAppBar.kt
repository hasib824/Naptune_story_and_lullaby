package com.naptune.lullabyandstory.presentation.components.common


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.naptune.lullabyandstory.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopAppBar(
    onBackClick: () -> Unit,
    title: String,
    currentScreen: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(/*if (currentScreen == Screen.StoryReader.route) StoryManagerBackground else*/ Color.Transparent) // Transparent to show gradient
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {

            val (backIconRef, titleRef) = createRefs()

            Box(
                modifier = Modifier
                    .constrainAs(backIconRef)
                    {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
                    .clip(CircleShape)
                    .background(Color.Transparent, CircleShape)
                    .clickable
                    { onBackClick() }

            )
            {
                Icon(
                    painter = painterResource(R.drawable.ic_menu_back),
                    contentDescription = "back",
                    tint = Color.Unspecified
                )
            }


            Text(
                modifier = Modifier.constrainAs(titleRef)
                {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                text = title,
                style = MaterialTheme.typography.titleMedium, // Uses Winky Sans from theme
                color = Color.White // White text on gradient background
            )

        }
    }
}
