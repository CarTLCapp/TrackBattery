/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.app.dependencyinjection

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.service.LocationUseCase
import com.cartlc.tracker.fresh.service.LocationUseCaseImpl
import com.cartlc.tracker.fresh.service.update.UpdateAppUseCase
import com.cartlc.tracker.fresh.service.update.UpdateAppUseCaseImpl
import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewHelper
import com.cartlc.tracker.fresh.ui.common.*
import com.cartlc.tracker.ui.util.helper.DialogHelper

open class BoundAct(
        val act: FragmentActivity
) {

    val repo: CarRepository = (act.applicationContext as TBApplication).repo
    val componentRoot: ComponentRoot = (act.applicationContext as TBApplication).componentRoot

    open val lifecycleOwner: LifecycleOwner
        get() = act

    val isFinishing: Boolean
        get() = act.isDestroyed || act.isFinishing

    val dialogHelper: DialogHelper
        get() = DialogHelper(act)

    val dialogNavigator: DialogNavigator
        get() = DialogNavigator(act, componentRoot.messageHandler)

    val fragmentHelper: FragmentHelper
        get() = FragmentHelper(act)

    val factoryViewHelper: FactoryViewHelper
        get() = FactoryViewHelper(this)

    val locationUseCase: LocationUseCase
        get() = LocationUseCaseImpl(act)

    val screenNavigator: ScreenNavigator
        get() = ScreenNavigator(act)

    val updateAppUseCase: UpdateAppUseCase by lazy {
        with(componentRoot) {
            UpdateAppUseCaseImpl(act, messageHandler, prefHelper, deviceHelper)
        }
    }

    val permissionUseCase: PermissionUseCase by lazy {
        PermissionUseCaseImpl(act)
    }

    open fun bindObserver(observer: LifecycleObserver): LifecycleObserver {
        (act as LifecycleOwner).lifecycle.addObserver(observer)
        return observer
    }

}