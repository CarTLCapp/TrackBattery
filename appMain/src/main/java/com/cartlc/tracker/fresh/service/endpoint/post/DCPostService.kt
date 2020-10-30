package com.cartlc.tracker.fresh.service.endpoint.post

import android.app.job.JobParameters
import android.app.job.JobService
import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.ComponentRoot

class DCPostService : JobService() {

    private val app: TBApplication by lazy {
        applicationContext as TBApplication
    }

    private val componentRoot: ComponentRoot by lazy {
        app.componentRoot
    }

    private val postController: DCPostController by lazy {
        componentRoot.postController
    }

    // region JobService

    override fun onStartJob(params: JobParameters?): Boolean {
        postController.onStart(this, params)
        return true

    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return postController.hasUploads
    }

    // endregion JobService

}