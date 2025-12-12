package com.naptune.lullabyandstory.utils.fcm

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.naptune.lullabyandstory.data.fcm.TokenUploadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scheduler for FCM-related WorkManager tasks
 * Handles scheduling of token upload and sync operations
 */
@Singleton
class FcmWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "FcmWorkScheduler"
    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule periodic token sync (every 24 hours)
     * Ensures token stays registered with server
     */
    fun schedulePeriodicTokenSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<TokenUploadWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            TokenUploadWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            periodicWorkRequest
        )

        Log.d(TAG, "Periodic token sync scheduled (every 24 hours)")
    }

    /**
     * Schedule one-time immediate token upload
     * Used for initial token registration or manual refresh
     */
    fun scheduleImmediateTokenUpload() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<TokenUploadWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "${TokenUploadWorker.WORK_NAME}_immediate",
            ExistingWorkPolicy.REPLACE, // Replace any existing immediate work
            oneTimeWorkRequest
        )

        Log.d(TAG, "Immediate token upload scheduled")
    }

    /**
     * Cancel all FCM work
     * Used when user opts out of notifications
     */
    fun cancelAllWork() {
        workManager.cancelUniqueWork(TokenUploadWorker.WORK_NAME)
        workManager.cancelUniqueWork("${TokenUploadWorker.WORK_NAME}_immediate")
        Log.d(TAG, "All FCM work cancelled")
    }
}
