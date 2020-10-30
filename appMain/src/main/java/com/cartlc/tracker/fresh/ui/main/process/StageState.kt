/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.model.core.data.DataStates
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.flow.CompanyFlow
import com.cartlc.tracker.fresh.model.flow.Flow
import com.cartlc.tracker.fresh.model.flow.Stage
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import java.util.ArrayList

class StageState(
        shared: MainController.Shared
) : ProcessBase(shared) {

    fun process(flow: Flow) {
        with(shared) {
            var isEditing = flow.stage == Stage.ADD_STATE
            titleUseCase.subTitleText = if (isEditing) editProjectHint else curProjectHint
            titleUseCase.mainTitleVisible = true
            titleUseCase.subTitleVisible = true
            mainListUseCase.visible = true
            buttonsUseCase.nextVisible = false
            val company = prefHelper.company
            val zipcode = prefHelper.zipCode
            if (company == null) {
                curFlowValue = CompanyFlow()
                return
            }
            var states: MutableList<String> = db.tableAddress.queryStates(company, zipcode).toMutableList()
            if (states.size == 0) {
                val state = zipcode?.let { db.tableZipCode.queryState(it) }
                if (state != null) {
                    states = ArrayList()
                    states.add(state)
                } else {
                    isEditing = true
                }
            }
            if (isEditing) {
                states = DataStates.getUnusedStates(states).toMutableList()
                prefHelper.state = null
            } else {
                autoNarrowStates(states)
                if (states.size == 1 && isAutoNarrowOkay) {
                    prefHelper.state = states[0]
                    buttonsUseCase.skip()
                    return
                }
            }
            if (isEditing) {
                setList(StringMessage.title_state, PrefHelper.KEY_STATE, states)
            } else {
                setList(StringMessage.title_state, PrefHelper.KEY_STATE, states)
                if (mainListUseCase.keyValue != null) {
                    buttonsUseCase.nextVisible = true
                }
                buttonsUseCase.centerVisible = true
            }
        }
    }

    private fun autoNarrowStates(states: MutableList<String>) {
        with(shared) {
            if (!isAutoNarrowOkay) {
                return
            }
            if (states.size == 1) {
                return
            }
            fabAddress?.let {
                val state = locationUseCase.matchState(it, states)
                if (state != null) {
                    states.clear()
                    states.add(state)
                }
                states.sort()
            }
        }
    }

}