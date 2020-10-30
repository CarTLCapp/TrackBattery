/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.ui.util.CheckError

class StageCurrentProject(
        shared: MainController.Shared
) : ProcessBase(shared) {

    fun process() {
        with(shared) {
            if (!repo.flowUseCase.wasFromNotify) {
                postUseCase.ping()
            }
            checkErrors()
            mainListUseCase.visible = true
            titleUseCase.separatorVisible = true
            titleUseCase.mainTitleVisible = true
            buttonsUseCase.centerVisible = true
            buttonsUseCase.prevVisible = hasCurrentProject
            buttonsUseCase.prevText = messageHandler.getString(StringMessage.btn_edit)
            buttonsUseCase.centerText = messageHandler.getString(StringMessage.btn_new_project)
            titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_current_project)
            pictureUseCase.clearCache()
        }
    }

    private fun checkErrors() {
        with(shared) {
            if (!prefHelper.doErrorCheck) {
                return
            }
            val entry = checkEntryErrors()
            if (entry != null) {
                screenNavigator.showTruckError(entry, object : CheckError.CheckErrorResult {
                    override fun doEdit() {
                        onEditEntry()
                    }

                    override fun doDelete(entry: DataEntry) {
                        db.tableEntry.remove(entry)
                    }

                    override fun setFromEntry(entry: DataEntry) {
                        prefHelper.setFromEntry(entry)
                    }
                })
            } else {
                prefHelper.doErrorCheck = false
            }
        }
    }

    private fun checkEntryErrors(): DataEntry? = shared.repo.checkEntryErrors()

}