package com.cartlc.tracker.fresh.ui.confirm

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.fresh.model.flow.CurrentProjectFlow
import com.cartlc.tracker.fresh.model.flow.Flow
import com.cartlc.tracker.fresh.model.flow.FlowUseCase
import com.cartlc.tracker.fresh.model.flow.Stage
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.ui.confirm.data.*
import java.io.File

class ConfirmFinalController(
        boundFrag: BoundFrag,
        private val viewMvc: ConfirmFinalViewMvc
) : ConfirmFinalUseCase,
        LifecycleObserver,
        FlowUseCase.Listener {

    private val ctx = boundFrag.act
    private val repo = boundFrag.repo
    private val componentRoot = boundFrag.componentRoot
    private val prefHelper = componentRoot.prefHelper
    private val messageHandler = componentRoot.messageHandler
    private val bitmapHelper = componentRoot.bitmapHelper
    private val db = repo.db
    private var curEntry: DataEntry? = null

    init {
        boundFrag.bindObserver(this)
    }

    // region lifecycle functions

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        repo.flowUseCase.registerListener(this)
        onStageChanged(repo.curFlowValue)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        repo.flowUseCase.unregisterListener(this)
        bitmapHelper.clearCache()
    }

    // endregion lifecycle functions

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
    }

    override fun onStageChanged(flow: Flow) {
        when (flow.stage) {
            Stage.CONFIRM -> {
                curEntry = prefHelper.saveEntry(false)
                curEntry?.let { entry -> fill(entry) }
            }
            else -> {
            }
        }
    }

    // endregion FlowUseCase.Listener

    /**
     * Fill up the UI elements with all the data acquired from the given DataEntry.
     */
    private fun fill(entry: DataEntry) {
        val items = mutableListOf<ConfirmDataType>()
        items.add(ConfirmDataType.BASICS(ConfirmDataBasics(
                entry.projectDashName,
                entry.addressBlock,
                entry.getStatus(ctx),
                partialInstallReason
        )))
        val notes = entry.notesWithValues
        if (notes.isNotEmpty()) {
            items.add(ConfirmDataType.NOTES(ConfirmDataNotes(notes)))
        }
        entry.equipmentNames?.let {
            items.add(ConfirmDataType.EQUIPMENT(ConfirmDataEquipment(it)))
        }
        entry.truck?.let { truck ->
            var title = messageHandler.getString(StringMessage.title_truck_number)
            db.tablePicture.query(truck.truckNumberPictureId.toLong())?.let { picture ->
                items.add(ConfirmDataType.PICTURES(ConfirmDataPicture(
                        title,
                        convert(picture.file),
                        listOf(NoteLabelValue(title, truck.truckNumberValue)))))
            }
            if (truck.truckHasDamage) {
                title = messageHandler.getString(StringMessage.title_truck_damage)
                db.tablePicture.query(truck.truckDamagePictureId.toLong())?.let { picture ->
                    items.add(ConfirmDataType.PICTURES(ConfirmDataPicture(
                            title,
                            convert(picture.file),
                            listOf(NoteLabelValue(title, truck.truckDamageValue)))))
                }
            }
        }
        items.addAll(convert(groupByStage(entry.pictures)))
        viewMvc.items = items
    }

    private val partialInstallReason: String?
        get() {
            return if (prefHelper.statusIsPartialInstall) {
                db.tableNote.notePartialInstall?.value
            } else null
        }

    override fun onConfirmOkay() {
        curEntry?.let { entry ->
            entry.isComplete = true
            repo.store(entry)
            curEntry = null
            prefHelper.clearCurProject()
            repo.curFlowValue = CurrentProjectFlow()
        }
    }

    private fun convert(file: File?): List<File> {
        return file?.let {
            listOf(it)
        } ?: emptyList()
    }

    private fun groupByStage(pictures: List<DataPicture>): List<ConfirmDataPicture> {
        if (pictures.isEmpty()) {
            return emptyList()
        }
        val sorted = pictures.toMutableList()
        sorted.sort()
        val list = mutableListOf<ConfirmDataPicture>()
        var currentLabel: String? = null
        var currentFlowElementId: Long = -100L
        var currentPictures = mutableListOf<File>()
        var currentNotes = listOf<NoteLabelValue>()
        for (picture in sorted) {
            if (picture.stage !is Stage.CUSTOM_FLOW) {
                continue
            }
            val flowElementId = picture.stage.flowElementId
            if (flowElementId != currentFlowElementId) {
                currentLabel?.let { label ->
                    list.add(ConfirmDataPicture(
                            label,
                            currentPictures,
                            currentNotes
                    ))
                }
                currentFlowElementId = flowElementId
                currentLabel = null
                currentPictures = mutableListOf()
                currentNotes = mutableListOf()
            }
            val file = picture.file ?: continue
            currentPictures.add(file)
            val flowElement = db.tableFlowElement.query(flowElementId) ?: continue
            if (currentLabel == null) {
                currentLabel = flowElement.prompt
            }
            currentNotes = getNotes(flowElementId)
        }
        list.add(ConfirmDataPicture(
                currentLabel ?: "Unknown",
                currentPictures,
                currentNotes
        ))
        return list
    }

    private fun convert(items: List<ConfirmDataPicture>): List<ConfirmDataType> {
        val list = mutableListOf<ConfirmDataType>()
        for (item in items) {
            list.add(ConfirmDataType.PICTURES(item))
        }
        return list
    }

    private fun getNotes(elementId: Long): List<NoteLabelValue> {
        val notes = mutableListOf<NoteLabelValue>()
        val elements = repo.db.tableFlowElementNote.query(elementId)
        for (element in elements) {
            repo.db.tableNote.query(element.noteId)?.let { note ->
                notes.add(NoteLabelValue(note.name, note.value))
            }
        }
        return notes
    }

}