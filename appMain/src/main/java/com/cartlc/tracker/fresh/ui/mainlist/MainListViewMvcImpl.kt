/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataEquipment
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.model.misc.EntryHint
import com.cartlc.tracker.fresh.ui.app.factory.FactoryAdapterController
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvcImpl
import com.cartlc.tracker.fresh.ui.mainlist.adapter.*

class MainListViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?,
        factoryViewMvc: FactoryViewMvc,
        factoryAdapterController: FactoryAdapterController
) : ObservableViewMvcImpl<MainListViewMvc.Listener>(),
        MainListViewMvc,
        SimpleListController.Listener,
        ProjectGroupListController.Listener,
        EquipmentSelectController.Listener,
        RadioListController.Listener,
        NoteListEntryController.Listener,
        CheckBoxListController.Listener,
        SubFlowsListController.Listener {

    override val rootView: View = inflater.inflate(R.layout.frame_main_list, container, false) as ViewGroup

    private val mainList = findViewById<RecyclerView>(R.id.main_list)
    private val emptyView = findViewById<TextView>(R.id.empty)
    private val simpleListController = factoryAdapterController.allocSimpleListController(this)
    private val simpleListAdapter = SimpleListAdapter(factoryViewMvc, R.layout.mainlist_item_simple, simpleListController)
    private val projectGroupController = factoryAdapterController.allocProjectGroupController(this)
    private val projectGroupAdapter = ProjectGroupListAdapter(factoryViewMvc, projectGroupController)
    private val equipmentSelectController = factoryAdapterController.allocEquipmentSelectController(this)
    private val equipmentSelectAdapter = EquipmentSelectListAdapter(factoryViewMvc, equipmentSelectController)
    private val radioController = factoryAdapterController.allocRadioListController(this)
    private val radioAdapter = RadioListAdapter(factoryViewMvc, radioController)
    private val noteListEntryController = factoryAdapterController.allocNoteListEntryController(this)
    private val noteListEntryAdapter = NoteListEntryAdapter(factoryViewMvc, noteListEntryController)
    private val checkBoxListController = factoryAdapterController.allocCheckBoxListController(this)
    private val checkBoxListAdapter = CheckBoxListAdapter(factoryViewMvc, checkBoxListController)
    private val subFlowListController = factoryAdapterController.allocSubFlowListController(this)
    private val subFlowListAdapter = SubFlowsListAdapter(factoryViewMvc, subFlowListController)

    init {
        val linearLayoutManager = LinearLayoutManager(mainList.context)
        mainList.layoutManager = linearLayoutManager
        val divider = DividerItemDecoration(mainList.context, linearLayoutManager.orientation)
        mainList.addItemDecoration(divider)
    }

    // region MainListViewMvc

    override var visible: Boolean
        get() = mainList.visibility == View.VISIBLE
        set(value) {
            mainList.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var emptyVisible: Boolean
        get() = emptyView.visibility == View.VISIBLE
        set(value) {
            emptyView.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var simpleItems: List<String>
        get() = simpleListAdapter.items
        set(value) {
            mainList.adapter = simpleListAdapter
            simpleListAdapter.items = value
        }

    override var radioItems: List<String>
        get() = radioController.list
        set(value) {
            mainList.adapter = radioAdapter
            radioController.list = value
        }

    override var radioSelectedText: String?
        get() = radioController.selectedText
        set(value) {
            radioController.selectedText = value
        }

    override var checkBoxItems: List<String>
        get() = checkBoxListController.list
        set(value) {
            mainList.adapter = checkBoxListAdapter
            checkBoxListController.list = value
        }

    override fun setAdapter(adapter: MainListViewMvc.Adapter) {
        visible = true
        mainList.adapter = when (adapter) {
            MainListViewMvc.Adapter.SIMPLE -> simpleListAdapter
            MainListViewMvc.Adapter.PROJECT -> projectGroupAdapter
            MainListViewMvc.Adapter.EQUIPMENT -> equipmentSelectAdapter
            MainListViewMvc.Adapter.SUB_FLOWS -> subFlowListAdapter
            MainListViewMvc.Adapter.RADIO -> radioAdapter
            MainListViewMvc.Adapter.NOTE_ENTRY -> noteListEntryAdapter
            MainListViewMvc.Adapter.CHECK_BOX -> checkBoxListAdapter
        }
        when (adapter) {
            MainListViewMvc.Adapter.PROJECT -> projectGroupController.onProjectDataChanged()
            MainListViewMvc.Adapter.EQUIPMENT -> equipmentSelectController.onEquipmentDataChanged()
            MainListViewMvc.Adapter.NOTE_ENTRY -> noteListEntryController.onNoteDataChanged()
            MainListViewMvc.Adapter.SUB_FLOWS -> subFlowListController.onDataChanged()
        }
    }

    override val numNotes: Int
        get() = noteListEntryController.numNotes

    override val notes: List<DataNote>
        get() = noteListEntryController.notes

    override fun setSimpleNoneSelected() {
        simpleListAdapter.setNoneSelected()
    }

    override fun setSimpleSelected(value: String): Int {
        return simpleListAdapter.setSelected(value)
    }

    override fun scrollToPosition(position: Int) {
        mainList.scrollToPosition(position)
    }

    // endregion MainListViewMvc

    // region SimpleListController.Listener

    override var simpleSelectedPostion: Int
        get() = simpleListAdapter.selectedPos
        set(value) {
            simpleListAdapter.selectedPos = value
        }

    override fun onSimpleItemClicked(position: Int, text: String) {
        listeners.forEach { it.onSimpleItemClicked(position, text) }
    }

    // endregion SimpleListController.Listener

    // region ProjectGroupController.Listener

    override fun onProjectRefreshNeeded() {
        projectGroupAdapter.notifyDataSetChanged()
    }

    override fun onProjectGroupSelected(projectGroup: DataProjectAddressCombo) {
        listeners.forEach { it.onProjectGroupSelected(projectGroup) }
    }

    // endregion ProjectGroupController.Listener

    // region EquipmentSelectController.Listener

    override fun onEquipmentDataChanged(items: List<DataEquipment>) {
        equipmentSelectAdapter.items = items
    }

    // endregion EquipmentSelectController.Listener

    // region RadioListController.Listener

    override fun onRadioItemSelected(text: String) {
        listeners.forEach { it.onRadioItemSelected(text) }
    }

    override fun onRadioRefreshNeeded() {
        radioAdapter.notifyDataSetChanged()
    }

    // endregion RadioListController.Listener

    // region CheckBoxListController.Listener

    override fun onCheckBoxRefreshNeeded() {
        checkBoxListAdapter.notifyDataSetChanged()
    }

    override fun onCheckBoxItemChanged(position: Int, item: String, isChecked: Boolean) {
        listeners.forEach { it.onCheckBoxItemChanged(position, item, isChecked) }
    }

    override fun isChecked(position: Int): Boolean {
        for (listener in listeners) {
            if (listener.isCheckBoxItemSelected(position)) {
                return true
            }
        }
        return false
    }

    // endregion CheckBoxListController.Listener

    // region NoteListEntryController.Listener

    override fun onEntryHintChanged(entryHint: EntryHint) {
        listeners.forEach { it.onEntryHintChanged(entryHint) }
    }

    override fun onNotesChanged(items: List<DataNote>) {
        noteListEntryAdapter.items = items
    }

    override fun onNoteChanged(note: DataNote) {
        listeners.forEach { it.onNoteChanged(note) }
    }

    // endregion NoteListEntryController.Listener

    // region SubFlowsListController.Listener

    override fun onSubFlowSelected(position: Int) {
        subFlowListAdapter.notifyDataSetChanged()
        listeners.forEach { it.onSubFlowSelected(position) }
    }

    // endregion SubFlowsListController.Listener

}