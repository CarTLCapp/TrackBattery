/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter

import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.NoteListEntryItemViewMvc
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.misc.EntryHint

class NoteListEntryController(
        private val repo: CarRepository,
        private val listener: Listener
) : NoteListEntryAdapter.Listener,
        NoteListEntryUseCase {

    interface Listener {
        fun onEntryHintChanged(entryHint: EntryHint)
        fun onNotesChanged(items: List<DataNote>)
        fun onNoteChanged(note: DataNote)
    }

    private val prefHelper = repo.prefHelper
    private val currentEditEntry: DataEntry?
        get() = prefHelper.currentEditEntry

    private var currentFocus: DataNote? = null

    private val isInNotes: Boolean
        get() {
            return repo.currentFlowElementId?.let { elementId ->
                repo.db.tableFlowElementNote.hasNotes(elementId)
            } ?: false
        }

    private var items: MutableList<DataNote> = mutableListOf()

    // region NoteListEntryAdapter.Listener

    override fun onBindViewHolder(viewMvc: NoteListEntryItemViewMvc, position: Int) {
        val item = items[position]
        viewMvc.label = item.name
        viewMvc.entryText = item.value
        viewMvc.bind(object : NoteListEntryItemViewMvc.Listener {
            override fun afterTextChanged(text: String) {
                onAfterTextChanged(viewMvc, item, text)
            }

            override fun onEntryFocused(hasFocus: Boolean) {
                viewMvc.isSelected = hasFocus
                onNoteFocused(item)
            }
        })
        when {
            item.type === DataNote.Type.ALPHANUMERIC -> {
                viewMvc.inputType = android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                viewMvc.maxLines = 1
            }
            item.type === DataNote.Type.NUMERIC_WITH_SPACES -> {
                viewMvc.inputType = android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or android.text.InputType.TYPE_CLASS_NUMBER
                viewMvc.maxLines = 1
            }
            item.type === DataNote.Type.NUMERIC -> {
                viewMvc.inputType = android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or android.text.InputType.TYPE_CLASS_NUMBER
                viewMvc.maxLines = 1
            }
            item.type === DataNote.Type.MULTILINE -> {
                viewMvc.inputType = android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                viewMvc.maxLines = 3
                viewMvc.numLines = 3
            }
            else -> {
                viewMvc.inputType = android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                viewMvc.maxLines = 1
            }
        }
    }

    private fun onAfterTextChanged(viewMvc: NoteListEntryItemViewMvc, item: DataNote, text: String?) {
        if (viewMvc.isSelected) {
            item.value = text
            updateNoteValue(item)
            onNoteChanged(item)
        }
    }

    private fun onNoteChanged(note: DataNote) {
        if (currentFocus === note) {
            display(note)
        }
    }

    private fun onNoteFocused(note: DataNote) {
        currentFocus = note
        display(note)
    }

    private fun display(note: DataNote) {
        if (!isInNotes) {
            return
        }
        listener.onEntryHintChanged(note.entryHint)
    }

    private fun updateNoteValue(note: DataNote) {
        repo.db.tableNote.updateValue(note)
        currentEditEntry?.updateNoteValue(note)
        listener.onNoteChanged(note)
    }

    // endregion NoteListEntryAdapter.Listener

    // region NoteListEntryUseCase

    override val numNotes: Int
        get() = items.size

    override val notes: List<DataNote>
        get() = items

    override fun onNoteDataChanged() {
        items = currentEditEntry?.overlayNoteValues(queryNotes())?.toMutableList()
                ?: queryNotes().toMutableList()
        listener.onNotesChanged(items)
    }

    // TODO: Perhaps if this is an existing value (see DataEntry.notesWithValues), then overlay those values
    private fun queryNotes(): List<DataNote> = repo.db.noteHelper.getNotesFromCurrentFlowElementId(repo.currentFlowElementId)

    // endregion NoteListEntryUseCase
}
