package com.naptune.lullabyandstory.debug

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import com.naptune.lullabyandstory.data.local.source.LocalDataSourceImpl
import com.naptune.lullabyandstory.data.network.source.story.StoryRemoteDataSourceImpl
import com.naptune.lullabyandstory.domain.usecase.story.FetchStoriesUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StoryDebugViewModel @Inject constructor(
    private val storyRemoteDataSourceImpl: StoryRemoteDataSourceImpl,
    private val localDataSourceImpl: LocalDataSourceImpl,
    private val appPreferences: AppPreferences,
    private val fetchStoriesUsecase: FetchStoriesUsecase
) : ViewModel() {

    fun debugStoryFlow() {
        viewModelScope.launch {
            try {
                Log.d("StoryDebug", "==========================================")
                Log.d("StoryDebug", "🔍 STARTING STORY DEBUG FLOW")
                Log.d("StoryDebug", "==========================================")

                // ✅ Step 1: Check sync preferences (on IO thread)
                withContext(Dispatchers.IO) {
                    val syncNeeded = appPreferences.isSyncNeeded(isFromStory = true)
                    val lastSyncTime = appPreferences.getLastSyncTimeFlow(isFromStory = true).first()
                    Log.d("StoryDebug", "📅 Sync needed: $syncNeeded")
                    Log.d("StoryDebug", "📅 Last sync time: $lastSyncTime")
                    Log.d("StoryDebug", "📅 Current time: ${System.currentTimeMillis()}")

                    // Step 2: Check local database count
                    val localCount = localDataSourceImpl.getStoriesCount()
                    Log.d("StoryDebug", "💾 Local story count: $localCount")
                }

                // Step 3: Test remote data source directly
                Log.d("StoryDebug", "🌐 Testing remote data source...")
                val remoteResult = storyRemoteDataSourceImpl.fetchStoryData()
                remoteResult.fold(
                    onSuccess = { stories ->
                        Log.d("StoryDebug", "✅ Remote fetch successful: ${stories.size} stories")
                        stories.forEachIndexed { index, story ->
                            Log.d("StoryDebug", "  $index. ${story.storyName} (ID: ${story.documentId})")
                        }
                    },
                    onFailure = { error ->
                        Log.e("StoryDebug", "❌ Remote fetch failed: ${error.message}")
                        Log.e("StoryDebug", "❌ Error type: ${error.javaClass.simpleName}")
                        error.printStackTrace()
                    }
                )

                // ✅ Step 4: Check local stories (on IO thread)
                withContext(Dispatchers.IO) {
                    Log.d("StoryDebug", "💾 Checking local stories...")
                    val localStories = localDataSourceImpl.getAllStories().first()
                    Log.d("StoryDebug", "💾 Local stories count: ${localStories.size}")
                    localStories.forEachIndexed { index, story ->
                        Log.d("StoryDebug", "  $index. ${story.storyName} (ID: ${story.documentId})")
                    }
                }

                // Step 5: Test use case
                Log.d("StoryDebug", "🎯 Testing use case...")
                fetchStoriesUsecase().collect { stories ->
                    Log.d("StoryDebug", "🎯 Use case result: ${stories.size} stories")
                    stories.forEachIndexed { index, story ->
                        Log.d("StoryDebug", "  $index. ${story.storyName} (ID: ${story.documentId})")
                    }
                }

                Log.d("StoryDebug", "==========================================")
                Log.d("StoryDebug", "✅ STORY DEBUG FLOW COMPLETED")
                Log.d("StoryDebug", "==========================================")

            } catch (e: Exception) {
                Log.e("StoryDebug", "💥 Debug flow error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun resetStoryData() {
        viewModelScope.launch {
            try {
                Log.d("StoryDebug", "🔄 Resetting story data...")
                
                // ✅ All database operations on IO thread
                withContext(Dispatchers.IO) {
                    // Clear local data
                    localDataSourceImpl.deleteAllStories()
                    
                    // Reset sync time
                    appPreferences.resetSyncTime(isFromStory = true)
                }
                
                Log.d("StoryDebug", "✅ Story data reset completed")
                
                // Re-run debug
                debugStoryFlow()
                
            } catch (e: Exception) {
                Log.e("StoryDebug", "❌ Error resetting story data: ${e.message}")
            }
        }
    }

    fun forceRemoteSync() {
        viewModelScope.launch {
            try {
                Log.d("StoryDebug", "🚀 Force syncing from remote...")
                
                val result = storyRemoteDataSourceImpl.fetchStoryData()
                result.fold(
                    onSuccess = { stories ->
                        Log.d("StoryDebug", "✅ Force sync successful: ${stories.size} stories")
                        
                        // ✅ Database operations on IO thread
                        withContext(Dispatchers.IO) {
                            // Clear and insert
                            localDataSourceImpl.deleteAllStories()
                            localDataSourceImpl.insertAllStories(stories.map { remote ->
                                // Convert remote to local entity
                                com.naptune.lullabyandstory.data.local.entity.StoryLocalEntity(
                                    documentId = remote.documentId,
                                    id = remote.id,
                                    storyName = remote.storyName,
                                    storyDescription = remote.storyDescription,
                                    storyAudioPath = remote.storyAudioPath,
                                    imagePath = remote.imagePath,
                                    story_reading_time = remote.story_reading_time,
                                    story_listen_time_in_millis = remote.story_listen_time_in_millis,
                                    popularity_count = remote.popularity_count, // Local data doesn't have this, default to 0
                                    isFree = remote.isFree // Local data doesn't have this, default to false
                                )
                            })
                            
                            // Update sync time
                            appPreferences.updateLastSyncTime(isFromStory = true)
                        }
                        
                        Log.d("StoryDebug", "✅ Force sync completed")
                        
                    },
                    onFailure = { error ->
                        Log.e("StoryDebug", "❌ Force sync failed: ${error.message}")
                    }
                )
                
            } catch (e: Exception) {
                Log.e("StoryDebug", "💥 Force sync error: ${e.message}")
            }
        }
    }
}

// Add this composable to test from UI
@Composable
fun StoryDebugScreen(
    debugViewModel: StoryDebugViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Story Debug Tools",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Button(
            onClick = { debugViewModel.debugStoryFlow() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔍 Debug Story Flow")
        }
        
        Button(
            onClick = { debugViewModel.resetStoryData() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔄 Reset Story Data")
        }
        
        Button(
            onClick = { debugViewModel.forceRemoteSync() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🚀 Force Remote Sync")
        }
        
        Text(
            text = "✅ All database operations now run on IO thread\nCheck Logcat for detailed debug information",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
