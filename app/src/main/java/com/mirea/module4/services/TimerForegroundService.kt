package com.mirea.module4.services

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mirea.module4.Module4Application
import kotlinx.coroutines.*

// Задание 5: Foreground Service — счётчик времени

class TimerForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var seconds = 0
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val ACTION_START = "action_start"
        const val ACTION_STOP = "action_stop"
        const val EXTRA_SECONDS = "extra_seconds"
        const val NOTIFICATION_ID = 101
        const val BROADCAST_TICK = "com.mirea.module4.TIMER_TICK"
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startCounting()
            ACTION_STOP -> {
                stopCounting()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startCounting() {
        seconds = 0
        startForeground(NOTIFICATION_ID, buildNotification(seconds))
        serviceScope.launch {
            while (isActive) {
                delay(1000)
                seconds++
                notificationManager.notify(NOTIFICATION_ID, buildNotification(seconds))
                sendBroadcast(Intent(BROADCAST_TICK).putExtra(EXTRA_SECONDS, seconds))
            }
        }
    }

    private fun stopCounting() {
        serviceScope.cancel()
    }

    private fun buildNotification(sec: Int): Notification {
        return NotificationCompat.Builder(this, Module4Application.CHANNEL_TIMER)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Таймер работает")
            .setContentText("Прошло $sec сек${if (sec % 10 in 2..4 && sec % 100 !in 11..14) "унды" else "унд"}")
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
