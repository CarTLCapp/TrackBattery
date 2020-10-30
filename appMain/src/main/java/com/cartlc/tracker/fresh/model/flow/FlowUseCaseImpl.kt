package com.cartlc.tracker.fresh.model.flow

import com.cartlc.tracker.fresh.ui.common.observable.BaseObservableImpl

class FlowUseCaseImpl : BaseObservableImpl<FlowUseCase.Listener>(), FlowUseCase {

    override var previousFlowValue: Flow? = null
    override var wasFromNotify: Boolean = false

    override var curFlow: Flow = LoginFlow()
        set(value) {
            wasFromNotify = false
            listeners.forEach { it.onStageChangedAboutTo(value) }
            previousFlowValue = field
            field = value
            listeners.forEach { it.onStageChanged(value) }
        }

    override fun notifyListeners() {
        wasFromNotify = true
        listeners.forEach { it.onStageChangedAboutTo(curFlow) }
        listeners.forEach { it.onStageChanged(curFlow) }
    }

}