/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist

import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.model.misc.EntryHint
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvc

interface MainListViewMvc : ObservableViewMvc<MainListViewMvc.Listener> {

    interface Listener {
        fun onEntryHintChanged(entryHint: EntryHint)
        fun onNoteChanged(note: DataNote)
        fun onSimpleItemClicked(position: Int, value: String)
        fun onProjectGroupSelected(projectGroup: DataProjectAddressCombo)
        fun onRadioItemSelected(text: String)
        fun onCheckBoxItemChanged(position: Int, prompt: String, isChecked: Boolean)
        fun isCheckBoxItemSelected(position: Int): Boolean
        fun onSubFlowSelected(position: Int)
    }

    enum class Adapter {
        SIMPLE,
        PROJECT,
        EQUIPMENT,
        SUB_FLOWS,
        RADIO,
        NOTE_ENTRY,
        CHECK_BOX
    }

    val numNotes: Int
    val notes: List<DataNote>

    var visible: Boolean
    var emptyVisible: Boolean
    var simpleItems: List<String>
    var radioItems: List<String>
    var radioSelectedText: String?
    var checkBoxItems: List<String>

    fun setAdapter(adapter: Adapter)
    fun scrollToPosition(position: Int)
    fun setSimpleNoneSelected()
    fun setSimpleSelected(value: String): Int

}