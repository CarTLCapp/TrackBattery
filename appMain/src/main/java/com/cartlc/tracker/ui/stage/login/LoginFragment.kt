package com.cartlc.tracker.ui.stage.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cartlc.tracker.ui.app.FactoryController
import com.cartlc.tracker.ui.app.FactoryViewMvc
import com.cartlc.tracker.ui.app.dependencyinjection.ComponentRoot
import com.cartlc.tracker.ui.base.BaseFragment
import com.cartlc.tracker.ui.stage.StageHook

class LoginFragment(
        private val stageHook: StageHook
) : BaseFragment() {

    private val componentRoot: ComponentRoot
        get() = boundFrag.componentRoot
    private val factoryViewMvc: FactoryViewMvc
        get() = componentRoot.factoryViewMvc
    private val factoryController: FactoryController
        get() = componentRoot.factoryController

    private lateinit var controller: LoginController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewMvc = factoryViewMvc.allocLoginViewMvc(null)
        controller = factoryController.allocLoginController(boundFrag, viewMvc, stageHook)
        return viewMvc.rootView
    }

}