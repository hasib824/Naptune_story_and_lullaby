package com.naptune.lullabyandstory.presentation.profile

import androidx.compose.runtime.Immutable
import com.naptune.lullabyandstory.data.model.Language
import com.naptune.lullabyandstory.presentation.main.AdUiState

/**
 * Data class representing the UI state of the Profile screen
 * Following MVI pattern for reactive state management
 */
@Immutable
data class ProfileUiState(
    /**
     * Indicates if any operation is currently in progress
     */
    val isLoading: Boolean = false,

    /**
     * Error message to display to user, null if no error
     */
    val error: String? = null,

    /**
     * Success message to display to user, null if no success message
     */
    val successMessage: String? = null,

    /**
     * User profile information
     */
    val userName: String = "NightWish",

    /**
     * Premium status of the user
     */
    val isPremiumUser: Boolean = false,

    /**
     * Current selected language
     */
    val currentLanguage: Language = Language("en", "English", "English", "🇺🇸", "https://flagcdn.com/w80/us.png"),

    /**
     * AdMob banner ad state
     */
    val adState: AdUiState = AdUiState()
)