package com.mirea.module4.workers

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.mirea.module4.Module4Application
import kotlinx.coroutines.delay

// Задание 9: WorkManager — параллельная загрузка погоды для городов

const val KEY_CITY = "city"
const val KEY_TEMP = "temp"
const val KEY_CITIES_DONE = "cities_done"
const val KEY_REPORT = "report"

class WeatherWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val city = inputData.getString(KEY_CITY) ?: "Город"
        setForeground(getForegroundInfo(city))
        delay((2000..4000).random().toLong()) // имитация сетевого запроса
        val temp = (-20..30).random()
        return Result.success(workDataOf(KEY_CITY to city, KEY_TEMP to temp))
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = getForegroundInfo("загрузка...")

    private fun getForegroundInfo(city: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, Module4Application.CHANNEL_WEATHER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Загрузка погоды")
            .setContentText("Получаем данные: $city")
            .setOngoing(true)
            .build()
        return ForegroundInfo(200, notification)
    }
}

class WeatherReportWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        delay(500)
        // InputMerger merges outputs from parallel workers
        val report = "Отчёт сформирован в ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"
        showFinalNotification(report)
        return Result.success(workDataOf(KEY_REPORT to report))
    }

    private fun showFinalNotification(report: String) {
        val notification = NotificationCompat.Builder(applicationContext, Module4Application.CHANNEL_WEATHER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Прогноз погоды готов!")
            .setContentText(report)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        try {
            NotificationManagerCompat.from(applicationContext).notify(201, notification)
        } catch (e: SecurityException) { /* нет разрешения */ }
    }
}
