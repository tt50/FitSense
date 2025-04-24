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

    private var jumpCount = 0
    private var isInAir = false

    companion object {
        const val SEA_LEVEL_PRESSURE = 1013.25f // hPa
        const val FEET_PER_METER = 3.28084f
        const val JUMP_THRESHOLD_FEET = 0.60f // requirement for jump height, based on device I tested with
    }

    fun startTracking() {
        initialPressure = 0f
        maxHeightPressure = 0f
        isTracking = true
        isInAir = false
        jumpCount = 0

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
            callback(currentPressure)

            if (initialPressure == 0f) {
                initialPressure = currentPressure
                maxHeightPressure = currentPressure
                return
            }

            if (currentPressure < maxHeightPressure) {
                maxHeightPressure = currentPressure
            }

            val jumpHeight = calculateJumpHeight()

            if (jumpHeight > JUMP_THRESHOLD_FEET && !isInAir) {
                isInAir = true // Jump started
            } else if (isInAir && currentPressure >= initialPressure - 0.1f) {
                // Returned to near ground level
                if (jumpHeight > JUMP_THRESHOLD_FEET) {
                    jumpCount++ // Count only if jump height exceeds the threshold
                }
                isInAir = false
                resetJumpMeasurement()
                initialPressure = currentPressure // Reset base
            }

            // Update jump height every time a jump occurs
            callback(jumpHeight)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    fun calculateJumpHeight(): Float {
        if (initialPressure == 0f || maxHeightPressure == 0f) return 0f

        val heightMeters = SensorManager.getAltitude(SEA_LEVEL_PRESSURE, maxHeightPressure) -
                SensorManager.getAltitude(SEA_LEVEL_PRESSURE, initialPressure)
        return heightMeters * FEET_PER_METER
    }

    fun resetJumpMeasurement() {
        maxHeightPressure = initialPressure
    }

    fun getJumpCount(): Int = jumpCount

    fun isJumping(): Boolean = isInAir
}
