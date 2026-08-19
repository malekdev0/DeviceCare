package com.malek.devicecare.schedule

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.malek.devicecare.MainActivity
import com.malek.devicecare.R
import com.malek.devicecare.data.DeviceRepository
import com.malek.devicecare.data.ScanHistoryRepository
import com.malek.devicecare.domain.model.HealthScanRecord
import com.malek.devicecare.domain.model.HealthStatus

class WeeklyHealthScanWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val repository = DeviceRepository(applicationContext)
        val battery = repository.getBatteryInfo()
        val storage = repository.getStorageInfo()
        val memory = repository.getMemoryInfo()
        val security = repository.scanSecurity()
        val batteryStatus = if (battery.percentage < 20) HealthStatus.WARNING else HealthStatus.GOOD
        val storageStatus = when {
            storage.usagePercentage > 90 -> HealthStatus.CRITICAL
            storage.usagePercentage > 75 -> HealthStatus.WARNING
            else -> HealthStatus.GOOD
        }
        val memoryStatus = if (memory.lowMemory) HealthStatus.CRITICAL else HealthStatus.GOOD
        val networkStatus = if (repository.getNetworkInfo().isConnected) HealthStatus.GOOD else HealthStatus.WARNING
        val securityStatus = when {
            security.overallScore < 50 -> HealthStatus.CRITICAL
            security.overallScore < 85 -> HealthStatus.WARNING
            else -> HealthStatus.GOOD
        }
        val score = listOf(batteryStatus, storageStatus, memoryStatus, networkStatus, securityStatus).sumOf {
            when (it) {
                HealthStatus.WARNING -> -15
                HealthStatus.CRITICAL -> -30
                else -> 0
            }
        }.plus(100).coerceAtLeast(0)
        ScanHistoryRepository(applicationContext).add(
            HealthScanRecord(
                timestamp = System.currentTimeMillis(),
                score = score,
                batteryStatus = batteryStatus,
                storageStatus = storageStatus,
                memoryStatus = memoryStatus,
                networkStatus = networkStatus,
                securityStatus = securityStatus
            )
        )
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Scheduled scans", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Weekly Device Care system health summaries"
            }
        )

        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        NotificationManagerCompat.from(applicationContext).notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Your weekly scan is here!")
                .setContentText("Open Device Care to view your results.")
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()
        )
    }

    private companion object {
        const val CHANNEL_ID = "weekly_health_scans"
        const val NOTIFICATION_ID = 1001
    }
}
