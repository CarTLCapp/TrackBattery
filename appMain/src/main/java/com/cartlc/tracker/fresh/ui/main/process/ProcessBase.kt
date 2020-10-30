/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.model.flow.RootProjectFlow
import com.cartlc.tracker.fresh.model.flow.Stage
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.ui.main.MainController

open class ProcessBase(
        protected val shared: MainController.Shared
) {

    protected fun setList(msg: StringMessage, key: String, list: List<String>) {
        with(shared) {
            titleUseCase.mainTitleText = messageHandler.getString(msg)
            mainListUseCase.key = key
            if (list.isEmpty()) {
                onEmptyList()
            } else {
                mainListUseCase.visible = true
                mainListUseCase.simpleItems = list
            }
        }
    }

    private fun onEmptyList() {
        with(shared) {
            when (curFlowValue.stage) {
                Stage.ROOT_PROJECT -> {
                    prefHelper.reloadProjects()
                    postUseCase.ping()
                }
                Stage.SUB_PROJECT -> curFlowValue = RootProjectFlow()
                Stage.STREET,
                Stage.CITY,
                Stage.EQUIPMENT,
                Stage.STATE -> buttonsUseCase.center()
                else -> {
                }
            }
        }
    }
}