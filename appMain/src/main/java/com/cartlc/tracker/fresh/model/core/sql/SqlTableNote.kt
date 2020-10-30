/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableNote
import com.cartlc.tracker.fresh.model.core.table.TableNote.Companion.NOTE_PARTIAL_INSTALL_REASON
import com.cartlc.tracker.fresh.model.core.table.TableNote.Companion.NOTE_TRUCK_DAMAGE_NAME
import com.cartlc.tracker.fresh.model.core.table.TableNote.Companion.NOTE_TRUCK_NUMBER_NAME

import com.cartlc.tracker.fresh.ui.app.TBApplication

import java.util.ArrayList

import timber.log.Timber

/**
 * Created by dug on 4/17/17.
 */
class SqlTableNote constructor(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
): TableNote {

    companion object {
        private val TAG = SqlTableNote::class.simpleName

        private const val TABLE_NAME = "list_notes"

        private const val KEY_ROWID = "_id"
        private const val KEY_NAME = "name"
        private const val KEY_VALUE = "value"
        private const val KEY_TYPE = "type"
        private const val KEY_NUM_DIGITS = "num_digits"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_IS_BOOT = "is_boot_strap"
    }

    override val noteTruckNumber: DataNote?
        get() = queryByName(NOTE_TRUCK_NUMBER_NAME)

    override val noteTruckDamage: DataNote?
        get() = queryByName(NOTE_TRUCK_DAMAGE_NAME)

    override val notePartialInstall: DataNote?
        get() = queryByName(NOTE_PARTIAL_INSTALL_REASON)

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_NAME)
        sbuf.append(" text not null, ")
        sbuf.append(KEY_VALUE)
        sbuf.append(" text, ")
        sbuf.append(KEY_TYPE)
        sbuf.append(" int, ")
        sbuf.append(KEY_NUM_DIGITS)
        sbuf.append(" smallint default 0, ")
        sbuf.append(KEY_SERVER_ID)
        sbuf.append(" int, ")
        sbuf.append(KEY_IS_BOOT)
        sbuf.append(" bit default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    fun clear() {
        try {
            dbSql.delete(TABLE_NAME, null, null)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableNote::class.java, "clear()", "db")
        }
    }

    fun count(): Int {
        var count = 0
        try {
            val cursor = dbSql.query(TABLE_NAME, null, null, null, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableNote::class.java, "count()", "db")
        }
        return count
    }

    fun add(list: List<DataNote>) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            for (value in list) {
                values.clear()
                values.put(KEY_NAME, value.name)
                values.put(KEY_TYPE, value.type!!.ordinal)
                values.put(KEY_VALUE, value.value)
                values.put(KEY_NUM_DIGITS, value.numDigits)
                values.put(KEY_SERVER_ID, value.serverId)
                values.put(KEY_IS_BOOT, if (value.isBootStrap) 1 else 0)
                dbSql.insert(TABLE_NAME, null, values)
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableNote::class.java, "add(list)", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun add(item: DataNote): Long {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.clear()
            values.put(KEY_NAME, item.name)
            values.put(KEY_TYPE, item.type!!.ordinal)
            values.put(KEY_VALUE, item.value)
            values.put(KEY_NUM_DIGITS, item.numDigits)
            values.put(KEY_SERVER_ID, item.serverId)
            values.put(KEY_IS_BOOT, if (item.isBootStrap) 1 else 0)
            item.id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableNote::class.java, "add(item)", "db")
        } finally {
            dbSql.endTransaction()
        }
        return item.id
    }

    override fun clearValues() {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_VALUE, null as String?)
            dbSql.update(TABLE_NAME, values, null, null)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableNote::class.java, "clearValues()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun query(name: String): Long {
        var rowId = -1L
        try {
            val columns = arrayOf(KEY_ROWID)
            val selection = "$KEY_NAME=?"
            val selectionArgs = arrayOf(name)
            val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
            val idxValue = cursor.getColumnIndex(KEY_ROWID)
            if (cursor.moveToFirst()) {
                rowId = cursor.getLong(idxValue)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableNote::class.java, "query()", name)
        }
        return rowId
    }

    override fun query(id: Long): DataNote? {
        val selection = "$KEY_ROWID=?"
        val selectionArgs = arrayOf(id.toString())
        val list = query(selection, selectionArgs)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    private fun queryByName(name: String): DataNote? {
        val selection = "$KEY_NAME=?"
        val selectionArgs = arrayOf(name)
        val list = query(selection, selectionArgs)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    override fun queryByServerId(server_id: Int): DataNote? {
        val selection = "$KEY_SERVER_ID=?"
        val selectionArgs = arrayOf(server_id.toString())
        val list = query(selection, selectionArgs)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    override fun query(selection: String?, selectionArgs: Array<String>?): List<DataNote> {
        val list = ArrayList<DataNote>()
        try {
            val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)
            val idxRowId = cursor.getColumnIndex(KEY_ROWID)
            val idxName = cursor.getColumnIndex(KEY_NAME)
            val idxValue = cursor.getColumnIndex(KEY_VALUE)
            val idxType = cursor.getColumnIndex(KEY_TYPE)
            val idxNumDigits = cursor.getColumnIndex(KEY_NUM_DIGITS)
            val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
            val idxTest = cursor.getColumnIndex(KEY_IS_BOOT)
            while (cursor.moveToNext()) {
                val item = DataNote()
                item.id = cursor.getLong(idxRowId)
                item.name = cursor.getString(idxName)
                item.value = cursor.getString(idxValue)
                item.type = DataNote.Type.from(cursor.getInt(idxType))
                item.numDigits = cursor.getShort(idxNumDigits)
                item.serverId = cursor.getInt(idxServerId)
                item.isBootStrap = cursor.getShort(idxTest).toInt() != 0
                list.add(item)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableNote::class.java, "query(selection)", "db")
        }
        return list
    }

    override fun update(item: DataNote) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.clear()
            values.put(KEY_NAME, item.name)
            values.put(KEY_TYPE, item.type?.ordinal)
            values.put(KEY_VALUE, item.value)
            values.put(KEY_SERVER_ID, item.serverId)
            values.put(KEY_NUM_DIGITS, item.numDigits)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(item.id.toString())
            dbSql.update(TABLE_NAME, values, where, whereArgs)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableNote::class.java, "update(item)", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun updateValue(item: DataNote) {
        updateValues(listOf(item))
    }

    override fun updateValues(notes: List<DataNote>) {
        dbSql.beginTransaction()
        try {
            notes.forEach { note ->
                val values = ContentValues()
                values.put(KEY_VALUE, note.value)
                val where = "$KEY_ROWID=?"
                val whereArgs = arrayOf(note.id.toString())
                dbSql.update(TABLE_NAME, values, where, whereArgs)
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableNote::class.java, "updateValue()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    internal fun remove(id: Long) {
        val where = "$KEY_ROWID=?"
        val whereArgs = arrayOf(id.toString())
        dbSql.delete(TABLE_NAME, where, whereArgs)
    }

    override fun removeIfUnused(note: DataNote) {
        if (db.tableCollectionNoteEntry.countNotes(note.id) == 0) {
            remove(note.id)
        } else {
            Timber.tag(TAG).i("Did not remove unused note because some entries are using it: $note")
        }
    }

    fun clearUploaded() {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_SERVER_ID, 0)
            dbSql.update(TABLE_NAME, values, null, null)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableNote::class.java, "clearUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }


}
