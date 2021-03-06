package com.cartlc.tracker.fresh.model.flow

import com.cartlc.tracker.fresh.ui.common.observable.BaseObservable
import com.cartlc.tracker.fresh.model.event.Action

interface ActionUseCase : BaseObservable<ActionUseCase.Listener> {

    interface Listener {

        fun onActionChanged(action: Action)
    }

    fun dispatchActionEvent(action: Action)

}