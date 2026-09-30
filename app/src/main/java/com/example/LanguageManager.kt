package com.example

import android.content.Context
import android.content.SharedPreferences

object LanguageManager {
    private const val PREFS_NAME = "cropsaathi_prefs"
    private const val KEY_LANGUAGE = "advisory_language"

    fun getLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "English") ?: "English"
    }

    fun setLanguage(context: Context, language: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }
    
    val supportedLanguages = listOf(
        "English",
        "हिंदी (Hindi)",
        "मराठी (Marathi)",
        "ਪੰਜਾਬੀ (Punjabi)",
        "ગુજરાતી (Gujarati)",
        "தமிழ் (Tamil)",
        "తెలుగు (Telugu)"
    )
}
