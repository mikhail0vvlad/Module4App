package com.mirea.module4.ui.task13

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

// Задание 13: StateFlow — живой курс валют

class Task13ViewModel : ViewModel() {

    private val _rate = MutableStateFlow(90.5)
    val rate: StateFlow<Double> = _rate

    private val _direction = MutableStateFlow(0) // -1 вниз, 0 нейтрально, +1 вверх
    val direction: StateFlow<Int> = _direction

    private val _lastUpdate = MutableStateFlow("")
    val lastUpdate: StateFlow<String> = _lastUpdate

    init {
        startAutoUpdate()
    }

    private fun startAutoUpdate() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                updateRate()
            }
        }
    }

    fun forceUpdate() {
        viewModelScope.launch { updateRate() }
    }

    private fun updateRate() {
        val prev = _rate.value
        val newRate = (90.5 + (Random.nextDouble() - 0.5) * 4.0)
            .coerceIn(80.0, 110.0)
            .let { Math.round(it * 100) / 100.0 }
        _direction.value = when {
            newRate > prev -> 1
            newRate < prev -> -1
            else -> 0
        }
        _rate.value = newRate
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        _lastUpdate.value = sdf.format(java.util.Date())
    }
}
