package com.cartlc.tracker.fresh.service.endpoint.post

import android.app.job.JobParameters
import android.app.job.JobService
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.event.EventController
import com.cartlc.tracker.fresh.model.event.EventPingStatus
import com.cartlc.tracker.fresh.service.endpoint.DCService
import com.cartlc.tracker.fresh.ui.main.MainController
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.lang.ref.WeakReference

class DCPostControllerImpl(
        private val db: DatabaseTable,
        eventController: EventController
) : DCPostController {

    companion object {
        private val TAG = DCPostControllerImpl::class.simpleName
    }

    private var jobParams: JobParameters? = null
    private var jobRef: WeakReference<JobService>? = null
    private val hasJob: Boolean
        get() = jobRef?.get() != null

    init {
        eventController.register(this)
    }

    // region DCPostController

    override val hasUploads: Boolean
        get() = db.tableEntry.hasEntriesToUpload || db.tableEntry.queryEmptyServerIds().isNotEmpty()

    override fun onStart(service: JobService, params: JobParameters?) {
        jobRef = WeakReference(service)
        jobParams = params
        DCService.newInstance(service)
    }

    // endregion DCPostController

    // region EventController

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EventPingStatus) {
        msg("onEvent($event), hasJob=$hasJob")
        jobRef?.get()?.jobFinished(jobParams, !event.uploadsAllDone)
    }

    // endregion EventController

    private fun msg(msg: String) {
        Timber.tag(TAG).i(msg)
    }

}