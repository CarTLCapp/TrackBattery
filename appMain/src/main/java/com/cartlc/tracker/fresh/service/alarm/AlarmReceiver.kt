/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.service.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cartlc.tracker.fresh.ui.app.TBApplication

class AlarmReceiver : BroadcastReceiver() {

    companion object {

        fun scheduleIn(context: Context, action: String, afterMillis: Long) {
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.action = action
            val alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmMgr.cancel(alarmIntent)
            val alertAt = System.currentTimeMillis()+afterMillis
            alarmMgr.set(AlarmManager.RTC_WAKEUP, alertAt, alarmIntent)
        }

    }

    override fun onReceive(context: Context, intent: Intent) {
        val componentRoot = (context.applicationContext as TBApplication).componentRoot
        val alarmController = componentRoot.alarmController
        alarmController.onAction(intent.action)
    }

}