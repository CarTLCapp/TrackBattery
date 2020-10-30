/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.app

import android.util.Log

import com.cartlc.tracker.BuildConfig
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable

import timber.log.Timber

/**
 * Created by dug on 8/16/17.
 */
class CrashReportingTree(private val db: DatabaseTable) : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.DEBUG) {
            if (BuildConfig.DEBUG) {
                Log.d(tag, message)
            }
        } else if (priority == Log.VERBOSE) {
            if (BuildConfig.DEBUG) {
                Log.v(tag, message)
            }
        } else if (priority == Log.WARN) {
            Log.w(tag, message)
        } else if (priority == Log.INFO) {
            Log.i(tag, message)
        } else {
            if (priority == Log.ERROR) {
                Log.e(tag, message)
            } else if (priority == Log.ASSERT) {
                Log.wtf(tag, message)
            }
            if (t != null) {
                db.tableCrash.message(priority, t.message ?: "unknown", message)
            } else {
                db.tableCrash.message(priority, message, null)
            }
        }
//        Timber.tag("CarTLC")
    }
}