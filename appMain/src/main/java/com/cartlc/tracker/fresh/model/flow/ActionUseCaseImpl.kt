package com.cartlc.tracker.fresh.model.flow

import com.cartlc.tracker.fresh.ui.common.observable.BaseObservableImpl
import com.cartlc.tracker.fresh.model.event.Action

class ActionUseCaseImpl : BaseObservableImpl<ActionUseCase.Listener>(), ActionUseCase {

    override fun dispatchActionEvent(action: Action) {
        listeners.forEach { it.onActionChanged(action) }
    }

}