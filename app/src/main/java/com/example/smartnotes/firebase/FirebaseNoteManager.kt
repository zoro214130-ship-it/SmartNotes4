package com.smartnotes.firebase

import com.example.smartnotes.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseNoteManager {
    private val db = FirebaseFirestore.getInstance()

    /** Save or update a note in Firestore */
    suspend fun saveNoteForUser(uid: String, note: Note): String? {
        return try {
            val docRef = if (note.remoteId != null)
                db.collection("users").document(uid)
                    .collection("notes").document(note.remoteId!!)
            else
                db.collection("users").document(uid)
                    .collection("notes").document()

            val data = hashMapOf(
                "title" to note.title,
                "content" to note.content,
                "timestamp" to note.timestamp,
                "pinned" to note.pinned
            )
            docRef.set(data).await()
            docRef.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Fetch all notes for a user */
    suspend fun getNotesForUser(uid: String): List<Note> {
        return try {
            val snap = db.collection("users").document(uid)
                .collection("notes").get().await()
            snap.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: ""
                val content = doc.getString("content") ?: ""
                val timestamp = doc.getLong("timestamp") ?: 0L
                val pinned = doc.getBoolean("pinned") ?: false
                Note(
                    localId = 0,
                    remoteId = doc.id,
                    title = title,
                    content = content,
                    timestamp = timestamp,
                    pinned = pinned
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Delete note from Firestore */
    suspend fun deleteNoteForUser(uid: String, remoteId: String) {
        try {
            db.collection("users").document(uid)
                .collection("notes").document(remoteId).delete().await()
        } catch (_: Exception) {}
    }
}
