package com.example.smartnotes

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.smartnotes.util.Prefs

class SmartNotesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        // Apply saved theme
        val isDark = Prefs.isDarkTheme()
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
