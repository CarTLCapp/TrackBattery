/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.service.alarm

import android.content.Context
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.flow.LoginFlow
import timber.log.Timber
import java.util.concurrent.TimeUnit


class AlarmController(
        private val context: Context,
        private val repo: CarRepository
) {

    companion object {
        private val TAG = AlarmController::class.simpleName

        private const val DEBUG = false

        private val CHECK_ACTIVITY_TRIGGER_MINUTES = if (DEBUG) 2 else 60
        private val CHECK_ACTIVITY_TRIGGER = TimeUnit.MINUTES.toMillis(CHECK_ACTIVITY_TRIGGER_MINUTES.toLong())
        private val CHECK_ACTIVITY_TRIGGER_RETRY = CHECK_ACTIVITY_TRIGGER

        private val JUST_LOGGED_IN_TRIGGER = if (DEBUG) TimeUnit.MINUTES.toMillis(2) else TimeUnit.HOURS.toMillis(24)

        private const val ACTION_LOGOUT = "logout"

        fun justLoggedIn(context: Context) {
            AlarmReceiver.scheduleIn(context, ACTION_LOGOUT, JUST_LOGGED_IN_TRIGGER)
        }
    }

    private val prefHelper = repo.prefHelper

    private val hasHadRecentActivity: Boolean
        get() {
            val lastTime = prefHelper.lastActivityTime
            val now = System.currentTimeMillis()
            val diff = now - lastTime
            return diff < CHECK_ACTIVITY_TRIGGER
        }

    fun onAction(action: String?) {
        if (action == ACTION_LOGOUT) {
            if (!hasHadRecentActivity) {
                clearLogin()
                repo.curFlowValue = LoginFlow()
            } else {
                AlarmReceiver.scheduleIn(context, ACTION_LOGOUT, CHECK_ACTIVITY_TRIGGER_RETRY)
            }
        } else {
            Timber.tag(TAG).e("Unrecognized action received: $action")
        }
    }

    private fun clearLogin() {
        Timber.tag(TAG).i("LOGIN CLEARED")
        prefHelper.firstTechCode = null
        prefHelper.techFirstName = null
        prefHelper.techLastName = null
        prefHelper.techID = 0
        prefHelper.secondaryTechCode = null
        prefHelper.secondaryTechFirstName = null
        prefHelper.secondaryTechLastName = null
        prefHelper.secondaryTechID = 0
    }
}