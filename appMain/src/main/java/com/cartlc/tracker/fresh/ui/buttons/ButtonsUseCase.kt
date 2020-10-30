package com.cartlc.tracker.fresh.ui.buttons

import com.cartlc.tracker.fresh.ui.common.observable.BaseObservable
import com.cartlc.tracker.fresh.model.event.Button
import com.cartlc.tracker.fresh.model.flow.Flow
import com.cartlc.tracker.fresh.ui.bits.SoftKeyboardDetect

interface ButtonsUseCase : BaseObservable<ButtonsUseCase.Listener> {

    interface Listener {
        fun onButtonConfirm(action: Button): Boolean
        fun onButtonEvent(action: Button)
    }

    var wasPrev: Boolean
    var wasNext: Boolean
    var wasSkip: Boolean

    var nextVisible: Boolean
    var prevVisible: Boolean
    var centerVisible: Boolean

    var nextText: String?
    var prevText: String?
    var centerText: String?

    var softKeyboardDetect: SoftKeyboardDetect?

    fun reset()
    fun reset(flow: Flow)

    // TODO: These elements will eventually be superseded
    fun skip()
    fun back()
    fun next()
    fun center()
}