package com.smartnotes.ui.ui


import com.example.smartnotes.model.Note

interface NoteClickListener {
    fun onNoteClicked(note: Note)
    fun onNoteLongClicked(note: Note)
}
