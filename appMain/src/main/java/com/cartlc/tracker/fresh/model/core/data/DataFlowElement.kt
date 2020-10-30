/*
 * *
 *   * Copyright 2019, FleetTLC. All rights reserved
 *
 */

package com.cartlc.tracker.fresh.model.core.data

import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import timber.log.Timber

class DataFlowElement(
        var id: Long = 0,
        var serverId: Int = 0,
        var flowId: Long = 0,
        var order: Short = 0,
        var prompt: String? = null,
        var type: Type = Type.UNSET,
        var numImages: Short = 0
) {

    companion object {
        private val TAG = DataFlowElement::class.simpleName

        fun hasNotes(db: DatabaseTable, flow_element_id: Long): Boolean {
            return db.tableFlowElementNote.countNotes(flow_element_id) > 0
        }
    }

    enum class Type(val code: Char) {
        UNSET('U'),
        NONE('X'),
        TOAST('T'),
        DIALOG('D'),
        CONFIRM('C'),
        CONFIRM_NEW('N'),
        SUB_FLOW_DIVIDER('S');

        companion object {

            fun from(code: String): Type {
                for (value in values()) {
                    if (value.code == code[0]) {
                        return value
                    }
                }
                Timber.tag(TAG).e("Unrecognized prompt type: $code")
                return UNSET
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataFlowElement

        if (flowId != other.flowId) return false
        if (order != other.order) return false
        if (prompt != other.prompt) return false
        if (type != other.type) return false
        if (numImages != other.numImages) return false

        return true
    }

    override fun hashCode(): Int {
        var result = flowId.hashCode()
        result = 31 * result + order
        result = 31 * result + (prompt?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        result = 31 * result + numImages
        return result
    }

    override fun toString(): String {
        return "DataFlowElement(id=$id, serverId=$serverId, flowId=$flowId, order=$order, prompt=$prompt, type=$type, numImages=$numImages)"
    }

    val hasImages: Boolean
        get() = numImages > 0

    val isConfirmType: Boolean
        get() = type == Type.CONFIRM_NEW || type == Type.CONFIRM

    fun hasNotes(db: DatabaseTable): Boolean {
        return hasNotes(db, id)
    }
}