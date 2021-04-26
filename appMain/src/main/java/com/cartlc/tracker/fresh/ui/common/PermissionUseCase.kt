package com.cartlc.tracker.fresh.ui.common

import androidx.annotation.StringRes

interface PermissionUseCase {

    interface Listener {
        fun onSuccess()
        fun onDenied(onExit: Boolean)
    }
    data class PermissionRequest(val permission: String, @StringRes val explanationResId: Int)

    fun checkPermissions(perms: Array<PermissionRequest>, listener: Listener)
    fun onHandlePermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)

}