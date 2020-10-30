/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataAddress
import com.cartlc.tracker.fresh.model.core.data.DataStates
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableAddress

import com.cartlc.tracker.fresh.ui.app.TBApplication

import java.util.Locale

import timber.log.Timber

/**
 * Created by dug on 5/10/17.
 */
class SqlTableAddress(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
) : TableAddress {

    companion object {
        private const val TABLE_NAME = "list_address"

        private const val KEY_ROWID = "_id"
        private const val KEY_COMPANY = "company"
        private const val KEY_STREET = "street"
        private const val KEY_CITY = "city"
        private const val KEY_STATE = "state"
        private const val KEY_ZIPCODE = "zipcode"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_DISABLED = "disabled"
        private const val KEY_LOCAL = "local"
        private const val KEY_IS_BOOT = "is_boot_strap"
    }

    internal inner class SelectionArgs(company: String?, street: String?, city: String?, state: String?, zipcode: String?) {

        var selection: String? = null
        var selectionArgs: Array<String>? = null

        init {
            val sbuf = StringBuilder()
            val args = mutableListOf<String>()
            if (company != null && company.isNotEmpty()) {
                sbuf.append(KEY_COMPANY)
                sbuf.append("=?")
                args.add(company)
            }
            if (state != null && state.isNotEmpty()) {
                if (sbuf.isNotEmpty()) {
                    sbuf.append(" AND ")
                }
                val dstate = DataStates[state]
                if (dstate != null) {
                    sbuf.append("(")
                    sbuf.append("LOWER(")
                    sbuf.append(KEY_STATE)
                    sbuf.append(")")
                    sbuf.append("=?")
                    args.add(dstate.full.toLowerCase(Locale.getDefault()))

                    sbuf.append(" OR ")

                    sbuf.append("LOWER(")
                    sbuf.append(KEY_STATE)
                    sbuf.append(")")
                    sbuf.append("=?")
                    args.add(dstate.abbr.toLowerCase(Locale.getDefault()))

                    sbuf.append(")")
                } else {
                    sbuf.append(KEY_STATE)
                    sbuf.append("=?")
                    args.add(state)
                }
            }
            if (city != null && city.isNotEmpty()) {
                if (sbuf.isNotEmpty()) {
                    sbuf.append(" AND ")
                }
                sbuf.append("LOWER(")
                sbuf.append(KEY_CITY)
                sbuf.append(")")
                sbuf.append("=?")

                args.add(city.toLowerCase(Locale.getDefault()))
            }
            if (street != null && street.isNotEmpty()) {
                if (sbuf.isNotEmpty()) {
                    sbuf.append(" AND ")
                }
                sbuf.append("LOWER(")
                sbuf.append(KEY_STREET)
                sbuf.append(")")
                sbuf.append("=?")
                args.add(street.toLowerCase(Locale.getDefault()))
            }
            if (zipcode != null && zipcode.isNotEmpty()) {
                if (sbuf.isNotEmpty()) {
                    sbuf.append(" AND ")
                }
                sbuf.append(KEY_ZIPCODE)
                sbuf.append("=?")
                args.add(zipcode)
            }
            if (sbuf.isNotEmpty()) {
                selection = sbuf.toString()
                selectionArgs = args.toTypedArray()
            } else {
                selection = null
                selectionArgs = null
            }
        }
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
        sbuf.append(KEY_COMPANY)
        sbuf.append(" text, ")
        sbuf.append(KEY_STREET)
        sbuf.append(" text, ")
        sbuf.append(KEY_CITY)
        sbuf.append(" text, ")
        sbuf.append(KEY_STATE)
        sbuf.append(" text, ")
        sbuf.append(KEY_ZIPCODE)
        sbuf.append(" text, ")
        sbuf.append(KEY_SERVER_ID)
        sbuf.append(" int, ")
        sbuf.append(KEY_DISABLED)
        sbuf.append(" bit default 0, ")
        sbuf.append(KEY_LOCAL)
        sbuf.append(" bit default 0, ")
        sbuf.append(KEY_IS_BOOT)
        sbuf.append(" bit default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    override fun add(list: List<DataAddress>) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            for (address in list) {
                values.clear()
                values.put(KEY_COMPANY, address.company)
                values.put(KEY_STREET, address.street)
                values.put(KEY_CITY, address.city)
                values.put(KEY_STATE, address.state)
                values.put(KEY_ZIPCODE, address.zipcode)
                values.put(KEY_SERVER_ID, address.serverId)
                values.put(KEY_DISABLED, if (address.disabled) 1 else 0)
                values.put(KEY_LOCAL, if (address.isLocal) 1 else 0)
                values.put(KEY_IS_BOOT, if (address.isBootStrap) 1 else 0)
                dbSql.insert(TABLE_NAME, null, values)
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableAddress::class.java, "add(list)", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun add(address: DataAddress): Long {
        var id = -1L
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.clear()
            values.put(KEY_COMPANY, address.company)
            values.put(KEY_STREET, address.street)
            values.put(KEY_CITY, address.city)
            values.put(KEY_STATE, address.state)
            values.put(KEY_ZIPCODE, address.zipcode)
            values.put(KEY_SERVER_ID, address.serverId)
            values.put(KEY_DISABLED, if (address.disabled) 1 else 0)
            values.put(KEY_LOCAL, if (address.isLocal) 1 else 0)
            values.put(KEY_IS_BOOT, if (address.isBootStrap) 1 else 0)
            id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableAddress::class.java, "add(address)", "db")
        } finally {
            dbSql.endTransaction()
        }
        return id
    }

    fun add(company: String): Long {
        var id = -1L
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.clear()
            values.put(KEY_COMPANY, company)
            values.put(KEY_LOCAL, 1)
            values.put(KEY_DISABLED, 0)
            values.put(KEY_IS_BOOT, 0)
            id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableAddress::class.java, "add(company)", "db")
        } finally {
            dbSql.endTransaction()
        }
        return id
    }

    override fun update(address: DataAddress) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.clear()
            values.put(KEY_COMPANY, address.company)
            values.put(KEY_STREET, address.street)
            values.put(KEY_CITY, address.city)
            values.put(KEY_STATE, address.state)
            values.put(KEY_ZIPCODE, address.zipcode)
            values.put(KEY_SERVER_ID, address.serverId)
            values.put(KEY_DISABLED, if (address.disabled) 1 else 0)
            values.put(KEY_LOCAL, if (address.isLocal) 1 else 0)
            values.put(KEY_IS_BOOT, if (address.isBootStrap) 1 else 0)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(java.lang.Long.toString(address.id))
            dbSql.update(TABLE_NAME, values, where, whereArgs)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableAddress::class.java, "update(Address)", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override val count: Int
        get() {
            var count = 0
            try {
                val cursor = dbSql.query(true, TABLE_NAME, null, null, null, null, null, null, null)
                count = cursor.count
                cursor.close()
            } catch (ex: Exception) {
                TBApplication.ReportError(ex, SqlTableAddress::class.java, "count()", "db")
            }
            return count
        }

    override fun isLocalCompanyOnly(company: String?): Boolean {
        if (company == null) {
            return false
        }
        val list = queryByCompanyName(company)
        if (list.isEmpty()) {
            return false
        }
        for (address in list) {
            if (!address.isLocal) {
                return false
            }
        }
        return true
    }

    override fun query(id: Long): DataAddress? {
        val orderBy = "$KEY_COMPANY ASC"
        val selection = "$KEY_ROWID =?"
        val selectionArgs = arrayOf(id.toString())
        val list = query(selection, selectionArgs, orderBy)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    override fun queryByCompanyName(name: String): List<DataAddress> {
        val selection = "$KEY_COMPANY =?"
        val selectionArgs = arrayOf(name)
        return query(selection, selectionArgs, null)
    }

    override fun query(): List<DataAddress> {
        val orderBy = "$KEY_COMPANY ASC"
        return query(null, null, orderBy)
    }

    override fun queryByServerId(serverId: Int): DataAddress? {
        val selection = "$KEY_SERVER_ID=?"
        val selectionArgs = arrayOf(serverId.toString())
        val list = query(selection, selectionArgs, null)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    internal fun query(selection: String?, selectionArgs: Array<String>?, orderBy: String?): List<DataAddress> {
        val list = mutableListOf<DataAddress>()
        try {
            val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, orderBy, null)
            val idxCompany = cursor.getColumnIndex(KEY_COMPANY)
            val idxState = cursor.getColumnIndex(KEY_STATE)
            val idxCity = cursor.getColumnIndex(KEY_CITY)
            val idxStreet = cursor.getColumnIndex(KEY_STREET)
            val idxZipCode = cursor.getColumnIndex(KEY_ZIPCODE)
            val idxRowId = cursor.getColumnIndex(KEY_ROWID)
            val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
            val idxDisabled = cursor.getColumnIndex(KEY_DISABLED)
            val idxLocal = cursor.getColumnIndex(KEY_LOCAL)
            val idxTest = cursor.getColumnIndex(KEY_IS_BOOT)
            while (cursor.moveToNext()) {
                val address = DataAddress(
                        cursor.getLong(idxRowId),
                        cursor.getInt(idxServerId),
                        cursor.getString(idxCompany),
                        cursor.getString(idxStreet),
                        cursor.getString(idxCity),
                        cursor.getString(idxState),
                        cursor.getString(idxZipCode))
                address.disabled = cursor.getShort(idxDisabled).toInt() == 1
                address.isLocal = cursor.getShort(idxLocal).toInt() == 1
                address.isBootStrap = cursor.getShort(idxTest).toInt() == 1
                list.add(address)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableAddress::class.java, "query()", "db")
        }

        return list
    }

    override fun queryZipCodes(company: String): List<String> {
        val args = SelectionArgs(company, null, null, null, null)
        return queryStrings(KEY_ZIPCODE, args)
    }

    override fun queryStates(company: String, zipcode: String?): List<String> {
        val args = SelectionArgs(company, null, null, null, zipcode)
        return queryStrings(KEY_STATE, args)
    }

    override fun queryCities(company: String, zipcode: String?, state: String): List<String> {
        val args = SelectionArgs(company, null, null, state, zipcode)
        return queryStrings(KEY_CITY, args)
    }

    override fun queryStreets(company: String, city: String, state: String, zipcode: String?): List<String> {
        val args = SelectionArgs(company, null, city, state, zipcode)
        return queryStrings(KEY_STREET, args)
    }

    override fun queryCompanies(): List<String> {
        return queryStrings(KEY_COMPANY, null)
    }

    private fun queryStrings(key: String, args: SelectionArgs?): List<String> {
        val list = mutableListOf<String>()
        try {
            val columns = arrayOf(key)
            val orderBy = "$key ASC"
            val cursor: Cursor
            if (args != null) {
                cursor = dbSql.query(true, TABLE_NAME, columns, args.selection, args.selectionArgs, null, null, orderBy, null)
            } else {
                cursor = dbSql.query(true, TABLE_NAME, columns, null, null, null, null, orderBy, null)
            }
            val idxValue = cursor.getColumnIndex(key)
            while (cursor.moveToNext()) {
                val value = cursor.getString(idxValue)
                if (!value.isEmpty()) {
                    list.add(value)
                }
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableAddress::class.java, "queryStrings()", key)
        }

        return list
    }

    override fun queryAddressId(company: String, street: String, city: String, state: String, zipcode: String?): Long {
        var id = -1L
        try {
            val columns = arrayOf(KEY_ROWID)
            val args = SelectionArgs(company, street, city, state, zipcode)
            val cursor = dbSql.query(TABLE_NAME, columns, args.selection, args.selectionArgs, null, null, null, null)
            val idxRowId = cursor.getColumnIndex(KEY_ROWID)
            if (cursor.moveToFirst()) {
                id = cursor.getLong(idxRowId)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableAddress::class.java, "queryAddressId()", "db")
        }

        return id
    }

    fun remove(id: Long) {
        try {
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(id.toString())
            dbSql.delete(TABLE_NAME, where, whereArgs)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableAddress::class.java, "remove()", "db")
        }

    }

    override fun removeOrDisable(item: DataAddress) {
        if (db.tableEntry.countAddresses(item.id) == 0 && db.tableProjectAddressCombo.countAddress(item.id) == 0) {
            remove(item.id)
        } else {
            item.disabled = true
            update(item)
        }
    }

    override fun toString(): String {
        val sbuf = StringBuilder()
        for (address in query()) {
            sbuf.append(address.toString())
            sbuf.append("\n")
        }
        return sbuf.toString()
    }

    fun clearUploaded() {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_SERVER_ID, 0)
            dbSql.update(TABLE_NAME, values, null, null)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableAddress::class.java, "clearUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

}
