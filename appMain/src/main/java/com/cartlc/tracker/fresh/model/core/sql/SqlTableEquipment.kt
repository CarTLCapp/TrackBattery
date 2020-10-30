/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataEquipment
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableEquipment

import com.cartlc.tracker.fresh.ui.app.TBApplication

import java.util.ArrayList

import timber.log.Timber

/**
 * Created by dug on 4/17/17.
 */
class SqlTableEquipment(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
) : TableEquipment {

    companion object {
        private val TAG = SqlTableEquipment::class.simpleName

        private const val TABLE_NAME = "list_equipment"

        private const val KEY_ROWID = "_id"
        private const val KEY_NAME = "name"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_CHECKED = "is_checked"
        private const val KEY_LOCAL = "is_local"
        private const val KEY_IS_BOOT = "is_boot_strap"
        private const val KEY_DISABLED = "disabled"
    }

    fun clear() {
        try {
            dbSql.delete(TABLE_NAME, null, null)
        } catch (ex: Exception) {
        }
    }

    fun drop() {
        dbSql.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_NAME)
        sbuf.append(" text, ")
        sbuf.append(KEY_SERVER_ID)
        sbuf.append(" int, ")
        sbuf.append(KEY_CHECKED)
        sbuf.append(" bit default 0, ")
        sbuf.append(KEY_LOCAL)
        sbuf.append(" bit default 0, ")
        sbuf.append(KEY_IS_BOOT)
        sbuf.append(" bit default 0, ")
        sbuf.append(KEY_DISABLED)
        sbuf.append(" bit default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    fun count(): Int {
        var count = 0
        try {
            val cursor = dbSql.query(TABLE_NAME, null, null, null, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEquipment::class.java, "count()", "db")
        }

        return count
    }

    override fun countChecked(): Int {
        var count = 0
        try {
            val selection = "$KEY_CHECKED=1"
            val cursor = dbSql.query(TABLE_NAME, null, selection, null, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEquipment::class.java, "countChecked()", "db")
        }

        return count
    }

    override fun addTest(name: String): Long {
        var id = -1L
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_NAME, name)
            values.put(KEY_IS_BOOT, 1)
            values.put(KEY_DISABLED, 0)
            id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEquipment::class.java, "addTest()", "db")
        } finally {
            dbSql.endTransaction()
        }
        return id
    }

    override fun addLocal(name: String): Long {
        var id = -1L
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_NAME, name)
            values.put(KEY_LOCAL, 1)
            values.put(KEY_CHECKED, 1)
            values.put(KEY_DISABLED, 0)
            id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEquipment::class.java, "addLocal()", "db")
        } finally {
            dbSql.endTransaction()
        }
        return id
    }

    fun add(list: List<DataEquipment>) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            for (value in list) {
                values.clear()
                values.put(KEY_NAME, value.name)
                values.put(KEY_CHECKED, if (value.isChecked) 1 else 0)
                values.put(KEY_LOCAL, if (value.isLocal) 1 else 0)
                values.put(KEY_SERVER_ID, value.serverId)
                values.put(KEY_IS_BOOT, if (value.isBootStrap) 1 else 0)
                values.put(KEY_DISABLED, if (value.disabled) 1 else 0)
                value.id = dbSql.insert(TABLE_NAME, null, values)
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEquipment::class.java, "add(list)", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun add(item: DataEquipment) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.clear()
            values.put(KEY_NAME, item.name)
            values.put(KEY_CHECKED, if (item.isChecked) 1 else 0)
            values.put(KEY_LOCAL, if (item.isLocal) 1 else 0)
            values.put(KEY_SERVER_ID, item.serverId)
            values.put(KEY_IS_BOOT, if (item.isBootStrap) 1 else 0)
            values.put(KEY_DISABLED, if (item.disabled) 1 else 0)
            item.id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEquipment::class.java, "add(item)", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun query(id: Long): DataEquipment? {
        val selection = "$KEY_ROWID=?"
        val selectionArgs = arrayOf(id.toString())
        val list = query(selection, selectionArgs)
        return if (list.size > 0) {
            list[0]
        } else null
    }

    override fun queryEquipmentName(id: Long): String? {
        val equip = query(id)
        return equip?.name
    }

    override fun queryByServerId(server_id: Int): DataEquipment? {
        val selection = "$KEY_SERVER_ID=?"
        val selectionArgs = arrayOf(server_id.toString())
        val list = query(selection, selectionArgs)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    override fun query(): List<DataEquipment> {
        return query(null, null)
    }

    internal fun query(selection: String?, selectionArgs: Array<String>?): List<DataEquipment> {
        val list = ArrayList<DataEquipment>()
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        val idxName = cursor.getColumnIndex(KEY_NAME)
        val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
        val idxChecked = cursor.getColumnIndex(KEY_CHECKED)
        val idxLocal = cursor.getColumnIndex(KEY_LOCAL)
        val idxTest = cursor.getColumnIndex(KEY_IS_BOOT)
        val idxDisabled = cursor.getColumnIndex(KEY_DISABLED)
        var item: DataEquipment
        while (cursor.moveToNext()) {
            item = DataEquipment(
                    cursor.getLong(idxRowId),
                    cursor.getString(idxName),
                    cursor.getShort(idxChecked).toInt() != 0,
                    cursor.getShort(idxLocal).toInt() != 0)
            item.serverId = cursor.getLong(idxServerId)
            item.isBootStrap = cursor.getShort(idxTest).toInt() != 0
            item.disabled = cursor.getShort(idxDisabled).toInt() != 0
            list.add(item)
        }
        cursor.close()
        return list
    }

    override fun queryChecked(): List<DataEquipment> {
        val selection = "$KEY_CHECKED=1"
        return query(selection, null)
    }

    override fun queryIdsChecked(): List<Long> {
        val list = ArrayList<Long>()
        try {
            val columns = arrayOf(KEY_ROWID)
            val selection = "$KEY_CHECKED=1"
            val cursor = dbSql.query(TABLE_NAME, columns, selection, null, null, null, null, null)
            val idxRowId = cursor.getColumnIndex(KEY_ROWID)
            while (cursor.moveToNext()) {
                list.add(cursor.getLong(idxRowId))
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEquipment::class.java, "queryIdsChecked()", "db")
        }
        return list
    }

    override fun query(name: String): Long {
        var id = -1L
        val columns = arrayOf(KEY_ROWID)
        val selection = "$KEY_NAME=?"
        val selectionArgs = arrayOf(name)
        val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null)
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        if (cursor.moveToFirst()) {
            id = cursor.getLong(idxRowId)
        }
        cursor.close()
        return id
    }

    override fun setChecked(ids: List<Long>) {
        clearChecked()
        dbSql.beginTransaction()
        try {
            for (id in ids) {
                val values = ContentValues()
                val where = "$KEY_ROWID=?"
                val whereArgs = arrayOf(id.toString())
                values.put(KEY_CHECKED, 1)
                dbSql.update(TABLE_NAME, values, where, whereArgs)
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEquipment::class.java, "setChecked(list)", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun setChecked(item: DataEquipment, flag: Boolean) {
        item.isChecked = flag
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(java.lang.Long.toString(item.id))
            values.put(KEY_CHECKED, if (item.isChecked) 1 else 0)
            dbSql.update(TABLE_NAME, values, where, whereArgs)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEquipment::class.java, "setChecked(item)", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun clearChecked() {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_CHECKED, 0)
            dbSql.update(TABLE_NAME, values, null, null)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEquipment::class.java, "clearChecked()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun update(item: DataEquipment) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_NAME, item.name)
            values.put(KEY_LOCAL, if (item.isLocal) 1 else 0)
            values.put(KEY_CHECKED, if (item.isChecked) 1 else 0)
            values.put(KEY_IS_BOOT, if (item.isBootStrap) 1 else 0)
            values.put(KEY_DISABLED, if (item.disabled) 1 else 0)
            values.put(KEY_SERVER_ID, item.serverId)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(item.id.toString())
            dbSql.update(TABLE_NAME, values, where, whereArgs)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEquipment::class.java, "update()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    internal fun remove(id: Long) {
        val where = "$KEY_ROWID=?"
        val whereArgs = arrayOf(id.toString())
        dbSql.delete(TABLE_NAME, where, whereArgs)
    }

    override fun removeOrDisable(equip: DataEquipment) {
        if (db.tableCollectionEquipmentEntry.countValues(equip.id) == 0) {
            Timber.tag(TAG).i("remove(${equip.id}, ${equip.name})")
            remove(equip.id)
        } else {
            Timber.tag(TAG).i("disable(${equip.id}, ${equip.name})")
            equip.disabled = true
            update(equip)
        }
    }

    fun clearUploaded() {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_SERVER_ID, 0)
            if (dbSql.update(TABLE_NAME, values, null, null) == 0) {
                Timber.tag(TAG).e("SqlTableEquipment.clearUploaded(): Unable to update entries")
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEquipment::class.java, "clearUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

}
