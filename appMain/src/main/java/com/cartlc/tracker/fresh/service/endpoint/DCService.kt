/*
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.service.endpoint

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.event.EventController
import com.cartlc.tracker.fresh.model.event.EventPingStatus
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.fresh.service.endpoint.post.DCPostUseCaseImpl
import com.cartlc.tracker.fresh.service.network.NetworkUseCase
import com.cartlc.tracker.fresh.ui.app.TBApplication
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by dug on 5/22/17.
 */
class DCService : JobIntentService() {

    companion object {

        private val TAG = DCService::class.simpleName
        const val ACTION_ZIP_CODE = "zipcode"
        const val DATA_ZIP_CODE = "zipcode"

        private const val JOB_ID = 1962

        fun newInstance(context: Context) {
            val intent = Intent(context, DCService::class.java)
            enqueueWork(context, DCService::class.java, JOB_ID, intent)
        }
    }

    private val mZip: DCZip by lazy {  DCZip(db)}
    private val app: TBApplication by lazy { applicationContext as TBApplication }
    private val ping: DCPing by lazy { app.ping }
    private val repo: CarRepository by lazy { app.repo }
    private val db: DatabaseTable by lazy { repo.db }
    private val prefHelper: PrefHelper by lazy { repo.prefHelper }
    private val networkDetector: NetworkUseCase by lazy { app.componentRoot.networkUseCase }
    private val eventController: EventController by lazy { app.componentRoot.eventController }
    private val pingWorking = AtomicBoolean(false)

    override fun onHandleWork(intent: Intent) {
        if (!networkDetector.hasConnection) {
            msg("No connection -- service aborted & detector installed")
            eventController.post(EventPingStatus(uploadsAllDone = false, noConnection = true))
            return
        }
        val action = intent.action
        if (ACTION_ZIP_CODE == action) {
            val zipCode = intent.getStringExtra(DATA_ZIP_CODE)
            mZip.findZipCode(zipCode)
        } else {
            if (prefHelper.techID == 0 && prefHelper.hasCode) {
                prefHelper.firstTechCode?.let {
                    ping.sendRegistration(it, prefHelper.secondaryTechCode)
                } ?: run {
                    error("Need to register first!")
                    return
                }
            }
            if (pingWorking.get()) {
                msg("ping() request ignored: already working")
            } else {
                pingWorking.set(true)
                try {
                    msg("ping() request started")
                    ping.ping()
                } catch (ex: Exception) {
                    error(ex)
                } finally {
                    pingWorking.set(false)
                    msg("ping() request complete")
                }
            }
        }
    }

    private fun msg(msg: String) {
        Timber.tag(TAG).i(msg)
    }

    private fun error(msg: String) {
        Timber.tag(TAG).e(msg)
    }


}
