package com.cartlc.tracker.fresh.ui.common.rx

import io.reactivex.disposables.Disposable

class RxDisposables {

    private val disposables = mutableListOf<Disposable>()

    fun add(disposable: Disposable) {
        disposables.add(disposable)
    }

    fun dispose() {
        disposables.forEach { it.dispose() }
        disposables.clear()
    }

    fun size(): Int {
        return disposables.size
    }

}