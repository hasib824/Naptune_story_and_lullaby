package com.naptune.lullabyandstory.data.manager

/**
 * Type of content that can be unlocked via rewarded ads
 */
sealed class UnlockType {
    object Lullaby : UnlockType() {
        override fun toString() = "Lullaby"
    }

    object Story : UnlockType() {
        override fun toString() = "Story"
    }
}
