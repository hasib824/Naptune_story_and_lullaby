package com.naptune.lullabyandstory.presentation.navigation

import androidx.lifecycle.ViewModel
import com.naptune.lullabyandstory.presentation.player.service.MusicController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Simple ViewModel to provide access to MusicController in Composables
 * Used for state synchronization between notification and mini controller
 */
@HiltViewModel
class MusicControllerAccessViewModel @Inject constructor(
    val musicController: MusicController
) : ViewModel()