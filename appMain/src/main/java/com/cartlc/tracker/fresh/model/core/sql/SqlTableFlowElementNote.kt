/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataFlowElementNote
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableFlowElementNote
import com.cartlc.tracker.fresh.ui.app.TBApplication

/**
 * Created by dug on 8/31/17.
 */

class SqlTableFlowElementNote(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
) : TableFlowElementNote {

    companion object {

        private const val TABLE_NAME = "table_flow_element_note"

        private const val KEY_ROWID = "_id"
        private const val KEY_FLOW_ELEMENT_ID = "flow_element_id"
        private const val KEY_NOTE_ID = "note_id"
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_FLOW_ELEMENT_ID)
        sbuf.append(" integer default 0, ")
        sbuf.append(KEY_NOTE_ID)
        sbuf.append(" int default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    override fun add(item: DataFlowElementNote): Long {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_FLOW_ELEMENT_ID, item.flowElementId)
            values.put(KEY_NOTE_ID, item.noteId)
            item.id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableFlowElementNote::class.java, "add(item)", "db")
        } finally {
            dbSql.endTransaction()
        }
        return item.id
    }

    override fun query(): List<DataFlowElementNote> {
        return query(null, null)
    }

    private fun query(selection: String?, selectionArgs: Array<String>?): List<DataFlowElementNote> {
        val list = ArrayList<DataFlowElementNote>()
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        val idxFlowElementId = cursor.getColumnIndex(KEY_FLOW_ELEMENT_ID)
        val idxNoteId = cursor.getColumnIndex(KEY_NOTE_ID)
        var item: DataFlowElementNote
        while (cursor.moveToNext()) {
            item = DataFlowElementNote(
                    cursor.getLong(idxRowId),
                    cursor.getLong(idxFlowElementId),
                    cursor.getLong(idxNoteId))
            list.add(item)
        }
        cursor.close()
        return list
    }

    override fun query(flowElementId: Long, noteId: Long): DataFlowElementNote? {
        val selection = "$KEY_FLOW_ELEMENT_ID=? AND $KEY_NOTE_ID=?"
        val selectionArgs = arrayOf(flowElementId.toString(), noteId.toString())
        val list = query(selection, selectionArgs)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    override fun query(flowElementId: Long): List<DataFlowElementNote> {
        val selection = "$KEY_FLOW_ELEMENT_ID=?"
        val selectionArgs = arrayOf(flowElementId.toString())
        return query(selection, selectionArgs)
    }

    override fun hasNotes(flowElementId: Long): Boolean {
        return countNotes(flowElementId) > 0
    }

    override fun countNotes(flowElementId: Long): Int {
        val selection = "$KEY_FLOW_ELEMENT_ID=?"
        val selectionArgs = arrayOf(flowElementId.toString())
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        val count = cursor.count
        cursor.close()
        return count
    }

    override fun queryNotes(flowElementId: Long): List<DataNote> {
        val items = query(flowElementId)
        val list = mutableListOf<DataNote>()
        for (item in items) {
            db.tableNote.query(item.noteId)?.let { note ->
                list.add(note)
            }
        }
        return list
    }

    override fun update(item: DataFlowElementNote) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_FLOW_ELEMENT_ID, item.flowElementId)
            values.put(KEY_NOTE_ID, item.noteId)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(item.id.toString())
            dbSql.update(TABLE_NAME, values, where, whereArgs)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableFlowElementNote::class.java, "update()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun remove(item: DataFlowElementNote) {
        val where = "$KEY_ROWID=?"
        val whereArgs = arrayOf(item.id.toString())
        dbSql.delete(TABLE_NAME, where, whereArgs)
    }

    override fun toString(flowElementId: Long): String {
        val sbuf = StringBuilder()
        val list = query(flowElementId)
        sbuf.append("[")
        for (elementNote in list) {
            if (sbuf.length > 1) {
                sbuf.append(", ")
            }
            val note = db.tableNote.query(elementNote.noteId)
            note?.let {
                sbuf.append(note.name)
            } ?: run {
                sbuf.append("NOT FOUND ${elementNote.noteId}")
            }
        }
        sbuf.append("]")
        return sbuf.toString()
    }

}
