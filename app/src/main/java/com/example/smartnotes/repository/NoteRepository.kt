package com.smartnotes.repository

import com.example.smartnotes.data.NoteDao
import com.smartnotes.firebase.FirebaseHelper
import com.smartnotes.firebase.FirebaseStorageHelper
import com.example.smartnotes.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class NoteRepository(private val noteDao: NoteDao? = null) {

    fun getAllNotes(): Flow<List<Note>> = noteDao!!.getAllNotes()

    suspend fun getById(id: Long): Note? = withContext(Dispatchers.IO) {
        noteDao?.getById(id)
    }

    suspend fun insert(note: Note) = withContext(Dispatchers.IO) {
        var toSave = note
        // if local audio exists, upload and set audioUrl
        if (!note.audioPath.isNullOrEmpty() && File(note.audioPath).exists()) {
            val url = FirebaseStorageHelper.uploadAudio(note.audioPath)
            toSave = note.copy(audioUrl = url)
        }
        noteDao?.insert(toSave)
        FirebaseHelper.saveNoteRemote(toSave)
    }

    suspend fun update(note: Note) = withContext(Dispatchers.IO) {
        var toUpdate = note
        if (!note.audioPath.isNullOrEmpty() && File(note.audioPath).exists()) {
            val url = FirebaseStorageHelper.uploadAudio(note.audioPath)
            toUpdate = note.copy(audioUrl = url)
        }
        noteDao?.update(toUpdate)
        FirebaseHelper.updateNoteRemote(toUpdate)
    }

    suspend fun delete(note: Note) = withContext(Dispatchers.IO) {
        noteDao?.delete(note)
        note.remoteId?.let { FirebaseHelper.deleteNoteRemote(it) }
        note.audioUrl?.let { FirebaseStorageHelper.deleteAudio(it) }
    }

    suspend fun insertOrUpdate(note: Note) {
        if (note.localId == 0L) insert(note) else update(note)
    }

    suspend fun syncFromCloud() = withContext(Dispatchers.IO) {
        val cloudNotes = FirebaseHelper.getAllNotesRemote()
        noteDao?.insertAll(cloudNotes)
    }
}
