package com.naptune.lullabyandstory.presentation.components.language

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.data.model.getSupportedLanguages
import com.naptune.lullabyandstory.ui.theme.AccentColor
import com.naptune.lullabyandstory.ui.theme.LanguageBottomSheetColor
import com.naptune.lullabyandstory.ui.theme.ModalHeaderColor
import com.naptune.lullabyandstory.ui.theme.NunitoFamily
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionBottomSheet(
    bottomSheetState : SheetState,
    isVisible: Boolean,
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState,
            dragHandle = null,
            containerColor = LanguageBottomSheetColor,
            contentColor = Color.White
        ) {
            LanguageSelectionContent(
                currentLanguage = currentLanguage,
                onLanguageSelected = onLanguageSelected,
                onCancel = onDismiss
            )
        }
    }
}

@Composable
fun LanguageSelectionContent(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onCancel: () -> Unit
) {
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }
    val availableLanguages = getSupportedLanguages()




    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        // Header (Similar to TimerModal)
        Row(Modifier.fillMaxWidth().padding(end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.language_selection_title),
                textAlign = TextAlign.Left,
                fontSize = 20.sp,
                fontFamily = NunitoFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp,top = 16.dp, bottom = 16.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            // ✅ Wrap in Box with circular clip to fix square click area
            Box(
                modifier = Modifier
                    .size(32.dp) // ✅ Fixed size for circular click area
                    .clip(CircleShape) // ✅ Clip to circle BEFORE clickable
                    .clickable(
                    ) {
                        //isPressed = true // ✅ Trigger alpha animation
                        onCancel() // ✅ Close bottom sheet
                    }
                    ,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_languageselector_cancel),
                    tint = Color.Unspecified,
                    contentDescription = "Close"
                )
            }
        }


        // Language List (Reduced height compared to timer items)
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Reduced from timer's 16.dp
        ) {
            items(availableLanguages) { language ->
                LanguageItemComponent(
                    language = language,
                    isSelected = selectedLanguage == language.code,
                    onClick = {
                        selectedLanguage = language.code
                        onLanguageSelected(selectedLanguage)
                    }
                )
            }
        }

        // Action Buttons Row (Same as TimerModal)
/*        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cancel Button
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Set Button
            Button(
                onClick = { onLanguageSelected(selectedLanguage) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.action_set),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }*/

        // Bottom padding for navigation gesture
        Spacer(modifier = Modifier.height(16.dp))
    }
}