package com.smartnotes.ui.ui

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartnotes.R
import com.smartnotes.databinding.ActivityMainBinding
import com.smartnotes.firebase.FirebaseHelper
import com.example.smartnotes.model.Note
import com.smartnotes.repository.NoteRepository
import com.example.smartnotes.data.AppDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NoteClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repo: NoteRepository
    private lateinit var adapter: NoteAdapter

    private var multiSelectMode = false
    private val selectedNotes = mutableListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        com.google.android.material.color.DynamicColors.applyIfAvailable(this)

        setSupportActionBar(binding.topAppBar)

        val db = AppDatabase.getDatabase(this)
        repo = NoteRepository(db.noteDao())

        setupRecycler()
        setupFab()
        setupSelectionActions()
        observeNotes()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView
        searchView?.queryHint = "Search notes..."
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also { adapter.filter(query ?: "") }
            override fun onQueryTextChange(newText: String?) = true.also { adapter.filter(newText ?: "") }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_select -> toggleMultiSelect()
            R.id.menu_logout -> {
                FirebaseHelper.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecycler() {
        adapter = NoteAdapter(mutableListOf(), this)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddEditNoteActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this, android.R.anim.fade_in, android.R.anim.fade_out
            )
            startActivity(intent, options.toBundle())
        }
    }

    private fun startShimmer() {
        binding.shimmerLayout.startShimmer()
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.recycler.visibility = View.GONE
        binding.emptyState.visibility = View.GONE
    }

    private fun stopShimmer() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.recycler.visibility = View.VISIBLE
    }

    private fun observeNotes() {
        lifecycleScope.launch {
            startShimmer()
            repo.getAllNotes().collectLatest { list ->
                stopShimmer()
                adapter.updateList(list)
                binding.recycler.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.tvNotesCount.text = "(${list.size})"
            }
        }

        lifecycleScope.launch {
            while (true) {
                repo.syncFromCloud()
                kotlinx.coroutines.delay(30000)
            }
        }
    }

    private fun toggleMultiSelect() {
        multiSelectMode = !multiSelectMode
        selectedNotes.clear()
        adapter.enableMultiSelect(multiSelectMode)
        binding.topAppBar.title =
            if (multiSelectMode) "Select notes" else getString(R.string.app_name)
        binding.selectionActions.isVisible = multiSelectMode
    }

    override fun onNoteClicked(note: Note) {
        if (multiSelectMode) {
            adapter.toggleSelection(note)
            selectedNotes.clear()
            selectedNotes.addAll(adapter.getSelected())
            binding.topAppBar.title = "${selectedNotes.size} selected"
        } else {
            val intent = Intent(this, AddEditNoteActivity::class.java)
            intent.putExtra("localId", note.localId)
            val options = ActivityOptions.makeCustomAnimation(
                this, android.R.anim.slide_in_left, android.R.anim.slide_out_right
            )
            startActivity(intent, options.toBundle())
        }
    }

    override fun onNoteLongClicked(note: Note) {
        if (!multiSelectMode) toggleMultiSelect()
        adapter.toggleSelection(note)
        selectedNotes.clear()
        selectedNotes.addAll(adapter.getSelected())
        binding.topAppBar.title = "${selectedNotes.size} selected"
    }

    private fun setupSelectionActions() {
        binding.btnDelete.setOnClickListener {
            lifecycleScope.launch {
                selectedNotes.forEach { repo.delete(it) }
                Toast.makeText(this@MainActivity, "Deleted ${selectedNotes.size} notes", Toast.LENGTH_SHORT).show()
                toggleMultiSelect()
            }
        }
        binding.btnShare.setOnClickListener {
            if (selectedNotes.isNotEmpty()) {
                val shareText = selectedNotes.joinToString("\n\n") { it.content ?: "" }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Share notes via"))
            }
        }
        binding.btnPin.setOnClickListener {
            Toast.makeText(this, "Pin feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        binding.btnEdit.setOnClickListener {
            if (selectedNotes.size == 1) {
                val intent = Intent(this, AddEditNoteActivity::class.java)
                intent.putExtra("localId", selectedNotes.first().localId)
                startActivity(intent)
                toggleMultiSelect()
            } else {
                Toast.makeText(this, "Select only one note to edit", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
