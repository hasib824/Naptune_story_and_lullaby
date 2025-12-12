package com.naptune.lullabyandstory.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ProfileRepository that handles Android intents for sharing, feedback, and rating
 * Follows Clean Architecture principles by implementing domain interface
 */
@Singleton
class ProfileRepositoryImpl @Inject constructor() : ProfileRepository {

    companion object {
        private const val APP_NAME = "Naptune"
        private const val FEEDBACK_EMAIL = "feedback@naptuneapps.com"
        private const val SAMPLE_PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=com.whatsapp"
    }

    /**
     * Share the Naptune app using Android share intent
     */
    override suspend fun shareApp(context: Context): Result<String> {
        return try {
            val shareText = "Check out this amazing $APP_NAME app! Download it from Play Store: $SAMPLE_PLAY_STORE_LINK"
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "Share $APP_NAME App")
            }
            
            val chooserIntent = Intent.createChooser(shareIntent, "Share $APP_NAME App")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            context.startActivity(chooserIntent)
            
            Result.success("Share dialog opened successfully")
        } catch (e: Exception) {
            Result.failure(Exception("Failed to open share dialog: ${e.message}"))
        }
    }

    /**
     * Send feedback via email intent
     */
    override suspend fun sendFeedback(context: Context): Result<String> {
        return try {
            val emailBody = """
                Hi $APP_NAME Team,
                
                I would like to share my feedback about the $APP_NAME app:
                
                [Please write your feedback here]
                
                Device Information:
                - Device: ${android.os.Build.MODEL}
                - Android Version: ${android.os.Build.VERSION.RELEASE}
                - App Version: [App Version]
                
                Thank you!
            """.trimIndent()

            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_EMAIL))
                putExtra(Intent.EXTRA_SUBJECT, "Feedback for $APP_NAME App")
                putExtra(Intent.EXTRA_TEXT, emailBody)
            }

            if (emailIntent.resolveActivity(context.packageManager) != null) {
                emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(emailIntent)
                Result.success("Email app opened successfully")
            } else {
                // Fallback: Show toast with email address
                Toast.makeText(context, context.getString(R.string.toast_no_email_app, FEEDBACK_EMAIL), Toast.LENGTH_LONG).show()
                Result.success("Email address copied for manual sending")
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to open email app: ${e.message}"))
        }
    }

    /**
     * Rate the app on Play Store
     */
    override suspend fun rateApp(context: Context): Result<String> {
        return try {
            // Try to open in Play Store app first
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${getPackageFromUrl(SAMPLE_PLAY_STORE_LINK)}"))
            playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            if (playStoreIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(playStoreIntent)
                Result.success("Play Store opened successfully")
            } else {
                // Fallback: Open in browser
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(SAMPLE_PLAY_STORE_LINK))
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(browserIntent)
                Result.success("Browser opened with Play Store link")
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to open Play Store: ${e.message}"))
        }
    }

    /**
     * Extract package name from Play Store URL
     */
    private fun getPackageFromUrl(url: String): String {
        return url.substringAfter("id=").substringBefore("&")
    }
}