package com.cartlc.tracker.fresh.service.endpoint.post

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.cartlc.tracker.fresh.model.event.EventController
import com.cartlc.tracker.fresh.model.event.EventPingStatus
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


class DCPostUseCaseImpl(
        private val context: Context,
        eventController: EventController,
        private val prefHelper: PrefHelper
) : DCPostUseCase {

    companion object {
        private val TAG = DCPostUseCaseImpl::class.simpleName
        private val CLEAR_UPLOAD_WORKING = TimeUnit.SECONDS.toMillis(45)
        private const val JOB_ID = 0
    }

    private val uploadWorking = AtomicBoolean(false)
    private val handler = Handler(Looper.getMainLooper())

    private val jobScheduler: JobScheduler by lazy {
        context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    }

    private val packageName: String by lazy {
        context.packageName
    }

    init {
        eventController.register(this)
    }

    // region DCPostUseCase

    override fun reloadFromServer(): Boolean {
        if (uploadWorking.get()) {
            return false
        }
        prefHelper.reloadFromServer()
        uploadWorking.set(true)
        handler.postDelayed({
            uploadWorking.set(false)
        }, CLEAR_UPLOAD_WORKING)
        ping()
        return true
    }

    override fun ping() {
        msg("ping()")
        val serviceName = ComponentName(packageName, DCPostService::class.java.name)
        val builder = JobInfo.Builder(JOB_ID, serviceName)
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        builder.setPersisted(true)
        jobScheduler.schedule(builder.build())
    }

    // endregion DCPostUseCase

    // region EventController

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EventPingStatus) {
        if (event.uploadsAllDone) {
            uploadWorking.set(false)
        }
    }

    // endregion EventController

    private fun msg(msg: String) {
        Timber.tag(TAG).i(msg)
    }
}