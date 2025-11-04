package com.smartnotes.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun saveUserData(uid: String, name: String, email: String) {
        val user = hashMapOf(
            "uid" to uid,
            "name" to name,
            "email" to email
        )
        db.collection("users").document(uid).set(user)
    }
}
