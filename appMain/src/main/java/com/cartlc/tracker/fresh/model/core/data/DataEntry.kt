/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.data

import android.content.Context
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.misc.TruckStatus
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.emptyList
import kotlin.collections.isNotEmpty
import kotlin.collections.mutableListOf

/**
 * Created by dug on 5/13/17.
 */

class DataEntry(private val db: DatabaseTable) {

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'z"
        const val UPLOAD_DEBUG = false
    }

    var id: Long = 0
    var date: Long = 0
    var projectAddressCombo: DataProjectAddressCombo? = null
    var equipmentCollection: DataCollectionEquipmentEntry? = null
    var pictures: List<DataPicture> = emptyList()
    var noteCollectionId: Long = 0
    var truckId: Long = 0
    var status: TruckStatus? = null
    var serverId: Int = 0
    var serverErrorCount: Short = 0
    var flowProgress: Short = 0
    var uploadedMaster: Boolean = false
    var uploadedAws: Boolean = false
    var isComplete: Boolean = false
    var hasError: Boolean = false

    val dateString: String
        get() = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date(date))

    val projectDashName: String
        get() = projectAddressCombo?.projectDashName ?: ""

    val project: DataProject?
        get() = projectAddressCombo?.project

    val pictureCollectionId: Long
        get() {
            if (pictures.isNotEmpty()) {
                return pictures[0].collectionId ?: 0
            }
            return 0
        }

    val address: DataAddress?
        get() = projectAddressCombo?.address

    val addressBlock: String?
        get() {
            val address = address
            return address?.block
        }

    val addressLine: String
        get() {
            val address = address
            return address?.line ?: "Invalid"
        }

    /**
     * Overlay the current value for each of the incoming notes and then store that note back into the TableNote table.
     *  The current value is fetched from the TableCollectionNoteEntry table.
     *  It will then be stored into the TableNote table.
     */
    fun overlayNoteValues(incoming: List<DataNote>): List<DataNote> {
        val valueNotes = notesWithValues
        val result = ArrayList<DataNote>()
        for (note in incoming) {
            val valueNote = getNoteFrom(valueNotes, note)
            if (valueNote != null) {
                db.tableNote.update(valueNote)
                result.add(valueNote)
            } else {
                result.add(note)
            }
        }
        return result
    }

    fun overlayNoteValue(noteId: Long): DataNote? {
        return db.tableCollectionNoteEntry.query(noteCollectionId, noteId)
    }

    /**
     * Update TableCollectionNoteEntry with the value stored in the passed in note.
     */
    fun updateNoteValue(note: DataNote) {
        db.tableCollectionNoteEntry.updateValue(noteCollectionId, note)
    }

    // Get all the notes as indicated by the project.
    // This will also include any current edits in place as well.
    private fun pendingNotes(withPartialInstallReason: Boolean): List<DataNote> {
        return projectAddressCombo?.projectNameId?.let { db.noteHelper.getPendingNotes(it, withPartialInstallReason) }
                ?: emptyList()
    }

    // Return the notes for the collection along with their values.
    val notesWithValues: List<DataNote>
        get() = db.tableCollectionNoteEntry.query(noteCollectionId)

    val notesLine: String
        get() {
            val sbuf = StringBuilder()
            for (note in notesWithValues) {
                if (!note.value.isNullOrBlank()) {
                    if (sbuf.isNotEmpty()) {
                        sbuf.append(", ")
                    }
                    sbuf.append(note.value)
                }
            }
            return sbuf.toString()
        }

    val equipmentNames: List<String>?
        get() = if (equipmentCollection != null) {
            equipmentCollection!!.equipmentNames
        } else null

    val equipment: List<DataEquipment>?
        get() = if (equipmentCollection != null) {
            equipmentCollection!!.equipment
        } else null

    val truck: DataTruck?
        get() = db.tableTruck.query(truckId)

    fun getStatus(ctx: Context): String {
        return if (status != null) {
            status!!.getString(ctx)
        } else TruckStatus.UNKNOWN.getString(ctx)
    }

    private fun getNoteFrom(list: List<DataNote>, check: DataNote): DataNote? {
        for (note in list) {
            if (note.id == check.id) {
                return note
            }
        }
        return null
    }

    fun saveNotes(collectionId: Long, withPartialInstallReason: Boolean) {
        noteCollectionId = collectionId
        saveNotes(withPartialInstallReason)
    }

    fun saveNotes(withPartialInstallReason: Boolean) {
        val useNotes = pendingNotes(withPartialInstallReason)
        db.tableCollectionNoteEntry.remove(noteCollectionId)
        db.tableCollectionNoteEntry.save(noteCollectionId, useNotes)
    }

    fun checkPictureUploadComplete(): Boolean {
        if (UPLOAD_DEBUG) {
            val files = mutableListOf<String>()
            for (picture in pictures) {
                if (picture.uploaded) {
                    files.add(picture.scaledFilename ?: "null")
                }
            }
        }
        for (item in pictures) {
            if (!item.uploaded) {
                return false
            }
        }
        uploadedAws = true
        db.tableEntry.saveUploaded(this)
        return true
    }

    fun getTruckLine(ctx: Context): String {
        val truck = truck
        return truck?.toString() ?: ctx.getString(R.string.error_missing_truck_short)
    }

    fun getEquipmentLine(ctx: Context): String {
        val sbuf = StringBuilder()
        for (name in equipmentNames!!) {
            if (sbuf.length > 0) {
                sbuf.append(", ")
            }
            sbuf.append(name)
        }
        if (sbuf.length == 0) {
            sbuf.append(ctx.getString(R.string.status_no_equipment))
        }
        return sbuf.toString()
    }

    fun toLongString(db: DatabaseTable): String {
        val sbuf = StringBuilder()
        sbuf.append("ID=")
        sbuf.append(id)
        if (projectAddressCombo != null) {
            sbuf.append("\nCOMBO=[")
            sbuf.append(projectAddressCombo!!.toString())
            sbuf.append("]")
        }
        if (equipmentCollection != null) {
            sbuf.append("\nEQUIP=")
            sbuf.append(equipmentCollection!!.toString())
        }
        sbuf.append("\nNOTES=[")
        for (note in notesWithValues) {
            sbuf.append("[")
            sbuf.append(note.toString())
            sbuf.append("] ")
        }
        sbuf.append("]\n")
        val truck = truck
        if (truck != null) {
            sbuf.append("TRUCK=")
            sbuf.append(truck.toLongString(db))
            sbuf.append("\n")
        }
        if (date > 0) {
            sbuf.append("DATE=")
            sbuf.append(SimpleDateFormat("yy-MM-dd_HH:mm:ss", Locale.getDefault()).format(date))
            sbuf.append(" ")
        }
        sbuf.append("SERVERID=")
        sbuf.append(serverId)
        if (status != null) {
            sbuf.append(", STATUS=")
            sbuf.append(status!!.toString())
        }
        sbuf.append(", FLAGS=[")
        if (uploadedAws) {
            sbuf.append("UPLOADAWS")
        }
        if (uploadedMaster) {
            sbuf.append(" UPLOADMASTER")
        }
        if (hasError) {
            sbuf.append(" ERROR")
        }
        sbuf.append("]")
        return sbuf.toString()
    }

}
