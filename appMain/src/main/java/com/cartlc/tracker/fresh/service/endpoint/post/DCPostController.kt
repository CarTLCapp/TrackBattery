package com.cartlc.tracker.fresh.service.endpoint.post

import android.app.job.JobParameters
import android.app.job.JobService

interface DCPostController {

    val hasUploads: Boolean
    fun onStart(service: JobService, params: JobParameters?)

}