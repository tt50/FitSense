package com.example.fitsense

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class Barometer(context: Context, private val callback: (Float) -> Unit) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var initialPressure: Float = 0f
    private var maxHeightPressure: Float = 0f
    private var isTracking = false

    // Atmospheric constants
    companion object {
        const val SEA_LEVEL_PRESSURE = 1013.25f // hPa (standard atmospheric pressure)
        const val FEET_PER_METER = 3.28084f
        const val JUMP_THRESHOLD_FEET = 2f
    }

    fun startTracking() {
        initialPressure = 0f
        maxHeightPressure = 0f
        isTracking = true
        sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopTracking() {
        isTracking = false
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_PRESSURE && isTracking) {
            val currentPressure = event.values[0]
            callback(currentPressure) // Send pressure update to UI

            if (initialPressure == 0f) {
                initialPressure = currentPressure
                maxHeightPressure = currentPressure
            } else if (currentPressure < maxHeightPressure) {
                maxHeightPressure = currentPressure
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    fun calculateJumpHeight(): Float {
        if (initialPressure == 0f || maxHeightPressure == 0f) return 0f

        // Subtract initial altitude FROM peak altitude
        val heightMeters = SensorManager.getAltitude(SEA_LEVEL_PRESSURE, maxHeightPressure) -
                SensorManager.getAltitude(SEA_LEVEL_PRESSURE, initialPressure)
        return heightMeters * FEET_PER_METER
    }

    fun isJumping(): Boolean {
        return calculateJumpHeight() > JUMP_THRESHOLD_FEET
    }

    fun resetJumpMeasurement() {
        initialPressure = 0f
        maxHeightPressure = 0f
    }
}