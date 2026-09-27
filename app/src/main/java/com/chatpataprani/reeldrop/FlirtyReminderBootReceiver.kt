package com.chatpataprani.reeldrop

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FlirtyReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        FlirtyReminderScheduler.schedule(context)
    }
}
