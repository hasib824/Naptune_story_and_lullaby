package com.naptune.lullabyandstory.data.manager

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages session-only unlocked items after watching rewarded ads.
 * Data is cleared automatically when app restarts.
 *
 * This provides a centralized, observable state for tracking which items
 * (lullabies/stories) have been unlocked via rewarded ads in the current session.
 */
@Singleton
class SessionUnlockManager @Inject constructor() {

    // Private mutable state
    private val _unlockedItems = MutableStateFlow<Set<String>>(emptySet())

    // Public immutable state - ViewModels observe this
    val unlockedItems: StateFlow<Set<String>> = _unlockedItems.asStateFlow()

    /**
     * Unlock an item after successful rewarded ad completion
     * @param itemId Document ID of the item (lullaby or story)
     * @param itemType Type of item (Lullaby or Story)
     */
    fun unlockItem(itemId: String, itemType: UnlockType) {
        _unlockedItems.value = _unlockedItems.value + itemId
        Log.d("SessionUnlock", "✅ Unlocked ${itemType}: $itemId (Total: ${_unlockedItems.value.size})")
    }

    /**
     * Check if an item is currently unlocked in this session
     * @param itemId Document ID to check
     * @return true if unlocked, false otherwise
     */
    fun isItemUnlocked(itemId: String): Boolean {
        return itemId in _unlockedItems.value
    }

    /**
     * Get all currently unlocked item IDs
     * @return Set of unlocked item IDs
     */
    fun getUnlockedItems(): Set<String> {
        return _unlockedItems.value
    }

    /**
     * Clear all unlocked items (useful for testing or logout)
     */
    fun clearAllUnlocks() {
        _unlockedItems.value = emptySet()
        Log.d("SessionUnlock", "🧹 Cleared all unlocked items")
    }

    /**
     * Get count of unlocked items
     */
    fun getUnlockCount(): Int {
        return _unlockedItems.value.size
    }

    /**
     * Remove a specific item from unlocked set
     * @param itemId Document ID to remove
     */
    fun removeUnlock(itemId: String) {
        _unlockedItems.value = _unlockedItems.value - itemId
        Log.d("SessionUnlock", "🗑️ Removed unlock for: $itemId")
    }
}
