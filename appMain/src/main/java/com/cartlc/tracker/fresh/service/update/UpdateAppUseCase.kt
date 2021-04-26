package com.cartlc.tracker.fresh.service.update

interface UpdateAppUseCase {

    val isUpdateAvailable: Boolean
    fun updateApp(requestCode: Int)

}