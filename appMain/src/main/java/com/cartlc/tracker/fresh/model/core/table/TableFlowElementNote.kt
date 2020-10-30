/*
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataFlowElementNote
import com.cartlc.tracker.fresh.model.core.data.DataNote

interface TableFlowElementNote {

    fun add(item: DataFlowElementNote): Long
    fun query(): List<DataFlowElementNote>
    fun query(flowElementId: Long, noteId: Long): DataFlowElementNote?
    fun query(flowElementId: Long): List<DataFlowElementNote>
    fun queryNotes(flowElementId: Long): List<DataNote>
    fun hasNotes(flowElementId: Long): Boolean
    fun countNotes(flowElementId: Long): Int
    fun remove(item: DataFlowElementNote)
    fun update(item: DataFlowElementNote)
    fun toString(flowElementId: Long): String

}