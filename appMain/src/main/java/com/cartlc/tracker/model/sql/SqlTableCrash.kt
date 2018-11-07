/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log

import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.model.table.TableCrash

import timber.log.Timber

/**
 * Created by dug on 8/16/17.
 */

class SqlTableCrash(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
): TableCrash {

    companion object {

        internal val TABLE_NAME = "table_crash"

        internal val KEY_ROWID = "_id"
        internal val KEY_DATE = "date"
        internal val KEY_CODE = "code"
        internal val KEY_MESSAGE = "message"
        internal val KEY_TRACE = "trace"
        internal val KEY_VERSION = "version"
        internal val KEY_UPLOADED = "uploaded"

        fun upgrade10(db: DatabaseTable, dbSql: SQLiteDatabase) {
            try {
                dbSql.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $KEY_VERSION text")
            } catch (ex: Exception) {
                db.reportError(ex, SqlTableCrash::class.java, "upgrade10()", "db")
            }
        }
    }

    class CrashLine {
        var id: Long = 0
        var code: Int = 0
        var message: String? = null
        var trace: String? = null
        var version: String? = null
        var date: Long = 0
        var uploaded: Boolean = false
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
        sbuf.append(KEY_CODE)
        sbuf.append(" smallint, ")
        sbuf.append(KEY_MESSAGE)
        sbuf.append(" text, ")
        sbuf.append(KEY_TRACE)
        sbuf.append(" text, ")
        sbuf.append(KEY_VERSION)
        sbuf.append(" text, ")
        sbuf.append(KEY_UPLOADED)
        sbuf.append(" bit default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    fun clearUploaded() {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_UPLOADED, 0)
            if (dbSql.update(TABLE_NAME, values, null, null) == 0) {
                val cursor = dbSql.query(TABLE_NAME, null, null, null, null, null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst() && cursor.count > 0) {
                        Timber.e("SqlTableCrash.clearUploaded(): Unable to update tableCrash entries")
                    }
                    cursor.close()
                }
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            Timber.e(ex)
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun queryNeedsUploading(): List<CrashLine> {
        val orderBy = "$KEY_DATE DESC"
        val where = "$KEY_UPLOADED=0"
        val cursor = dbSql.query(TABLE_NAME, null, where, null, null, null, orderBy, null)
        val idxRow = cursor.getColumnIndex(KEY_ROWID)
        val idxDate = cursor.getColumnIndex(KEY_DATE)
        val idxCode = cursor.getColumnIndex(KEY_CODE)
        val idxMessage = cursor.getColumnIndex(KEY_MESSAGE)
        val idxTrace = cursor.getColumnIndex(KEY_TRACE)
        val idxVersion = cursor.getColumnIndex(KEY_VERSION)
        val idxUploaded = cursor.getColumnIndex(KEY_UPLOADED)
        val lines = ArrayList<CrashLine>()
        while (cursor.moveToNext()) {
            val line = CrashLine()
            line.id = cursor.getLong(idxRow)
            line.date = cursor.getLong(idxDate)
            line.code = cursor.getShort(idxCode).toInt()
            line.message = cursor.getString(idxMessage)
            line.version = cursor.getString(idxVersion)
            line.trace = cursor.getString(idxTrace)
            line.uploaded = cursor.getShort(idxUploaded).toInt() != 0
            lines.add(line)
        }
        cursor.close()
        return lines
    }

    override fun message(code: Int, message: String, trace: String?) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.clear()
            values.put(KEY_DATE, System.currentTimeMillis())
            values.put(KEY_CODE, code)
            values.put(KEY_MESSAGE, message)
            values.put(KEY_TRACE, trace)
            values.put(KEY_VERSION, db.appVersion)
            dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            Log.e("SqlTableCrash", ex.message)
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun setUploaded(line: CrashLine) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_UPLOADED, 1)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(java.lang.Long.toString(line.id))
            if (dbSql.update(TABLE_NAME, values, where, whereArgs) == 0) {
                Timber.e("Unable to update tableCrash tableEntry")
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            Log.e("SqlTableCrash", ex.message)
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun info(message: String) {
        message(Log.INFO, message, null)
    }

}
