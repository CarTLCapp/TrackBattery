/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.ui.listentries

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.listentries.item.ListEntriesItemViewMvc
import com.cartlc.tracker.fresh.ui.main.MainController
import timber.log.Timber

class ListEntriesController(
        private val boundAct: BoundAct,
        private val viewMvc: ListEntriesViewMvc
) : LifecycleObserver,
        ListEntriesViewMvc.Listener {

    interface Listener {
        var titleString: String?
    }

    private val repo = boundAct.repo
    private val prefHelper: PrefHelper = repo.prefHelper
    private val db: DatabaseTable = repo.db
    private val messageHandler = boundAct.componentRoot.messageHandler
    private val screenNavigator = boundAct.screenNavigator

    var listener: Listener? = null

    private val titleString: String
        get() {
            val sbuf = StringBuilder()
            sbuf.append(prefHelper.projectDashName)
            sbuf.append(" - ")
            val count = items.size
            if (count == 1) {
                sbuf.append(messageHandler.getString(StringMessage.title_element))
            } else {
                sbuf.append(messageHandler.getString(StringMessage.title_elements(count)))
            }
            return sbuf.toString()
        }

    private var items: List<DataEntry> = emptyList()

    init {
        boundAct.bindObserver(this)
    }

    // region LifecycleObserver

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        viewMvc.registerListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        build()
        viewMvc.notifyDataSetChanged()
        setProjectDisplay()
        screenNavigator.setResult(Activity.RESULT_CANCELED)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        viewMvc.unregisterListener(this)
    }

    private fun setProjectDisplay() {
        val combo = prefHelper.currentProjectGroup
        if (combo == null) {
            viewMvc.projectName = ""
            viewMvc.projectAddress = ""
        } else {
            viewMvc.projectName = combo.projectDashName
            viewMvc.projectAddress = combo.addressLine
        }
    }

    // endregion LifecycleObserver

    // region ListEntriesViewMvc.Listener

    override fun onEditAddress() {
        prefHelper.setFromCurrentProjectId()
        screenNavigator.finish(MainController.RESULT_EDIT_PROJECT)
    }

    override fun onDelete() {
        val projectGroup = prefHelper.currentProjectGroup
        if (projectGroup != null) {
            db.tableProjectAddressCombo.remove(projectGroup.id)
        }
        screenNavigator.finish(MainController.RESULT_DELETE_PROJECT)
    }

    override val itemCount: Int
        get() = items.size

    override fun onBindViewHolder(itemViewMvc: ListEntriesItemViewMvc, position: Int) {
        val entry = items[position]
        itemViewMvc.truckValue = entry.getTruckLine(boundAct.act)
        itemViewMvc.status = entry.getStatus(boundAct.act)
        itemViewMvc.notesLine = entry.notesLine
        itemViewMvc.equipmentLine = entry.getEquipmentLine(boundAct.act)
        itemViewMvc.subProjectValue = entry.projectAddressCombo?.project?.subProject ?: ""
        if (entry.isComplete) {
            itemViewMvc.incompleteVisible = false
        } else {
            itemViewMvc.incompleteVisible = true
            itemViewMvc.incompleteText = messageHandler.getString(buildIncompleteText(entry))
        }
        itemViewMvc.bindOnEditListener {
            prefHelper.setFromEntry(entry)
            screenNavigator.finish(MainController.RESULT_EDIT_ENTRY)
        }
    }

    // endregion ListEntriesViewMvc.Listener

    private fun build() {
        items = prefHelper.currentProjectGroup?.entries ?: emptyList()
        viewMvc.deleteVisible = items.isEmpty()
        listener?.titleString = titleString
    }

    private fun buildIncompleteText(entry: DataEntry): StringMessage {
        entry.projectAddressCombo?.let { combo ->
            combo.project?.let { project ->
                db.tableFlow.queryBySubProjectId(project.id.toInt())?.let { flow ->
                    val completed = countCompleted(repo.progressInSubFlows(entry, flow.id))
                    val size = db.tableFlowElement.flowSize(flow.id)
                    return StringMessage.note_incomplete(completed, size)
                }
            }
        }
        return StringMessage.note_incomplete(entry.flowProgress.toInt())
    }

    private fun countCompleted(list: List<CarRepository.SubFlowInfo>): Int {
        var count = 0
        for (item in list) {
            if (item.completed >= item.total) {
                count++
            }
        }
        return count
    }
}