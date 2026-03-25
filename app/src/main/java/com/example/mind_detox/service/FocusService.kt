package com.example.mind_detox.service

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mind_detox.MainActivity
import com.example.mind_detox.R
import com.example.mind_detox.data.AppDatabase
import com.example.mind_detox.data.repository.AppRepository
import com.example.mind_detox.ui.BlockActivity
import kotlinx.coroutines.*

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
            startMonitoring()
            Log.d("FocusService", "Monitoring Service Started")
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "focus_mode_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Mind Detox Background Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Running in foreground to monitor distracting apps"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Background Monitor Active")
            .setContentText("Monitoring your focus. Distracting apps will be blocked.")
            .setSmallIcon(R.drawable.app_logo)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0, Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        startForeground(1001, notification)
    }

    private fun startMonitoring() {
        serviceScope.launch(Dispatchers.Default) {
            while (isRunning) {
                val currentApp = getForegroundApp()
                Log.d("FocusService", "Foreground App: $currentApp")
                
                if (currentApp != null && !isSelf(currentApp)) {
                    if (repository.isAppBlocked(currentApp)) {
                        Log.d("FocusService", "Blocking Distraction: $currentApp")
                        showBlockScreen(currentApp)
                    }
                }
                delay(1000) 
            }
        }
    }

    private fun isSelf(packageName: String): Boolean {
        return packageName == this.packageName || packageName == "com.android.settings"
    }

    private fun getForegroundApp(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 10,
            time
        )
        
        return if (stats != null && stats.isNotEmpty()) {
            val sortedStats = stats.sortedByDescending { it.lastTimeUsed }
            sortedStats[0].packageName
        } else {
            null
        }
    }

    private fun showBlockScreen(packageName: String) {
        val intent = Intent(this, BlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("PACKAGE_NAME", packageName)
        }
        startActivity(intent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceJob.cancel()
        Log.d("FocusService", "Monitoring Service Stopped")
    }
}
