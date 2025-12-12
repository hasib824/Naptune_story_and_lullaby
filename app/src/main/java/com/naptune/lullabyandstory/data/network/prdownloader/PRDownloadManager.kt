package com.naptune.lullabyandstory.data.network.prdownloader

import android.content.Context
import android.util.Log
import com.downloader.Error
import com.downloader.OnCancelListener
import com.downloader.OnDownloadListener
import com.downloader.OnPauseListener
import com.downloader.OnProgressListener
import com.downloader.OnStartOrResumeListener
import com.downloader.PRDownloader
import com.downloader.Progress
import com.naptune.lullabyandstory.domain.data.DownloadLullabyResult
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PRDownloadManager @Inject constructor(@ApplicationContext private val applicationContext: Context) {

    fun getDownloadPath(): String {
        return applicationContext.filesDir.absolutePath
    }

    fun downloadFile(lullabyItem: LullabyDomainModel): Flow<DownloadLullabyResult> =
        callbackFlow {

            Log.e("PRDownload", "📁 Starting download:")
            Log.e("PRDownload", "URL: ${lullabyItem.musicPath}")
            Log.e("PRDownload", "FileName: ${lullabyItem.musicName}")
            Log.e("PRDownload", "Download Path: ${getDownloadPath()}")
            
            // ✅ Validate URL before starting download
            if (lullabyItem.musicPath.isBlank() || !lullabyItem.musicPath.startsWith("http")) {
                Log.e("PRDownload", "❌ Invalid URL: ${lullabyItem.musicPath}")
                trySend(DownloadLullabyResult.Error(lullabyItem.id, "Invalid URL: ${lullabyItem.musicPath}"))
                close(Exception("Invalid URL"))
                return@callbackFlow
            }

            var downloadRequest: Int? = null
            var isCompleted = false

            try {
                downloadRequest = PRDownloader.download(lullabyItem.musicPath, getDownloadPath(), "${lullabyItem.musicName}.wav")
                    .build()
                    .setOnStartOrResumeListener(object : OnStartOrResumeListener {
                        override fun onStartOrResume() {
                            if (isActive) {
                                Log.e("PRDownload", "🚀 Download started for: ${lullabyItem.musicName}")
                            }
                        }
                    })
                    .setOnPauseListener(object : OnPauseListener {
                        override fun onPause() {
                            if (isActive) {
                                Log.e("PRDownload", "⏸️ Download paused for: ${lullabyItem.musicName}")
                            }
                        }
                    })
                    .setOnCancelListener(object : OnCancelListener {
                        override fun onCancel() {
                            if (isActive && !isCompleted) {
                                Log.e("PRDownload", "❌ Download cancelled for: ${lullabyItem.musicName}")
                                trySend(DownloadLullabyResult.Error(lullabyItem.id, "Download cancelled by user"))
                            }
                            close()
                        }
                    })
                    .setOnProgressListener(object : OnProgressListener {
                        override fun onProgress(progress: Progress?) {
                            if (isActive && !isCompleted && progress != null) {
                                val progressPercentage = if (progress.currentBytes > 0) {
                                    ((progress.currentBytes * 100) / lullabyItem.musicSize.toInt()).toInt()
                                } else {
                                    0
                                }
                                
                                Log.e("PRDownload", "📈 Progress: $progressPercentage%")
                                trySend(DownloadLullabyResult.Progress(lullabyItem.id, progressPercentage))
                            }
                        }
                    })
                    .start(object : OnDownloadListener {
                        override fun onDownloadComplete() {
                            if (isActive) {
                                isCompleted = true
                                val completedFilePath = "${getDownloadPath()}/${lullabyItem.musicName}.wav"
                                Log.e("PRDownload", "✅ Download completed: $completedFilePath")
                                trySend(DownloadLullabyResult.Completed(lullabyItem.documentId,completedFilePath))
                            }
                            close()
                        }

                        override fun onError(error: Error) {
                            if (isActive && !isCompleted) {
                                Log.e("PRDownload", "💥 Download error details:")
                                Log.e("PRDownload", "Error message: ${error.serverErrorMessage}")
                                Log.e("PRDownload", "Error code: ${error.responseCode}")
                                Log.e("PRDownload", "Connection exception: ${error.connectionException}")
                                
                                val errorMessage = when {
                                    error.serverErrorMessage != null -> "Server error: ${error.serverErrorMessage}"
                                    error.connectionException != null -> "Connection error: ${error.connectionException?.message}"
                                    error.responseCode != 0 -> "HTTP error: ${error.responseCode}"
                                    else -> "Unknown download error"
                                }
                                
                                trySend(DownloadLullabyResult.Error(lullabyItem.id, errorMessage))
                            }
                            close(Exception("Download failed"))
                        }
                    })

            } catch (e: Exception) {
                // Only log real errors, not cancellation
                if (!e.message.orEmpty().contains("cancelled", ignoreCase = true) && 
                    !e.message.orEmpty().contains("CancellationException", ignoreCase = true)) {
                    Log.e("PRDownload", "💥 Setup error: ${e.message}")
                    trySend(DownloadLullabyResult.Error(lullabyItem.id, "Setup failed: ${e.message}"))
                } else {
                    Log.d("PRDownload", "🔄 Download flow cancelled (normal cleanup)")
                }
                close(e)
            }

            // ✅ REQUIRED: awaitClose block for cleanup
            awaitClose {
                Log.d("PRDownload", "🧹 Cleaning up download for: ${lullabyItem.musicName}")
                try {
                    downloadRequest?.let { requestId ->
                        PRDownloader.cancel(requestId)
                    }
                } catch (e: Exception) {
                    // Silently handle cleanup errors as they're expected during cancellation
                    Log.d("PRDownload", "Cleanup completed for: ${lullabyItem.musicName}")
                }
            }
        }

    /**
     * Test download with a dummy URL for debugging
     */

}