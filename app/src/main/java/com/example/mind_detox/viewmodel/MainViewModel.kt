package com.example.mind_detox.viewmodel

import android.app.Application
import android.location.Geocoder
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
    val allBlockedApps: LiveData<List<BlockedApp>>
    val totalFocusTime: LiveData<Int?>

    private val _currentLocation = MutableLiveData<String>("Waiting for update...")
    val currentLocation: LiveData<String> = _currentLocation

    private val _quoteAndLocationMessage = MutableLiveData<String>()
    val quoteAndLocationMessage: LiveData<String> = _quoteAndLocationMessage

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.blockedAppDao(), database.focusSessionDao())
        allBlockedApps = repository.allBlockedApps.asLiveData()
        totalFocusTime = repository.totalFocusTime.asLiveData()
        prepareQuote(null) // Prepare a quote without location initially
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        // Initial update with coordinates
        _currentLocation.postValue(String.format("Location: %.4f, %.4f", latitude, longitude))
        
        // Reverse Geocoding to get location name
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(getApplication(), Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val cityName = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Unknown City"
                    
                    // Update UI with both name and coordinates
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
        val message = if (locationName != null) {
            randomQuote + "\n\nCurrently logged in from \"$locationName\""
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
