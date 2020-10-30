package com.cartlc.tracker.fresh.ui.mainlist

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.fresh.model.core.data.DataFlowElement
import com.cartlc.tracker.fresh.model.core.data.DataFlowElement.Type
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.model.core.table.TableFlowElement
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.common.observable.BaseObservableImpl
import com.cartlc.tracker.fresh.model.flow.Flow
import com.cartlc.tracker.fresh.model.flow.FlowUseCase
import com.cartlc.tracker.fresh.model.flow.Stage
import com.cartlc.tracker.fresh.model.misc.EntryHint
import com.cartlc.tracker.fresh.model.misc.TruckStatus

class MainListController(
        boundAct: BoundAct,
        private val viewMvc: MainListViewMvc
) : BaseObservableImpl<MainListUseCase.Listener>(),
        LifecycleObserver,
        MainListUseCase,
        MainListViewMvc.Listener,
        FlowUseCase.Listener {

    private val ctx = boundAct.act
    private val repo = boundAct.repo
    private val prefHelper = repo.prefHelper
    private val status: TruckStatus?
        get() = prefHelper.status
    private val checkBoxItemEnabled = mutableListOf<Boolean>()
    private val checkBoxItems = mutableListOf<DataFlowElement>()

    init {
        boundAct.bindObserver(this)
        repo.flowUseCase.registerListener(this)
    }

    // region lifecycle

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        viewMvc.registerListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        viewMvc.unregisterListener(this)
        repo.flowUseCase.unregisterListener(this)
    }

    // endregion lifecycle

    // region MainListUseCase

    override var visible: Boolean
        get() = viewMvc.visible
        set(value) {
            viewMvc.visible = value
        }

    override var key: String? = null

    override var keyValue: String?
        get() = key?.let { prefHelper.getKeyValue(it) }
        set(value) {
            key?.let { key_ ->
                prefHelper.setKeyValue(key_, value)
                listeners.forEach { it.onKeyValueChanged(key_, value) }
            }
        }

    override var simpleItems: List<String>
        get() = viewMvc.simpleItems
        set(value) {
            viewMvc.setAdapter(MainListViewMvc.Adapter.SIMPLE)
            viewMvc.simpleItems = value
            setSimpleSelected()
        }

    override val areNotesComplete: Boolean
        get() = repo.areNotesComplete(viewMvc.notes)

    override val notes: List<DataNote>
        get() = viewMvc.notes

    override val isConfirmReady: Boolean
        get() = isAllChecked

    // endregion MainListUseCase

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
        viewMvc.emptyVisible = false
        viewMvc.visible = false
    }

    override fun onStageChanged(flow: Flow) {
        when (flow.stage) {
            Stage.CURRENT_PROJECT -> {
                key = null
                viewMvc.setAdapter(MainListViewMvc.Adapter.PROJECT)
            }
            Stage.EQUIPMENT -> {
                viewMvc.setAdapter(MainListViewMvc.Adapter.EQUIPMENT)
            }
            Stage.SUB_FLOWS -> {
                viewMvc.setAdapter(MainListViewMvc.Adapter.SUB_FLOWS)
            }
            is Stage.CUSTOM_FLOW -> {
                repo.currentFlowElement?.let { element ->
                    when (element.type) {
                        Type.CONFIRM_NEW,
                        Type.CONFIRM -> {
                            checkBoxItems.clear()
                            checkBoxItems.addAll(repo.db.tableFlowElement.queryConfirmBatch(element.flowId, element.id))
                            viewMvc.checkBoxItems = convert(checkBoxItems)
                            buildCheckBoxMemory()
                            notifyConfirmListeners()
                        }
                        else -> {
                            if (!element.hasImages && repo.db.tableFlowElementNote.hasNotes(element.id)) {
                                viewMvc.setAdapter(MainListViewMvc.Adapter.NOTE_ENTRY)
                                viewMvc.visible = true
                            } else {
                                viewMvc.visible = false
                                viewMvc.emptyVisible = false
                            }
                        }
                    }
                }
            }
            Stage.STATUS -> {
                viewMvc.radioItems = listOf(
                        TruckStatus.COMPLETE.getString(ctx),
                        TruckStatus.PARTIAL.getString(ctx),
                        TruckStatus.NEEDS_REPAIR.getString(ctx)
                )
                viewMvc.radioSelectedText = status?.getStringNull(ctx)
            }
        }
    }

    private fun convert(items: List<DataFlowElement>): List<String> {
        return TableFlowElement.convertToStrings(items)
    }

    private fun buildCheckBoxMemory() {
        checkBoxItemEnabled.clear()
        for (item in checkBoxItems) {
            checkBoxItemEnabled.add(prefHelper.getConfirmValue(item.id))
        }
    }

    // endregion FlowUseCase.Listener

    // region MainListViewMvc.Listener

    override fun onEntryHintChanged(entryHint: EntryHint) {
        listeners.forEach { it.onEntryHintChanged(entryHint) }
    }

    override fun onNoteChanged(note: DataNote) {
        listeners.forEach { it.onNoteChanged(note, areNotesComplete) }
    }

    override fun onSimpleItemClicked(position: Int, value: String) {
        keyValue = value
    }

    override fun onProjectGroupSelected(projectGroup: DataProjectAddressCombo) {
        listeners.forEach { it.onProjectGroupSelected(projectGroup) }
    }

    override fun onRadioItemSelected(text: String) {
        prefHelper.status = TruckStatus.from(ctx, text)
    }

    override fun onCheckBoxItemChanged(position: Int, prompt: String, isChecked: Boolean) {
        checkBoxItemEnabled[position] = isChecked
        lookupFlowElementId(prompt)?.let { id ->
            prefHelper.setConfirmValue(id, isChecked)
        }
        notifyConfirmListeners()
    }

    private fun lookupFlowElementId(prompt: String): Long? {
        for (item in checkBoxItems) {
            if (item.prompt == prompt) {
                return item.id
            }
        }
        return null
    }

    private fun notifyConfirmListeners() {
        listeners.forEach { it.onConfirmItemChecked(isAllChecked) }
    }

    private val isAllChecked: Boolean
        get() {
            for (i in 0 until checkBoxItemEnabled.size) {
                if (!checkBoxItemEnabled[i]) {
                    return false
                }
            }
            return true
        }

    override fun isCheckBoxItemSelected(position: Int): Boolean {
        return if (position < checkBoxItemEnabled.size) {
            checkBoxItemEnabled[position]
        } else {
            false
        }
    }

    override fun onSubFlowSelected(position: Int) {
        listeners.forEach { it.onSubFlowSelected() }
    }

    // endregion MainListViewMvc.Listener

    private fun setSimpleSelected() {
        keyValue?.let {
            val position = viewMvc.setSimpleSelected(it)
            if (position >= 0) {
                viewMvc.scrollToPosition(position)
            }
        } ?: run {
            viewMvc.setSimpleNoneSelected()
        }
    }

}
