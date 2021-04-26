/*
 * Copyright 2020-2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.app

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.multidex.MultiDex
import com.cartlc.tracker.BuildConfig
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.VehicleRepository
import com.cartlc.tracker.fresh.model.core.sql.DatabaseManager
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.event.EventError
import com.cartlc.tracker.fresh.model.flow.FlowUseCase
import com.cartlc.tracker.fresh.model.flow.FlowUseCaseImpl
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.fresh.service.endpoint.DCPing
import com.cartlc.tracker.fresh.service.endpoint.DCServerRx
import com.cartlc.tracker.fresh.service.endpoint.DCServerRxImpl
import com.cartlc.tracker.fresh.service.endpoint.post.DCPostUseCase
import com.cartlc.tracker.fresh.service.help.AmazonHelper
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.ComponentRoot
import com.cartlc.tracker.fresh.ui.common.PermissionUseCase
import com.cartlc.tracker.fresh.ui.common.PermissionUseCase.PermissionRequest
import com.cartlc.tracker.fresh.ui.common.PermissionUseCaseImpl
import com.cartlc.tracker.ui.util.CheckError
import com.cartlc.tracker.ui.util.helper.LocationHelper
import com.cartlc.tracker.viewmodel.vehicle.VehicleViewModel
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.io.File

class TBApplication : Application() {

    companion object {

        const val REPORT_LOCATION = false

        val PERMISSIONS = arrayOf(
                PermissionRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        R.string.perm_read_external_storage),
                PermissionRequest(Manifest.permission.READ_EXTERNAL_STORAGE,
                        R.string.perm_write_external_storage),
                PermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION,
                        R.string.perm_location),
                PermissionRequest(Manifest.permission.REQUEST_INSTALL_PACKAGES,
                        R.string.perm_install_packages))

        fun getUri(ctx: Context, file: File): Uri {
            return FileProvider.getUriForFile(ctx, "com.cartcl.tracker.fileprovider", file)
        }

        fun ReportError(ex: Exception, claz: Class<*>, function: String, type: String): String {
            return ReportError(ex.message
                    ?: "unknown", claz, function, type)
        }

        fun ReportError(msg: String, claz: Class<*>, function: String, type: String): String {
            val sbuf = StringBuilder()
            sbuf.append("Class:")
            sbuf.append(claz.simpleName)
            sbuf.append(".")
            sbuf.append(function)
            sbuf.append(" ")
            sbuf.append(type)
            sbuf.append(": ")
            sbuf.append(msg)
            Timber.e(sbuf.toString())
            return sbuf.toString()
        }

        fun ReportServerError(ex: Exception, claz: Class<*>, function: String, type: String): String {
            val msg = ReportError(ex, claz, function, type)
            ShowError(msg)
            return msg
        }

        fun ShowError(msg: String) {
            EventBus.getDefault().post(EventError(msg))
        }
    }

    private val carRepo: CarRepository by lazy {
        CarRepository(
                dm,
                prefHelper,
                flowUseCase
        )
    }
    val componentRoot: ComponentRoot by lazy {
        ComponentRoot(this,
                dm,
                prefHelper,
                flowUseCase,
                carRepo,
                ping,
                dcRx
        )
    }

    private val prefHelper: PrefHelper by lazy { PrefHelper(this, dm) }
    private val dm: DatabaseManager by lazy { DatabaseManager(this) }
    private val dcRx: DCServerRx by lazy { DCServerRxImpl(ping) }
    private val vehicleRepository: VehicleRepository by lazy {VehicleRepository(this, dm) }
    private val postUseCase: DCPostUseCase by lazy { componentRoot.postUseCase }

    val amazonHelper: AmazonHelper by lazy { AmazonHelper(db) }
    val flowUseCase: FlowUseCase by lazy { FlowUseCaseImpl() }
    val ping: DCPing by lazy { DCPing(this, repo) }
    val vehicleViewModel: VehicleViewModel by lazy {VehicleViewModel(vehicleRepository) }
    val repo: CarRepository
        get() = carRepo

    val db: DatabaseTable
        get() = dm

    val version: String
        get() = componentRoot.deviceHelper.versionName

    override fun onCreate() {
        super.onCreate()
        carRepo.db.tablePicture.removeFileDoesNotExist()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree(db))
        }
        CheckError.Init()
        LocationHelper.Init(this, db)
        if (prefHelper.detectOneTimeReloadFromServerCheck()) {
            postUseCase.reloadFromServer()
        }
        carRepo.computeCurStageLight()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun checkPermissions(act: Activity, listener: PermissionUseCase.Listener) {
        val useCase = PermissionUseCaseImpl(act)
        useCase.checkPermissions(PERMISSIONS, listener)
    }

//    fun requestZipCode(tableZipCode: String) {
//        val data = db.tableZipCode.query(tableZipCode)
//        if (data != null) {
//            data.check()
//            EventBus.getDefault().post(data)
//        } else if (ServerHelper.instance.hasConnection(this)) {
//            val intent = Intent(this, DCService::class.java)
//            intent.action = DCService.ACTION_ZIP_CODE
//            intent.putExtra(DCService.DATA_ZIP_CODE, tableZipCode)
//            startService(intent)
//        }
//    }

}
