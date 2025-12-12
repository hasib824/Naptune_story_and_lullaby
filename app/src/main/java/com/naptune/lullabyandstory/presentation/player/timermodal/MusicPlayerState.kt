package com.naptune.lullabyandstory.presentation.player.timermodal

sealed class MusicPlayerState {
    object Hidden : MusicPlayerState()
    object Collapsed : MusicPlayerState()
    object Expanded : MusicPlayerState()
}