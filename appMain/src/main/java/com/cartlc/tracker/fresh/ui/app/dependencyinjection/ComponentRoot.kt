/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.app.dependencyinjection

import android.app.Application
import android.view.LayoutInflater
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.SchedulerPlan
import com.cartlc.tracker.fresh.model.SchedulerPlanImpl
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.event.EventController
import com.cartlc.tracker.fresh.model.flow.FlowUseCase
import com.cartlc.tracker.fresh.model.msg.MessageHandler
import com.cartlc.tracker.fresh.model.msg.MessageHandlerImpl
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.fresh.service.alarm.AlarmController
import com.cartlc.tracker.fresh.service.endpoint.DCPing
import com.cartlc.tracker.fresh.service.endpoint.DCServerRx
import com.cartlc.tracker.fresh.service.endpoint.post.DCPostController
import com.cartlc.tracker.fresh.service.endpoint.post.DCPostControllerImpl
import com.cartlc.tracker.fresh.service.endpoint.post.DCPostUseCase
import com.cartlc.tracker.fresh.service.endpoint.post.DCPostUseCaseImpl
import com.cartlc.tracker.fresh.service.network.NetworkUseCase
import com.cartlc.tracker.fresh.service.network.NetworkUseCaseImpl
import com.cartlc.tracker.fresh.service.instabug.InstaBugUseCase
import com.cartlc.tracker.fresh.ui.app.factory.FactoryAdapterController
import com.cartlc.tracker.fresh.ui.app.factory.FactoryController
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.common.BitmapHelper
import com.cartlc.tracker.fresh.ui.common.ContextWrapper
import com.cartlc.tracker.fresh.ui.common.DeviceHelper
import com.cartlc.tracker.fresh.ui.common.PermissionHelper

class ComponentRoot(
        app: Application,
        val db: DatabaseTable,
        val prefHelper: PrefHelper,
        val flowUseCase: FlowUseCase,
        val repo: CarRepository,
        val ping: DCPing,
        dcRx: DCServerRx
) {

    val contextWrapper: ContextWrapper by lazy { ContextWrapper(app) }
    val messageHandler: MessageHandler by lazy { MessageHandlerImpl(app) }
    private val factoryAdapterController: FactoryAdapterController by lazy { FactoryAdapterController(repo, messageHandler, prefHelper) }
    val factoryViewMvc: FactoryViewMvc by lazy { FactoryViewMvc(LayoutInflater.from(app), factoryAdapterController) }
    private val schedulerPlan: SchedulerPlan by lazy { SchedulerPlanImpl() }
    val factoryController: FactoryController by lazy { FactoryController(dcRx, schedulerPlan) }
    val eventController: EventController by lazy { EventController() }
    val permissionHelper = PermissionHelper(app)
    val deviceHelper = DeviceHelper(app)
    val bitmapHelper = BitmapHelper()
    val alarmController: AlarmController by lazy { AlarmController(app, repo) }
    val networkUseCase: NetworkUseCase by lazy { NetworkUseCaseImpl(app) }
    val instaBugUseCase = InstaBugUseCase(app)
    val postController: DCPostController by lazy { DCPostControllerImpl(db, eventController) }
    val postUseCase: DCPostUseCase by lazy { DCPostUseCaseImpl(app, eventController, prefHelper) }

}
