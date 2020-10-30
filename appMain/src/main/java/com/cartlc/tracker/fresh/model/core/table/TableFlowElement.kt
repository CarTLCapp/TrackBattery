/*
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataFlowElement

interface TableFlowElement {

    companion object {

        fun convertToStrings(items: List<DataFlowElement>): List<String> {
            val list = mutableListOf<String>()
            for (item in items) {
                item.prompt?.let {
                    if (it.isNotBlank()) {
                        list.add(it)
                    }
                }
            }
            return list
        }

    }

    fun add(item: DataFlowElement): Long
    fun query(): List<DataFlowElement>
    fun query(flow_element_id: Long): DataFlowElement?
    fun first(flow_id: Long): Long?
    fun firstOfSubFlow(flow_id: Long, element_within: Long): Long?
    fun last(flow_id: Long): Long?
    fun next(flow_element_id: Long): Long?
    fun nextOfSubFlow(element_within: Long): Long?
    fun prev(flow_element_id: Long): Long?
    fun prevOfSubFlow(flow_element_id: Long): Long?
    fun progressInSubFlow(flow_element_id: Long): Pair<Int,Int>?
    fun flowSize(flow_id: Long): Int
    fun isConfirmTop(flow_id: Long, flow_element_id: Long): Boolean
    fun queryFlowId(flow_element_id: Long): Long?
    fun queryByServerId(server_id: Int): DataFlowElement?
    fun queryByFlowId(flow_id: Long): List<DataFlowElement>
    fun queryConfirmBatch(flow_id: Long, flow_element_id: Long): List<DataFlowElement>
    fun querySubFlow(flow_id: Long, element_within: Long): List<DataFlowElement>
    fun querySubFlows(flow_id: Long): List<List<DataFlowElement>>
    fun hasSubFlows(flow_id: Long): Boolean
    fun subFlowSize(flow_id: Long, element_within: Long): Int
    fun remove(item: DataFlowElement)
    fun update(item: DataFlowElement)
    fun toString(flow_id: Long): String
}