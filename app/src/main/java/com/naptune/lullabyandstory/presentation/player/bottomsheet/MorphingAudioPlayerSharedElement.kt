package com.naptune.lullabyandstory.presentation.player.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.naptune.lullabyandstory.presentation.player.bottomsheet.AudioInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import coil.compose.AsyncImage
import androidx.compose.material3.Text
import androidx.constraintlayout.compose.layoutId

@OptIn(androidx.constraintlayout.compose.ExperimentalMotionApi::class)

@Composable
fun MorphingAudioPlayerSharedElement(
    morphProgress: Float,
    audioInfo: AudioInfo,
    modifier: Modifier = Modifier
) {
    // Motion scene XML as string: morph between sheet (id: full) and mini (id: mini)
    val motionScene = MotionScene(
        """
        {
          ConstraintSets: {
            full: {
              image: {
                width: 120, height: 120,
                start: ['parent', 'start', 0],
                top: ['parent', 'top', 32],
                translationZ: 10
              },
              title: {
                width: 'spread', height: 'wrap',
                start: ['parent', 'start', 16],
                top: ['image', 'bottom', 16]
              }
            },
            mini: {
              image: {
                width: 48, height: 48,
                start: ['parent', 'start', 16],
                top: ['parent', 'top', 16],
                translationZ: 10
              },
              title: {
                width: 'spread', height: 'wrap',
                start: ['image', 'end', 12],
                top: ['parent', 'top', 22]
              }
            }
          },
          Transitions: {
            default: {
              from: 'full', to: 'mini',
              pathMotionArc: 'startHorizontal',
              KeyFrames: {}
            }
          }
        }
        """
    )

    MotionLayout(
        motionScene = motionScene,
        progress = morphProgress,
        modifier = modifier
    ) {
        AsyncImage(
            model = audioInfo.imagePath,
            contentDescription = "audio image",
            modifier = Modifier
                .layoutId("image")
                .size(120.dp)
        )
        Text(
            text = audioInfo.musicName,
            modifier = Modifier.layoutId("title")
        )
    }
}
