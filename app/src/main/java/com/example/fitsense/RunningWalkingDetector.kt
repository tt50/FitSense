import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.sqrt


class RunningWalkingDetector : SensorEventListener {
    private var isTracking = false
    private var lastStepTime = 0L
    private var previousStepTime = 0L
    private var stepCounter = 0
    private val stepThreshold = 12.0
    private var lastSpeed = 0.0
    private val stepDurationThreshold = 1000L

    private var currentActivity = "Unknown"
    private val idleSpeedThreshold = 0.10; // limit of idle speed in m/s
    private val walkingSpeedThreshold = 0.49 // limit of walking speed in m/s, this is estimated based on device I tested with

    fun startTracking(){
        isTracking = true
        stepCounter = 0
        lastSpeed = 0.0
        currentActivity = "Unknown"
        lastStepTime = 0L
        previousStepTime = 0L
    }

    fun stopTracking(){
        isTracking = false

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isTracking || event == null) return
        // Data Extraction: of x, y, and z axes
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Preprocessing: use data from axes and calculate the magnitude
        val magnitude = sqrt(x * x + y * y + z * z)


        if (magnitude > stepThreshold) {
            val currentTime = System.currentTimeMillis()
            val timeInterval = currentTime - lastStepTime

            // Preprocessing:
            // calculate speed between steps
            if (timeInterval > stepDurationThreshold) {
                previousStepTime = lastStepTime
                lastStepTime = currentTime
                stepCounter++ // Feature extraction : step counter data to be displayed in label
                updateSpeed()
            }
        }
    }

    private fun updateSpeed() {
        // Preprocessing: calculating time intervals
        val timeInterval = lastStepTime - previousStepTime

        // Feature extraction : calculate last speed to be displayed in label
        lastSpeed = if (timeInterval > 0) {
            0.5 / (timeInterval / 1000.0) // 0.5m per step
        } else {
            0.0
        }

        // Classification: Use speed data to differentiate between idle, walking and running
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
