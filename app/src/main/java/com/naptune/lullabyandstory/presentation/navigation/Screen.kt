package com.naptune.lullabyandstory.presentation.navigation

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.ui.text.LinkAnnotation
import com.google.gson.Gson
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.domain.model.StoryDomainModel
import io.appwrite.extensions.toJson

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Main : Screen("main")
    object Favourite : Screen("favourite")
    object Profile : Screen("profile")
    object Premium : Screen("premium")
    object Settings : Screen("settings") // ✅ NEW: Settings screen
    object Debug : Screen("debug") // ✅ NEW: Debug screen
    object Explore : Screen("explore") // ✅ NEW: Explore screen with Lullaby/Story tabs

   // object StoryManager : Screen("read_manager/{storyId}/{storyName}/{storyDescription}/{imagePath}/{storyAudioPath}/{storyLength}/{documentId}/{isFavourite}")
    object StoryManager : Screen("read_manager/{storyJson}")
    {
 /*       fun createRoute(
            storyId: String,
            storyName: String,
            storyDescription: String,
            imagePath: String,
            storyAudioPath: String,
            storyLength: String,
            documentId: String,
            isFavourite: Boolean = false
        ): String {
            val encodedStoryName = java.net.URLEncoder.encode(storyName, "UTF-8")
            val encodedStoryDescription = java.net.URLEncoder.encode(storyDescription, "UTF-8")
            val encodedImagePath = java.net.URLEncoder.encode(imagePath, "UTF-8")
            val encodedStoryAudioPath = java.net.URLEncoder.encode(storyAudioPath, "UTF-8")
            val encodedStoryLength = java.net.URLEncoder.encode(storyLength, "UTF-8")
            val safeStoryId = storyId.ifEmpty { "unknown" }
            val safeDocumentId = documentId.ifEmpty { "unknown" }

            return "read_manager/$safeStoryId/$encodedStoryName/$encodedStoryDescription/$encodedImagePath/$encodedStoryAudioPath/$encodedStoryLength/$safeDocumentId/$isFavourite"
        }*/

        fun createJsonRoute(storyDomainModel: StoryDomainModel): String {

            val storyJson = Uri.encode(Gson().toJson(storyDomainModel))
            return "read_manager/$storyJson"
        }
    }

    object StoryReader : Screen("story_reader/{storyJson}") {
        fun createRoute(
            storyId: String,
            storyName: String,
            storyDescription: String,
            imagePath: String,
            documentId: String,
            isFavourite: Boolean = false
        ): String {
            val encodedStoryName = java.net.URLEncoder.encode(storyName, "UTF-8")
            val encodedStoryDescription = java.net.URLEncoder.encode(storyDescription, "UTF-8")
            val encodedImagePath = java.net.URLEncoder.encode(imagePath, "UTF-8")
            val safeStoryId = storyId.ifEmpty { "unknown" }
            val safeDocumentId = documentId.ifEmpty { "unknown" }
            
            return "story_reader/$safeStoryId/$encodedStoryName/$encodedStoryDescription/$encodedImagePath/$safeDocumentId/$isFavourite"
        }

        fun createJsonRoute(storyDomainModel: StoryDomainModel): String {
            val encode = Uri.encode(Gson().toJson(storyDomainModel))
            return "story_reader/$encode"
        }

    }
    // New content screens
    object Lullaby : Screen("lullaby")
    object Story : Screen("story")
    object SleepSounds : Screen("sleep_sounds")
    
    // Audio Player screen
    object AudioPlayer : Screen("audio_player/{documentId}/{audioId}/{isFromStory}/{musicPath}/{musicLocalPath}/{musicName}/{imagePath}/{isFavourite}/{isFromNotification}") {
        fun createRoute(
            audioId: String,
            isFromStory: Boolean,
            musicPath: String,
            musicLocalPath: String?,
            musicName: String,
            imagePath: String,
            isFavourite: Boolean = false,
            isFromNotification: Boolean = false,
            documentId: String
        ): String {
            // ✅ Safe URL encoding with null checks
            val safeMusicPath = musicPath ?: ""
            val safeMusicName = musicName ?: ""
            val safeImagePath = imagePath ?: ""
            val safeLocalPath = if(isFromStory) safeMusicPath else (musicLocalPath ?: "")
            val encodedMusicPath = java.net.URLEncoder.encode(safeMusicPath, "UTF-8")
            val encodedMusicName = java.net.URLEncoder.encode(safeMusicName, "UTF-8")
            val encodedLocalPath = java.net.URLEncoder.encode(safeLocalPath, "UTF-8")
            val encodedImagePath = java.net.URLEncoder.encode(safeImagePath, "UTF-8")
            val safeDocumentId = documentId ?: "unknown"
            val safeAudioId = audioId ?: "unknown"
            
            return "audio_player/$safeDocumentId/$safeAudioId/$isFromStory/$encodedMusicPath/$encodedLocalPath/$encodedMusicName/$encodedImagePath/$isFavourite/$isFromNotification"
        }
    }
}

// Bottom Navigation Items
sealed class BottomNavItem(val route: String, @DrawableRes val iconRes: Int, val title: String) {
    object Home : BottomNavItem("main", R.drawable.homenavic, "Home")
    object Favourite : BottomNavItem("favourite", R.drawable.favouritenavic, "favourite")
    object Profile : BottomNavItem("profile", R.drawable.profilenavic, "Profile")
}
