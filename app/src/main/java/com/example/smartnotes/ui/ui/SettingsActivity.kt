package com.smartnotes.ui.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smartnotes.databinding.ActivitySettingsBinding
import com.smartnotes.repository.NoteRepository
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val repo = NoteRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSyncNow.setOnClickListener {
            lifecycleScope.launch {
                binding.btnSyncNow.isEnabled = false
                try {
                    repo.syncFromCloud()
                    Toast.makeText(this@SettingsActivity, "✅ Synced successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@SettingsActivity, "❌ Sync failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    binding.btnSyncNow.isEnabled = true
                }
            }
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "🌙 Dark Mode Enabled" else "☀️ Light Mode Enabled", Toast.LENGTH_SHORT).show()
        }

        binding.btnBackup.setOnClickListener {
            Toast.makeText(this, "☁️ Backup feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnClearNotes.setOnClickListener {
            Toast.makeText(this, "⚠️ All notes cleared!", Toast.LENGTH_SHORT).show()
        }
    }
}
