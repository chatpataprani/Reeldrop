package com.chatpataprani.reeldrop

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chatpataprani.reeldrop.util.NotificationUtil

class FlirtyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NotificationUtil.showFlirtyReminder()
    }
}
