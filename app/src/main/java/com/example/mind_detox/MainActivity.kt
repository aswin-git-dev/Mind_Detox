package com.example.mind_detox

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mind_detox.databinding.ActivityMainBinding
import com.example.mind_detox.service.LocationService
import com.example.mind_detox.viewmodel.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var isQuoteShown = false

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                startLocationService()
            }
            else -> {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                // If denied, we still show the quote but it won't have the location
                viewModel.prepareQuote(null)
            }
        }
    }

    private val locationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val latitude = intent?.getDoubleExtra("latitude", 0.0) ?: 0.0
            val longitude = intent?.getDoubleExtra("longitude", 0.0) ?: 0.0
            Log.d("MainActivity", "Real-time Location received: $latitude, $longitude")
            viewModel.updateLocation(latitude, longitude)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.contentMain.bottomNavView?.let { bottomNav ->
            val appBarConfiguration = AppBarConfiguration(
                setOf(R.id.nav_home, R.id.nav_apps, R.id.nav_stats)
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            bottomNav.setupWithNavController(navController)
        }

        // Observe the message from ViewModel
        viewModel.quoteAndLocationMessage.observe(this) { message ->
            if (!isQuoteShown) {
                showQuotePopup(message)
                isQuoteShown = true
            }
        }

        checkLocationPermissions()
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationService()
        } else {
            locationPermissionRequest.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("LocationUpdate")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(locationUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(locationUpdateReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(locationUpdateReceiver)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error unregistering receiver", e)
        }
    }

    private fun showQuotePopup(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Quote to Start Your Day")
            .setMessage(message)
            .setPositiveButton("Let's Go!") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true) // Changed to true to allow closing by clicking outside
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
