package com.smartnotes.firebase

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.File

object FirebaseStorageHelper {
    private val storage = Firebase.storage

    // upload local audio file, return public download URL (or null)
    suspend fun uploadAudio(localPath: String): String? {
        return try {
            val file = File(localPath)
            if (!file.exists()) return null
            val ref = storage.reference.child("users_audio/${file.name}_${System.currentTimeMillis()}")
            ref.putFile(android.net.Uri.fromFile(file)).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // delete by storage download URL (best-effort)
    suspend fun deleteAudio(downloadUrl: String?) {
        if (downloadUrl.isNullOrBlank()) return
        try {
            val ref = storage.getReferenceFromUrl(downloadUrl)
            ref.delete().await()
        } catch (e: Exception) {
            // ignore
            e.printStackTrace()
        }
    }
}
