/*
 * Copyright 2020, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.model

import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.fresh.model.core.data.*
import com.cartlc.tracker.fresh.model.event.Action
import com.cartlc.tracker.fresh.model.msg.ErrorMessage
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.flow.*

// TODO: was open class for testing
class CarRepository(
        val db: DatabaseTable,
        val prefHelper: PrefHelper,
        val flowUseCase: FlowUseCase
) {

    companion object {
        private const val REASONABLE = 16
    }

    val isDevelopment: Boolean
        get() = prefHelper.isDevelopment

    var companyEditing: String? = null
    var editProject: Boolean = false

    // region Current Flow

    var curFlowValue: Flow
        get() = flowUseCase.curFlow
        set(value) {
            flowUseCase.curFlow = value
        }

    fun onPreviousFlow() {
        flowUseCase.previousFlowValue?.let {
            if (it.stage == Stage.LOGIN) {
                computeCurStageLight()
            } else {
                curFlowValue = it
            }
        } ?: run {
            computeCurStageLight()
        }
    }

    // endregion Current Flow

    // region ErrorEvent

    val error: MutableLiveData<ErrorMessage> by lazy {
        MutableLiveData<ErrorMessage>()
    }

    var errorValue: ErrorMessage
        get() = error.value!!
        set(value) {
            error.value = value
        }

    // endregion ErrorEvent

    // region Action

    val actionUseCase: ActionUseCase = ActionUseCaseImpl()

    fun dispatchActionEvent(action: Action) {
        actionUseCase.dispatchActionEvent(action)
    }

    // endregion Action

    val hasVehicleNameList: Boolean
        get() {
            return db.tableVehicleName.vehicleNames.isNotEmpty()
        }

    fun checkEntryErrors(): DataEntry? {
        val entries = db.tableEntry.query()
        for (entry in entries) {
            if (entry.hasError) {
                return entry
            }
        }
        return null
    }

    fun clearUploaded() {
        db.clearUploaded()
        prefHelper.clearUploaded()
    }

    /**
     * Add a new saved entry to the database.
     */
    fun store(entry: DataEntry): Boolean {
        return db.tableEntry.updateOrInsert(entry)
    }

    fun onCompanyChanged() {
        curFlowValue = CompanyFlow()
        prefHelper.state = null
        prefHelper.city = null
        prefHelper.company = null
        prefHelper.street = null
        prefHelper.zipCode = null
    }

    fun setIncomplete(entry: DataEntry) {
        currentFlowElement?.let { element ->
            entry.isComplete = db.tableFlowElement.queryFlowId(element.id)?.let { flowId -> isComplete(entry, flowId) }
                    ?: false
            entry.flowProgress = (db.tableFlowElement.progressInSubFlow(element.id)?.first
                    ?: 0).toShort()
        } ?: run {
            entry.isComplete = false
        }
    }

    // endregion ActionEvent

    // region COMPUTE

    fun computeCurStageLight() {
        when {
            prefHelper.firstTechCode.isNullOrBlank() -> {
                curFlowValue = LoginFlow()
            }
            db.tableProjects.query().isEmpty() -> {
                when {
                    prefHelper.projectRootName.isNullOrBlank() -> curFlowValue = RootProjectFlow()
                    prefHelper.company.isNullOrBlank() -> {
                        curFlowValue = if (db.tableAddress.count > 0) {
                            CompanyFlow()
                        } else {
                            RootProjectFlow()
                        }
                    }
                    prefHelper.state.isNullOrBlank() -> curFlowValue = StateFlow()
                    prefHelper.city.isNullOrBlank() -> curFlowValue = CityFlow()
                    prefHelper.street.isNullOrBlank() -> curFlowValue = StreetFlow()
                    else -> {
                        curFlowValue = CurrentProjectFlow()
                    }
                }
            }
            else -> {
                curFlowValue = CurrentProjectFlow()
            }
        }
    }

    fun computeCurStageDeep() {
        if (prefHelper.firstTechCode.isNullOrBlank()) {
            curFlowValue = LoginFlow()
        } else if (db.tableProjects.query().isEmpty()) {
            when {
                prefHelper.projectRootName.isNullOrBlank() -> curFlowValue = RootProjectFlow()
                prefHelper.company.isNullOrBlank() -> {
                    curFlowValue = if (db.tableAddress.count > 0) {
                        CompanyFlow()
                    } else {
                        RootProjectFlow()
                    }
                }
                prefHelper.state.isNullOrBlank() -> curFlowValue = StateFlow()
                prefHelper.city.isNullOrBlank() -> curFlowValue = CityFlow()
                prefHelper.street.isNullOrBlank() -> curFlowValue = StreetFlow()
                else -> {
                    curFlowValue = CurrentProjectFlow()
                }
            }
        } else {
            val hasSubProject = !prefHelper.projectSubName.isNullOrBlank()
            if (hasSubProject) {
                val hasTruckNumberValue = !prefHelper.truckNumberValue.isNullOrEmpty()
                val hasTruckNumberPicture = db.tablePicture.countPictures(prefHelper.currentPictureCollectionId, Stage.TRUCK_NUMBER_PICTURE) > 0
                val hasTruckDamagePicture = db.tablePicture.countPictures(prefHelper.currentPictureCollectionId, Stage.TRUCK_DAMAGE_PICTURE) > 0
                val hasTruckDamage = prefHelper.truckHasDamage ?: false
                val hasTruckDamageValue = prefHelper.truckHasDamage != null
                val hasEquipment = prefHelper.projectId?.let { projectId ->
                    db.tableCollectionEquipmentProject.hasEquipment(projectId)
                } ?: false
                val hasEquipmentChecked = prefHelper.currentEditEntry?.equipmentCollection?.hasChecked
                        ?: false
                curFlowValue = if (!hasTruckNumberValue || !hasTruckNumberPicture) {
                    TruckNumberPictureFlow()
                } else if (!hasTruckDamageValue || (hasTruckDamageValue && hasTruckDamage && !hasTruckDamagePicture)) {
                    TruckDamagePictureFlow()
                } else if (hasEquipment && !hasEquipmentChecked) {
                    EquipmentFlow()
                } else {
                    CustomFlow(computeFirstIncompleteFlowElementId)
                }
            } else {
                curFlowValue = CurrentProjectFlow()
            }
        }
    }

    fun areNotesComplete(items: List<DataNote>): Boolean {
        for (note in items) {
            if (note.numDigits > 0) {
                note.value?.let { value ->
                    if (value.length < note.numDigits.toInt()) {
                        if (note.numDigits < REASONABLE) {
                            return false
                        } else if (note.value.isNullOrEmpty()) {
                            return false
                        }
                    }
                } ?: return false
            } else if (note.value.isNullOrEmpty()) {
                return false
            }
        }
        return true
    }

    // endregion COMPUTE

    // region flow element support

    val currentFlowElement: DataFlowElement?
        get() = currentFlowElementId?.let { elementId -> db.tableFlowElement.query(elementId) }

    val currentFlowElementId: Long?
        get() {
            val stage = curFlowValue.stage
            return when (stage) {
                is Stage.CUSTOM_FLOW -> {
                    when {
                        stage.isFirstElement -> firstFlowElementId
                        stage.isLastElement -> lastFlowElementId
                        else -> stage.flowElementId
                    }
                }
                is Stage.SUB_FLOWS -> {
                    firstFlowElementId
                }
                else -> {
                    null
                }
            }
        }

    val isCurrentFlowEntryComplete: Boolean
        get() = currentFlowElement?.flowId?.let { flowId ->
            prefHelper.currentEditEntry?.let { entry -> isComplete(entry, flowId) }
        } ?: false

    private val firstFlowElementId: Long?
        get() {
            prefHelper.currentProjectGroup?.let { combo ->
                combo.project?.let { project ->
                    db.tableFlow.queryBySubProjectId(project.id.toInt())?.let { flow ->
                        return subFlowSelectedElementId?.let { selectedFlowElementId ->
                            db.tableFlowElement.firstOfSubFlow(flow.id, selectedFlowElementId)?.let { it }
                        } ?: run {
                            db.tableFlowElement.first(flow.id)?.let { it }
                        }
                    }
                }
            }
            return null
        }

    private val subFlowSelectedElementId: Long?
        get() = prefHelper.subFlowSelectedElementId

    private val lastFlowElementId: Long?
        get() {
            prefHelper.currentProjectGroup?.let { combo ->
                combo.project?.let { project ->
                    db.tableFlow.queryBySubProjectId(project.id.toInt())?.let { flow ->
                        return db.tableFlowElement.last(flow.id)
                    }
                }
            }
            return null
        }

    val curFlowValueStage: Stage
        get() {
            val flow = curFlowValue
            if (flow.stage is Stage.CUSTOM_FLOW) {
                if (flow.stage.isFirstElement) {
                    firstFlowElementId?.let { firstId ->
                        return Stage.CUSTOM_FLOW(firstId)
                    }
                } else if (flow.stage.isLastElement) {
                    lastFlowElementId?.let { lastId ->
                        return Stage.CUSTOM_FLOW(lastId)
                    }
                }
            }
            return flow.stage
        }

    private val computeFirstIncompleteFlowElementId: Long
        get() {
            val currentEntry = prefHelper.currentEditEntry ?: return Stage.FIRST_ELEMENT
            var elementId = firstFlowElementId ?: Stage.FIRST_ELEMENT
            val lastProgressMade = currentEntry.flowProgress
            var progress = 0
            while (progress < lastProgressMade) {
                db.tableFlowElement.query(elementId)?.let { element ->
                    if (!isEntryComplete(currentEntry, element)) {
                        return element.id
                    }
                }
                elementId = db.tableFlowElement.next(elementId) ?: return elementId
                progress = db.tableFlowElement.progressInSubFlow(elementId)?.first
                        ?: return elementId
            }
            return elementId
        }

    private fun isEntryComplete(entry: DataEntry, element: DataFlowElement): Boolean {
        val stage = Stage.CUSTOM_FLOW(element.id)
        if (element.hasImages) {
            val currentNumPictures = db.tablePicture.countPictures(entry.pictureCollectionId, stage)
            if (currentNumPictures < element.numImages) {
                return false
            }
        }
        if (!areNotesComplete(entry, element)) {
            return false
        }
        if (element.isConfirmType) {
            val items = db.tableFlowElement.queryConfirmBatch(element.flowId, element.id)
            for (item in items) {
                if (!prefHelper.getConfirmValue(item.id)) {
                    return false
                }
            }
        }
        return true
    }

    fun areNotesComplete(element: DataFlowElement): Boolean {
        return areNotesComplete(prefHelper.currentEditEntry, element)
    }

    private fun areNotesComplete(entry: DataEntry?, element: DataFlowElement): Boolean {
        val hasNotes = db.tableFlowElementNote.hasNotes(element.id)
        return if (hasNotes) {
            when (element.type) {
                DataFlowElement.Type.NONE -> {
                    val notes = db.noteHelper.getNotesFromCurrentFlowElementId(element.id)
                    entry?.let {
                        val notesWithValues = entry.overlayNoteValues(notes)
                        if (!areNotesComplete(notesWithValues)) {
                            return false
                        }
                    } ?: run {
                        if (notes.isNotEmpty()) {
                            if (!areNotesComplete(notes)) {
                                return false
                            }
                        }
                    }
                }
                else -> {
                    val notes = db.noteHelper.getNotesOverlaidFrom(element.id, entry)
                    if (notes.isNotEmpty()) {
                        if (!areNotesComplete(notes)) {
                            return false
                        }
                    }
                }
            }
            true
        } else true
    }

    // endregion flow element support

    // region Flow Support

    fun isComplete(entry: DataEntry, flowId: Long): Boolean {
        val items = progressInSubFlows(entry, flowId)
        for (item in items) {
            if (item.completed < item.total) {
                return false
            }
        }
        return true
    }

    data class SubFlowInfo(
            val flowElementId: Long,
            val title: String,
            val completed: Int,
            val total: Int
    )

    fun progressInSubFlows(entry: DataEntry, flowId: Long): List<SubFlowInfo> {
        val items = mutableListOf<SubFlowInfo>()
        val subFlows = db.tableFlowElement.querySubFlows(flowId)
        for (subFlowList in subFlows) {
            val item = subFlowList[0]
            val title = item.prompt ?: ""
            val completed = countCompleted(entry, subFlowList)
            val total = countTotal(subFlowList)
            items.add(SubFlowInfo(item.id, title, completed, total))
        }
        return items
    }

    private fun countCompleted(entry: DataEntry, list: List<DataFlowElement>): Int {
        var count = 0
        var wasConfirm = false
        for (element in list) {
            if (element.type == DataFlowElement.Type.SUB_FLOW_DIVIDER) {
                continue
            }
            if (wasConfirm && element.type == DataFlowElement.Type.CONFIRM) {
                continue
            }
            if (element.type == DataFlowElement.Type.DIALOG || element.type == DataFlowElement.Type.TOAST) {
                if (element.numImages == 0.toShort() && !element.hasNotes(db)) {
                    continue // Ignore
                }
            }
            wasConfirm = element.type == DataFlowElement.Type.CONFIRM_NEW || element.type == DataFlowElement.Type.CONFIRM
            if (isEntryComplete(entry, element)) {
                count++
            }
        }
        return count
    }

    private fun countTotal(list: List<DataFlowElement>): Int {
        if (list.isEmpty()) {
            return 0
        }
        val element = list[0]
        return db.tableFlowElement.subFlowSize(element.flowId, element.id)
    }

    // endregion Flow Suppoer

}
