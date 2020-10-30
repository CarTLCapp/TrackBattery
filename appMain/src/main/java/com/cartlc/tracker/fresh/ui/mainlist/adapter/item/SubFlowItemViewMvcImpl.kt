/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvcImpl

class SubFlowItemViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ObservableViewMvcImpl<SubFlowItemViewMvc.Listener>(), SubFlowItemViewMvc {

    override val rootView: View = inflater.inflate(R.layout.mainlist_item_subflow, container, false)

    private val titleView = findViewById<TextView>(R.id.title)
    private val completedView = findViewById<TextView>(R.id.completed)

    init {
        rootView.setOnClickListener { listeners.forEach { it.onClicked(position) }}
    }

    // region SubFlowItemViewMvc

    override var position: Int = 0

    override var title: String
        get() = titleView.text.toString()
        set(value) { titleView.text = value }
    override var completed: String
        get() = completedView.text.toString()
        set(value) { completedView.text = value }

    override var selected: Boolean = false
        set(value) {
            field = value
            if (value) {
                rootView.setBackgroundResource(R.color.list_item_selected)
            } else {
                rootView.setBackgroundResource(android.R.color.transparent)
            }
        }

    // endregion SubFlowItemViewMvc

}