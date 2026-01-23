package com.example.mind_detox.data.repository

import com.example.mind_detox.data.dao.BlockedAppDao
import com.example.mind_detox.data.dao.FocusSessionDao
import com.example.mind_detox.data.entity.BlockedApp
import com.example.mind_detox.data.entity.FocusSession
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val blockedAppDao: BlockedAppDao,
    private val focusSessionDao: FocusSessionDao
) {
    val allBlockedApps: Flow<List<BlockedApp>> = blockedAppDao.getAllBlockedApps()
    val allFocusSessions: Flow<List<FocusSession>> = focusSessionDao.getAllSessions()
    val totalFocusTime: Flow<Int?> = focusSessionDao.getTotalFocusTime()

    suspend fun insertBlockedApp(app: BlockedApp) {
        blockedAppDao.insert(app)
    }

    suspend fun deleteBlockedApp(app: BlockedApp) {
        blockedAppDao.delete(app)
    }

    suspend fun isAppBlocked(packageName: String): Boolean {
        return blockedAppDao.getAppByPackage(packageName) != null
    }

    suspend fun insertFocusSession(session: FocusSession) {
        focusSessionDao.insert(session)
    }
}
