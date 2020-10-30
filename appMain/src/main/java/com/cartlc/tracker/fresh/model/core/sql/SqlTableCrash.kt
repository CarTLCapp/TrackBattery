/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log

import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableCrash

import timber.log.Timber

/**
 * Created by dug on 8/16/17.
 */

class SqlTableCrash(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
) : TableCrash {

    companion object {
        private val TAG = SqlTableCrash::class.simpleName
        private const val TABLE_NAME = "table_crash"

        private const val KEY_ROWID = "_id"
        private const val KEY_DATE = "date"
        private const val KEY_CODE = "code"
        private const val KEY_MESSAGE = "message"
        private const val KEY_TRACE = "trace"
        private const val KEY_VERSION = "version"
        private const val KEY_UPLOADED = "uploaded"

        fun upgrade10(db: DatabaseTable, dbSql: SQLiteDatabase) {
            try {
                dbSql.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $KEY_VERSION text")
            } catch (ex: Exception) {
                db.reportError(ex, SqlTableCrash::class.java, "upgrade10()", "db")
            }
        }

        private const val MAX_MESSAGE_SIZE = 10000
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
            dbSql.update(TABLE_NAME, values, null, null)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            Timber.tag(TAG).e(ex)
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
        val useMessage = if (message.length > MAX_MESSAGE_SIZE) {
            message.substring(0, MAX_MESSAGE_SIZE)
        } else message
        val useTrace = trace?.let {
            if (trace.length > MAX_MESSAGE_SIZE) {
                trace.substring(0, MAX_MESSAGE_SIZE)
            } else trace
        }
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.clear()
            values.put(KEY_DATE, System.currentTimeMillis())
            values.put(KEY_CODE, code)
            values.put(KEY_MESSAGE, useMessage)
            values.put(KEY_TRACE, useTrace)
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
                Timber.tag(TAG).e("Unable to update tableCrash tableEntry")
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            Log.e("SqlTableCrash", ex.message)
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun delete(line: CrashLine) {
        dbSql.beginTransaction()
        try {
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(java.lang.Long.toString(line.id))
            dbSql.delete(TABLE_NAME, where, whereArgs)
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
