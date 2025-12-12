package com.naptune.lullabyandstory.presentation.sleepsounds

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naptune.lullabyandstory.R

@Composable
fun SleepSoundsScreen(
    // ✅ NEW: Dynamic content padding based on mini controller
    contentBottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.sleep_sounds_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.sleep_sounds_subtitle),
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sleep sounds list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = contentBottomPadding)
        ) {
            val sleepSounds = listOf(
                SleepSoundItem("🌊 Ocean Waves", "Continuous", "Nature", "Gentle ocean waves for deep sleep"),
                SleepSoundItem("🌧️ Rain Sounds", "Continuous", "Weather", "Soft rain drops on leaves"),
                SleepSoundItem("🔥 Crackling Fire", "Continuous", "Indoor", "Warm fireplace sounds"),
                SleepSoundItem("🌬️ White Noise", "Continuous", "Ambient", "Pure white noise for focus"),
                SleepSoundItem("🦗 Night Crickets", "Continuous", "Nature", "Peaceful cricket symphony"),
                SleepSoundItem("🌲 Forest Ambience", "Continuous", "Nature", "Deep forest atmosphere"),
                SleepSoundItem("💨 Wind Chimes", "Continuous", "Zen", "Gentle wind chime melodies"),
                SleepSoundItem("🏔️ Mountain Breeze", "Continuous", "Nature", "Cool mountain air sounds"),
            )
            
            items(sleepSounds) { sound ->
                SleepSoundCard(sound = sound)
            }
        }
    }
}

@Composable
private fun SleepSoundCard(sound: SleepSoundItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = sound.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = sound.category,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.sleep_sounds_duration_format, sound.duration),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                // Play button
                Button(
                    onClick = { /* TODO: Play sleep sound */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sleep_sounds_play),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = sound.description,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )
        }
    }
}

data class SleepSoundItem(
    val title: String,
    val duration: String,
    val category: String,
    val description: String
)
