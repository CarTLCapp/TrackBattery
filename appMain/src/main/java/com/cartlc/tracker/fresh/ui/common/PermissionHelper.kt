/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.ui.common

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import timber.log.Timber
import java.util.*

/*
 Example call in Application class:

 val PERMISSIONS = arrayOf(
     PermissionRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE,R.string.perm_read_external_storage),
     PermissionRequest(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.perm_write_external_storage),
     PermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, R.string.perm_location)
 )
 PermissionHelper(context).checkPermissions(PERMISSIONS, listener)
*/

class PermissionHelper(
        private val context: Context
) {

    companion object {
        private val TAG = PermissionHelper::class.simpleName

        private const val LOG = false
        private const val REQUEST_CODE = 111
    }

    interface PermissionListener {
        fun onGranted(permission: String)
        fun onDenied(permission: String)
    }

    private var tokens = HashMap<String, RequestToken>()
    private var live: Queue<RequestToken> = LinkedList()

    private var alertDialog: AlertDialog? = null
    private var lastRequests = mutableListOf<RequestToken>()

    class PermissionRequest(var permission: String, var explanationResId: Int)

    @TargetApi(23)
    private inner class RequestToken(
            var activity: Activity,
            val permission: String,
            var explainResId: Int
    ) {
        var mListeners = ArrayList<PermissionListener>()

        var explainApproved: Boolean = false
        val needsExplanation: Boolean = explainResId != 0 && !explainApproved && shouldShowExplanation
        val activityDied: Boolean
            get() = activity.isFinishing || activity.isDestroyed
        val hasPermission: Boolean
            get() {
                return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            }

        private val shouldShowExplanation: Boolean
            get() = activity.shouldShowRequestPermissionRationale(permission)

        init {
            tokens[permission] = this
        }

        fun postExplanation(onDone: () -> Unit): Boolean {
            try {
                val builder = AlertDialog.Builder(activity)
                builder.setMessage(explainResId)
                builder.setCancelable(false)
                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    explainApproved = true
                    onDone()
                }
                builder.setOnCancelListener { alertDialog = null }
                alertDialog = builder.create()
                alertDialog!!.show()
                return true
            } catch (ex: Exception) {
                error(ex)
            }
            return false
        }

        fun handlePermissionResult(grantResult: Int) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                informListenersGranted()
            } else {
                informListenersDenied()
            }
        }

        fun addListener(listener: PermissionListener?) {
            if (listener != null) {
                if (!mListeners.contains(listener)) {
                    mListeners.add(listener)
                }
            }
        }

        fun informListenersGranted() {
            for (listener in mListeners) {
                listener.onGranted(permission)
            }
            mListeners.clear()
        }

        private fun informListenersDenied() {
            for (listener in mListeners) {
                listener.onDenied(permission)
            }
            mListeners.clear()
        }

        override fun toString(): String {
            return "PermissionRequest[$permission]"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RequestToken

            if (permission != other.permission) return false

            return true
        }

        override fun hashCode(): Int {
            return permission.hashCode()
        }
    }

    fun checkPermissions(act: Activity, requests: Array<PermissionRequest>, listener: PermissionListener) {
        for (request in requests) {
            checkPermission(act, request, listener)
        }
        initiateNextRequest()
    }

    private fun checkPermission(act: Activity, request: PermissionRequest, listener: PermissionListener) {
        checkPermission(act, request.permission, request.explanationResId, listener)
    }

    private fun checkPermission(
            act: Activity,
            permission: String,
            explanationResId: Int,
            listener: PermissionListener?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                listener?.onGranted(permission)
            } else {
                addNewRequest(act, permission, explanationResId, listener)
            }
        } else {
            listener?.onGranted(permission)
        }
    }

    private fun addNewRequest(act: Activity, permission: String, explanationResId: Int, listener: PermissionListener?) {
        verbose("addNewRequest($permission)")
        var token = getToken(permission)
        if (token == null) {
            token = RequestToken(act, permission, explanationResId)
        } else {
            token.activity = act
            token.explainResId = explanationResId
        }
        token.addListener(listener)
        addNewRequest(token)
    }

    fun hasPermission(permission: String): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            else -> true
        }
    }

    // Call this from the activity
    fun onHandlePermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (LOG) {
            verbose("handlePermissionResult got ${permissions.size}")
        }
        when (requestCode) {
            REQUEST_CODE -> {
                for (i in permissions.indices) {
                    val permission = permissions[i]
                    val token = getToken(permission)
                    if (token == null) {
                        error("Could not find request associated with code: $permission")
                    } else {
                        val grantResult = grantResults[i]
                        token.handlePermissionResult(grantResult)
                        live.remove(token)
                        lastRequests.remove(token)
                    }
                }
            }
            else -> verbose("Ignoring request code : $requestCode")
        }
        if (lastRequests.isEmpty()) {
            initiateNextRequest()
        }
    }

    private fun getToken(permission: String): RequestToken? {
        return tokens[permission]
    }

    private fun addNewRequest(token: RequestToken) {
        if (LOG) {
            verbose("addNewRequest(${token.permission})")
        }
        if (!isPosted(token)) {
            live.add(token)
        }
    }

    private fun initiateNextRequest() {
        if (live.size > 0) {
            val missingPermissions = mutableListOf<RequestToken>()
            val hasPermissions = mutableListOf<RequestToken>()
            val died = mutableListOf<RequestToken>()
            val explain = mutableListOf<RequestToken>()
            for (token in live) {
                when {
                    token.hasPermission -> hasPermissions.add(token)
                    token.activityDied -> died.add(token)
                    token.needsExplanation -> explain.add(token)
                    else -> missingPermissions.add(token)
                }
            }
            for (token in died) {
                live.remove(token)
            }
            if (missingPermissions.isNotEmpty()) {
                isWaiting?.let { existing ->
                    msg(
                            "Already have a request pending so no new request issues.\n\tActive: ${existing.permission}"
                    )
                    return
                }
            }
            when {
                explain.isNotEmpty() -> explain[0].postExplanation { initiateNextRequest() }
                missingPermissions.isNotEmpty() -> requestPermission(missingPermissions)
            }
        } else {
            lastRequests.clear()
            if (LOG) {
                verbose("initiateNextRequest(): No more requests.")
            }
        }
    }

    private val isWaiting: RequestToken?
        get() {
            for (existing in lastRequests) {
                if (!existing.hasPermission) {
                    return existing
                }
            }
            return null
        }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermission(list: List<RequestToken>) {
        val permissions = mutableListOf<String>()
        var activity: Activity? = null
        for (token in list) {
            if (activity == null || activity == token.activity) {
                permissions.add(token.permission)
                activity = token.activity
            }
        }
        activity?.requestPermissions(permissions.toTypedArray(), REQUEST_CODE)
    }

    private fun isPosted(token: RequestToken): Boolean {
        for (t in live) {
            if (t == token) {
                return true
            }
        }
        return false
    }

    private fun msg(msg: String) {
        Timber.tag(TAG).i(msg)
    }

    private fun verbose(msg: String) {
        Timber.tag(TAG).d(msg)
    }

    private fun error(msg: String) {
        Timber.tag(TAG).e(msg)
    }

}
