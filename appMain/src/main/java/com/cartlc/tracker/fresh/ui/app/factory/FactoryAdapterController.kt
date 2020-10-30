/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.app.factory

import com.cartlc.tracker.fresh.ui.mainlist.adapter.*
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.msg.MessageHandler
import com.cartlc.tracker.fresh.model.pref.PrefHelper

class FactoryAdapterController(
        private val repo: CarRepository,
        private val messageHandler: MessageHandler,
        private val prefHelper: PrefHelper
) {
    fun allocSimpleListController(listener: SimpleListController.Listener): SimpleListController {
        return SimpleListController(listener)
    }

    fun allocProjectGroupController(listener: ProjectGroupListController.Listener): ProjectGroupListController {
        return ProjectGroupListController(repo, messageHandler, listener)
    }

    fun allocEquipmentSelectController(listener: EquipmentSelectController.Listener): EquipmentSelectController {
        return EquipmentSelectController(repo, listener)
    }

    fun allocRadioListController(listener: RadioListController.Listener): RadioListController {
        return RadioListController(listener)
    }

    fun allocNoteListEntryController(listener: NoteListEntryController.Listener): NoteListEntryController {
        return NoteListEntryController(repo, listener)
    }

    fun allocCheckBoxListController(listener: CheckBoxListController.Listener): CheckBoxListController {
        return CheckBoxListController(listener)
    }

    fun allocSubFlowListController(listener: SubFlowsListController.Listener): SubFlowsListController {
        return SubFlowsListController(repo, prefHelper, listener)
    }

}