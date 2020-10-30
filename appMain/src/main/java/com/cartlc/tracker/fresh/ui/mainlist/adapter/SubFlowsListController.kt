/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter

import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.SubFlowItemViewMvc

class SubFlowsListController(
        private val repo: CarRepository,
        private val prefHelper: PrefHelper,
        private val listener: Listener
) : SubFlowsListAdapter.Listener,
        SubFlowItemViewMvc.Listener {

    private val items = mutableListOf<CarRepository.SubFlowInfo>()
    private var selected: Int = -1
    private var subFlowSelectedElementId: Long
        get() = repo.prefHelper.subFlowSelectedElementId
        set(value) {
            repo.prefHelper.subFlowSelectedElementId = value
        }

    // region public

    interface Listener {
        fun onSubFlowSelected(position: Int)
    }

    fun onDataChanged() {
        val selId = subFlowSelectedElementId
        selected = -1
        items.clear()
        prefHelper.currentEditEntry?.let { entry ->
            repo.currentFlowElement?.flowId?.let { flowId ->
                items.addAll(repo.progressInSubFlows(entry, flowId))
                for ((index, item) in items.withIndex()) {
                    if (selId == item.flowElementId) {
                        selected = index
                    }
                }
            }
        }
    }

    // endregion public

    // region SubFlowListAdapter.Listener

    override val count: Int
        get() = items.size

    override fun onSubFlowBind(position: Int, item: SubFlowItemViewMvc) {
        val data = items[position]
        item.position = position
        item.title = data.title
        item.completed = "" + data.completed + "/" + data.total
        item.selected = (selected == position)
        item.registerListener(this)
    }

    // endregion SubFlowListAdapter.Listener

    // region SubFlowItemViewMvc.Listener

    override fun onClicked(position: Int) {
        selected = position
        subFlowSelectedElementId = items[position].flowElementId
        listener.onSubFlowSelected(position)
    }

    // endregion SubFlowItemViewMvc.Listener

}