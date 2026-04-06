package com.indrive.clone.ui.common

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * Utility to manage and persist theme selection (Light/Dark mode).
 */
object ThemeHelper {

    private const val PREFS_NAME = "coride_prefs"
    private const val KEY_DARK_MODE = "is_dark_mode"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Persists and applies the theme mode.
     */
    fun setDarkMode(context: Context, isEnabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DARK_MODE, isEnabled).apply()
        applyTheme(isEnabled)
    }

    /**
     * Checks the stored preference.
     */
    fun isDarkMode(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DARK_MODE, false)
    }

    /**
     * Applies the theme mode globally using AppCompatDelegate.
     */
    fun applyTheme(isDark: Boolean) {
        val mode = if (isDark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    /**
     * Call this on app startup (e.g. Activity onCreate) to set the initial theme state.
     */
    fun init(context: Context) {
        applyTheme(isDarkMode(context))
    }
}
