package com.coride.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.coride.ui.custom.CurvedBottomNavigationView
import com.coride.R
import com.coride.data.repository.MockDataRepository
import com.coride.ui.common.ThemeHelper
import com.coride.ui.verification.VerificationPopupDialogFragment

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
}

