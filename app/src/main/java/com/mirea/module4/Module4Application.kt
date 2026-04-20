package com.mirea.module4

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class Module4Application : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        listOf(
            NotificationChannel(CHANNEL_TIMER, "Таймер", NotificationManager.IMPORTANCE_LOW)
                .apply { description = "Уведомления таймера" },
            NotificationChannel(CHANNEL_ALARM, "Напоминания", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Ежедневные напоминания" },
            NotificationChannel(CHANNEL_WEATHER, "Погода", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Сводка погоды" },
            NotificationChannel(CHANNEL_GENERAL, "Общие", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Общие уведомления" }
        ).forEach { manager.createNotificationChannel(it) }
    }

    companion object {
        const val CHANNEL_TIMER = "channel_timer"
        const val CHANNEL_ALARM = "channel_alarm"
        const val CHANNEL_WEATHER = "channel_weather"
        const val CHANNEL_GENERAL = "channel_general"
    }
}
