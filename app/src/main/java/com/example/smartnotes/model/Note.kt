package com.example.smartnotes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
    val remoteId: String? = null,
    val audioPath: String? = null,   // local file path
    val audioUrl: String? = null     // cloud storage download URL
)
