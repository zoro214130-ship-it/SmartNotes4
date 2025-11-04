package com.smartnotes

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.FirebaseApp

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Apply Material You dynamic color to all activities (Android 12+)
        DynamicColors.applyToActivitiesIfAvailable(this)

        // Initialize Firebase safely
        try {
            FirebaseApp.initializeApp(this)
        } catch (_: Exception) {
            // already initialized
        }

        // Enable Firestore offline support
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        db.firestoreSettings = settings
    }
}
