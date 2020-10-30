package com.cartlc.tracker.fresh.ui.login

import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvc

interface LoginViewMvc : ObservableViewMvc<LoginViewMvc.Listener> {

    interface Listener {
        fun onFirstTechCodeChanged(value: String)
        fun onSecondaryTechCodeChanged(value: String)
        fun onSecondaryCheckBoxChanged(value: Boolean)
    }

    var firstTechCode: String
    var firstTechName: String
    var secondaryTechCode: String
    var secondaryTechName: String
    var secondaryTechCodeEnabled: Boolean
    var secondaryCheckBoxChecked: Boolean

}