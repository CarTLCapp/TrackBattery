/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.cartlc.tracker.fresh.model.core.data.DataProject
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableProjects

import com.cartlc.tracker.fresh.ui.app.TBApplication

/**
 * Created by dug on 4/17/17.
 */

class SqlTableProjects(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
) : TableProjects {

    companion object {

        private const val TABLE_NAME = "list_projects"

        private const val KEY_ROWID = "_id"
        private const val KEY_NAME = "name" // sub_project if root_project not null
        private const val KEY_ROOT_PROJECT = "root_project"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_DISABLED = "disabled"
        private const val KEY_IS_BOOT = "is_boot_strap"
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_NAME)
        sbuf.append(" text not null, ")
        sbuf.append(KEY_ROOT_PROJECT)
        sbuf.append(" text, ")
        sbuf.append(KEY_SERVER_ID)
        sbuf.append(" integer, ")
        sbuf.append(KEY_DISABLED)
        sbuf.append(" bit default 0, ")
        sbuf.append(KEY_IS_BOOT)
        sbuf.append(" bit default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    fun upgrade17() {
        try {
            dbSql.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $KEY_ROOT_PROJECT text")
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "upgrade17()", "db")
        }
    }

    fun clear() {
        try {
            dbSql.delete(TABLE_NAME, null, null)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "clear()", "db")
        }
    }

    fun remove(value: String) {
        try {
            val where = "$KEY_NAME=?"
            val whereArgs = arrayOf(value)
            dbSql.delete(TABLE_NAME, where, whereArgs)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "remove(value)", "db")
        }
    }

    fun remove(id: Long) {
        try {
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(id.toString())
            dbSql.delete(TABLE_NAME, where, whereArgs)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "remove(id)", "db")
        }
    }

    override fun addTest(item: String): Long {
        var id = -1L
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_NAME, item)
            values.put(KEY_IS_BOOT, 1)
            values.put(KEY_DISABLED, 0)
            id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "addTest()", "db")
        } finally {
            dbSql.endTransaction()
        }
        return id
    }

    override fun add(rootProject: String, subProject: String, serverId: Int, disabled: Boolean): Long {
        var id = -1L
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_ROOT_PROJECT, rootProject)
            values.put(KEY_NAME, subProject)
            values.put(KEY_SERVER_ID, serverId)
            values.put(KEY_DISABLED, if (disabled) 1 else 0)
            id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "add(rootProject,subProject)", "db")
        } finally {
            dbSql.endTransaction()
        }
        return id
    }

    override fun add(rootProject: String): Long {
        var id = -1L
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_ROOT_PROJECT, rootProject)
            values.put(KEY_NAME, "")
            values.put(KEY_SERVER_ID, 0)
            values.put(KEY_DISABLED, 0)
            id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "add(rootProject)", "db")
        } finally {
            dbSql.endTransaction()
        }
        return id
    }

    override fun update(project: DataProject): Long {
        val ret: Long = -1
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_NAME, project.subProject)
            values.put(KEY_ROOT_PROJECT, project.rootProject)
            values.put(KEY_SERVER_ID, project.serverId)
            values.put(KEY_DISABLED, if (project.disabled) 1 else 0)
            values.put(KEY_IS_BOOT, if (project.isBootStrap) 1 else 0)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(project.id.toString())
            if (dbSql.update(TABLE_NAME, values, where, whereArgs) == 0) {
                project.id = dbSql.insert(TABLE_NAME, null, values)
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "update()", "db")
        } finally {
            dbSql.endTransaction()
        }
        return ret
    }

    fun count(): Int {
        var count = 0
        try {
            val cursor = dbSql.query(TABLE_NAME, null, null, null, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "count()", "db")
        }
        return count
    }

    /**
     * Returns list of project names.
     *
     * If the project has a rootProject, the return string will be on the form:
     * $rootProject - $subProject
     */
    override fun query(activeOnly: Boolean): List<DataProject> {
        return query(null, null)
    }

    /**
     * Return project name from id in the form of <Root Project>, <Sub Project>
     */
    override fun queryProjectName(id: Long): Pair<String, String>? {
        try {
            val columns = arrayOf(KEY_NAME, KEY_ROOT_PROJECT)
            val selection = "$KEY_ROWID=?"
            val selectionArgs = arrayOf(id.toString())
            val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
            val idxValue = cursor.getColumnIndex(KEY_NAME)
            val idxRootProject = cursor.getColumnIndex(KEY_ROOT_PROJECT)
            if (cursor.moveToFirst()) {
                val subName = cursor.getString(idxValue)
                val rootName = cursor.getString(idxRootProject)
                cursor.close()
                return Pair(rootName, subName)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "queryProjectName()", "db")
        }
        return null
    }

    override fun queryByServerId(server_id: Int): DataProject? {
        val selection = "$KEY_SERVER_ID=?"
        val selectionArgs = arrayOf(server_id.toString())
        val list = query(selection, selectionArgs)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    override fun queryById(id: Long): DataProject? {
        val selection = "$KEY_ROWID=?"
        val selectionArgs = arrayOf(id.toString())
        val list = query(selection, selectionArgs)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    override fun isDisabled(id: Long): Boolean {
        val project = queryById(id) ?: return true
        return project.disabled
    }

    override fun queryByName(rootName: String, subProject: String): DataProject? {
        val selection = "$KEY_ROOT_PROJECT=? AND $KEY_NAME=?"
        val selectionArgs = arrayOf(rootName, subProject)
        val list = query(selection, selectionArgs)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    override fun queryByName(rootName: String): DataProject? {
        val selection = "$KEY_ROOT_PROJECT=? AND $KEY_NAME=?"
        val selectionArgs = arrayOf(rootName, "")
        val list = query(selection, selectionArgs)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    private fun query(selection: String?, selectionArgs: Array<String>?): List<DataProject> {
        val list = mutableListOf<DataProject>()
        try {
            val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)
            val idxValue = cursor.getColumnIndex(KEY_NAME)
            val idxRowId = cursor.getColumnIndex(KEY_ROWID)
            val idxRootProject = cursor.getColumnIndex(KEY_ROOT_PROJECT)
            val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
            val idxDisabled = cursor.getColumnIndex(KEY_DISABLED)
            val idxTest = cursor.getColumnIndex(KEY_IS_BOOT)
            while (cursor.moveToNext()) {
                val project = DataProject()
                project.subProject = cursor.getString(idxValue)
                project.rootProject = cursor.getString(idxRootProject)
                project.disabled = cursor.getShort(idxDisabled).toInt() != 0
                project.isBootStrap = cursor.getShort(idxTest).toInt() != 0
                project.serverId = cursor.getInt(idxServerId)
                project.id = cursor.getLong(idxRowId)
                list.add(project)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "query()", "db")
        }
        return list
    }

    override fun queryRootProjectNames(): List<String> {
        val projects = query(true)
        val names = mutableListOf<String>()
        for (project in projects) {
            project.rootProject?.let { name ->
                if (!names.contains(name)) {
                    names.add(name)
                }
            }
        }
        names.sort()
        return names
    }

    override fun querySubProjects(rootName: String): List<DataProject> {
        val selection = "$KEY_ROOT_PROJECT=?"
        val selectionArgs = arrayOf(rootName)
        return query(selection, selectionArgs)
    }

    override fun queryProjectId(rootName: String, subProject: String): Long {
        var rowId = -1L
        try {
            val columns = arrayOf(KEY_ROWID)
            val selection = "$KEY_ROOT_PROJECT=? AND $KEY_NAME=?"
            val selectionArgs = arrayOf(rootName, subProject)
            val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
            val idxValue = cursor.getColumnIndex(KEY_ROWID)
            if (cursor.moveToFirst()) {
                rowId = cursor.getLong(idxValue)
            }
            cursor.close()
        } catch (ex: SQLiteException) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "queryProjectId()", "$rootName - $subProject")
        }
        return rowId
    }

    override fun queryRootProjectId(rootName: String): Long {
        var rowId = -1L
        try {
            val columns = arrayOf(KEY_ROWID)
            val selection = "$KEY_ROOT_PROJECT IS NULL AND $KEY_NAME=?"
            val selectionArgs = arrayOf(rootName)
            val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
            val idxValue = cursor.getColumnIndex(KEY_ROWID)
            if (cursor.moveToFirst()) {
                rowId = cursor.getLong(idxValue)
            }
            cursor.close()
        } catch (ex: SQLiteException) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "queryRootProjectId()", "$rootName")
        }
        return rowId
    }

    override fun hasServerId(rootName: String, subProject: String): Boolean {
        var valid = false
        try {
            val columns = arrayOf(KEY_SERVER_ID)
            val selection = "$KEY_ROOT_PROJECT=? AND $KEY_NAME=?"
            val selectionArgs = arrayOf(rootName, subProject)
            val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
            val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
            if (cursor.moveToFirst()) {
                val serverId = cursor.getLong(idxServerId)
                valid = serverId > 0
            }
            cursor.close()
        } catch (ex: SQLiteException) {
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "hasServerId()", "$rootName - $subProject")
        }
        return valid
    }

    override fun removeOrDisable(project: DataProject) {
        if (db.tableEntry.countProjects(project.id) == 0 && db.tableProjectAddressCombo.countProjects(project.id) == 0) {
            // No entries for this, so just remove.
            remove(project.id)
        } else {
            project.disabled = true
            update(project)
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
            TBApplication.ReportError(ex, SqlTableProjects::class.java, "clearUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

}
