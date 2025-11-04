package com.example.smartnotes.util

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val NAME = "smartnotes_prefs"
    private const val KEY_DARK = "dark_theme"
    private lateinit var prefs: SharedPreferences

    fun init(ctx: Context) {
        prefs = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    fun setDarkTheme(enabled: Boolean) = prefs.edit().putBoolean(KEY_DARK, enabled).apply()
    fun isDarkTheme(): Boolean = prefs.getBoolean(KEY_DARK, false)
}
