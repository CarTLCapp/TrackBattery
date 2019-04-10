package com.cartlc.tracker.ui.app.dependencyinjection

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.cartlc.tracker.ui.app.TBApplication

class BoundAct(
        val act: AppCompatActivity
) : BoundBase(
        (act.applicationContext as TBApplication).repo,
        (act.applicationContext as TBApplication).componentRoot) {

    override val lifecycleOwner: LifecycleOwner
        get() = act

    fun bindObserver(observer: LifecycleObserver): LifecycleObserver {
        (act as LifecycleOwner).lifecycle.addObserver(observer)
        return observer
    }

}