/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.ui.common

import android.content.Context
import android.content.pm.PackageManager
import androidx.fragment.app.FragmentActivity
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.fresh.ui.app.TBApplication

class DeviceHelper(
        val act: Context
) {

    private val repo: CarRepository = (act.applicationContext as TBApplication).repo
    private val prefHelper: PrefHelper = repo.prefHelper
    private val packageManager = act.packageManager
    private val packageName = act.packageName

    val versionName: String
        @Throws(PackageManager.NameNotFoundException::class)
        get() {
            val sbuf = StringBuilder()
            val version = packageManager.getPackageInfo(packageName, 0).versionName
            sbuf.append("v")
            sbuf.append(version)
            if (prefHelper.isDevelopment) {
                sbuf.append("d")
            }
            return sbuf.toString()
        }

    val versionInt: Int
        get() {
            return prefHelper.convertVersionToInt(packageManager.getPackageInfo(packageName, 0).versionName)
        }

}