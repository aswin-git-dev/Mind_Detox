package com.example.mind_detox.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.mind_detox.data.AppDatabase
import com.example.mind_detox.data.entity.BlockedApp
import com.example.mind_detox.data.entity.FocusSession
import com.example.mind_detox.data.repository.AppRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    val allBlockedApps: LiveData<List<BlockedApp>>
    val totalFocusTime: LiveData<Int?>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.blockedAppDao(), database.focusSessionDao())
        allBlockedApps = repository.allBlockedApps.asLiveData()
        totalFocusTime = repository.totalFocusTime.asLiveData()
    }

    fun toggleAppBlock(packageName: String, appName: String, isBlocked: Boolean) {
        viewModelScope.launch {
            if (isBlocked) {
                repository.insertBlockedApp(BlockedApp(packageName, appName))
            } else {
                repository.deleteBlockedApp(BlockedApp(packageName, appName, false))
            }
        }
    }

    fun saveFocusSession(session: FocusSession) {
        viewModelScope.launch {
            repository.insertFocusSession(session)
        }
    }
}
