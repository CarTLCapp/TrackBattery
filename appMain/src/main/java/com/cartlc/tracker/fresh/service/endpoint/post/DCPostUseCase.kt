package com.cartlc.tracker.fresh.service.endpoint.post

interface DCPostUseCase {

    fun reloadFromServer(): Boolean
    fun ping()

}