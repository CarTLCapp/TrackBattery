/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.SubFlowItemViewMvc

class SubFlowsListAdapter(
        private val viewMvcFactory: FactoryViewMvc,
        private val listener: Listener?
) : RecyclerView.Adapter<SubFlowsListAdapter.MyViewHolder>() {

    interface Listener {
        val count: Int
        fun onSubFlowBind(position: Int, item: SubFlowItemViewMvc)
    }

    class MyViewHolder(val viewMvc: SubFlowItemViewMvc) : RecyclerView.ViewHolder(viewMvc.rootView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(viewMvcFactory.allocSubFlowItemViewMvc(parent))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        listener?.let { it.onSubFlowBind(position, holder.viewMvc) }
    }

    override fun getItemCount(): Int {
        return listener?.count ?: 0
    }

}
