/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.VisibleForTesting
import com.cartlc.tracker.fresh.model.core.data.DataVehicle
import com.cartlc.tracker.fresh.model.misc.HashLongList
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableVehicle
import com.cartlc.tracker.fresh.ui.app.TBApplication
import timber.log.Timber

/**
 * Created by dug on 8/31/17.
 */

@VisibleForTesting
open class SqlTableVehicle(
        private val dm: DatabaseTable,
        private val dbSql: SQLiteDatabase
) : TableVehicle {

    companion object {
        private val TAG = SqlTableVehicle::class.simpleName

        private const val TABLE_NAME = "table_vehicle"

        private const val KEY_ROWID = "_id"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_INSPECTING = "inspecting"
        private const val KEY_TYPE_OF_INSPECTION = "type_of_inspection"
        private const val KEY_MILEAGE = "mileage"
        private const val KEY_HEAD_LIGHTS = "head_lights"
        private const val KEY_TAIL_LIGHTS = "tail_lights"
        private const val KEY_EXTERIOR_LIGHTS = "exterior_lights"
        private const val KEY_FLUID_CHECKS = "fluid_checks"
        private const val KEY_FLUID_PROBLEMS_DETECTED = "fluid_problems_detected"
        private const val KEY_TIRE_INSPECTION = "tire_inspection"
        private const val KEY_EXTERIOR_DAMAGE = "exterior_damage"
        private const val KEY_OTHER = "other"
        private const val KEY_UPLOADED = "uploaded"
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_INSPECTING)
        sbuf.append(" int default 0, ")
        sbuf.append(KEY_TYPE_OF_INSPECTION)
        sbuf.append(" int default 0, ")
        sbuf.append(KEY_SERVER_ID)
        sbuf.append(" int default 0, ")
        sbuf.append(KEY_MILEAGE)
        sbuf.append(" int default 0, ")
        sbuf.append(KEY_HEAD_LIGHTS)
        sbuf.append(" text, ")
        sbuf.append(KEY_TAIL_LIGHTS)
        sbuf.append(" text, ")
        sbuf.append(KEY_EXTERIOR_LIGHTS)
        sbuf.append(" varchar(256), ")
        sbuf.append(KEY_FLUID_CHECKS)
        sbuf.append(" text, ")
        sbuf.append(KEY_FLUID_PROBLEMS_DETECTED)
        sbuf.append(" varchar(256), ")
        sbuf.append(KEY_TIRE_INSPECTION)
        sbuf.append(" text, ")
        sbuf.append(KEY_EXTERIOR_DAMAGE)
        sbuf.append(" varchar(256), ")
        sbuf.append(KEY_OTHER)
        sbuf.append(" varchar(256), ")
        sbuf.append(KEY_UPLOADED)
        sbuf.append(" bit default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    override fun save(vehicle: DataVehicle): Long {
        val values = ContentValues()
        values.put(KEY_SERVER_ID, vehicle.serverId)
        values.put(KEY_INSPECTING, vehicle.inspecting)
        values.put(KEY_TYPE_OF_INSPECTION, vehicle.typeOfInspection)
        values.put(KEY_MILEAGE, vehicle.mileage)
        values.put(KEY_HEAD_LIGHTS, vehicle.headLights.mash())
        values.put(KEY_TAIL_LIGHTS, vehicle.tailLights.mash())
        values.put(KEY_EXTERIOR_LIGHTS, vehicle.exteriorLightIssues)
        values.put(KEY_FLUID_CHECKS, vehicle.fluidChecks.mash())
        values.put(KEY_FLUID_PROBLEMS_DETECTED, vehicle.fluidProblemsDetected)
        values.put(KEY_TIRE_INSPECTION, vehicle.tireInspection.mash())
        values.put(KEY_EXTERIOR_DAMAGE, vehicle.exteriorDamage)
        values.put(KEY_OTHER, vehicle.other)
        values.put(KEY_UPLOADED, if (vehicle.uploaded) 1 else 0)
        var saved = false
        if (vehicle.id > 0) {
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(vehicle.id.toString())
            saved = dbSql.update(TABLE_NAME, values, where, whereArgs) != 0
        }
        if (!saved) {
            vehicle.id = dbSql.insert(TABLE_NAME, null, values)
        }
        return vehicle.id
    }

    // Right now only used to update a few fields.
    // Later this will be extended to saveUploaded everything.
    override fun saveUploaded(vehicle: DataVehicle) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_SERVER_ID, vehicle.serverId)
            values.put(KEY_UPLOADED, if (vehicle.uploaded) 1 else 0)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(java.lang.Long.toString(vehicle.id))
            if (dbSql.update(TABLE_NAME, values, where, whereArgs) == 0) {
                Timber.tag(TAG).e("SqlTableVehicle.saveUploaded(): Unable to update tableEntry")
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableEntry::class.java, "saveUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun query(id: Long): DataVehicle? {
        val selection = "$KEY_ROWID=?"
        val selectionArgs = arrayOf(id.toString())
        return query(selection, selectionArgs)
    }

    override fun queryByServerId(id: Long): DataVehicle? {
        val selection = "$KEY_SERVER_ID=?"
        val selectionArgs = arrayOf(id.toString())
        return query(selection, selectionArgs)
    }

    private fun query(selection: String, selectionArgs: Array<String>): DataVehicle? {
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        val vehicle: DataVehicle?
        if (cursor.moveToNext()) {
            val idxId = cursor.getColumnIndex(KEY_ROWID)
            val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
            val idxInspecting = cursor.getColumnIndex(KEY_INSPECTING)
            val idxMileage = cursor.getColumnIndex(KEY_MILEAGE)
            val idxTypeOfInspecting = cursor.getColumnIndex(KEY_TYPE_OF_INSPECTION)
            val idxHeadLights = cursor.getColumnIndex(KEY_HEAD_LIGHTS)
            val idxTailLights = cursor.getColumnIndex(KEY_TAIL_LIGHTS)
            val idxExteriorLights = cursor.getColumnIndex(KEY_EXTERIOR_LIGHTS)
            val idxFluidChecks = cursor.getColumnIndex(KEY_FLUID_CHECKS)
            val idxFluidProblemsDetected = cursor.getColumnIndex(KEY_FLUID_PROBLEMS_DETECTED)
            val idxTireInspection = cursor.getColumnIndex(KEY_TIRE_INSPECTION)
            val idxExteriorDamage = cursor.getColumnIndex(KEY_EXTERIOR_DAMAGE)
            val idxOther = cursor.getColumnIndex(KEY_OTHER)
            val idxUploaded = cursor.getColumnIndex(KEY_UPLOADED)
            vehicle = DataVehicle(dm.tableString)
            vehicle.id = cursor.getLong(idxId)
            vehicle.serverId = cursor.getLong(idxServerId)
            vehicle.inspecting = cursor.getLong(idxInspecting)
            vehicle.typeOfInspection = cursor.getLong(idxTypeOfInspecting)
            vehicle.mileage = cursor.getInt(idxMileage)
            vehicle.headLights = HashLongList(dm.tableString, cursor.getString(idxHeadLights))
            vehicle.tailLights = HashLongList(dm.tableString, cursor.getString(idxTailLights))
            vehicle.exteriorLightIssues = cursor.getString(idxExteriorLights)
            vehicle.fluidChecks = HashLongList(dm.tableString, cursor.getString(idxFluidChecks))
            vehicle.fluidProblemsDetected = cursor.getString(idxFluidProblemsDetected)
            vehicle.tireInspection = HashLongList(dm.tableString, cursor.getString(idxTireInspection))
            vehicle.exteriorDamage = cursor.getString(idxExteriorDamage)
            vehicle.other = cursor.getString(idxOther)
            vehicle.uploaded = cursor.getShort(idxUploaded).toInt() != 0
        } else {
            vehicle = null
        }
        cursor.close()
        return vehicle
    }

    override fun queryNotUploaded(): List<DataVehicle> {
        val selection = "$KEY_UPLOADED=0"
        val cursor = dbSql.query(TABLE_NAME, null, selection, null, null, null, null, null)
        val idxId = cursor.getColumnIndex(KEY_ROWID)
        val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
        val idxInspecting = cursor.getColumnIndex(KEY_INSPECTING)
        val idxMileage = cursor.getColumnIndex(KEY_MILEAGE)
        val idxTypeOfInspecting = cursor.getColumnIndex(KEY_TYPE_OF_INSPECTION)
        val idxHeadLights = cursor.getColumnIndex(KEY_HEAD_LIGHTS)
        val idxTailLights = cursor.getColumnIndex(KEY_TAIL_LIGHTS)
        val idxExteriorLights = cursor.getColumnIndex(KEY_EXTERIOR_LIGHTS)
        val idxFluidChecks = cursor.getColumnIndex(KEY_FLUID_CHECKS)
        val idxFluidProblemsDetected = cursor.getColumnIndex(KEY_FLUID_PROBLEMS_DETECTED)
        val idxTireInspection = cursor.getColumnIndex(KEY_TIRE_INSPECTION)
        val idxExteriorDamage = cursor.getColumnIndex(KEY_EXTERIOR_DAMAGE)
        val idxOther = cursor.getColumnIndex(KEY_OTHER)
        val idxUploaded = cursor.getColumnIndex(KEY_UPLOADED)
        val list = mutableListOf<DataVehicle>()
        while (cursor.moveToNext()) {
            val vehicle = DataVehicle(dm.tableString)
            vehicle.id = cursor.getLong(idxId)
            vehicle.serverId = cursor.getLong(idxServerId)
            vehicle.inspecting = cursor.getLong(idxInspecting)
            vehicle.typeOfInspection = cursor.getLong(idxTypeOfInspecting)
            vehicle.mileage = cursor.getInt(idxMileage)
            vehicle.headLights = HashLongList(dm.tableString, cursor.getString(idxHeadLights))
            vehicle.tailLights = HashLongList(dm.tableString, cursor.getString(idxTailLights))
            vehicle.exteriorLightIssues = cursor.getString(idxExteriorLights)
            vehicle.fluidChecks = HashLongList(dm.tableString, cursor.getString(idxFluidChecks))
            vehicle.fluidProblemsDetected = cursor.getString(idxFluidProblemsDetected)
            vehicle.tireInspection = HashLongList(dm.tableString, cursor.getString(idxTireInspection))
            vehicle.exteriorDamage = cursor.getString(idxExteriorDamage)
            vehicle.other = cursor.getString(idxOther)
            vehicle.uploaded = cursor.getShort(idxUploaded).toInt() != 0
            list.add(vehicle)
        }
        cursor.close()
        return list
    }

    fun clearUploaded() {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_UPLOADED, 0)
            values.put(KEY_SERVER_ID, 0)
            dbSql.update(TABLE_NAME, values, null, null)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            Timber.tag(TAG).e(ex)
        } finally {
            dbSql.endTransaction()
        }
    }

}
