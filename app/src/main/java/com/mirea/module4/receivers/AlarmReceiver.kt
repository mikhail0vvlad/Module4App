package com.mirea.module4.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mirea.module4.Module4Application

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        try {
            val notification = NotificationCompat.Builder(context, Module4Application.CHANNEL_ALARM)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("💊 Время принять таблетку!")
                .setContentText("Не забудьте принять лекарство")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(301, notification)
        } catch (e: SecurityException) { /* нет разрешения */ }
    }
}
