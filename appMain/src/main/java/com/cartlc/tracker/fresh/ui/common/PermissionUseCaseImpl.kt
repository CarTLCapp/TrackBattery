package com.cartlc.tracker.fresh.ui.common

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.anthonycr.grant.PermissionsManager
import com.anthonycr.grant.PermissionsResultAction
import java.util.concurrent.atomic.AtomicBoolean

class PermissionUseCaseImpl(
        private val activity: Activity
) : PermissionUseCase {

    companion object {

        fun hasPermission(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

    }

    private val context: Context
        get() = activity
    private var pendingResponse = AtomicBoolean(false)

    private val requestPending: Boolean
        get() = pendingResponse.get()

    private var showedRationale = false
    private var alertDialog: AlertDialog? = null
    private fun map(perms: Array<PermissionUseCase.PermissionRequest>): Array<String> {
        val array = mutableListOf<String>()
        perms.forEach { array.add(it.permission) }
        return array.toTypedArray()
    }

    private fun lookupExplain(perms: Array<PermissionUseCase.PermissionRequest>, perm: String): Int? {
        return perms.find { it.permission == perm }?.explanationResId
    }

    // region PermissionUseCase

    override fun checkPermissions(perms: Array<PermissionUseCase.PermissionRequest>, listener: PermissionUseCase.Listener) {
        if (arePermissionsGranted(perms)) {
            requestEnd()
            listener.onSuccess()
            return
        }
        if (requestPending) {
            return
        }
        requestStart()
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(activity, map(perms), object : PermissionsResultAction() {
            override fun onGranted() {
                listener.onSuccess()
                requestEnd()
            }

            override fun onDenied(permission: String) {
                if (!showedRationale) {
                    showedRationale = true
                    lookupExplain(perms, permission)?.let { explainId ->
                        postExplanation(explainId) { accepted ->
                            if (accepted) {
                                listener.onDenied(false)
                            } else {
                                listener.onDenied(true)
                            }
                        }
                    }
                }
                requestEnd()
            }
        })
    }

    override fun onHandlePermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }


    // endregion PermissionUseCase

    private fun hasPermission(permission: String): Boolean {
        return hasPermission(context, permission)
    }

    private fun requestStart() {
        pendingResponse.set(true)
    }

    private fun requestEnd() {
        pendingResponse.set(false)
    }

    private fun arePermissionsGranted(perms: Array<PermissionUseCase.PermissionRequest>): Boolean {
        perms.forEach {
            if (!hasPermission(it.permission)) {
                return false
            }
        }
        return true
    }


    fun postExplanation(@StringRes explainResId: Int, onDone: (accepted: Boolean) -> Unit) {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(explainResId)
        builder.setCancelable(false)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            onDone(true)
        }
        builder.setOnCancelListener {
            alertDialog = null
            onDone(false)
        }
        alertDialog?.dismiss()
        val alertDialog = builder.create()
        this.alertDialog = alertDialog
        alertDialog.show()
    }
}