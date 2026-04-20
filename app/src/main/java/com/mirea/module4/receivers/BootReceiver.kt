package com.mirea.module4.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("alarm_enabled", false)) {
            scheduleAlarm(context)
        }
    }

    companion object {
        fun scheduleAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
            }
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
                    .edit().putBoolean("alarm_enabled", true)
                    .putLong("next_alarm", calendar.timeInMillis).apply()
            } catch (e: SecurityException) { /* нет разрешения SCHEDULE_EXACT_ALARM */ }
        }

        fun cancelAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
                .edit().putBoolean("alarm_enabled", false).apply()
        }

        fun getPrefs(context: Context): SharedPreferences =
            context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
    }
}
