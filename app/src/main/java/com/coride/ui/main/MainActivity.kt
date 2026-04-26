package com.coride.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.coride.R
import com.coride.data.repository.MockDataRepository
import com.coride.ui.common.ThemeHelper
import com.coride.ui.custom.CurvedBottomNavigationView
import com.coride.ui.verification.VerificationPopupDialogFragment
import com.coride.utils.FirebaseSafetyHelper
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: CurvedBottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyThemeState(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottomNav)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.mainNavHost) as NavHostFragment
        val navController = navHostFragment.navController

        val navItems = listOf(
            CurvedBottomNavigationView.NavItem(R.id.historyFragment, R.drawable.history, R.string.nav_activity),
            CurvedBottomNavigationView.NavItem(R.id.homeFragment, R.drawable.house, R.string.nav_rides),
            CurvedBottomNavigationView.NavItem(R.id.profileFragment, R.drawable.profile2, R.string.nav_profile)
        )
        bottomNav.setupWithNavController(navController, navItems)

        // Hide bottom nav on specialized screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.historyFragment, R.id.profileFragment -> {
                    bottomNav.visibility = View.VISIBLE
                }
                else -> {
                    // Hide bottom nav for Ride flow and Driver Interface
                    bottomNav.visibility = View.GONE
                }
            }
        }

        val user = MockDataRepository.getCurrentUser()
        if (user.isDriverMode) {
            navController.navigate(R.id.action_global_to_driver_dashboard)
        } else if (MockDataRepository.isLoggedIn() && !MockDataRepository.isUserVerified()) {
            VerificationPopupDialogFragment.newInstance {
                // Verified
            }.show(supportFragmentManager, "VerificationPopup")
        }
    }

    // --- HARDWARE SOS TRIGGER LOGIC ---
    private var volumeDownCount = 0
    private var lastVolumeDownTime = 0L
    private val SOS_TRIGGER_WINDOW = 3000L // 3 seconds window for 3 presses

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (event.keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN && event.action == android.view.KeyEvent.ACTION_DOWN) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastVolumeDownTime > SOS_TRIGGER_WINDOW) {
                // Reset if it's been too long since the last press
                volumeDownCount = 1
            } else {
                volumeDownCount++
            }
            lastVolumeDownTime = currentTime

            if (volumeDownCount >= 3) {
                // Trigger SOS and consume event
                volumeDownCount = 0
                triggerHardwareSos()
                return true 
            }
            // Allow default volume behavior to continue while counting
        }
        return super.dispatchKeyEvent(event)
    }

    private fun triggerHardwareSos() {
        val user = MockDataRepository.getCurrentUser()
        val activeRideId = com.coride.service.RideForegroundService.getActiveRideId()
        val rideId = if (activeRideId.isNotEmpty()) activeRideId else "hardware_sos_${System.currentTimeMillis()}"
        
        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            val trackingDuration = 10 * 60 * 1000L // Track for 10 minutes
            var isFirstNotification = true

            while (System.currentTimeMillis() - startTime < trackingDuration) {
                com.coride.ui.common.LocationHelper.getCurrentLocation(this@MainActivity) { location ->
                    val lat = location?.latitude ?: 0.0
                    val lng = location?.longitude ?: 0.0
                    
                    // 1. Update Live Firebase Tracking
                    FirebaseSafetyHelper.pushLocationUpdate(rideId, "user", lat, lng)
                    
                    if (isFirstNotification) {
                        // 2. Trigger App Internal SOS State
                        MockDataRepository.triggerSOS()
                        
                        // 3. Dispatch Admin Email Alert
                        com.coride.utils.EmailNotificationHelper.sendSosAlert(user, rideId, lat, lng)
                        
                        // 4. Dispatch SMS Alerts to Trusted Contacts
                        val smsMsg = com.coride.utils.SmsSafetyHelper.buildSosMessage(rideId, lat, lng)
                        com.coride.utils.SmsSafetyHelper.sendToAllEmergencyContacts(this@MainActivity, smsMsg)
                        
                        runOnUiThread {
                            android.widget.Toast.makeText(this@MainActivity, "🆘 EMERGENCY HARDWARE SOS TRIGGERED!\nLive location is being shared.", android.widget.Toast.LENGTH_LONG).show()
                        }
                        isFirstNotification = false
                    }
                }
                delay(10000) // Update every 10 seconds
            }
        }
    }
}

