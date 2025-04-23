package com.example.fitsense

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class Barometer(
    context: Context,
    private val onPressureChanged: (Float) -> Unit
) : SensorEventListener {

        private val sensorManager =
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        private val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        fun startTracking() {
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_UI)
        }

        fun stopTracking() {
            sensorManager.unregisterListener(this)
        }

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val pressure = it.values[0]
                onPressureChanged(pressure)
            }
        }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }
}
