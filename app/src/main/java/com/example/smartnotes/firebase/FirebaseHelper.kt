package com.smartnotes.firebase

import com.example.smartnotes.model.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseHelper {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun signUpWithEmail(email: String, password: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun signOut() = auth.signOut()

    // save note for current user and return remote id
    suspend fun saveNoteRemote(note: Note): String? {
        return try {
            val uid = auth.currentUser?.uid ?: return null
            val data = note.copy(localId = 0L) // don't store localId
            val ref = db.collection("users").document(uid).collection("notes").add(data).await()
            ref.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // update note remote (if remoteId present)
    suspend fun updateNoteRemote(note: Note) {
        try {
            val uid = auth.currentUser?.uid ?: return
            val rid = note.remoteId ?: return
            db.collection("users").document(uid).collection("notes").document(rid)
                .set(note).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteNoteRemote(remoteId: String) {
        try {
            val uid = auth.currentUser?.uid ?: return
            db.collection("users").document(uid).collection("notes").document(remoteId)
                .delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getAllNotesRemote(): List<Note> {
        return try {
            val uid = auth.currentUser?.uid ?: return emptyList()
            val snap = db.collection("users").document(uid).collection("notes").get().await()
            snap.documents.mapNotNull { it.toObject(Note::class.java) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
