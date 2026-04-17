package com.coride.ui.common

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Utility to enforce the "Monochrome & Obsidian" dark/light design
 * and completely prevent system Dark Mode overrides.
 */
object ThemeHelper {

    /**
     * Applies the hardcoded branding theme to an activity. 
     * MUST be called before super.onCreate().
     */
    fun applyThemeState(activity: android.app.Activity) {
        activity.setTheme(com.coride.R.style.Theme_CoRide)
        // Permanently enforce light mode styling (with monochrome design)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    /**
     * Call this on app startup.
     */
    fun init(context: Context) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}


