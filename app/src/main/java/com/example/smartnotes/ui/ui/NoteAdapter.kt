package com.smartnotes.ui.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartnotes.R
import com.example.smartnotes.model.Note
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(
    private var list: MutableList<Note>,
    private val listener: NoteClickListener
) : RecyclerView.Adapter<NoteAdapter.VH>() {

    private val original = ArrayList(list)
    private val selectedNotes = mutableListOf<Note>()
    private var multiSelect = false

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val content: TextView = view.findViewById(R.id.content)
        val date: TextView = view.findViewById(R.id.date)
        val pin: ImageView = view.findViewById(R.id.pin)
        val checkBox: CheckBox = view.findViewById(R.id.checkBoxSelect)

        init {
            view.setOnClickListener { listener.onNoteClicked(list[bindingAdapterPosition]) }
            view.setOnLongClickListener {
                listener.onNoteLongClicked(list[bindingAdapterPosition]); true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val note = list[position]
        holder.title.text = note.title.ifBlank { "Untitled" }
        holder.content.text = note.content
        holder.pin.visibility = if (note.pinned) View.VISIBLE else View.GONE

        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        holder.date.text = sdf.format(Date(note.timestamp))

        holder.checkBox.visibility = if (multiSelect) View.VISIBLE else View.GONE
        holder.checkBox.isChecked = selectedNotes.contains(note)
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<Note>) {
        list = newList.toMutableList()
        original.clear(); original.addAll(list)
        notifyDataSetChanged()
    }

    fun filter(q: String) {
        val s = q.lowercase(Locale.getDefault()).trim()
        list = if (s.isEmpty()) ArrayList(original) else original.filter {
            it.title.lowercase(Locale.getDefault()).contains(s) ||
                    it.content.lowercase(Locale.getDefault()).contains(s)
        }.toMutableList()
        notifyDataSetChanged()
    }

    fun enableMultiSelect(enable: Boolean) {
        multiSelect = enable
        selectedNotes.clear()
        notifyDataSetChanged()
    }

    fun toggleSelection(note: Note) {
        if (selectedNotes.contains(note)) selectedNotes.remove(note)
        else selectedNotes.add(note)
        notifyDataSetChanged()
    }

    fun getSelected(): List<Note> = selectedNotes
}
