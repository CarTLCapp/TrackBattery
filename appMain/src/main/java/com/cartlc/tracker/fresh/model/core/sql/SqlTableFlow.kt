/*
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataFlow
import com.cartlc.tracker.fresh.model.core.data.DataProject
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableFlow
import com.cartlc.tracker.fresh.ui.app.TBApplication

/**
 * Created by dug on 8/31/17.
 */

class SqlTableFlow(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
) : TableFlow {

    companion object {
        private const val TABLE_NAME = "table_flow"

        private const val KEY_ROWID = "_id"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_SUB_PROJECT_ID = "sub_project_id"
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_SERVER_ID)
        sbuf.append(" integer, ")
        sbuf.append(KEY_SUB_PROJECT_ID)
        sbuf.append(" int default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    override fun add(item: DataFlow): Long {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_SERVER_ID, item.serverId)
            values.put(KEY_SUB_PROJECT_ID, item.subProjectId)
            item.id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableFlow::class.java, "add(item)", "db")
        } finally {
            dbSql.endTransaction()
        }
        return item.id
    }

    override fun query(): List<DataFlow> {
        return query(null, null)
    }

    private fun query(selection: String?, selectionArgs: Array<String>?): List<DataFlow> {
        val list = ArrayList<DataFlow>()
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
        val idxSubProjectId = cursor.getColumnIndex(KEY_SUB_PROJECT_ID)
        var item: DataFlow
        while (cursor.moveToNext()) {
            item = DataFlow(
                    cursor.getLong(idxRowId),
                    cursor.getInt(idxServerId),
                    cursor.getLong(idxSubProjectId))
            list.add(item)
        }
        cursor.close()
        return list
    }

    override fun queryByServerId(server_id: Int): DataFlow? {
        val selection = "$KEY_SERVER_ID=?"
        val selectionArgs = arrayOf(server_id.toString())
        val list = query(selection, selectionArgs)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    override fun queryBySubProjectId(project_id: Int): DataFlow? {
        val selection = "$KEY_SUB_PROJECT_ID=?"
        val selectionArgs = arrayOf(project_id.toString())
        val list = query(selection, selectionArgs)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    override fun filterHasFlow(incoming: List<DataProject>): List<DataProject> {
        val list = mutableListOf<DataProject>()
        for (project in incoming) {
            if (count(project.id) > 0) {
                list.add(project)
            }
        }
        return list
    }

    private fun count(project_id: Long): Int {
        val selection = "$KEY_SUB_PROJECT_ID=?"
        val selectionArgs = arrayOf(project_id.toString())
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        val count = cursor.count
        cursor.close()
        return count
    }

    override fun update(item: DataFlow) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_SERVER_ID, item.serverId)
            values.put(KEY_SUB_PROJECT_ID, item.subProjectId)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(item.id.toString())
            dbSql.update(TABLE_NAME, values, where, whereArgs)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableFlow::class.java, "update()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun remove(item: DataFlow) {
        val where = "$KEY_ROWID=?"
        val whereArgs = arrayOf(item.id.toString())
        dbSql.delete(TABLE_NAME, where, whereArgs)
    }

    override fun toString(): String {
        val sbuf = StringBuilder()
        for (flow in query()) {
            val project = db.tableProjects.queryById(flow.subProjectId)
            project?.let {
                sbuf.append("Project \"${project.dashName}\"")
            } ?: run {
                sbuf.append("Unknown project with ID ${flow.subProjectId}")
            }
            sbuf.append(": ")
            sbuf.append(db.tableFlowElement.toString(flow.id))
            sbuf.append("\n")
        }
        return sbuf.toString()
    }
}
