package com.mirea.module4.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mirea.module4.Module4Application
import kotlinx.coroutines.*

// Задание 6: Background Service — одноразовый таймер с уведомлением

class TimerBackgroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        const val EXTRA_DURATION = "extra_duration_seconds"
        const val NOTIFICATION_ID = 102
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val duration = intent?.getIntExtra(EXTRA_DURATION, 30) ?: 30
        serviceScope.launch {
            delay(duration * 1000L)
            showNotification(duration)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun showNotification(duration: Int) {
        val notification = NotificationCompat.Builder(this, Module4Application.CHANNEL_GENERAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Таймер завершён!")
            .setContentText("Время вышло (${duration} сек)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) { /* Нет разрешения */ }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
