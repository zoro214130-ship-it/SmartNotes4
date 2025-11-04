package com.smartnotes.utils

import android.app.Activity
import android.view.View
import com.google.android.material.snackbar.Snackbar

// ✅ Global snackbar helper
fun showSnack(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
}

// ✅ Smooth fade transition between screens
fun Activity.smoothTransition() {
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}
