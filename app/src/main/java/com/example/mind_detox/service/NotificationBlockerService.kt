package com.example.mind_detox.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.mind_detox.data.AppDatabase
import com.example.mind_detox.data.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NotificationBlockerService : NotificationListenerService() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var repository: AppRepository

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        repository = AppRepository(database.blockedAppDao(), database.focusSessionDao())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val packageName = sbn?.packageName ?: return

        serviceScope.launch {
            if (repository.isAppBlocked(packageName)) {
                cancelNotification(sbn.key)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
