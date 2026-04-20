package com.mirea.module4.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

// Задание 8: WorkManager — последовательная цепочка обработки фото

const val KEY_STATUS = "status"
const val KEY_PROGRESS = "progress"
const val KEY_RESULT = "result"
const val KEY_FILENAME = "filename"

class CompressPhotoWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val filename = inputData.getString(KEY_FILENAME) ?: "photo.jpg"
        // Имитация сжатия фото (0→100%)
        for (i in 0..100 step 10) {
            setProgress(workDataOf(KEY_PROGRESS to i, KEY_STATUS to "Сжимаем фото... $i%"))
            delay(150)
        }
        return Result.success(workDataOf(KEY_FILENAME to "compressed_$filename"))
    }
}

class WatermarkWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val filename = inputData.getString(KEY_FILENAME) ?: "compressed_photo.jpg"
        for (i in 0..100 step 10) {
            setProgress(workDataOf(KEY_PROGRESS to i, KEY_STATUS to "Добавляем водяной знак... $i%"))
            delay(120)
        }
        return Result.success(workDataOf(KEY_FILENAME to "watermarked_$filename"))
    }
}

class UploadWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val filename = inputData.getString(KEY_FILENAME) ?: "watermarked_photo.jpg"
        for (i in 0..100 step 20) {
            setProgress(workDataOf(KEY_PROGRESS to i, KEY_STATUS to "Загружаем в облако... $i%"))
            delay(200)
        }
        return Result.success(workDataOf(
            KEY_RESULT to "Готово! Фото загружено",
            KEY_FILENAME to filename
        ))
    }
}
