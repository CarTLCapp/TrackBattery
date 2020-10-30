/*
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataFlowElement
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableFlowElement
import com.cartlc.tracker.fresh.model.flow.Stage
import com.cartlc.tracker.fresh.ui.app.TBApplication

/**
 * Created by dug on 8/31/17.
 */

class SqlTableFlowElement(
        private val db: DatabaseTable,
        private val dbSql: SQLiteDatabase
) : TableFlowElement {

    companion object {

        private const val TABLE_NAME = "table_flow_element"

        private const val KEY_ROWID = "_id"
        private const val KEY_ORDER = "position"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_FLOW_ID = "sub_flow_id"
        private const val KEY_PROMPT = "prompt"
        private const val KEY_TYPE = "type"
        private const val KEY_NUM_IMAGES = "num_images"

    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_ORDER)
        sbuf.append(" smallint default 0, ")
        sbuf.append(KEY_SERVER_ID)
        sbuf.append(" integer, ")
        sbuf.append(KEY_FLOW_ID)
        sbuf.append(" int default 0, ")
        sbuf.append(KEY_PROMPT)
        sbuf.append(" text default null, ")
        sbuf.append(KEY_TYPE)
        sbuf.append(" char(2) default null, ")
        sbuf.append(KEY_NUM_IMAGES)
        sbuf.append(" int default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    // region TableFlowElement

    override fun add(item: DataFlowElement): Long {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_ORDER, item.order)
            values.put(KEY_SERVER_ID, item.serverId)
            values.put(KEY_FLOW_ID, item.flowId)
            values.put(KEY_PROMPT, item.prompt)
            values.put(KEY_TYPE, item.type.code.toString())
            values.put(KEY_NUM_IMAGES, item.numImages)
            item.id = dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableFlowElement::class.java, "add(item)", "db")
        } finally {
            dbSql.endTransaction()
        }
        return item.id
    }

    override fun query(): List<DataFlowElement> {
        return query(null, null)
    }

    override fun query(flow_element_id: Long): DataFlowElement? {
        val selection = "$KEY_ROWID=?"
        val selectionArgs = arrayOf(flow_element_id.toString())
        val items = query(selection, selectionArgs)
        if (items.isEmpty()) {
            return null
        }
        return items[0]
    }

    private fun query(selection: String?, selectionArgs: Array<String>?): List<DataFlowElement> {
        val list = ArrayList<DataFlowElement>()
        val sortBy = "$KEY_ORDER asc"
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, sortBy, null)
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        val idxOrderId = cursor.getColumnIndex(KEY_ORDER)
        val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
        val idxFlowId = cursor.getColumnIndex(KEY_FLOW_ID)
        val idxPrompt = cursor.getColumnIndex(KEY_PROMPT)
        val idxType = cursor.getColumnIndex(KEY_TYPE)
        val idxNumImages = cursor.getColumnIndex(KEY_NUM_IMAGES)
        var item: DataFlowElement
        while (cursor.moveToNext()) {
            item = DataFlowElement(
                    cursor.getLong(idxRowId),
                    cursor.getInt(idxServerId),
                    cursor.getLong(idxFlowId),
                    cursor.getShort(idxOrderId),
                    cursor.getString(idxPrompt),
                    DataFlowElement.Type.from(cursor.getString(idxType)),
                    cursor.getInt(idxNumImages).toShort()
            )
            list.add(item)
        }
        cursor.close()
        return list
    }

    override fun queryByServerId(server_id: Int): DataFlowElement? {
        val selection = "$KEY_SERVER_ID=?"
        val selectionArgs = arrayOf(server_id.toString())
        val list = query(selection, selectionArgs)
        return if (list.isNotEmpty()) {
            list[0]
        } else null
    }

    override fun queryByFlowId(flow_id: Long): List<DataFlowElement> {
        val selection = "$KEY_FLOW_ID=?"
        val selectionArgs = arrayOf(flow_id.toString())
        return query(selection, selectionArgs)
    }

    override fun first(flow_id: Long): Long? {
        val selection = "$KEY_FLOW_ID=?"
        val selectionArgs = arrayOf(flow_id.toString())
        val columns = arrayOf(KEY_ROWID)
        val sortBy = "$KEY_ORDER asc"
        val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, sortBy, null)
        var rowId: Long? = null
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        if (cursor.moveToNext()) {
            rowId = cursor.getLong(idxRowId)
        }
        cursor.close()
        return rowId
    }

    override fun firstOfSubFlow(flow_id: Long, element_within: Long): Long? {
        val firstOfSubFlow = findTopOfSubFlow(flow_id, element_within)
        if (firstOfSubFlow == 0L) {
            return first(flow_id)
        }
        return firstOfSubFlow
    }

    // TODO: Should have just used KEY_ORDER desc
    override fun last(flow_id: Long): Long? {
        val selection = "$KEY_FLOW_ID=?"
        val selectionArgs = arrayOf(flow_id.toString())
        val columns = arrayOf(KEY_ROWID, KEY_TYPE)
        val sortBy = "$KEY_ORDER asc"
        val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, sortBy, null)
        var rowId: Long? = null
        var lastType: DataFlowElement.Type? = null
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        val idxType = cursor.getColumnIndex(KEY_TYPE)
        while (cursor.moveToNext()) {
            val testRowId = cursor.getLong(idxRowId)
            val testType = DataFlowElement.Type.from(cursor.getString(idxType))
            if (testType != DataFlowElement.Type.CONFIRM ||
                    lastType == null ||
                    (lastType != DataFlowElement.Type.CONFIRM && lastType != DataFlowElement.Type.CONFIRM_NEW)) {
                rowId = testRowId
            }
            lastType = testType
        }
        cursor.close()
        return rowId
    }

    /**
     * Find the next flow element just beyond the passed flow element.
     */
    override fun next(flow_element_id: Long): Long? {
        val selection = "$KEY_ROWID=?"
        val selectionArgs = arrayOf(flow_element_id.toString())
        val columns = arrayOf(KEY_FLOW_ID)
        val sortBy = "$KEY_ORDER asc"
        val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, sortBy, null)
        var flowId: Long? = null
        if (cursor.moveToFirst()) {
            flowId = cursor.getLong(cursor.getColumnIndex(KEY_FLOW_ID))
        }
        cursor.close()
        if (flowId == null) {
            return null
        }
        return next(flowId, flow_element_id)
    }

    private fun next(flow_id: Long, flow_element_id: Long): Long? {
        val selection = "$KEY_FLOW_ID=?"
        val selectionArgs = arrayOf(flow_id.toString())
        val columns = arrayOf(KEY_ROWID, KEY_TYPE)
        val sortBy = "$KEY_ORDER asc"
        val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, sortBy, null)
        var rowId: Long? = null
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        val idxType = cursor.getColumnIndex(KEY_TYPE)
        var foundElement = false
        var wasConfirm = false
        while (cursor.moveToNext()) {
            val testRowId = cursor.getLong(idxRowId)
            val testType = DataFlowElement.Type.from(cursor.getString(idxType))
            if (foundElement) {
                rowId = testRowId
                /**
                 * If the element was a confirm element, then we need to skip over confirm elements until the next
                 * non-confirm element, because consecutive confirm elements are grouped together as one element.
                 */
                if (wasConfirm && testType == DataFlowElement.Type.CONFIRM) {
                    rowId = null
                    continue
                }
                break
            } else if (testRowId == flow_element_id || flow_element_id == Stage.FIRST_ELEMENT) {
                /**
                 * Found element, so the very next element will be the next element.
                 */
                foundElement = true
                wasConfirm = testType == DataFlowElement.Type.CONFIRM_NEW || testType == DataFlowElement.Type.CONFIRM
            }
        }
        cursor.close()
        return rowId
    }

    override fun nextOfSubFlow(element_within: Long): Long? {
        val nextElementId = next(element_within) ?: return null
        val item = query(nextElementId) ?: return null
        if (item.type == DataFlowElement.Type.SUB_FLOW_DIVIDER) {
            return null
        }
        return item.id
    }

    /**
     * Find the previous flow element just before the current flow element.
     * Take into account CONFIRM elements, in which sequential confirm elements are grouped
     * together as a single element.
     */
    override fun prev(flow_element_id: Long): Long? {
        val selection = "$KEY_ROWID=?"
        val selectionArgs = arrayOf(flow_element_id.toString())
        val columns = arrayOf(KEY_FLOW_ID)
        val sortBy = "$KEY_ORDER asc"
        val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, sortBy, null)
        var flowId: Long? = null
        if (cursor.moveToFirst()) {
            flowId = cursor.getLong(cursor.getColumnIndex(KEY_FLOW_ID))
        }
        cursor.close()
        if (flowId == null) {
            return null
        }
        return prev(flowId, flow_element_id)
    }

    private fun prev(flow_id: Long, flow_element_id: Long): Long? {
        if (flow_element_id == 0L) {
            return null
        }
        val selection = "$KEY_FLOW_ID=?"
        val selectionArgs = arrayOf(flow_id.toString())
        val columns = arrayOf(KEY_ROWID, KEY_TYPE)
        val sortBy = "$KEY_ORDER asc"
        val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, sortBy, null)
        var rowId: Long? = null
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        val idxType = cursor.getColumnIndex(KEY_TYPE)
        var wasConfirm = false
        while (cursor.moveToNext()) {
            val testRowId = cursor.getLong(idxRowId)
            if (testRowId == flow_element_id) {
                break
            }
            val testType = DataFlowElement.Type.from(cursor.getString(idxType))
            if (wasConfirm && testType == DataFlowElement.Type.CONFIRM) {
                continue
            }
            wasConfirm = testType == DataFlowElement.Type.CONFIRM_NEW || testType == DataFlowElement.Type.CONFIRM
            rowId = testRowId
        }
        cursor.close()
        return rowId
    }

    override fun prevOfSubFlow(flow_element_id: Long): Long? {
        val nextElementId = prev(flow_element_id) ?: return null
        val item = query(nextElementId) ?: return null
        if (item.type == DataFlowElement.Type.SUB_FLOW_DIVIDER) {
            return null
        }
        return item.id
    }

    override fun queryFlowId(flow_element_id: Long): Long? {
        val selection = "$KEY_ROWID=?"
        val selectionArgs = arrayOf(flow_element_id.toString())
        val columns = arrayOf(KEY_FLOW_ID)
        val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null)
        var flowId: Long? = null
        if (cursor.moveToFirst()) {
            flowId = cursor.getLong(cursor.getColumnIndex(KEY_FLOW_ID))
        }
        cursor.close()
        if (flowId == null) {
            return null
        }
        return flowId
    }

    override fun progressInSubFlow(flow_element_id: Long): Pair<Int, Int>? {
        return queryFlowId(flow_element_id)?.let { flowId -> progress(flowId, flow_element_id) }
    }

    /**
     * Progress through the chain. Returns position element is at, with the total chain size.
     * If this chain has sub-flows, then instead returns relative position wihtin the sub-flow.
     */
    private fun progress(flow_id: Long, flow_element_id: Long): Pair<Int, Int>? {
        val selection = "$KEY_FLOW_ID=?"
        val selectionArgs = arrayOf(flow_id.toString())
        val columns = arrayOf(KEY_ROWID, KEY_TYPE, KEY_NUM_IMAGES)
        val sortBy = "$KEY_ORDER asc"
        val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, sortBy, null)
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        val idxType = cursor.getColumnIndex(KEY_TYPE)
        val idxNumImages = cursor.getColumnIndex(KEY_NUM_IMAGES)
        var wasConfirm = false
        var positionInChain = 0
        var found = false
        var chainSize = 0
        while (cursor.moveToNext()) {
            val testRowId = cursor.getLong(idxRowId)
            val testType = DataFlowElement.Type.from(cursor.getString(idxType))
            val testNumImages = cursor.getInt(idxNumImages)
            if (testRowId == flow_element_id || flow_element_id == Stage.FIRST_ELEMENT) {
                found = true
            }
            if (wasConfirm && testType == DataFlowElement.Type.CONFIRM) {
                // Note: need to collapse all confirm elements into one element
                continue
            }
            if (testType == DataFlowElement.Type.DIALOG || testType == DataFlowElement.Type.TOAST) {
                if (testNumImages == 0 && !db.tableFlowElementNote.hasNotes(testRowId)) {
                    // Ignore simple dialogs and toast messages without images nor notes
                    continue
                }
            }
            wasConfirm = testType == DataFlowElement.Type.CONFIRM_NEW || testType == DataFlowElement.Type.CONFIRM

            // Note progress only through sub-flow if this is a sub-flow.
            if (testType == DataFlowElement.Type.SUB_FLOW_DIVIDER) {
                if (found && chainSize > 0) {
                    // Reached the end of the current sub-flow
                    break
                } else {
                    // Starting a new sub-flow
                    chainSize = 0
                    positionInChain = 0
                    continue
                }
            } else {
                chainSize++
            }
            if (!found) {
                positionInChain++
            }
        }
        cursor.close()
        return Pair(positionInChain, chainSize)
    }

    override fun flowSize(flow_id: Long): Int {
        val selection = "$KEY_FLOW_ID=?"
        val selectionArgs = arrayOf(flow_id.toString())
        val columns = arrayOf(KEY_ROWID, KEY_TYPE, KEY_NUM_IMAGES)
        val sortBy = "$KEY_ORDER asc"
        val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, sortBy, null)
        val idxType = cursor.getColumnIndex(KEY_TYPE)
        val idxNumImages = cursor.getColumnIndex(KEY_NUM_IMAGES)
        var wasConfirm = false
        var chainSize = 0
        if (cursor.count > 0) {
            chainSize++;
        }
        while (cursor.moveToNext()) {
            val testType = DataFlowElement.Type.from(cursor.getString(idxType))
            val testNumImages = cursor.getInt(idxNumImages)
            if (wasConfirm && testType == DataFlowElement.Type.CONFIRM) {
                // Note: need to collapse all confirm elements into one element
                continue
            }
            if (testType == DataFlowElement.Type.SUB_FLOW_DIVIDER) {
                continue
            }
            if (testType == DataFlowElement.Type.DIALOG || testType == DataFlowElement.Type.TOAST) {
                if (testNumImages == 0) {
                    // Ignore simple dialogs and toast messages without images.
                    continue
                }
            }
            wasConfirm = testType == DataFlowElement.Type.CONFIRM_NEW || testType == DataFlowElement.Type.CONFIRM
            chainSize++
        }
        cursor.close()
        return chainSize
    }

    override fun subFlowSize(flow_id: Long, element_within: Long): Int {
        val topOfSubFlowElementId = findTopOfSubFlow(flow_id, element_within)
        if (topOfSubFlowElementId == 0L) {
            return 0
        }
        val selection = "$KEY_FLOW_ID=?"
        val selectionArgs = arrayOf(flow_id.toString())
        val columns = arrayOf(KEY_ROWID, KEY_TYPE, KEY_NUM_IMAGES)
        val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null)
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        val idxType = cursor.getColumnIndex(KEY_TYPE)
        val idxNumImages = cursor.getColumnIndex(KEY_NUM_IMAGES)
        var wasConfirm = false
        var found = false
        var count = 0
        while (cursor.moveToNext()) {
            val testId = cursor.getLong(idxRowId)
            val testType = DataFlowElement.Type.from(cursor.getString(idxType))
            val testNumImages = cursor.getInt(idxNumImages)
            if (testId == topOfSubFlowElementId) {
                found = true
            }
            if (!found) {
                continue
            }
            if (testType == DataFlowElement.Type.SUB_FLOW_DIVIDER) {
                if (count > 0) {
                    break   // end
                } else {
                    continue // start
                }
            }
            if (wasConfirm && testType == DataFlowElement.Type.CONFIRM) {
                continue
            }
            if (testType == DataFlowElement.Type.DIALOG || testType == DataFlowElement.Type.TOAST) {
                if (testNumImages == 0 && !DataFlowElement.hasNotes(db, testId)) {
                    // Ignore simple dialogs and toast messages without images nor notes
                    continue
                }
            }
            wasConfirm = testType == DataFlowElement.Type.CONFIRM_NEW || testType == DataFlowElement.Type.CONFIRM
            count++
        }
        cursor.close()
        return count
    }

    override fun update(item: DataFlowElement) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_SERVER_ID, item.serverId)
            values.put(KEY_FLOW_ID, item.flowId)
            values.put(KEY_ORDER, item.order)
            values.put(KEY_PROMPT, item.prompt)
            values.put(KEY_TYPE, item.type.code.toString())
            values.put(KEY_NUM_IMAGES, item.numImages)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(item.id.toString())
            dbSql.update(TABLE_NAME, values, where, whereArgs)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableFlowElement::class.java, "update()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun isConfirmTop(flow_id: Long, flow_element_id: Long): Boolean {
        val list = queryByFlowId(flow_id)
        var previous: DataFlowElement? = null
        for (element in list) {
            if (element.id == flow_element_id) {
                if (element.type == DataFlowElement.Type.CONFIRM_NEW) {
                    return true
                }
                if (element.type == DataFlowElement.Type.CONFIRM) {
                    return previous?.let {
                        it.type != DataFlowElement.Type.CONFIRM
                    } ?: true
                }
            }
            previous = element
        }
        return false
    }

    override fun queryConfirmBatch(flow_id: Long, flow_element_id: Long): List<DataFlowElement> {
        val elements = queryByFlowId(flow_id)
        val list = mutableListOf<DataFlowElement>()
        var collect = false
        for (element in elements) {
            if (element.id == flow_element_id) {
                collect = true
            } else if (collect) {
                if (element.type != DataFlowElement.Type.CONFIRM) {
                    break
                }
            }
            if (collect) {
                list.add(element)
            }
        }
        return list
    }

    // region SubFlow

    override fun querySubFlow(flow_id: Long, element_within: Long): List<DataFlowElement> {
        val topOfSubFlowElementId = findTopOfSubFlow(flow_id, element_within)
        if (topOfSubFlowElementId == 0L) {
            return emptyList()
        }
        val list = ArrayList<DataFlowElement>()
        val selection = "$KEY_FLOW_ID=?"
        val selectionArgs = arrayOf(flow_id.toString())
        val sortBy = "$KEY_ORDER asc"
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, sortBy, null)
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        val idxOrderId = cursor.getColumnIndex(KEY_ORDER)
        val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
        val idxFlowId = cursor.getColumnIndex(KEY_FLOW_ID)
        val idxPrompt = cursor.getColumnIndex(KEY_PROMPT)
        val idxType = cursor.getColumnIndex(KEY_TYPE)
        val idxNumImages = cursor.getColumnIndex(KEY_NUM_IMAGES)
        var item: DataFlowElement
        var capture = false
        while (cursor.moveToNext()) {
            val rowId = cursor.getLong(idxRowId)
            val rowType = DataFlowElement.Type.from(cursor.getString(idxType))
            if (rowId == topOfSubFlowElementId) {
                capture = true
            } else if (capture && rowType == DataFlowElement.Type.SUB_FLOW_DIVIDER) {
                break
            }
            if (!capture) {
                continue
            }
            item = DataFlowElement(
                    rowId,
                    cursor.getInt(idxServerId),
                    cursor.getLong(idxFlowId),
                    cursor.getShort(idxOrderId),
                    cursor.getString(idxPrompt),
                    rowType,
                    cursor.getInt(idxNumImages).toShort()
            )
            list.add(item)
        }
        cursor.close()
        return list
    }

    override fun querySubFlows(flow_id: Long): List<List<DataFlowElement>> {
        val list = ArrayList<ArrayList<DataFlowElement>>()
        var subList = ArrayList<DataFlowElement>()
        val selection = "$KEY_FLOW_ID=?"
        val selectionArgs = arrayOf(flow_id.toString())
        val sortBy = "$KEY_ORDER asc"
        val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, sortBy, null)
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        val idxOrderId = cursor.getColumnIndex(KEY_ORDER)
        val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
        val idxFlowId = cursor.getColumnIndex(KEY_FLOW_ID)
        val idxPrompt = cursor.getColumnIndex(KEY_PROMPT)
        val idxType = cursor.getColumnIndex(KEY_TYPE)
        val idxNumImages = cursor.getColumnIndex(KEY_NUM_IMAGES)
        var item: DataFlowElement
        while (cursor.moveToNext()) {
            val rowType = DataFlowElement.Type.from(cursor.getString(idxType))
            if (rowType == DataFlowElement.Type.SUB_FLOW_DIVIDER) {
                if (subList.isNotEmpty()) {
                    list.add(subList)
                    subList = ArrayList()
                }
            }
            item = DataFlowElement(
                    cursor.getLong(idxRowId),
                    cursor.getInt(idxServerId),
                    cursor.getLong(idxFlowId),
                    cursor.getShort(idxOrderId),
                    cursor.getString(idxPrompt),
                    rowType,
                    cursor.getInt(idxNumImages).toShort()
            )
            subList.add(item)
        }
        cursor.close()
        if (subList.isNotEmpty()) {
            list.add(subList)
        }
        return list

    }

    override fun hasSubFlows(flow_id: Long): Boolean {
        val selection = "$KEY_FLOW_ID=? AND $KEY_TYPE=?"
        val selectionArgs = arrayOf(
                flow_id.toString(),
                DataFlowElement.Type.SUB_FLOW_DIVIDER.code.toString()
        )
        val columns = arrayOf(KEY_ROWID)
        val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null)
        val count = cursor.count
        cursor.close()
        return count > 0
    }

    private fun findTopOfSubFlow(flow_id: Long, element_within: Long): Long {
        val selection = "$KEY_FLOW_ID=?"
        val selectionArgs = arrayOf(flow_id.toString())
        val columns = arrayOf(KEY_ROWID, KEY_TYPE)
        val sortBy = "$KEY_ORDER asc"
        val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, sortBy, null)
        var lastTopDivider: Long = 0
        val idxRowId = cursor.getColumnIndex(KEY_ROWID)
        val idxType = cursor.getColumnIndex(KEY_TYPE)
        while (cursor.moveToNext()) {
            val testRowId = cursor.getLong(idxRowId)
            val testType = DataFlowElement.Type.from(cursor.getString(idxType))
            if (testType == DataFlowElement.Type.SUB_FLOW_DIVIDER || lastTopDivider == 0L) {
                lastTopDivider = testRowId
            }
            if (testRowId == element_within) {
                break
            }
        }
        cursor.close()
        return lastTopDivider
    }

    // endregion SubFlow

    override fun remove(item: DataFlowElement) {
        val where = "$KEY_ROWID=?"
        val whereArgs = arrayOf(item.id.toString())
        dbSql.delete(TABLE_NAME, where, whereArgs)
    }

    override fun toString(flow_id: Long): String {
        val sbuf = StringBuilder()
        val elements = queryByFlowId(flow_id)
        for (element in elements) {
            if (sbuf.isNotEmpty()) {
                sbuf.append(" ")
            }
            sbuf.append("[")
            sbuf.append(element.order)
            sbuf.append(" ")
            sbuf.append(element.type.code.toString())
            if (!element.prompt.isNullOrEmpty()) {
                sbuf.append(" \"")
                sbuf.append(element.prompt)
                sbuf.append("\"")
            }
            if (element.numImages.toInt() == 1) {
                sbuf.append(" IMAGE")
            } else if (element.numImages > 1) {
                sbuf.append("${element.numImages} IMAGES")
            }
            if (db.tableFlowElementNote.hasNotes(element.id)) {
                sbuf.append(" NOTES")
                sbuf.append(db.tableFlowElementNote.toString(element.id))
            }
            sbuf.append("]")
        }
        return sbuf.toString()
    }

    // endregion TableFlowElement

}
