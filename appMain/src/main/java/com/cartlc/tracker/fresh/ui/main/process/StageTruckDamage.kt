/*
 * Copyright 2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.msg.StringMessage

class StageTruckDamage(
        shared: MainController.Shared,
        private val taskPicture: TaskPicture
) : ProcessBase(shared) {

    fun process() {
        with(shared) {
            picturesVisible = true
            prefHelper.truckHasDamage?.let { hasDamage ->
                if (!hasDamage) {
                    if (buttonsController.wasPrev) {
                        prefHelper.truckHasDamage = null
                        curFlowValue.prev()
                    } else {
                        curFlowValue.next()
                    }
                    return
                }
            } ?: run {
                dialogNavigator.showTruckDamageQuery { answer ->
                    prefHelper.truckHasDamage = answer
                    process()
                }
                return
            }
            var showToast = false
            if (buttonsController.wasNext) {
                showToast = true
                buttonsController.wasNext = false
            }
            pictureUseCase.pictureItems = db.tablePicture.removeFileDoesNotExist(
                    db.tablePicture.query(prefHelper.currentPictureCollectionId, repo.curFlowValueStage)
            )
            // TODO: Perhaps if this is an existing value (see DataEntry.notesWithValues), then overlay those values
            db.tableNote.noteTruckDamage?.let { pictureUseCase.pictureNotes = listOf(it) }

            val numPictures = pictureUseCase.pictureItems.size
            if (numPictures == 0) {
                if (showToast) {
                    screenNavigator.showToast(messageHandler.getString(StringMessage.truck_damage_request))
                }
                buttonsController.nextVisible = false

                if (taskPicture.takingPictureAborted) {
                    buttonsController.centerVisible = true
                    buttonsController.centerText = messageHandler.getString(StringMessage.btn_another)
                } else {
                    taskPicture.dispatchPictureRequest()
                }
            } else {
                if (!hasTruckDamageValue) {
                    if (showToast) {
                        screenNavigator.showToast(messageHandler.getString(StringMessage.truck_damage_enter))
                    }
                    buttonsController.nextVisible = false
                }
            }
            titleUseCase.mainTitleText = title
            titleUseCase.mainTitleVisible = true
        }
    }

    val okToAsk: Boolean
        get() {
            with(shared) {
                return repo.currentFlowId?.let { flowId ->
                    repo.db.tableFlow.queryById(flowId)?.hasFlagTruckNumber ?: true
                } ?: true
            }
        }

    private val hasTruckDamageValue: Boolean
        get() {
            with(shared) {
                return db.tableNote.noteTruckDamage?.value?.isNotBlank() ?: false
            }
        }

    private val title: String?
        get() {
            with(shared) {
                return db.tableNote.noteTruckDamage?.name
            }
        }

    fun save(): Boolean {
        return true
    }

    fun pictureStateChanged() {
        with(shared) {
            buttonsController.nextVisible = pictureUseCase.pictureItems.size == 1 && hasTruckDamageValue

            val currentNumPictures = pictureUseCase.pictureItems.size
            if (currentNumPictures == 0) {
                buttonsController.centerVisible = true
                buttonsController.centerText = messageHandler.getString(StringMessage.btn_another)
            }
        }
    }

    fun clearDamage() {
        with(shared) {
            prefHelper.truckHasDamage = null
            process()
        }
    }

    fun center() {
        taskPicture.clearFlags()
        taskPicture.dispatchPictureRequest()
    }

}