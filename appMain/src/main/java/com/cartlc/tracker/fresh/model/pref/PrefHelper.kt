/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.pref

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import com.cartlc.tracker.fresh.model.core.data.*
import com.cartlc.tracker.fresh.model.misc.TruckStatus
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.flow.Stage
import com.cartlc.tracker.fresh.model.misc.HashLongList
import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.fresh.ui.main.MainController

import java.io.File
import java.text.SimpleDateFormat

import timber.log.Timber
import java.util.*

/**
 * Created by dug on 4/17/17.
 */

class PrefHelper constructor(
        ctx: Context,
        private val db: DatabaseTable
) : PrefHelperBase(ctx) {

    companion object {
        private val TAG = PrefHelper::class.simpleName

        const val KEY_ROOT_PROJECT = "root_project"
        const val KEY_SUB_PROJECT = "sub_project"
        const val KEY_SUB_FLOW_ELEMENT_ID = "sub_flow_element_id"
        const val KEY_COMPANY = "company"
        const val KEY_STREET = "street"
        const val KEY_STATE = "state"
        const val KEY_CITY = "city"
        const val KEY_ZIPCODE = "zipcode"
        const val KEY_TRUCK = "truck" // Number & License
        const val KEY_STATUS = "status"

        private const val KEY_CURRENT_PROJECT_GROUP_ID = "current_project_group_id"
        private const val KEY_SAVED_PROJECT_GROUP_ID = "saved_project_group_id"
        private const val KEY_FIRST_TECH_CODE = "first_tech_code"
        private const val KEY_SECONDARY_TECH_CODE = "secondary_tech_code"
        private const val KEY_TRUCK_DAMAGE_EXISTS = "truck_damage_exists"
        private const val KEY_EDIT_ENTRY_ID = "edit_id"
        private const val KEY_NEXT_PICTURE_COLLECTION_ID = "next_picture_collection_id"
        private const val KEY_NEXT_EQUIPMENT_COLLECTION_ID = "next_equipment_collection_id"
        private const val KEY_NEXT_NOTE_COLLECTION_ID = "next_note_collection_id"
        private const val KEY_CURRENT_PICTURE_COLLECTION_ID = "picture_collection_id"
        private const val KEY_TECH_ID = "tech_id"
        private const val KEY_TECH_FIRST_NAME = "tech_first_name"
        private const val KEY_TECH_LAST_NAME = "tech_last_name"
        private const val KEY_SECONDARY_TECH_ID = "secondary_tech_id"
        private const val KEY_SECONDARY_TECH_FIRST_NAME = "secondary_tech_first_name"
        private const val KEY_SECONDARY_TECH_LAST_NAME = "secondary_tech_last_name"
        private const val KEY_IS_DEVELOPMENT = "is_development"
        private const val KEY_RELOAD_FROM_SERVER = "reload_from_server"
        private const val KEY_DO_ERROR_CHECK = "do_error_check"
        private const val KEY_AUTO_ROTATE_PICTURE = "auto_rotate_picture"
        private const val KEY_LAST_ACTIVITY_TIME = "last_activity_time"

        const val VERSION_PROJECT = "version_project"
        const val VERSION_COMPANY = "version_company"
        const val VERSION_EQUIPMENT = "version_equipment"
        const val VERSION_NOTE = "version_note"
        const val VERSION_TRUCK = "version_truck"
        const val VERSION_FLOW = "version_flow"
        const val VERSION_VEHICLE_NAMES = "version_vehicle_names"

        private const val PICTURE_DATE_FORMAT = "yy-MM-dd_HH:mm:ss"
        private const val VERSION_RESET = -1

        private const val CONFIRM_PROMPT_PREFIX = "PROMPT__"
    }

    val isLocalCompany: Boolean
        get() = db.tableAddress.isLocalCompanyOnly(company)

    var techID: Int
        get() = getInt(KEY_TECH_ID, 0)
        set(id) = setInt(KEY_TECH_ID, id)

    var techFirstName: String?
        get() = getString(KEY_TECH_FIRST_NAME, null)
        set(value) {
            setString(KEY_TECH_FIRST_NAME, value)
        }

    var techLastName: String?
        get() = getString(KEY_TECH_LAST_NAME, null)
        set(value) {
            setString(KEY_TECH_LAST_NAME, value)
        }

    val techName: String
        get() {
            return if (techFirstName != null && techLastName != null) {
                "$techFirstName $techLastName"
            } else ""
        }

    var secondaryTechID: Int
        get() = getInt(KEY_SECONDARY_TECH_ID, 0)
        set(id) = setInt(KEY_SECONDARY_TECH_ID, id)

    var secondaryTechFirstName: String?
        get() = getString(KEY_SECONDARY_TECH_FIRST_NAME, null)
        set(value) {
            setString(KEY_SECONDARY_TECH_FIRST_NAME, value)
        }

    var secondaryTechLastName: String?
        get() = getString(KEY_SECONDARY_TECH_LAST_NAME, null)
        set(value) {
            setString(KEY_SECONDARY_TECH_LAST_NAME, value)
        }

    val secondaryTechName: String
        get() {
            return if (secondaryTechFirstName != null && secondaryTechLastName != null) {
                "$secondaryTechFirstName $secondaryTechLastName"
            } else ""
        }

    var street: String?
        get() = getString(KEY_STREET, null)
        set(value) = setString(KEY_STREET, value)

    var state: String?
        get() = getString(KEY_STATE, null)
        set(value) = setString(KEY_STATE, value)

    var company: String?
        get() = getString(KEY_COMPANY, null)
        set(value) = setString(KEY_COMPANY, value)

    var city: String?
        get() = getString(KEY_CITY, null)
        set(value) = setString(KEY_CITY, value)

    var zipCode: String?
        get() = getString(KEY_ZIPCODE, null)
        set(value) = setString(KEY_ZIPCODE, value)

    val projectDashName: String
        get() {
            val rootName = projectRootName ?: ""
            val subName = projectSubName ?: return rootName
            if (subName.isEmpty()) {
                return rootName
            }
            return "$rootName - $subName"
        }

    var projectRootName: String?
        get() = getString(KEY_ROOT_PROJECT, null)
        set(value) = setString(KEY_ROOT_PROJECT, value)

    var projectSubName: String?
        get() = getString(KEY_SUB_PROJECT, null)
        set(value) = setString(KEY_SUB_PROJECT, value)

    val projectId: Long?
        get() {
            val rootName = projectRootName
            val subName = projectSubName
            return if (rootName != null && subName != null) {
                val id = db.tableProjects.queryProjectId(rootName, subName)
                if (id >= 0) id else null
            } else null
        }

    var subFlowSelectedElementId: Long
        get() = getLong(KEY_SUB_FLOW_ELEMENT_ID, 0L)
        set(value) = setLong(KEY_SUB_FLOW_ELEMENT_ID, value)

    val subFlowSelectedElementName: String?
        get() {
            return subFlowSelectedElementId.let { id ->
                if (id != 0L) {
                    db.tableFlowElement.query(id)?.prompt
                } else {
                    null
                }
            }
        }

    private var savedProjectGroupId: Long
        get() = getLong(KEY_SAVED_PROJECT_GROUP_ID, -1L)
        set(id) = setLong(KEY_SAVED_PROJECT_GROUP_ID, id)

    var lastActivityTime: Long
        get() = getLong(KEY_LAST_ACTIVITY_TIME, 0L)
        private set(value) = setLong(KEY_LAST_ACTIVITY_TIME, value)

    var currentPictureCollectionId: Long
        get() = getLong(KEY_CURRENT_PICTURE_COLLECTION_ID, 0)
        set(id) = setLong(KEY_CURRENT_PICTURE_COLLECTION_ID, id)

    var firstTechCode: String?
        get() = getString(KEY_FIRST_TECH_CODE, null)
        set(name) = setString(KEY_FIRST_TECH_CODE, name)

    var secondaryTechCode: String?
        get() = getString(KEY_SECONDARY_TECH_CODE, null)
        set(name) = setString(KEY_SECONDARY_TECH_CODE, name)

    var versionProject: Int
        get() = getInt(VERSION_PROJECT, VERSION_RESET)
        set(value) = setInt(VERSION_PROJECT, value)

    var versionEquipment: Int
        get() = getInt(VERSION_EQUIPMENT, VERSION_RESET)
        set(value) = setInt(VERSION_EQUIPMENT, value)

    var versionNote: Int
        get() = getInt(VERSION_NOTE, VERSION_RESET)
        set(value) = setInt(VERSION_NOTE, value)

    var versionCompany: Int
        get() = getInt(VERSION_COMPANY, VERSION_RESET)
        set(value) = setInt(VERSION_COMPANY, value)

    var versionTruck: Int
        get() = getInt(VERSION_TRUCK, VERSION_RESET)
        set(value) = setInt(VERSION_TRUCK, value)

    var versionFlow: Int
        get() = getInt(VERSION_FLOW, VERSION_RESET)
        set(value) = setInt(VERSION_FLOW, value)

    var versionVehicleNames: Int
        get() = getInt(VERSION_VEHICLE_NAMES, VERSION_RESET)
        set(value) = setInt(VERSION_VEHICLE_NAMES, value)

    var status: TruckStatus?
        get() = TruckStatus.from(getInt(KEY_STATUS, TruckStatus.UNKNOWN.ordinal))
        set(status) = if (status == null) {
            setInt(KEY_STATUS, TruckStatus.UNKNOWN.ordinal)
        } else {
            setInt(KEY_STATUS, status.ordinal)
        }

    var currentEditEntryId: Long
        get() = getLong(KEY_EDIT_ENTRY_ID, 0)
        set(id) = setLong(KEY_EDIT_ENTRY_ID, id)

    val currentEditEntry: DataEntry?
        get() = db.tableEntry.query(currentEditEntryId)

    val isCurrentEditEntryComplete: Boolean
        get() {
            return currentEditEntry?.isComplete ?: false
        }

    var doErrorCheck: Boolean
        get() = getInt(KEY_DO_ERROR_CHECK, 1) != 0
        set(flag) = setInt(KEY_DO_ERROR_CHECK, if (flag) 1 else 0)

    val isDevelopment: Boolean
        get() = getInt(KEY_IS_DEVELOPMENT, if (TBApplication.IsDevelopmentServer()) 1 else 0) != 0

    var currentProjectGroupId: Long
        get() = getLong(KEY_CURRENT_PROJECT_GROUP_ID, -1L)
        set(id) = setLong(KEY_CURRENT_PROJECT_GROUP_ID, id)

    var currentProjectGroup: DataProjectAddressCombo?
        get() = db.tableProjectAddressCombo.query(currentProjectGroupId)
        set(group) {
            group?.let {
                currentProjectGroupId = group.id
                val projectName = group.projectName
                projectName?.let {
                    projectRootName = projectName.first
                    projectSubName = projectName.second
                }
                setAddress(group.address)
            }
            onCurrentProjecGroupChanged.invoke(group)
        }

    // TODO: This look likes something that can be improved:
    var onCurrentProjecGroupChanged: (group: DataProjectAddressCombo?) -> Unit = {}

    // Note: ID zero has a special meaning, it means that the set is pending.
    private val nextPictureCollectionID: Long
        get() = getLong(KEY_NEXT_PICTURE_COLLECTION_ID, 1L)

    private val nextEquipmentCollectionID: Long
        get() = getLong(KEY_NEXT_EQUIPMENT_COLLECTION_ID, 0L)

    private val nextNoteCollectionID: Long
        get() = getLong(KEY_NEXT_NOTE_COLLECTION_ID, 1L)

    val address: String
        get() {
            val sbuf = StringBuilder()
            val company = company
            if (company != null) {
                sbuf.append(company)
            }
            val street = street
            if (street != null) {
                if (sbuf.isNotEmpty()) {
                    sbuf.append("\n")
                }
                sbuf.append(street)
            }
            val city = city
            if (city != null) {
                if (sbuf.isNotEmpty()) {
                    sbuf.append("\n")
                }
                sbuf.append(city)
            }
            val state = state
            if (state != null) {
                if (sbuf.isNotEmpty()) {
                    sbuf.append(", ")
                }
                sbuf.append(state)
            }
            val zip = zipCode
            if (zip != null) {
                if (sbuf.isNotEmpty()) {
                    sbuf.append(" ")
                }
                sbuf.append(zip)
            }
            return sbuf.toString()
        }

    // region truck support

    var truckNumberValue: String?
        get() = db.tableNote.noteTruckNumber?.value
        set(value) {
            db.tableNote.noteTruckNumber?.let { truck ->
                truck.value = value
                db.tableNote.updateValue(truck)
            }
        }

    private var truckDamageValue: String?
        get() = db.tableNote.noteTruckDamage?.value
        set(value) {
            db.tableNote.noteTruckDamage?.let { truck ->
                truck.value = value
                db.tableNote.updateValue(truck)
            }
        }

    var truckHasDamage: Boolean?
        get() {
            return when (getInt(KEY_TRUCK_DAMAGE_EXISTS, 2)) {
                0 -> false
                1 -> true
                else -> null
            }
        }
        set(value) {
            value?.let {
                setInt(KEY_TRUCK_DAMAGE_EXISTS, if (value) 1 else 0)
            } ?: setInt(KEY_TRUCK_DAMAGE_EXISTS, 2)
        }

    private var truckNumberPictureId: Int
        get() {
            val items = db.tablePicture.removeFileDoesNotExist(
                    db.tablePicture.query(currentPictureCollectionId, Stage.TRUCK_NUMBER_PICTURE)
            )
            if (items.isNotEmpty()) {
                return items[0].id.toInt()
            }
            return 0
        }
        set(value) {
            setTruckValue(Stage.TRUCK_NUMBER_PICTURE, value.toLong())
        }

    private var truckDamagePictureId: Int
        get() {
            val items = db.tablePicture.removeFileDoesNotExist(
                    db.tablePicture.query(currentPictureCollectionId, Stage.TRUCK_DAMAGE_PICTURE)
            )
            if (items.isNotEmpty()) {
                return items[0].id.toInt()
            }
            return 0
        }
        set(value) {
            setTruckValue(Stage.TRUCK_DAMAGE_PICTURE, value.toLong())
        }

    val statusIsPartialInstall: Boolean
        get() = status == TruckStatus.PARTIAL

    private fun setTruckValue(stage: Stage, value: Long) {
        db.tablePicture.query(value)?.let { from ->
            if (from.collectionId != currentPictureCollectionId || from.stage != stage) {
                queryFirst(stage)?.let { existing ->
                    val to = DataPicture(existing.id,
                            from.unscaledFilename,
                            currentPictureCollectionId,
                            stage)
                    db.tablePicture.update(to)
                } ?: run {
                    db.tablePicture.add(from.unscaledFile, currentPictureCollectionId, stage)
                }
            } /* else already existing just the way we need it */
        } ?: run {
            db.tablePicture.remove(currentPictureCollectionId, stage)
        }
    }

    private fun queryFirst(stage: Stage): DataPicture? {
        val list = db.tablePicture.query(currentPictureCollectionId, stage)
        if (list.isEmpty()) {
            return null
        }
        return list[0]
    }

    // endregion truck support

    val numPicturesTaken: Int
        get() = db.tablePicture.countPictures(currentPictureCollectionId, null)

    val numEquipPossible: Int
        get() {
            val collection = db.tableCollectionEquipmentProject.queryForProject(currentProjectGroup!!.projectNameId)
            return collection.equipment.size
        }

    var autoRotatePicture: Int
        get() = getInt(KEY_AUTO_ROTATE_PICTURE, 0)
        set(value) { setInt(KEY_AUTO_ROTATE_PICTURE, value) }

    val hasCode: Boolean
        get() = !TextUtils.isEmpty(firstTechCode)

    val hasSecondary: Boolean
        get() = !TextUtils.isEmpty(secondaryTechCode)

    fun getKeyValue(key: String): String? {
        return getString(key, null)
    }

    fun setKeyValue(key: String, value: String?) {
        setString(key, value)
    }

    // region CONFIRM

    fun getConfirmValue(id: Long): Boolean {
        return getInt(CONFIRM_PROMPT_PREFIX + id.toString(), 0) != 0
    }

    fun setConfirmValue(id: Long, value: Boolean) {
        setInt(CONFIRM_PROMPT_PREFIX + id.toString(), if (value) 1 else 0)
    }

    private fun clearConfirmValues() {
        val editor = prefs.edit()
        val map = prefs.all
        for (key in map.keys) {
            if (key.startsWith(CONFIRM_PROMPT_PREFIX)) {
                editor.remove(key)
            }
        }
        editor.apply()
    }

    // endregion CONFIRM

    fun activityDetect() {
        lastActivityTime = System.currentTimeMillis()
    }

    fun reloadFromServer() {
        clearUploaded()
    }

    fun detectOneTimeReloadFromServerCheck(): Boolean {
        val value = getInt(KEY_RELOAD_FROM_SERVER, 0)
        if (value < 2) {
            setInt(KEY_RELOAD_FROM_SERVER, 2)
            return true
        }
        return false
    }

    fun setFromCurrentProjectId(): Boolean {
        val projectGroup = currentProjectGroup
        if (projectGroup != null) {
            val name = projectGroup.projectName
            name?.let {
                projectRootName = it.first
                projectSubName = it.second
            }
            val address = projectGroup.address
            if (address != null) {
                setAddress(address)
                return true
            }
        }
        return false
    }

    fun clearCurProject() {
        clearLastEntry()
        state = null
        city = null
        company = null
        street = null
        zipCode = null
        projectRootName = null
        projectSubName = null
        savedProjectGroupId = currentProjectGroupId
        currentProjectGroupId = -1L
        db.tableEquipment.clearChecked()
    }

    fun clearCurProjectIfMatching(rootName: String?, subName: String?) {
        if (projectRootName == rootName && projectSubName == subName) {
            clearCurProject()
        }
    }

    fun clearLastEntry() {
        truckNumberValue = null
        truckDamageValue = null
        truckHasDamage = null
        setKeyValue(KEY_TRUCK, null)
        status = null
        currentEditEntryId = 0
        currentPictureCollectionId = 0
        db.tablePicture.clearPendingPictures()
        db.tableNote.clearValues()
        db.tableEquipment.clearChecked()
        clearConfirmValues()
    }

    fun saveProjectAndAddressCombo(modifyCurrent: Boolean, needsValidServerId: Boolean = false): Boolean {
        val rootName = projectRootName ?: return false
        val subName = projectSubName ?: ""
        val company = company
        val state = state
        val street = street
        val city = city
        if (company.isNullOrBlank() || state.isNullOrBlank() || street.isNullOrBlank() || city.isNullOrBlank()) {
            setFromCurrentProjectId()
            if (company.isNullOrBlank() || state.isNullOrBlank() || street.isNullOrBlank() || city.isNullOrBlank()) {
                return false  // Okay to have nothing selected
            }
        }
        val zipCode = zipCode
        var addressId: Long
        addressId = db.tableAddress.queryAddressId(company, street, city, state, zipCode)
        if (addressId < 0) {
            val address = DataAddress(company, street, city, state, zipCode)
            address.isLocal = true
            addressId = db.tableAddress.add(address)
            if (addressId < 0) {
                error("saveProjectAndAddressCombo(): could not find address: $address")
                clearCurProject()
                return false
            }
        }
        val project = db.tableProjects.queryByName(rootName, subName)
        val projectNameId: Long
        if (project == null) {
            if (subName.isEmpty()) {
                projectNameId = db.tableProjects.add(rootName)
            } else {
                error("saveProjectAndAddressCombo(): could not find project: $rootName - $subName")
                clearCurProject()
                return false
            }
        } else {
            if (project.disabled) {
                project.disabled = false
                db.tableProjects.update(project)
            }
            projectNameId = project.id
        }
        if (needsValidServerId) {
            if (!db.tableProjects.hasServerId(rootName, subName)) {
                error("saveProjectAndAddressCombo(): current project is not associated with anything on the server: $rootName - $subName")
                clearCurProject()
                return false
            }
        }
        var projectGroupId: Long
        if (modifyCurrent) {
            projectGroupId = currentProjectGroupId
            if (projectGroupId < 0) {
                error("saveProjectAndAddressCombo(): could not modify current project, none is alive")
                return false
            }
            val combo = db.tableProjectAddressCombo.query(projectGroupId) ?: return false
            combo.reset(projectNameId, addressId)
            if (!db.tableProjectAddressCombo.save(combo)) {
                error("saveProjectAndAddressCombo(): could not update project combo")
                return false
            }
            db.tableProjectAddressCombo.mergeIdenticals(combo)
            val count = db.tableEntry.reUploadEntries(combo)
            if (count > 0) {
                msg("saveProjectAddressCombo(): re-upload $count entries")
            }
            db.tableProjectAddressCombo.updateUsed(projectGroupId)
        } else {
            projectGroupId = db.tableProjectAddressCombo.queryProjectGroupId(projectNameId, addressId)
            if (projectGroupId < 0) {
                projectGroupId = db.tableProjectAddressCombo.add(DataProjectAddressCombo(db, projectNameId, addressId))
            } else {
                db.tableProjectAddressCombo.updateUsed(projectGroupId)
            }
            currentProjectGroupId = projectGroupId
        }
        return true
    }

    private fun setAddress(address: DataAddress?) {
        company = address!!.company
        street = address.street
        city = address.city
        state = address.state
        zipCode = address.zipcode
    }


    private fun inc() {
        incNextEquipmentCollectionID()
        incNextPictureCollectionID()
        incNextNoteCollectionID()
    }

    private fun incNextPictureCollectionID() {
        setLong(KEY_NEXT_PICTURE_COLLECTION_ID, nextPictureCollectionID + 1)
    }

    private fun incNextEquipmentCollectionID() {
        setLong(KEY_NEXT_EQUIPMENT_COLLECTION_ID, nextEquipmentCollectionID + 1)
    }

    private fun incNextNoteCollectionID() {
        setLong(KEY_NEXT_NOTE_COLLECTION_ID, nextEquipmentCollectionID + 1)
    }

    /**
     * Store a new entry into the database that is ready to be uploaded.
     * Entry data is stored directly in the DataEntry object, or by a reference
     * to the table which has the unique entry values.
     */
    private fun createEntry(incOkay: Boolean): DataEntry? {
        val projectGroupId = currentProjectGroupId
        if (projectGroupId < 0) {
            return null
        }
        val projectGroup = db.tableProjectAddressCombo.query(projectGroupId) ?: return null
        val entry = DataEntry(db)
        entry.projectAddressCombo = projectGroup
        entry.equipmentCollection = DataCollectionEquipmentEntry(db, nextEquipmentCollectionID)
        entry.equipmentCollection!!.addChecked()
        entry.pictures = db.tablePicture.createCollectionFromPending(nextPictureCollectionID)
        entry.truckId = db.tableTruck.save(
                truckNumberValue ?: "",
                truckNumberPictureId,
                truckHasDamage ?: false,
                truckDamagePictureId,
                truckDamageValue ?: "",
                projectGroup.projectNameId,
                projectGroup.companyName!!)
        entry.status = status
        entry.saveNotes(nextNoteCollectionID, statusIsPartialInstall)
        entry.date = System.currentTimeMillis()
        if (incOkay) {
            inc()
        }
        return entry
    }

    fun setFromEntry(entry: DataEntry) {
        db.tableNote.clearValues()
        db.tableNote.updateValues(entry.notesWithValues)
        currentEditEntryId = entry.id
        currentProjectGroupId = entry.projectAddressCombo?.id ?: 0
        currentPictureCollectionId = entry.pictureCollectionId
        entry.project?.let { project ->
            projectRootName = project.rootProject
            projectSubName = project.subProject
        } ?: run {
            projectRootName = null
            projectSubName = null
        }
        setAddress(entry.address)
        entry.equipmentCollection?.setChecked()
        entry.truck?.let { truck ->
            truckNumberValue = truck.truckNumberValue
            truckNumberPictureId = truck.truckNumberPictureId
            truckHasDamage = truck.truckHasDamage
            truckDamageValue = truck.truckDamageValue
            truckDamagePictureId = truck.truckDamagePictureId
        } ?: run {
            truckNumberValue = null
            truckHasDamage = false
            truckDamageValue = null
            truckNumberPictureId = 0
            truckDamagePictureId = 0
        }
        status = entry.status
    }

    /**
     * Save the data entered for either a currently edited entry, or save a new entry.
     */
    fun saveEntry(incOkay: Boolean): DataEntry? {
        val entry = currentEditEntry ?: return createEntry(incOkay)
        entry.equipmentCollection?.addChecked()
        var truck = entry.truck
        if (truck == null) {
            truck = DataTruck()
        }
        truck.truckNumberValue = truckNumberValue
        truck.truckNumberPictureId = truckNumberPictureId
        truck.truckDamagePictureId = truckDamagePictureId
        truck.truckHasDamage = truckHasDamage ?: false
        truck.hasEntry = true
        entry.truckId = db.tableTruck.save(truck)
        entry.status = status
        entry.uploadedMaster = false
        entry.uploadedAws = false
        entry.hasError = false
        entry.serverErrorCount = 0.toShort()
        // Be careful here: I use the date to match an tableEntry when looking up the server id for older APP versions.
        if (entry.serverId > 0) {
            entry.date = System.currentTimeMillis()
        }
        entry.saveNotes(statusIsPartialInstall)
        return entry
    }

    private fun genPictureFilename(): String {
        val techId = techID.toLong()
        val projId = projectId
        val sbuf = StringBuilder()
        sbuf.append("picture_t")
        sbuf.append(techId)
        sbuf.append("_p")
        sbuf.append(projId ?: "bad")
        sbuf.append("_d")
        val fmt = SimpleDateFormat(PICTURE_DATE_FORMAT, Locale.getDefault())
        sbuf.append(fmt.format(Date(System.currentTimeMillis())))
        sbuf.append(".jpg")
        return sbuf.toString()
    }

    fun genFullPictureFile(): File {
        return File(ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), genPictureFilename())
    }

    fun clearUploaded() {
        versionNote = VERSION_RESET
        versionProject = VERSION_RESET
        versionEquipment = VERSION_RESET
        versionCompany = VERSION_RESET
        versionTruck = VERSION_RESET
        versionFlow = VERSION_RESET
    }

    fun reloadProjects() {
        versionProject = VERSION_RESET
    }

    fun reloadEquipments() {
        versionEquipment = VERSION_RESET
    }

    private fun msg(msg: String) {
        Timber.tag(TAG).i(msg)
    }

    private fun verbose(msg: String) {
        Timber.tag(TAG).d(msg)
    }

    private fun error(msg: String) {
        Timber.tag(TAG).e(msg)
    }
}
