package com.example.mind_detox.viewmodel

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.*
import com.example.mind_detox.data.AppDatabase
import com.example.mind_detox.data.entity.BlockedApp
import com.example.mind_detox.data.entity.FocusSession
import com.example.mind_detox.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    private val sharedPref = application.getSharedPreferences("mind_detox_prefs", Context.MODE_PRIVATE)
    
    val allBlockedApps: LiveData<List<BlockedApp>>
    val totalFocusTime: LiveData<Int?>

    private val _currentLocation = MutableLiveData<String>("Waiting for update...")
    val currentLocation: LiveData<String> = _currentLocation

    private val _quoteAndLocationMessage = MutableLiveData<String>()
    val quoteAndLocationMessage: LiveData<String> = _quoteAndLocationMessage

    private val _isFocusModeActive = MutableLiveData<Boolean>()
    val isFocusModeActive: LiveData<Boolean> = _isFocusModeActive

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userMilestone = MutableLiveData<String>()
    val userMilestone: LiveData<String> = _userMilestone

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.blockedAppDao(), database.focusSessionDao())
        allBlockedApps = repository.allBlockedApps.asLiveData()
        totalFocusTime = repository.totalFocusTime.asLiveData()
        
        // Initialize from SharedPreferences
        _isFocusModeActive.value = sharedPref.getBoolean("is_focus_active", false)
        _userName.value = sharedPref.getString("user_name", "Focus") ?: "Focus"
        _userMilestone.value = sharedPref.getString("user_milestone", "No milestone set yet.")
        
        prepareQuote(null)
    }

    fun setFocusMode(active: Boolean) {
        sharedPref.edit().putBoolean("is_focus_active", active).apply()
        _isFocusModeActive.value = active
    }

    fun setUserName(name: String) {
        sharedPref.edit().putString("user_name", name).apply()
        _userName.value = name
    }

    fun setMilestone(milestone: String) {
        Log.d("MainViewModel", "Saving milestone: $milestone")
        sharedPref.edit().putString("user_milestone", milestone).apply()
        _userMilestone.postValue(milestone)
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _currentLocation.postValue(String.format("Location: %.4f, %.4f", latitude, longitude))
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(getApplication(), Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val cityName = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Unknown City"
                    sharedPref.edit().putString("last_location_name", cityName).apply()
                    _currentLocation.postValue(String.format("%s (%.4f, %.4f)", cityName, latitude, longitude))
                    prepareQuote(cityName)
                } else {
                    prepareQuote(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                prepareQuote(null)
            }
        }
    }

    fun prepareQuote(locationName: String?) {
        val quotes = listOf(
            "The secret of getting ahead is getting started.",
            "It always seems impossible until it's done.",
            "Quality is not an act, it is a habit.",
            "Believe you can and you're halfway there.",
            "Your time is limited, so don't waste it living someone else's life."
        )
        val randomQuote = quotes.random()
        val finalLocation = locationName ?: sharedPref.getString("last_location_name", null)
        
        val message = if (finalLocation != null) {
            randomQuote + "\n\nCurrently logged in from \"$finalLocation\""
        } else {
            randomQuote
        }
        _quoteAndLocationMessage.postValue(message)
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
