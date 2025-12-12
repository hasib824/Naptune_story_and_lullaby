package com.naptune.lullabyandstory.presentation.player.timermodal


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.naptune.lullabyandstory.ui.theme.PrimaryColor
import com.naptune.lullabyandstory.ui.theme.TimerModalBackground


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlideUpTimerModal(
    bottomSheetState: SheetState,
    isSheetVisible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {

    if (isSheetVisible) {
        ModalBottomSheet(

            onDismissRequest = { onDismiss() },
            sheetState = bottomSheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = PrimaryColor,
            dragHandle = {
                // Custom drag handle with custom color

                Box(
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .width(32.dp)
                        .height(4.dp)
                        .background(
                            Color.White, // Your desired color (Green)
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        ) {

            content()
        }
    }
}