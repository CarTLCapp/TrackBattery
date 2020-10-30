/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.model.flow.*
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import java.util.ArrayList

class StageCity (
        shared: MainController.Shared
) : ProcessBase(shared) {

    fun process(flow: Flow) {
        with(shared) {
            var isEditing = flow.stage == Stage.ADD_CITY
            titleUseCase.subTitleText = if (isEditing) editProjectHint else curProjectHint
            titleUseCase.mainTitleVisible = true
            titleUseCase.subTitleVisible = true
            buttonsUseCase.nextVisible = false
            val company = prefHelper.company
            val zipcode = prefHelper.zipCode
            val state = prefHelper.state
            if (company == null) {
                curFlowValue = CompanyFlow()
                return
            }
            if (state == null) {
                curFlowValue = StateFlow()
                return
            }
            var cities: MutableList<String> = db.tableAddress.queryCities(company, zipcode, state).toMutableList()
            if (cities.isEmpty()) {
                val city = zipcode?.let { db.tableZipCode.queryCity(zipcode) }
                if (city != null) {
                    cities = ArrayList()
                    cities.add(city)
                } else {
                    isEditing = true
                }
            }
            val hint: String?
            if (isEditing) {
                cities = mutableListOf()
                hint = null
            } else {
                autoNarrowCities(cities)
                if (cities.size == 1 && isAutoNarrowOkay) {
                    prefHelper.city = cities[0]
                    buttonsUseCase.skip()
                    return
                } else {
                    hint = prefHelper.address
                }
            }
            if (isEditing) {
                titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_city)
                entrySimpleUseCase.showing = true
                entrySimpleUseCase.simpleTextClear()
                entrySimpleUseCase.hintValue = messageHandler.getString(StringMessage.title_city)
                if (flow.stage == Stage.CITY) {
                    curFlowValue = AddCityFlow()
                }
            } else {
                entrySimpleUseCase.entryTextValue = hint
                mainListUseCase.visible = true
                setList(StringMessage.title_city, PrefHelper.KEY_CITY, cities)
                if (mainListUseCase.keyValue != null) {
                    buttonsUseCase.nextVisible = true
                }
                buttonsUseCase.centerVisible = true
            }
        }
    }

    private fun autoNarrowCities(cities: MutableList<String>) {
        with(shared) {
            if (!isAutoNarrowOkay) {
                return
            }
            if (cities.size == 1) {
                return
            }
            fabAddress?.let {
                val city = locationUseCase.matchCity(it, cities)
                if (city != null) {
                    cities.clear()
                    cities.add(city)
                }
                cities.sort()
            }
        }
    }

    fun saveAdd(): Boolean {
        with(shared) {
            prefHelper.city = entrySimpleUseCase.entryTextValue ?: ""
            return prefHelper.city?.isNotBlank() ?: false
        }
    }

}
