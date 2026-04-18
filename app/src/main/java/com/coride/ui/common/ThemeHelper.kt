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
        // Resolve by resource name so preview/sync issues in generated R do not break compilation.
        val coRideThemeId = activity.resources.getIdentifier("Theme.CoRide", "style", activity.packageName)
        if (coRideThemeId != 0) {
            activity.setTheme(coRideThemeId)
        }
        // Permanently enforce light mode styling (with monochrome design)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    /**
     * Call this on app startup.
     */
    fun init(context: Context) {
        @Suppress("UNUSED_VARIABLE")
        val appContext = context.applicationContext
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
