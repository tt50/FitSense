import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.sqrt

class RunningWalkingDetector : SensorEventListener {

    private var lastStepTime = 0L
    private var previousStepTime = 0L
    private var stepCounter = 0
    private val stepThreshold = 12.0
    private var lastSpeed = 0.0
    private val stepDurationThreshold = 1000L

    private var currentActivity = "Unknown"
    private val idleSpeedThreshold = 0.10; // limit of idle speed in m/s
    private val walkingSpeedThreshold = 0.50 // limit of walking speed in m/s

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt(x * x + y * y + z * z)

        if (magnitude > stepThreshold) {
            val currentTime = System.currentTimeMillis()
            val timeInterval = currentTime - lastStepTime

            // calculate speed between steps
            if (timeInterval > stepDurationThreshold) {
                previousStepTime = lastStepTime
                lastStepTime = currentTime
                stepCounter++
                updateSpeed()
            }
        }
    }

    private fun updateSpeed() {
        val timeInterval = lastStepTime - previousStepTime

        lastSpeed = if (timeInterval > 0) {
            0.5 / (timeInterval / 1000.0) // 0.5m per step
        } else {
            0.0
        }

        currentActivity = when {
            lastSpeed < idleSpeedThreshold -> "Idle"
            lastSpeed < walkingSpeedThreshold -> "Walking"
            else -> "Running"
        }
    }

    fun getCurrentActivity(): String = currentActivity

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun getCurrentSpeed(): Double = lastSpeed

    fun getStepCount(): Int = stepCounter
}
