package com.naptune.lullabyandstory.presentation.splash


import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.ui.theme.AccentColorSecondary
import com.naptune.lullabyandstory.ui.theme.SecondaryColor

@Composable
fun SplashScreen(
    onNavigateToNext: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
        val state by viewModel.state.collectAsStateWithLifecycle()

      /*  LaunchedEffect(state.shouldNavigateToNext) {
            if (state.shouldNavigateToNext) {
                onNavigateToNext()
            }
        }*/

    val systemUiController = rememberSystemUiController()
    LaunchedEffect(Unit) {
        systemUiController.isSystemBarsVisible = false
    }
    val view = LocalView.current

    val activity = LocalActivity.current
    val window = activity?.window
    /*    SideEffect {
            WindowCompat.getInsetsController(window!!, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }*/

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // আপনার PNG image এখানে দিন
        Image(
            painter = painterResource(id = R.drawable.splash_screen_vector), // আপনার image দিন
            contentDescription = stringResource(R.string.content_desc_splash),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(5f))

            Column(modifier = Modifier.weight(2.35f).padding(horizontal = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.splash_welcome),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.splash_description),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 12.dp),
                    textAlign = TextAlign.Center
                )
            }

            Column(modifier = Modifier.weight(1.15f), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    modifier = Modifier
                        .height(58.dp)
                       /* .border(
                            width = 1.dp,
                            color = AccentColorSecondary, // Border color specify করুন
                            shape = RoundedCornerShape(56.dp) // 56dp corner radius
                        )*/,
                    onClick = {
                        // ✅ Mark splash as shown (separate from language first launch)
                        viewModel.markSplashShown()
                        Log.d("SplashScreen", "✅ Splash screen marked as shown")
                        onNavigateToNext()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryColor
                    ),
                    shape = RoundedCornerShape(32.dp) // Button এর shape ও same রাখুন
                ) {
                    // Button content এখানে
                    Text(stringResource(R.string.splash_start_now), style = MaterialTheme.typography.titleSmall)
                }

                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .width(132.dp)
                        .height(4.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(100.dp) // Height এর অর্ধেক সাধারণত
                        ).padding(bottom = 24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

            }

        }


    }
}
