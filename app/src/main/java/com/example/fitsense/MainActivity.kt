package com.example.fitsense

import RunningWalkingDetector
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.hardware.Sensor
import android.hardware.SensorManager

class MainActivity : AppCompatActivity() {

    // Timer
    private lateinit var timerTextView: TextView
    private var isTimerRunning = false
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private val handler = Handler()

    // Detectors
    private lateinit var runningWalkingDetector: RunningWalkingDetector
    private lateinit var barometer: Barometer

    // UI Labels
    private lateinit var stepCounterLabel: TextView
    private lateinit var accelerationLabel: TextView
    private lateinit var activityExerciseLabel: TextView
    private lateinit var jumpHeightLabel: TextView
    private lateinit var jumpCountLabel: TextView

    private val updateTimerRunnable: Runnable = object : Runnable {
        override fun run() {
            if (isTimerRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                val seconds = (elapsedTime / 1000) % 60
                val minutes = (elapsedTime / (1000 * 60)) % 60
                val hours = (elapsedTime / (1000 * 60 * 60)) % 24
                timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize views
        timerTextView = findViewById(R.id.txtTimer)
        stepCounterLabel = findViewById(R.id.stepCounter)
        accelerationLabel = findViewById(R.id.currentAcceleration)
        activityExerciseLabel = findViewById(R.id.activityExercise)
        jumpHeightLabel = findViewById(R.id.txtJumpHeight)
        jumpCountLabel = findViewById(R.id.txtJumpCount)

        // Initialize detectors
        runningWalkingDetector = RunningWalkingDetector()

        barometer = Barometer(this) {
            runOnUiThread {
                jumpHeightLabel.text = "Jump Height: %.2f ft".format(barometer.calculateJumpHeight())
                jumpCountLabel.text = "Jumps: ${barometer.getJumpCount()}"
            }
        }

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            if (isTimerRunning) {
                stopTimer()
            } else {
                resetAndStartTimer()
                barometer.resetJumpMeasurement()
                barometer.startTracking()
            }
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopTimer()
            barometer.stopTracking()
            checkJumpHeight()
        }

        handler.post(object : Runnable {
            override fun run() {
                stepCounterLabel.text = "Steps: ${runningWalkingDetector.getStepCount()}"
                accelerationLabel.text = "Speed: %.2f m/s".format(runningWalkingDetector.getCurrentSpeed())
                activityExerciseLabel.text = if (barometer.isJumping()) {
                    "Activity: Jumping"
                } else {
                    "Activity: ${runningWalkingDetector.getCurrentActivity()}"
                }
                jumpHeightLabel.text = "Jump Height: %.2f ft".format(barometer.calculateJumpHeight())
                jumpCountLabel.text = "Jumps: ${barometer.getJumpCount()}"
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun resetAndStartTimer() {
        elapsedTime = 0
        startTime = System.currentTimeMillis()
        isTimerRunning = true
        handler.post(updateTimerRunnable)
        runningWalkingDetector.startTracking()
    }

    private fun stopTimer() {
        isTimerRunning = false
        handler.removeCallbacks(updateTimerRunnable)
        runningWalkingDetector.stopTracking()
    }

    private fun checkJumpHeight() {
        val jumpHeight = barometer.calculateJumpHeight()
        val message = if (barometer.isJumping()) {
            "Jump height: %.2f ft".format(jumpHeight)
        } else {
            "Jump height: %.2f ft (below threshold)".format(jumpHeight)
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(runningWalkingDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(runningWalkingDetector)
        barometer.stopTracking()
    }
}
