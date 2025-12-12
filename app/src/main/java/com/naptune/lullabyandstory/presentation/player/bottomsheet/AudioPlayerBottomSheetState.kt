package com.naptune.lullabyandstory.presentation.player.bottomsheet

import androidx.compose.runtime.Stable
import com.naptune.lullabyandstory.domain.model.StoryDomainModel

@Stable // Using @Stable instead of @Immutable because of lambda callbacks
data class AudioPlayerBottomSheetState(
    val audioId: String,
    val isFromStory: Boolean,
    val musicPath: String,
    val musicName: String,
    val imagePath: String,
    val documentId: String,
    val musicLocalPath: String? = null,
    val fromNotification: Boolean = false,
    val isUserSelection: Boolean = true, // ✅ NEW: Track if user manually selected (not from notification)
    val story_listen_time_in_millis: Long = 0L,
    // Callback functions
    val onNavigateToStoryReader: ((StoryDomainModel) -> Unit)? = null,
    val onDismiss: () -> Unit = {}
)