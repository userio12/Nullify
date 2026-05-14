package com.nullify.cleaner.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nullify.cleaner.data.local.entity.CleanupLogEntity
import com.nullify.cleaner.domain.cleaner.CorpseFinder
import com.nullify.cleaner.domain.cleaner.JunkCleaner
import com.nullify.cleaner.domain.mode.ExecutionMode
import com.nullify.cleaner.util.formatBytes
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val executionMode: ExecutionMode by inject()
    private val corpseFinder: CorpseFinder by inject()
    private val junkCleaner: JunkCleaner by inject()

    override suspend fun doWork(): Result {
        val toolTypes = inputData.getStringArray("tool_types") ?: return Result.failure()

        var totalDeleted = 0
        var totalBytes = 0L
        val startTime = System.currentTimeMillis()
        val description = StringBuilder()

        for (toolType in toolTypes) {
            when (toolType) {
                "corpse_finder" -> {
                    description.append("CorpseFinder, ")
                    corpseFinder.scan().collect { progress ->
                        totalBytes = progress.bytesFound
                    }
                }
                "junk_cleaner" -> {
                    description.append("JunkCleaner, ")
                    junkCleaner.scan().collect { progress ->
                        totalBytes = progress.bytesFound
                    }
                }
                "duplicate_finder" -> description.append("DuplicateFinder, ")
            }
        }

        val duration = System.currentTimeMillis() - startTime
        val logDao = com.nullify.cleaner.data.local.AppDatabase.getInstance(applicationContext).cleanupLogDao()
        logDao.insert(CleanupLogEntity(
            toolType = toolTypes.joinToString(","),
            filesDeleted = totalDeleted,
            bytesFreed = totalBytes,
            durationMs = duration,
            modeUsed = executionMode.name
        ))

        showNotification(
            title = "Cleanup Complete",
            message = "Freed ${formatBytes(totalBytes)} using ${executionMode.name}"
        )

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "cleanup_notifications"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Cleanup", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Scheduled cleanup notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_delete)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
