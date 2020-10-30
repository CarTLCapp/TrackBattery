package com.cartlc.tracker.fresh.model.flow

sealed class Stage(
        val ord: Int
) {
    object NONE : Stage(0)
    object LOGIN : Stage(1)
    object ROOT_PROJECT : Stage(2)
    object COMPANY : Stage(3)
    object ADD_COMPANY : Stage(4)
    object STATE : Stage(5)
    object ADD_STATE : Stage(6)
    object CITY : Stage(7)
    object ADD_CITY : Stage(8)
    object STREET : Stage(9)
    object ADD_STREET : Stage(10)
    object CONFIRM_ADDRESS : Stage(11)
    object CURRENT_PROJECT : Stage(12)
    object SUB_PROJECT : Stage(13)
    object SUB_FLOWS : Stage(21)
    object TRUCK_NUMBER_PICTURE : Stage(14)
    object TRUCK_DAMAGE_PICTURE : Stage(15)
    object EQUIPMENT : Stage(16)
    object ADD_EQUIPMENT : Stage(17)
    data class CUSTOM_FLOW(val flowElementId: Long) : Stage(18) {
        val isFirstElement: Boolean
            get() = flowElementId == FIRST_ELEMENT
        val isLastElement: Boolean
            get() = flowElementId == LAST_ELEMENT
    }
    object STATUS : Stage(19)
    object CONFIRM : Stage(20)

    companion object {

        fun from(ord: Int, flowElementId: Long): Stage {
            for (stage in values(flowElementId)) {
                if (stage.ord == ord) {
                    return stage
                }
            }
            return NONE
        }

        fun values(flowElementId: Long = 0): List<Stage> {
            return arrayListOf(
                    LOGIN,
                    ROOT_PROJECT,
                    COMPANY,
                    ADD_COMPANY,
                    STATE,
                    ADD_STATE,
                    CITY,
                    ADD_CITY,
                    STREET,
                    ADD_STREET,
                    CONFIRM_ADDRESS,
                    CURRENT_PROJECT,
                    SUB_PROJECT,
                    SUB_FLOWS,
                    TRUCK_NUMBER_PICTURE,
                    TRUCK_DAMAGE_PICTURE,
                    EQUIPMENT,
                    ADD_EQUIPMENT,
                    CUSTOM_FLOW(flowElementId),
                    STATUS,
                    CONFIRM
            )
        }

        const val FIRST_ELEMENT = 0L
        const val LAST_ELEMENT = -1L
    }

    override fun toString(): String {
        return when (this) {
            NONE -> "NONE"
            LOGIN -> "LOGIN"
            ROOT_PROJECT -> "ROOT_PROJECT"
            COMPANY -> "COMPANY"
            ADD_COMPANY -> "ADD_COMPANY"
            STATE -> "STATE"
            ADD_STATE -> "ADD_STATE"
            CITY -> "CITY"
            ADD_CITY -> "ADD_CITY"
            STREET -> "STREET"
            ADD_STREET -> "ADD_STREET"
            CONFIRM_ADDRESS -> "CONFIRM_ADDRESS"
            CURRENT_PROJECT -> "CURRENT_PROJECT"
            SUB_PROJECT -> "SUB_PROJECT"
            SUB_FLOWS -> "SUB_FLOWS"
            TRUCK_NUMBER_PICTURE -> "TRUCK_NUMBER_PICTURE"
            TRUCK_DAMAGE_PICTURE -> "TRUCK_DAMAGE_PICTURE"
            EQUIPMENT -> "EQUIPMENT"
            ADD_EQUIPMENT -> "ADD_EQUIPMENT"
            is CUSTOM_FLOW -> "CUSTOM_FLOW($flowElementId)"
            STATUS -> "STATUS"
            CONFIRM -> "CONFIRM"
        }
    }

}
