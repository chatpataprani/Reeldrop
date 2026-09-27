package com.chatpataprani.reeldrop.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.chatpataprani.reeldrop.FlirtyReminderReceiver
import java.util.Calendar

object FlirtyReminderScheduler {
    private const val BASE_REQUEST_CODE = 7100
    private val HOURS = intArrayOf(11, 16, 21)

    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        HOURS.forEachIndexed { index, hour ->
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    BASE_REQUEST_CODE + index,
                    Intent(context, FlirtyReminderReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            val trigger = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                trigger.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent,
            )
        }
    }
}
