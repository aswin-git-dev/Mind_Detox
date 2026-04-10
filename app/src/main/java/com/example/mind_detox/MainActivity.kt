package com.example.mind_detox

import android.content.Intent
import android.os.Bundle
import android.text.util.Linkify
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mind_detox.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.appBarMain.adView?.loadAd(adRequest)

        // Find the NavHostFragment correctly
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up the toolbar
        setSupportActionBar(binding.appBarMain.toolbar)

        // Setting up Bottom Navigation
        binding.appBarMain.contentMain.bottomNavView?.let { bottomNav ->
            val appBarConfiguration = AppBarConfiguration(
                setOf(R.id.nav_home, R.id.nav_apps, R.id.nav_stats)
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            bottomNav.setupWithNavController(navController)
        }

        // Share functionality for Social Media Integration
        binding.appBarMain.fab?.setOnClickListener {
            shareAppContent()
        }

        // Show Quote of the Day popup
        showQuotePopup()
    }

    private fun shareAppContent() {
        val shareText = "Check out Mind Detox to improve your focus and mental well-being! Download it here: https://www.mind-detox.example.com"
        
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Mind Detox via")
        startActivity(shareIntent)
    }

    private fun showQuotePopup() {
        val quotes = listOf(
            "The secret of getting ahead is getting started.",
            "It always seems impossible until it's done.",
            "Quality is not an act, it is a habit.",
            "Believe you can and you're halfway there.",
            "Your time is limited, so don't waste it living someone else's life."
        )
        val randomQuote = quotes.random()

        MaterialAlertDialogBuilder(this)
            .setTitle("Quote to Start Your Day")
            .setMessage(randomQuote)
            .setPositiveButton("Let's Go!") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Handle navigation when an item is selected
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
