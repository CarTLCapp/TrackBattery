/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter.item

import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvc

interface SubFlowItemViewMvc : ObservableViewMvc<SubFlowItemViewMvc.Listener> {

    interface Listener {
        fun onClicked(position: Int)
    }

    var selected: Boolean
    var title: String
    var completed: String
    var position: Int

}