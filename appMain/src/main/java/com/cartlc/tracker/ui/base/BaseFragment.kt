package com.cartlc.tracker.ui.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.viewmodel.BaseViewModel

open class BaseFragment : Fragment() {

    protected lateinit var baseVM: BaseViewModel // TODO: Will eventually be obsoleted

    protected lateinit var app: TBApplication
    protected lateinit var repo: CarRepository
    protected lateinit var boundFrag: BoundFrag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = activity!!.applicationContext as TBApplication
        repo = app.repo
        boundFrag = BoundFrag(this)
    }
}