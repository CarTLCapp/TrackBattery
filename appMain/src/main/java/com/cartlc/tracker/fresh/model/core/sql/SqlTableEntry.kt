/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.fresh.model.misc.TruckStatus
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableEntry

import java.util.ArrayList

import timber.log.Timber

/**
 * Created by dug on 4/17/17.
 */

class SqlTableEntry(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
) : TableEntry {

    companion object {
        private val TAG = SqlTableEntry::class.simpleName

        private const val TABLE_NAME = "table_entries"

        private const val KEY_ROWID = "_id"
        private const val KEY_DATE = "date"
        private const val KEY_PROJECT_ADDRESS_COMBO_ID = "combo_id"
        private const val KEY_EQUIPMENT_COLLECTION_ID = "equipment_collection_id"
        private const val KEY_PICTURE_COLLECTION_ID = "picture_collection_id"
        private const val KEY_NOTE_COLLECTION_ID = "note_collection_id"
        private const val KEY_TRUCK_ID = "truck_id"
        private const val KEY_STATUS = "status"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_SERVER_ERROR_COUNT = "server_error_count"
        private const val KEY_FLOW_PROGRESS = "flow_progress"
        private const val KEY_UPLOADED_MASTER = "uploaded_master"
        private const val KEY_UPLOADED_AWS = "uploaded_aws"
        private const val KEY_HAD_ERROR = "had_error"
        private const val KEY_IS_COMPLETE = "is_complete"
    }

    data class Count(
            var totalEntries: Int = 0,
            var totalUploadedAws: Int = 0,
            var totalUploadedMaster: Int = 0,
            var totalIncomplete: Int = 0
    ) {
        fun uploadedAll(): Boolean {
            return totalUploadedAws >= totalEntries && totalUploadedMaster >= totalEntries
        }
    }

    fun upgrade11() {
        try {
            dbSql.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $KEY_HAD_ERROR bit default 0")
            dbSql.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $KEY_SERVER_ERROR_COUNT smallint default 0")
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEntry::class.java, "upgrade11()", "db")
        }
    }

    fun upgrade21() {
        try {
            dbSql.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $KEY_IS_COMPLETE bit default 0")
            dbSql.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $KEY_FLOW_PROGRESS smallint default 0")
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEntry::class.java, "upgrade21()", "db")
        }
        setAllComplete()
    }

    fun clear() {
        try {
            dbSql.delete(TABLE_NAME, null, null)
        } catch (ex: Exception) {
        }
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_DATE)
        sbuf.append(" long, ")
        sbuf.append(KEY_PROJECT_ADDRESS_COMBO_ID)
        sbuf.append(" long, ")
        sbuf.append(KEY_EQUIPMENT_COLLECTION_ID)
        sbuf.append(" long, ")
        sbuf.append(KEY_PICTURE_COLLECTION_ID)
        sbuf.append(" long, ")
        sbuf.append(KEY_NOTE_COLLECTION_ID)
        sbuf.append(" long, ")
        sbuf.append(KEY_TRUCK_ID)
        sbuf.append(" long, ")
        sbuf.append(KEY_STATUS)
        sbuf.append(" tinyint, ")
        sbuf.append(KEY_SERVER_ID)
        sbuf.append(" long default 0, ")
        sbuf.append(KEY_SERVER_ERROR_COUNT)
        sbuf.append(" smallint default 0, ")
        sbuf.append(KEY_FLOW_PROGRESS)
        sbuf.append(" smallint default 0, ")
        sbuf.append(KEY_UPLOADED_MASTER)
        sbuf.append(" bit default 0, ")
        sbuf.append(KEY_UPLOADED_AWS)
        sbuf.append(" bit default 0, ")
        sbuf.append(KEY_HAD_ERROR)
        sbuf.append(" bit default 0, ")
        sbuf.append(KEY_IS_COMPLETE)
        sbuf.append(" bit default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    override fun queryPendingEntriesToUpload(): List<DataEntry> {
        val where = "$KEY_UPLOADED_MASTER=0"
        return query(where, null)
    }

    override fun queryPendingPicturesToUpload(): List<DataEntry> {
        val where = "$KEY_UPLOADED_AWS=0"
        return query(where, null)
    }

    override val hasEntriesToUpload: Boolean
        get() {
            var count = 0
            try {
                val columns = arrayOf(KEY_ROWID)
                val selection = "$KEY_UPLOADED_MASTER=0 OR $KEY_UPLOADED_AWS=0"
                val cursor = dbSql.query(TABLE_NAME, columns, selection, null, null, null, null, null)
                count = cursor.count
                cursor.close()
            } catch (ex: Exception) {
                TBApplication.ReportError(ex, SqlTableEntry::class.java, "hasEntriesToUpload()", "db")
            }
            return count > 0
        }

    override fun queryForProjectAddressCombo(id: Long): List<DataEntry> {
        val where = "$KEY_PROJECT_ADDRESS_COMBO_ID=?"
        val whereArgs = arrayOf(id.toString())
        return query(where, whereArgs)
    }

    override fun queryEmptyServerIds(): List<DataEntry> {
        val where = "$KEY_SERVER_ID=0"
        return query(where, null)
    }

    override fun query(): List<DataEntry> {
        return query(null, null)
    }

    override fun query(id: Long): DataEntry? {
        val where = "$KEY_ROWID=?"
        val whereArgs = arrayOf(id.toString())
        val list = query(where, whereArgs)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    internal fun query(where: String?, whereArgs: Array<String>?): List<DataEntry> {
        val list = ArrayList<DataEntry>()
        try {
            val orderBy = "$KEY_DATE DESC"
            val cursor = dbSql.query(TABLE_NAME, null, where, whereArgs, null, null, orderBy, null)
            val idxRow = cursor.getColumnIndex(KEY_ROWID)
            val idxProjectAddressCombo = cursor.getColumnIndex(KEY_PROJECT_ADDRESS_COMBO_ID)
            val idxDate = cursor.getColumnIndex(KEY_DATE)
            val idxEquipmentCollectionId = cursor.getColumnIndex(KEY_EQUIPMENT_COLLECTION_ID)
            val idxPictureCollectionId = cursor.getColumnIndex(KEY_PICTURE_COLLECTION_ID)
            val idxNoteCollectionId = cursor.getColumnIndex(KEY_NOTE_COLLECTION_ID)
            val idxTruckId = cursor.getColumnIndex(KEY_TRUCK_ID)
            val idxStatus = cursor.getColumnIndex(KEY_STATUS)
            val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
            val idxServerErrorCount = cursor.getColumnIndex(KEY_SERVER_ERROR_COUNT)
            val idxFlowProgress = cursor.getColumnIndex(KEY_FLOW_PROGRESS)
            val idxUploadedMaster = cursor.getColumnIndex(KEY_UPLOADED_MASTER)
            val idxUploadedAws = cursor.getColumnIndex(KEY_UPLOADED_AWS)
            val idxHasError = cursor.getColumnIndex(KEY_HAD_ERROR)
            val idxIsComplete = cursor.getColumnIndex(KEY_IS_COMPLETE)
            var entry: DataEntry
            while (cursor.moveToNext()) {
                entry = DataEntry(db)
                entry.id = cursor.getLong(idxRow)
                if (!cursor.isNull(idxDate)) {
                    entry.date = cursor.getLong(idxDate)
                }
                if (!cursor.isNull(idxProjectAddressCombo)) {
                    val projectAddressComboId = cursor.getLong(idxProjectAddressCombo)
                    entry.projectAddressCombo = db.tableProjectAddressCombo.query(projectAddressComboId)
                }
                if (!cursor.isNull(idxEquipmentCollectionId)) {
                    val equipmentCollectionId = cursor.getLong(idxEquipmentCollectionId)
                    entry.equipmentCollection = db.tableCollectionEquipmentEntry.queryForCollectionId(equipmentCollectionId)
                }
                if (!cursor.isNull(idxPictureCollectionId)) {
                    val pictureCollectionId = cursor.getLong(idxPictureCollectionId)
                    entry.pictures = db.tablePicture.query(pictureCollectionId, null)
                }
                if (!cursor.isNull(idxNoteCollectionId)) {
                    entry.noteCollectionId = cursor.getLong(idxNoteCollectionId)
                }
                if (!cursor.isNull(idxTruckId)) {
                    entry.truckId = cursor.getLong(idxTruckId)
                }
                if (!cursor.isNull(idxStatus)) {
                    entry.status = TruckStatus.from(cursor.getInt(idxStatus))
                }
                entry.serverId = cursor.getInt(idxServerId)
                entry.serverErrorCount = cursor.getShort(idxServerErrorCount)
                entry.flowProgress = cursor.getShort(idxFlowProgress)
                entry.uploadedMaster = cursor.getShort(idxUploadedMaster).toInt() != 0
                entry.uploadedAws = cursor.getShort(idxUploadedAws).toInt() != 0
                entry.hasError = cursor.getShort(idxHasError).toInt() != 0
                entry.isComplete = cursor.getShort(idxIsComplete).toInt() != 0
                list.add(entry)
            }
            cursor.close()
        } catch (ex: Exception) {
            val sbuf = StringBuilder()
            sbuf.append(ex.message)
            sbuf.append(" [")
            if (where != null) {
                sbuf.append(where)
                if (whereArgs != null && whereArgs.isNotEmpty()) {
                    for (arg in whereArgs) {
                        sbuf.append(", ")
                        sbuf.append(arg)
                    }
                }
            }
            sbuf.append("]")
            TBApplication.ReportError(sbuf.toString(), SqlTableEntry::class.java, "query()", "db")
        }

        return list
    }

    override fun countProjectAddressCombo(comboId: Long): Count {
        val count = Count()
        try {
            val selection = "$KEY_PROJECT_ADDRESS_COMBO_ID =?"
            val selectionArgs = arrayOf(comboId.toString())
            val columns = arrayOf(KEY_UPLOADED_AWS, KEY_UPLOADED_MASTER, KEY_IS_COMPLETE)
            val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null)
            val idxUploadedAws = cursor.getColumnIndex(KEY_UPLOADED_AWS)
            val idxUploadedMaster = cursor.getColumnIndex(KEY_UPLOADED_MASTER)
            val idxIsComplete = cursor.getColumnIndex(KEY_IS_COMPLETE)
            while (cursor.moveToNext()) {
                if (cursor.getShort(idxUploadedAws).toInt() != 0) {
                    count.totalUploadedAws++
                }
                if (cursor.getShort(idxUploadedMaster).toInt() != 0) {
                    count.totalUploadedMaster++
                }
                if (cursor.getShort(idxIsComplete).toInt() == 0) {
                    count.totalIncomplete++
                }
                count.totalEntries++
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEntry::class.java, "countProjectAddressCombo()", "db")
        }
        return count
    }

    override fun countAddresses(addressId: Long): Int {
        var count = 0
        try {
            val columns = arrayOf(KEY_PROJECT_ADDRESS_COMBO_ID)
            val cursor = dbSql.query(TABLE_NAME, columns, null, null, null, null, null, null)
            val idxProjectAddressCombo = cursor.getColumnIndex(KEY_PROJECT_ADDRESS_COMBO_ID)
            var projectAddressCombo: DataProjectAddressCombo?
            while (cursor.moveToNext()) {
                val projectAddressComboId = cursor.getLong(idxProjectAddressCombo)
                projectAddressCombo = db.tableProjectAddressCombo.query(projectAddressComboId)
                if (projectAddressCombo!!.addressId == addressId) {
                    count++
                }
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEntry::class.java, "countAddresses()", "db")
        }
        return count
    }

    override fun countTrucks(truckId: Long): Int {
        var count = 0
        try {
            val columns = arrayOf(KEY_TRUCK_ID)
            val selection = "$KEY_TRUCK_ID=?"
            val selectionArgs = arrayOf(truckId.toString())
            val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEntry::class.java, "countTrucks()", "db")
        }
        return count
    }

    override fun countProjects(projectId: Long): Int {
        var count = 0
        try {
            val columns = arrayOf(KEY_PROJECT_ADDRESS_COMBO_ID)
            val cursor = dbSql.query(TABLE_NAME, columns, null, null, null, null, null, null)
            val idxProjectAddressCombo = cursor.getColumnIndex(KEY_PROJECT_ADDRESS_COMBO_ID)
            var projectAddressCombo: DataProjectAddressCombo?
            while (cursor.moveToNext()) {
                val projectAddressComboId = cursor.getLong(idxProjectAddressCombo)
                projectAddressCombo = db.tableProjectAddressCombo.query(projectAddressComboId)
                if (projectAddressCombo!!.projectNameId == projectId) {
                    count++
                }
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEntry::class.java, "countProjects()", "db")
        }
        return count
    }

    override fun reUploadEntries(combo: DataProjectAddressCombo): Int {
        var count = 0
        try {
            val where = "$KEY_PROJECT_ADDRESS_COMBO_ID=?"
            val whereArgs = arrayOf(combo.id.toString())
            val values = ContentValues()
            values.put(KEY_UPLOADED_MASTER, false)
            count = dbSql.update(TABLE_NAME, values, where, whereArgs)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEntry::class.java, "reUploadEntries()", "db")
        }
        return count
    }

    override fun updateOrInsert(entry: DataEntry): Boolean {
        var incNextCollectionID = false
        dbSql.beginTransaction()
        try {
            db.tableCollectionEquipmentEntry.save(entry.equipmentCollection!!)
            db.tablePicture.add(entry.pictures)
            if (entry.id == 0L) {
                incNextCollectionID = true
            }
            val values = ContentValues()
            values.clear()
            values.put(KEY_DATE, entry.date)
            values.put(KEY_PROJECT_ADDRESS_COMBO_ID, entry.projectAddressCombo!!.id)
            values.put(KEY_EQUIPMENT_COLLECTION_ID, entry.equipmentCollection!!.id)
            values.put(KEY_TRUCK_ID, entry.truckId)
            values.put(KEY_NOTE_COLLECTION_ID, entry.noteCollectionId)
            values.put(KEY_PICTURE_COLLECTION_ID, entry.pictureCollectionId)
            values.put(KEY_SERVER_ID, entry.serverId)
            values.put(KEY_SERVER_ERROR_COUNT, entry.serverErrorCount)
            values.put(KEY_FLOW_PROGRESS, entry.flowProgress)
            values.put(KEY_UPLOADED_AWS, if (entry.uploadedAws) 1 else 0)
            values.put(KEY_UPLOADED_MASTER, if (entry.uploadedMaster) 1 else 0)
            values.put(KEY_HAD_ERROR, if (entry.hasError) 1 else 0)
            values.put(KEY_IS_COMPLETE, if (entry.isComplete) 1 else 0)
            if (entry.status != null) {
                values.put(KEY_STATUS, entry.status!!.ordinal)
            }
            var insert = true
            if (entry.id > 0) {
                val where = "$KEY_ROWID=?"
                val whereArgs = arrayOf(entry.id.toString())
                if (dbSql.update(TABLE_NAME, values, where, whereArgs) != 0) {
                    insert = false
                }
            }
            if (insert) {
                entry.id = dbSql.insert(TABLE_NAME, null, values)
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEntry::class.java, "add()", "db")
            incNextCollectionID = false
        } finally {
            dbSql.endTransaction()
        }
        return incNextCollectionID
    }

    // Right now only used to update a few fields.
    // Later this will be extended to saveUploaded everything.
    override fun saveUploaded(entry: DataEntry) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_SERVER_ID, entry.serverId)
            values.put(KEY_SERVER_ERROR_COUNT, entry.serverErrorCount)
            values.put(KEY_UPLOADED_AWS, if (entry.uploadedAws) 1 else 0)
            values.put(KEY_UPLOADED_MASTER, if (entry.uploadedMaster) 1 else 0)
            values.put(KEY_HAD_ERROR, if (entry.hasError) 1 else 0)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(entry.id.toString())
            if (dbSql.update(TABLE_NAME, values, where, whereArgs) == 0) {
                Timber.tag(TAG).e("SqlTableEntry.saveUploaded(): Unable to update tableEntry")
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEntry::class.java, "saveUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun saveProjectAddressCombo(entry: DataEntry) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_UPLOADED_MASTER, if (entry.uploadedMaster) 1 else 0)
            values.put(KEY_PROJECT_ADDRESS_COMBO_ID, entry.projectAddressCombo!!.id)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(entry.id.toString())
            if (dbSql.update(TABLE_NAME, values, where, whereArgs) == 0) {
                Timber.tag(TAG).e("SqlTableEntry.saveProjectAddressCombo(): Unable to update tableEntry")
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEntry::class.java, "saveProjectAddressCombo()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    fun clearUploaded() {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_UPLOADED_MASTER, 0)
            values.put(KEY_UPLOADED_AWS, 0)
            values.put(KEY_HAD_ERROR, 0)
            values.put(KEY_SERVER_ERROR_COUNT, 0)
            dbSql.update(TABLE_NAME, values, null, null)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEntry::class.java, "clearUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    private fun setAllComplete() {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_IS_COMPLETE, 1)
            dbSql.update(TABLE_NAME, values, null, null)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEntry::class.java, "setAllComplete()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun remove(entry: DataEntry) {
        val where = "$KEY_ROWID=?"
        val whereArgs = arrayOf(entry.id.toString())
        dbSql.delete(TABLE_NAME, where, whereArgs)
    }

}
