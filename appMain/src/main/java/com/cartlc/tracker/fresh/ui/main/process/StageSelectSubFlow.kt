/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.ui.main.MainController

class StageSelectSubFlow(
        shared: MainController.Shared
) : ProcessBase(shared) {

    private val isFlowComplete: Boolean
        get() = with(shared) { repo.isCurrentFlowEntryComplete }

    // region public

    fun process() {
        with(shared) {
            if (detectSkip()) {
                return
            }
            titleUseCase.mainTitleVisible = true
            titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_sub_flows)
            mainListUseCase.visible = true
            titleUseCase.subTitleText = curProjectSubFlowElementHint
            buttonsUseCase.nextVisible = hasSelectedSubFlow || isFlowComplete
        }
    }

    fun save(): Boolean {
        return true
    }

    fun onAboutTo() {
        with(shared) {
            prefHelper.subFlowSelectedElementId = 0
        }
    }

    fun onSubFlowSelected() {
        with(shared) {
            buttonsUseCase.nextVisible = hasSelectedSubFlow
        }
    }

    // endregion public

    private fun detectSkip(): Boolean {
        with(shared) {
            repo.currentFlowElement?.let { element ->
                if (!repo.db.tableFlowElement.hasSubFlows(element.flowId)) {
                    buttonsUseCase.skip()
                    return true
                }
            } ?: run {
                buttonsUseCase.skip()
                return true
            }
        }
        return false
    }
}