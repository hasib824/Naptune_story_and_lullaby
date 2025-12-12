package com.naptune.lullabyandstory.presentation.player.timermodal


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import androidx.compose.ui.text.font.FontWeight
import com.naptune.lullabyandstory.ui.theme.NunitoFamily
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.presentation.components.common.responsive.getTimerModalResponsiveSizes
import com.naptune.lullabyandstory.presentation.components.common.responsive.rememberScreenDimensionManager
import com.naptune.lullabyandstory.ui.theme.PrimaryColor
import com.naptune.lullabyandstory.ui.theme.TimerModalBackground


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SmartTimerModalContent(
    onClose: () -> Unit,
    onSetTimer: (Int, LocalTime) -> Unit,
    savedLocalTime: LocalTime,
    savedSelectedIndex: Int = 0,
    onStopTimer: () -> Unit,
    isTimerRunning: Boolean = false, // ✅ Timer running state
    isFromStory: Boolean = false, // ✅ NEW: Show "End of story" only for stories
) {
    var selectedOptionTime by remember { mutableStateOf(5) }
    var expanded by remember { mutableStateOf(false) }
    var selectedCustomeLocalTime by remember { mutableStateOf(LocalTime.now()) }
    var selectedIndex by remember { mutableStateOf(savedSelectedIndex) }
    var isFirstTime by remember { mutableStateOf(true) }
    val context = LocalContext.current


    val modalResponsiveSizes = getTimerModalResponsiveSizes(rememberScreenDimensionManager())

    LaunchedEffect(Unit) {
        expanded = selectedIndex == 3
    }

    val buttonsList = listOf(
        SmartButtonInfo(stringResource(R.string.timer_5_min), 5, 0),
        SmartButtonInfo(stringResource(R.string.timer_10_min), 10, 1),
        SmartButtonInfo(stringResource(R.string.timer_15_min), 15, 2),
        SmartButtonInfo(stringResource(R.string.timer_thirty_mins), 30, 3),
        SmartButtonInfo(stringResource(R.string.timer_one_hour), 60, 4),
        SmartButtonInfo(stringResource(R.string.end_of_story), 999, 5),
        SmartButtonInfo(stringResource(R.string.stop_timer), -1, 6)
    )

    Column(
        Modifier
            .fillMaxWidth()
            .background(PrimaryColor)
            .padding(
                horizontal = modalResponsiveSizes.contentHorizontalPadding,
                vertical = modalResponsiveSizes.contentVerticalPadding
            )
    ) {
        Text(
            text = stringResource(R.string.timer_set_timer),
            textAlign = TextAlign.Left,
            fontSize = modalResponsiveSizes.titleFontSize,
            fontFamily = NunitoFamily,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEEEEEE)

        )
        Spacer(modifier = Modifier.height(16.dp))

        buttonsList.forEach { option ->
            // ✅ Show stop timer option only when timer is actually running
            if (option.index == 6 && !isTimerRunning) {
                // Skip showing stop timer when no timer is active
                return@forEach
            }

            // ✅ Show "End of story" option only when playing story
            if (option.index == 5 && !isFromStory) {
                // Skip showing "End of story" when playing lullaby
                return@forEach
            }

            TimerOptionItem(
                smartButtonInfo = option,
                onClick = {
                    if (option.index == 6) {
                        onStopTimer()
                    } else if (option.index == 5) {
                        // Handle "End of Story" timer specially
                        onSetTimer(
                            option.index,
                            LocalTime.of(0, 1)
                        ) // Use 1 minute as placeholder, actual logic in TimerAlarmManager
                    } else {
                        onSetTimer(
                            option.index,
                            LocalTime.of(
                                if (option.index == 4) 1 else 0,
                                if (option.index < 4) option.timeInt else 0
                            )
                        )
                    }
                }
            )

            if (option != buttonsList.last()) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }


    }
}

@Composable
fun TimerOptionItem(smartButtonInfo: SmartButtonInfo, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = -16.dp, y = 0.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(16.dp)
    )
    {
        Text(text = smartButtonInfo.text, style = MaterialTheme.typography.bodyLarge.copy( color = Color(0xFFEDEDED)))
    }

}


data class SmartButtonInfo(val text: String, val timeInt: Int, val index: Int)