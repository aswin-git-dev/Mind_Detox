package com.example.mind_detox.service

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.mind_detox.MainActivity
import com.example.mind_detox.R
import com.example.mind_detox.data.AppDatabase
import com.example.mind_detox.data.repository.AppRepository
import com.example.mind_detox.ui.BlockActivity
import kotlinx.coroutines.*
import java.util.*

class FocusService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private lateinit var repository: AppRepository
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        repository = AppRepository(database.blockedAppDao(), database.focusSessionDao())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            startForegroundService()
            isRunning = true
            monitorApps()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "focus_mode_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Focus Mode Active",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Mind Detox Active")
            .setContentText("Focus mode is on. Stay productive!")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use a proper icon later
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0, Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        startForeground(1, notification)
    }

    private fun monitorApps() {
        serviceScope.launch(Dispatchers.Default) {
            while (isRunning) {
                val currentApp = getForegroundApp()
                if (currentApp != null && repository.isAppBlocked(currentApp)) {
                    showBlockScreen(currentApp)
                }
                delay(1000) // Check every second
            }
        }
    }

    private fun getForegroundApp(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 10,
            time
        )
        return stats?.maxByOrNull { it.lastTimeUsed }?.packageName
    }

    private fun showBlockScreen(packageName: String) {
        val intent = Intent(this, BlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("PACKAGE_NAME", packageName)
        }
        startActivity(intent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceJob.cancel()
    }
}
