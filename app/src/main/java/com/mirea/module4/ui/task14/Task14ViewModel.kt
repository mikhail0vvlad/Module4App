package com.mirea.module4.ui.task14

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Задание 14: Компас — сенсоры TYPE_ACCELEROMETER + TYPE_MAGNETIC_FIELD

class Task14ViewModel : ViewModel() {

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth

    private val _sensorAvailable = MutableStateFlow(true)
    val sensorAvailable: StateFlow<Boolean> = _sensorAvailable

    private var sensorManager: SensorManager? = null
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var lastAzimuth = 0f

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER ->
                    System.arraycopy(event.values, 0, accelerometerReading, 0, 3)
                Sensor.TYPE_MAGNETIC_FIELD ->
                    System.arraycopy(event.values, 0, magnetometerReading, 0, 3)
            }
            updateAzimuth()
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun updateAzimuth() {
        val success = SensorManager.getRotationMatrix(
            rotationMatrix, null, accelerometerReading, magnetometerReading
        )
        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            var azimuthRad = orientationAngles[0]
            var degrees = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            if (degrees < 0) degrees += 360f
            // Плавное сглаживание
            val delta = degrees - lastAzimuth
            val smoothed = lastAzimuth + delta * 0.15f
            lastAzimuth = smoothed
            _azimuth.value = smoothed
        }
    }

    fun registerSensors(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneto = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (accel == null || magneto == null) {
            _sensorAvailable.value = false
            return
        }
        _sensorAvailable.value = true
        sensorManager?.registerListener(sensorListener, accel, SensorManager.SENSOR_DELAY_UI)
        sensorManager?.registerListener(sensorListener, magneto, SensorManager.SENSOR_DELAY_UI)
    }

    fun unregisterSensors() {
        sensorManager?.unregisterListener(sensorListener)
    }

    override fun onCleared() {
        unregisterSensors()
        super.onCleared()
    }
}
