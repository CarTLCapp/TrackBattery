/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main.process

import android.text.InputType
import com.cartlc.tracker.fresh.model.flow.*
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.model.pref.PrefHelper

class StageStreet(
        shared: MainController.Shared
) : ProcessBase(shared) {

    fun process(flow: Flow) {

        with(shared) {
            var isEditing = flow.stage == Stage.ADD_STREET
            buttonsUseCase.nextVisible = false
            titleUseCase.subTitleText = if (isEditing) editProjectHint else curProjectHint
            titleUseCase.mainTitleVisible = true
            titleUseCase.subTitleVisible = true
            val company = prefHelper.company
            val city = prefHelper.city
            val state = prefHelper.state
            if (company == null) {
                curFlowValue = CompanyFlow()
                return
            }
            if (state == null) {
                curFlowValue = StateFlow()
                return
            }
            if (city == null) {
                curFlowValue = CityFlow()
                return
            }
            var streets = db.tableAddress.queryStreets(
                    company,
                    city,
                    state,
                    prefHelper.zipCode)
            if (streets.isEmpty()) {
                isEditing = true
            }
            val hint: String?
            if (isEditing) {
                streets = mutableListOf()
                hint = null
            } else {
                streets = streets.toMutableList()
                autoNarrowStreets(streets)
                if (streets.size == 1 && isAutoNarrowOkay) {
                    prefHelper.street = streets[0]
                    buttonsUseCase.skip()
                    return
                } else {
                    hint = prefHelper.address
                }
            }
            if (isEditing) {
                titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_street)
                entrySimpleUseCase.showing = true
                entrySimpleUseCase.simpleTextClear()
                entrySimpleUseCase.hintValue = messageHandler.getString(StringMessage.title_street)
                entrySimpleUseCase.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                if (flow.stage == Stage.STREET) {
                    curFlowValue = AddStreetFlow()
                }
            } else {
                entrySimpleUseCase.helpValue = hint
                buttonsUseCase.centerVisible = true
                mainListUseCase.visible = true
                setList(StringMessage.title_street, PrefHelper.KEY_STREET, streets)
                if (mainListUseCase.keyValue != null) {
                    buttonsUseCase.nextVisible = true
                }
            }
        }
    }

    private fun autoNarrowStreets(streets: MutableList<String>) {
        with(shared) {
            if (!isAutoNarrowOkay) {
                return
            }
            if (streets.size == 1) {
                return
            }
            fabAddress?.let {
                val reduced = locationUseCase.reduceStreets(it, streets)
                if (reduced.isNotEmpty()) {
                    streets.clear()
                    streets.addAll(reduced)
                }
                streets.sort()
            }
        }
    }

    fun saveAdd(isNext: Boolean): Boolean {
        with (shared) {
            prefHelper.street = entrySimpleUseCase.entryTextValue ?: ""
            return prefHelper.street?.isNotBlank() ?: run { !isNext }
        }
    }
}
