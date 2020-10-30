/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableCollectionNoteEntry

import com.cartlc.tracker.fresh.ui.app.TBApplication
import timber.log.Timber

import java.util.ArrayList

/**
 * Created by dug on 5/16/17.
 */
class SqlTableCollectionNoteEntry(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
) : TableCollectionNoteEntry {

    companion object {
        private const val TABLE_NAME = "note_entry_collection"

        private const val KEY_ROWID = "_id"
        private const val KEY_COLLECTION_ID = "collection_id"
        private const val KEY_NOTE_ID = "note_id"
        private const val KEY_VALUE = "value"
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_COLLECTION_ID)
        sbuf.append(" long, ")
        sbuf.append(KEY_NOTE_ID)
        sbuf.append(" long, ")
        sbuf.append(KEY_VALUE)
        sbuf.append(" text)")

        dbSql.execSQL(sbuf.toString())
    }

    override fun countNotes(noteId: Long): Int {
        var count = 0
        try {
            val where = "$KEY_NOTE_ID=?"
            val whereArgs = arrayOf(noteId.toString())
            val cursor = dbSql.query(TABLE_NAME, null, where, whereArgs, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollectionNoteEntry::class.java, "countNotes(id)", "db")
        }

        return count
    }

    // There are TWO note tables. One is TableCollectionNoteProject which stores the
    // defined notes for each project. The other is this one which stores the values.
    //
    // The values are stored right now in the SqlTableNote table which is represented by the incoming
    // notes. We want them to also be stored now into this table.
    //
    // The reason we need two tables for the values, is that each time a new tableEntry is created
    // a new set of values for the notes are needed as well. This new set of values is stored
    // in this table. The SqlTableNote table only holds the values temporarily while they are being entered.
    override fun save(collectionId: Long, notes: List<DataNote>) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            for (note in notes) {
                values.clear()
                values.put(KEY_COLLECTION_ID, collectionId)
                values.put(KEY_NOTE_ID, note.id)
                values.put(KEY_VALUE, note.value)
                query(collectionId, note.id)?.let { existing ->
                    val where = "$KEY_ROWID=?"
                    val whereArgs = arrayOf(existing.id.toString())
                    if (dbSql.update(TABLE_NAME, values, where, whereArgs) == 0) {
                        dbSql.insert(TABLE_NAME, null, values)
                    }
                } ?: run {
                    dbSql.insert(TABLE_NAME, null, values)
                }
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollectionNoteEntry::class.java, "saveUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun updateValue(collectionId: Long, note: DataNote) {
        dbSql.beginTransaction()
        try {
            val where = "$KEY_NOTE_ID=? AND $KEY_COLLECTION_ID=?"
            val whereArgs = arrayOf(note.id.toString(), collectionId.toString())
            val values = ContentValues()
            values.put(KEY_VALUE, note.value)
            dbSql.update(TABLE_NAME, values, where, whereArgs)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollectionNoteEntry::class.java, "saveUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    // Get list of notes the associated collection id in this table along with the current values stored in this table.
    override fun query(collectionId: Long): List<DataNote> {
        val list = ArrayList<DataNote>()
        try {
            val columns = arrayOf(KEY_NOTE_ID, KEY_VALUE)
            val selection = "$KEY_COLLECTION_ID =?"
            val selectionArgs = arrayOf(collectionId.toString())
            val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null)
            val idxNoteId = cursor.getColumnIndex(KEY_NOTE_ID)
            val idxValueId = cursor.getColumnIndex(KEY_VALUE)
            var note: DataNote?
            while (cursor.moveToNext()) {
                note = db.tableNote.query(cursor.getLong(idxNoteId)) // Fill out with original values.
                note!!.value = cursor.getString(idxValueId) // override
                list.add(note)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollectionNoteEntry::class.java, "query()", "db")
        }
        return list
    }

    override fun query(collectionId: Long, noteId: Long): DataNote? {
        var note: DataNote? = null
        try {
            val columns = arrayOf(KEY_VALUE)
            val selection = "$KEY_COLLECTION_ID=? AND $KEY_NOTE_ID=?"
            val selectionArgs = arrayOf(collectionId.toString(), noteId.toString())
            val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null)
            val idxValueId = cursor.getColumnIndex(KEY_VALUE)
            if (cursor.moveToNext()) {
                note = db.tableNote.query(noteId) // Fill out with original values.
                note!!.value = cursor.getString(idxValueId) // override
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollectionNoteEntry::class.java, "query()", "db")
        }
        return note
    }

    override fun remove(collection_id: Long) {
        val where = "$KEY_COLLECTION_ID=?"
        val whereArgs = arrayOf(collection_id.toString())
        dbSql.delete(TABLE_NAME, where, whereArgs)
    }

}
