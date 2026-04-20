package com.mirea.module4.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

// Задание 7: Bound Service — случайное число каждую секунду

class RandomNumberService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private val _number = MutableStateFlow(0)
    val number: StateFlow<Int> = _number

    inner class RandomBinder : Binder() {
        fun getService(): RandomNumberService = this@RandomNumberService
    }

    private val binder = RandomBinder()

    override fun onBind(intent: Intent?): IBinder {
        startGenerating()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        serviceScope.coroutineContext.cancelChildren()
        return super.onUnbind(intent)
    }

    private fun startGenerating() {
        serviceScope.launch {
            while (isActive) {
                _number.value = Random.nextInt(0, 101)
                delay(1000)
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
