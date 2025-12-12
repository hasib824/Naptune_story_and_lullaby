package com.naptune.lullabyandstory.domain.repository

import android.content.Context

/**
 * Repository interface for profile-related operations
 * Defines the contract for sharing, feedback, and rating functionality
 * Follows Clean Architecture principles - domain layer doesn't depend on implementation details
 */
interface ProfileRepository {
    
    /**
     * Share the Naptune app with others using Android share intent
     * @param context Android context needed for launching intents
     * @return Result with success message or error
     */
    suspend fun shareApp(context: Context): Result<String>
    
    /**
     * Send feedback via email intent
     * @param context Android context needed for launching intents  
     * @return Result with success message or error
     */
    suspend fun sendFeedback(context: Context): Result<String>
    
    /**
     * Rate the app on Play Store
     * @param context Android context needed for launching intents
     * @return Result with success message or error
     */
    suspend fun rateApp(context: Context): Result<String>
}