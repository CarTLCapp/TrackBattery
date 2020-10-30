/*
 * Copyright 2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.service.help

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

import com.google.android.gms.iid.InstanceID

/**
 * Created by dug on 5/22/17.
 */
object ServerHelper {

    fun deviceId(ctx: Context): String {
        return InstanceID.getInstance(ctx).id
    }

}

