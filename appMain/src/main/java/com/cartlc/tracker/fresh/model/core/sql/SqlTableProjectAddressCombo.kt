/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableProjectAddressCombo

import com.cartlc.tracker.fresh.ui.app.TBApplication

import timber.log.Timber

/**
 * Created by dug on 5/10/17.
 */

class SqlTableProjectAddressCombo(
        private val db: DatabaseTable,
        private val sqlDb: SQLiteDatabase
) : TableProjectAddressCombo {

    companion object {
        private val TAG = SqlTableProjectAddressCombo::class.simpleName

        private const val TABLE_NAME = "list_project_address_combo"

        private const val KEY_ROWID = "_id"
        private const val KEY_PROJECT_ID = "project_id"
        private const val KEY_ADDRESS_ID = "address_id"
        private const val KEY_LAST_USED = "last_used"
    }

    fun clear() {
        try {
            sqlDb.delete(TABLE_NAME, null, null)
        } catch (ex: Exception) {
        }
    }

    fun drop() {
        sqlDb.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_PROJECT_ID)
        sbuf.append(" long, ")
        sbuf.append(KEY_ADDRESS_ID)
        sbuf.append(" long, ")
        sbuf.append(KEY_LAST_USED)
        sbuf.append(" long)")
        sqlDb.execSQL(sbuf.toString())
    }

    override fun add(projectGroup: DataProjectAddressCombo): Long {
        sqlDb.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_PROJECT_ID, projectGroup.projectNameId)
            values.put(KEY_ADDRESS_ID, projectGroup.addressId)
            values.put(KEY_LAST_USED, System.currentTimeMillis())
            projectGroup.id = sqlDb.insert(TABLE_NAME, null, values)
            sqlDb.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjectAddressCombo::class.java, "add()", "sqlDb")
        } finally {
            sqlDb.endTransaction()
        }
        return projectGroup.id
    }

    override fun save(projectGroup: DataProjectAddressCombo): Boolean {
        sqlDb.beginTransaction()
        var success = false
        try {
            val values = ContentValues()
            values.put(KEY_PROJECT_ID, projectGroup.projectNameId)
            values.put(KEY_ADDRESS_ID, projectGroup.addressId)
            values.put(KEY_LAST_USED, System.currentTimeMillis())
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(java.lang.Long.toString(projectGroup.id))
            sqlDb.update(TABLE_NAME, values, where, whereArgs)
            sqlDb.setTransactionSuccessful()
            success = true
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjectAddressCombo::class.java, "save()", "sqlDb")
        } finally {
            sqlDb.endTransaction()
        }
        return success
    }

    /**
     * If there are enteredOther project groups that are exactly the same as the passed,
     * then merge all entries into this one and delete the enteredOther.
     *
     * @param projectGroup
     */
    override fun mergeIdenticals(projectGroup: DataProjectAddressCombo) {
        val identicals = queryProjectGroupIds(projectGroup.projectNameId, projectGroup.addressId)
        if (identicals.size <= 1) {
            return
        }
        identicals.remove(projectGroup.id)
        for (other_id in identicals) {
            val entries = db.tableEntry.queryForProjectAddressCombo(other_id)
            Timber.tag(TAG).i("Found ${entries.size} entries with matching combo id $other_id to ${projectGroup.id}")
            for (entry in entries) {
                entry.projectAddressCombo = projectGroup
                entry.uploadedMaster = false
                db.tableEntry.saveProjectAddressCombo(entry)
            }
            remove(other_id)
        }
    }

    override fun count(): Int {
        var count = 0
        try {
            val cursor = sqlDb.query(true, TABLE_NAME, null, null, null, null, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjectAddressCombo::class.java, "count()", "sqlDb")
        }
        return count
    }

    override fun countAddress(addressId: Long): Int {
        var count = 0
        try {
            val selection = "$KEY_ADDRESS_ID=?"
            val selectionArgs = arrayOf(addressId.toString())
            val cursor = sqlDb.query(true, TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjectAddressCombo::class.java, "countAddress()", "sqlDb")
        }
        return count
    }

    override fun countProjects(projectId: Long): Int {
        var count = 0
        try {
            val selection = "$KEY_PROJECT_ID=?"
            val selectionArgs = arrayOf(projectId.toString())
            val cursor = sqlDb.query(true, TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjectAddressCombo::class.java, "countProjects()", "sqlDb")
        }
        return count
    }

    override fun query(): List<DataProjectAddressCombo> {
        val list = ArrayList<DataProjectAddressCombo>()
        try {
            val columns = arrayOf(KEY_ROWID, KEY_PROJECT_ID, KEY_ADDRESS_ID)
            val cursor = sqlDb.query(true, TABLE_NAME, columns, null, null, null, null, null, null)
            val idxRowId = cursor.getColumnIndex(KEY_ROWID)
            val idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID)
            val idxAddressId = cursor.getColumnIndex(KEY_ADDRESS_ID)
            var item: DataProjectAddressCombo
            while (cursor.moveToNext()) {
                val projectId = cursor.getLong(idxProjectId)
                if (!db.tableProjects.isDisabled(projectId)) {
                    val id = cursor.getLong(idxRowId)
                    val addressId = cursor.getLong(idxAddressId)
                    item = DataProjectAddressCombo(db, id, projectId, addressId)
                    list.add(item)
                }
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjectAddressCombo::class.java, "query()", "sqlDb")
        }
        return list
    }

    override fun query(id: Long): DataProjectAddressCombo? {
        var item: DataProjectAddressCombo? = null
        try {
            val columns = arrayOf(KEY_PROJECT_ID, KEY_ADDRESS_ID)
            val selection = "$KEY_ROWID=?"
            val selectionArgs = arrayOf(id.toString())
            val cursor = sqlDb.query(true, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null)
            val idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID)
            val idxAddressId = cursor.getColumnIndex(KEY_ADDRESS_ID)
            if (cursor.moveToFirst()) {
                item = DataProjectAddressCombo(db, id,
                        cursor.getLong(idxProjectId),
                        cursor.getLong(idxAddressId))
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjectAddressCombo::class.java, "query(id)", "sqlDb")
        }
        return item
    }

    override fun queryProjectGroupId(projectNameId: Long, addressId: Long): Long {
        val ids = queryProjectGroupIds(projectNameId, addressId)
        return if (ids.size >= 1) {
            ids[0]
        } else -1
    }

    fun queryProjectGroupIds(projectNameId: Long, addressId: Long): ArrayList<Long> {
        val ids = ArrayList<Long>()
        try {
            val columns = arrayOf(KEY_ROWID)
            val sbuf = StringBuilder()
            sbuf.append(KEY_PROJECT_ID)
            sbuf.append("=? AND ")
            sbuf.append(KEY_ADDRESS_ID)
            sbuf.append("=?")
            val selection = sbuf.toString()
            val selectionArgs = arrayOf(projectNameId.toString(), addressId.toString())
            val cursor = sqlDb.query(true, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null)
            val idxRowId = cursor.getColumnIndex(KEY_ROWID)
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(idxRowId))
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjectAddressCombo::class.java, "query(project,address)", "sqlDb")
        }
        return ids
    }

    override fun updateUsed(id: Long) {
        try {
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(id.toString())
            val values = ContentValues()
            values.put(KEY_LAST_USED, System.currentTimeMillis())
            sqlDb.update(TABLE_NAME, values, where, whereArgs)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjectAddressCombo::class.java, "updateUsed()", "sqlDb")
        }
    }

    override fun remove(combo_id: Long) {
        val where = "$KEY_ROWID=?"
        val whereArgs = arrayOf(combo_id.toString())
        sqlDb.delete(TABLE_NAME, where, whereArgs)
    }

}
