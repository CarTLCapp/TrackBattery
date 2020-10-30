/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.ui.main.MainViewMvc
import com.cartlc.tracker.ui.util.helper.DialogHelper

class StageFinalConfirm(
        shared: MainController.Shared
) : ProcessBase(shared) {

    fun process() {
        with(shared) {
            buttonsUseCase.nextText = messageHandler.getString(StringMessage.btn_confirm)
            titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_confirmation)
        }
    }

    fun save(viewMvc: MainViewMvc, isNext: Boolean): Boolean {
        if (isNext) {
            showFinalConfirmDialog(viewMvc)
            return false
        }
        return true
    }

    private fun showFinalConfirmDialog(viewMvc: MainViewMvc) {
        with(shared) {
            dialogNavigator.showFinalConfirmDialog(object : DialogHelper.DialogListener {
                override fun onOkay() {
                    onConfirmOkay(viewMvc)
                }

                override fun onCancel() {}
            })
        }
    }

    private fun onConfirmOkay(viewMvc: MainViewMvc) {
        with(shared) {
            viewMvc.confirmUseCase?.onConfirmOkay()
            postUseCase.ping()
        }
    }

}