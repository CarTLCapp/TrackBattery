/*
 * *
 *   * Copyright 2019, FleetTLC. All rights reserved
 *
 */

package com.cartlc.tracker.fresh.model.core.data

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

    enum class Type(val code: Char) {
        UNSET('U'),
        NONE('X'),
        TOAST('T'),
        DIALOG('D'),
        CONFIRM('C'),
        CONFIRM_NEW('N'),
        CATEGORY('Y');

        companion object {

            fun from(code: String): Type {
                for (value in values()) {
                    if (value.code == code[0]) {
                        return value
                    }
                }
                Timber.e("Unrecognized prompt type: $code")
                return UNSET
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataFlowElement

        if (flowId != other.flowId) return false
        if (prompt != other.prompt) return false
        if (type != other.type) return false
        if (numImages != other.numImages) return false

        return true
    }

    override fun hashCode(): Int {
        var result = flowId.hashCode()
        result = 31 * result + (prompt?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        result = 31 * result + numImages.hashCode()
        return result
    }

    override fun toString(): String {
        return "DataFlowElement(id=$id, serverId=$serverId, flowId=$flowId, prompt=$prompt, type=$type, numImages=$numImages)"
    }

}