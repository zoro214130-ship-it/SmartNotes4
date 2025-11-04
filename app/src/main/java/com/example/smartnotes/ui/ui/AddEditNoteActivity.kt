package com.smartnotes.ui.ui

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.smartnotes.R
import com.smartnotes.databinding.ActivityAddEditNoteBinding
import com.example.smartnotes.model.Note
import com.smartnotes.repository.NoteRepository
import com.smartnotes.utils.smoothTransition
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditNoteBinding
    private lateinit var repo: NoteRepository
    private var editingLocalId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = NoteRepository()

        binding.toolbar.setNavigationOnClickListener { finishAfterTransition() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_share -> { shareNote(); true }
                R.id.action_export -> { exportToPDF(); true }
                else -> false
            }
        }

        editingLocalId = intent.getLongExtra("localId", -1L).takeIf { it != -1L }
        editingLocalId?.let { id ->
            lifecycleScope.launch {
                repo.getById(id)?.let {
                    binding.etTitle.setText(it.title)
                    binding.etContent.setText(it.content)
                }
            }
        }

        binding.btnSaveBottom.setOnClickListener { saveNote() }
    }

    private fun saveNote() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (title.isEmpty() && content.isEmpty()) {
            Snackbar.make(binding.root, "Note cannot be empty", Snackbar.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val note = Note(
                localId = editingLocalId ?: 0L,
                title = title,
                content = content,
                pinned = false,
                timestamp = System.currentTimeMillis()
            )
            repo.insertOrUpdate(note)
            Snackbar.make(binding.root, "Note saved ✅", Snackbar.LENGTH_SHORT).show()
            binding.btnSaveBottom.isEnabled = false
            binding.btnSaveBottom.alpha = 0.6f
            binding.btnSaveBottom.postDelayed({
                finish()
                smoothTransition()
            }, 600)
        }
    }

    private fun shareNote() {
        val subject = binding.etTitle.text.toString().ifBlank { "Smart Note" }
        val text = binding.etContent.text.toString()
        if (text.isEmpty()) {
            Snackbar.make(binding.root, "Nothing to share", Snackbar.LENGTH_SHORT).show()
            return
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share note via"))
    }

    private fun exportToPDF() {
        val title = binding.etTitle.text.toString().ifBlank { "Untitled Note" }
        val content = binding.etContent.text.toString()
        val folder = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "SmartNotes")
        if (!folder.exists()) folder.mkdirs()
        val file = File(folder, "${title.replace("[^a-zA-Z0-9-_]".toRegex(), "_")}.pdf")
        try {
            val bmp = Bitmap.createBitmap(1080, 1400, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.WHITE)
            val paintTitle = Paint().apply { color = Color.BLACK; textSize = 42f }
            val paintBody = Paint().apply { color = Color.DKGRAY; textSize = 30f }
            canvas.drawText(title, 60f, 80f, paintTitle)
            var y = 150f
            for (line in wrapText(content, paintBody, 960f)) {
                canvas.drawText(line, 60f, y, paintBody)
                y += 40f
            }
            FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            Snackbar.make(binding.root, "PDF saved: ${file.name}", Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split("\\s+".toRegex())
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val test = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(test) < maxWidth) {
                if (current.isNotEmpty()) current.append(" ")
                current.append(word)
            } else {
                lines.add(current.toString())
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }
}
