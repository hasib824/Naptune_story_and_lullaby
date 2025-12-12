package com.naptune.lullabyandstory.presentation.player.timermodal

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import androidx.compose.ui.text.font.FontWeight
import androidx.constraintlayout.compose.ConstraintLayout
import com.naptune.lullabyandstory.ui.theme.NunitoFamily
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.presentation.components.common.responsive.ResponsiveStylesOfTimerModal
import com.naptune.lullabyandstory.presentation.components.common.responsive.getTimerModalResponsiveSizes
import com.naptune.lullabyandstory.presentation.components.common.responsive.rememberScreenDimensionManager
import com.naptune.lullabyandstory.ui.theme.AccentColor
import com.naptune.lullabyandstory.ui.theme.ModalHeaderColor
import com.naptune.lullabyandstory.ui.theme.ModalItemColor
import com.naptune.lullabyandstory.ui.theme.ModalStrokeColor


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimerModalContent(
    onClose: () -> Unit,
    onSetTimer: (Int, LocalTime) -> Unit,
    savedLocalTime: LocalTime,
    savedSelectedIndex: Int = 0,
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
        ButtonInfo(stringResource(R.string.timer_5_min), 5, 0),
        ButtonInfo(stringResource(R.string.timer_10_min), 10, 1),
        ButtonInfo(stringResource(R.string.timer_15_min), 15, 2),
        ButtonInfo(stringResource(R.string.timer_custom), -1, 3)
    )

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = modalResponsiveSizes.contentHorizontalPadding, vertical = modalResponsiveSizes.contentVerticalPadding)
    ) {
        Text(
            text = stringResource(R.string.timer_set_timer),
            textAlign = TextAlign.Left,
            fontSize = modalResponsiveSizes.titleFontSize,
            fontFamily = NunitoFamily,
            fontWeight = FontWeight.Bold,
            color = ModalHeaderColor

        )

        // Timer Buttons Row 1
        Row(
            modifier = Modifier.padding(top = modalResponsiveSizes.itemMarginTop),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TimerButtonComponent(
                buttonsList[0], selectedIndex == buttonsList[0].index,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(36.dp))
                    .clickable {
                        expanded = false
                        selectedOptionTime = buttonsList[0].timeInt
                        selectedIndex = buttonsList[0].index
                    },
                modalResponsiveSizes = modalResponsiveSizes
            )
            TimerButtonComponent(
                buttonsList[1], selectedIndex == buttonsList[1].index,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(36.dp))
                    .clickable {
                        expanded = false
                        selectedOptionTime = buttonsList[1].timeInt
                        selectedIndex = buttonsList[1].index
                    },
                modalResponsiveSizes = modalResponsiveSizes
            )
        }

        // Timer Buttons Row 2
        Row(
            modifier = Modifier.padding(top = modalResponsiveSizes.itemMarginTop),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TimerButtonComponent(
                buttonsList[2], selectedIndex == buttonsList[2].index,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(36.dp))
                    .clickable {
                        expanded = false
                        selectedOptionTime = buttonsList[2].timeInt
                        selectedIndex = buttonsList[2].index
                    },
                modalResponsiveSizes = modalResponsiveSizes
            )
            TimerButtonComponent(
                buttonsList[3], selectedIndex == buttonsList[3].index,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(36.dp))
                    .clickable {
                        expanded = true
                        selectedOptionTime = buttonsList[3].timeInt
                        selectedIndex = buttonsList[3].index
                    },
                modalResponsiveSizes = modalResponsiveSizes
            )
        }

        // Pre-measure WheelTimePicker to avoid lag
        Box(modifier = Modifier.height(0.dp)) {
            TimerWheelPicker({}, savedLocalTime,modalResponsiveSizes)
        }

        // Expandable Section
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(if (savedSelectedIndex == 3 && isFirstTime == true) 0 else 500)),
            exit = shrinkVertically(animationSpec = tween(500)),
            modifier = Modifier.fillMaxWidth()
        ) {
            isFirstTime = false
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = modalResponsiveSizes.hhMMPaddingTop)
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .width(220.dp)
                        .align(Alignment.CenterHorizontally)
                        .wrapContentHeight()
                ) {
                    val centerGuideline = createGuidelineFromStart(0.5f)
                    val (item1, item2) = createRefs()

                    Text(
                        text = stringResource(R.string.timer_hh),
                        fontSize = modalResponsiveSizes.fontSize,
                        style = MaterialTheme.typography.titleSmall,
                        color = ModalItemColor,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.constrainAs(item1) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(centerGuideline)
                        }
                    )

                    Text(
                        text = stringResource(R.string.timer_mm),
                        fontSize = modalResponsiveSizes.fontSize,
                        fontWeight = FontWeight.Normal,
                        color = ModalItemColor,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.constrainAs(item2) {
                            top.linkTo(parent.top)
                            start.linkTo(centerGuideline)
                            end.linkTo(parent.end)
                        }
                    )
                }

                // Here wHEEL tIME
                TimerWheelPicker(
                    { time ->
                        Log.e("Selected Time :", "$time");
                        selectedCustomeLocalTime = time
                    }, savedLocalTime,
                    modalResponsiveSizes = modalResponsiveSizes
                )
            }
        }

        Spacer(modifier = Modifier.height(modalResponsiveSizes.setBtnMarginTop))

        // Set button
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(modalResponsiveSizes.setBtnHeight),
            onClick = {

                if (selectedIndex < 3) {
                    selectedCustomeLocalTime = LocalTime.of(0, selectedOptionTime)
                }
                if (selectedCustomeLocalTime.hour == 0 && selectedCustomeLocalTime.minute == 0) {

                    Toast.makeText(context, context.getString(R.string.toast_timer_select_valid_time), Toast.LENGTH_SHORT).show()
                } else {

                    onSetTimer(selectedIndex, selectedCustomeLocalTime);
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
        ) {
            Text(
                text = stringResource(R.string.action_set_uppercase),
                style = MaterialTheme.typography.titleSmall,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // Cancel Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = modalResponsiveSizes.spacing),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .clickable { onClose(); Log.e("Cancel", "Cancel Clicked"); }
                    .padding(16.dp),
                text = stringResource(R.string.timer_cancel),
                style = MaterialTheme.typography.titleSmall,
                fontSize = modalResponsiveSizes.fontSize,
                fontWeight = FontWeight.Normal,
                color = Color.Gray,

                )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimerWheelPicker(
    onTimeSelected: (LocalTime) -> Unit,
    savedLocalTime: LocalTime,
    modalResponsiveSizes: ResponsiveStylesOfTimerModal
) {


}

@Composable
fun TimerButtonComponent(
    buttonInfo: ButtonInfo,
    isSelected: Boolean,
    modifier: Modifier,
    modalResponsiveSizes: ResponsiveStylesOfTimerModal,

) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(36.dp))
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(36.dp),
                color = if (isSelected) AccentColor else ModalStrokeColor
            )
            .padding(start = 16.dp ,  end= modalResponsiveSizes.spacing , top = modalResponsiveSizes.spacing , bottom = modalResponsiveSizes.spacing),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(modalResponsiveSizes.spacing)
    ) {
        Box(
            modifier = Modifier
                .size(modalResponsiveSizes.radioButtonSize)
                .clip(RoundedCornerShape((modalResponsiveSizes.radioButtonSize/2)))
                .background(if (isSelected) AccentColor else Color.Transparent)
                .border(
                    width = 1.dp, color = if (isSelected) AccentColor else AccentColor,
                    shape = RoundedCornerShape((modalResponsiveSizes.radioButtonSize/2))
                ), contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.tick_mark_icm),
                    contentDescription = "Check Icon",
                    tint = Color.White,
                )
            }
        }

        Text(
            text = buttonInfo.text,
            color = ModalItemColor,
            fontSize = modalResponsiveSizes.fontSize,
            fontFamily = NunitoFamily,
            fontWeight = FontWeight.SemiBold

        )
    }
}

data class ButtonInfo(val text: String, val timeInt: Int, val index: Int)