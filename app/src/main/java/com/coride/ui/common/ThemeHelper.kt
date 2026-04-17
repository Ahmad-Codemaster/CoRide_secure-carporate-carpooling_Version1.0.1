package com.coride.ui.common

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Utility to enforce the exact "Eco & Trustworthy" Kelly Green design
 * and completely prevent system Dark Mode overrides.
 */
object ThemeHelper {

    /**
     * Applies the hardcoded branding theme to an activity. 
     * MUST be called before super.onCreate().
     */
    fun applyThemeState(activity: android.app.Activity) {
        activity.setTheme(com.coride.R.style.Theme_CoRideClone)
        // Permanently enforce light mode styling
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    /**
     * Call this on app startup.
     */
    fun init(context: Context) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}


