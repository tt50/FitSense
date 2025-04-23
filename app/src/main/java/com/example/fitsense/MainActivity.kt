package com.example.fitsense

import RunningWalkingDetector
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Button
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

    //  RunningWalkingDetector with acceleromter and steps
    private lateinit var runningWalkingDetector: RunningWalkingDetector
    private lateinit var stepCounterLabel: TextView
    private lateinit var accelerationLabel: TextView
    private lateinit var activityExerciseLabel: TextView

    // Timer
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

        // timer label
        timerTextView = findViewById(R.id.txtTimer)

        // record button
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            if (isTimerRunning) {
                stopTimer()
            } else {
                resetAndStartTimer()
            }
        }

        // stop button
        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopTimer()
        }

        stepCounterLabel = findViewById(R.id.stepCounter)
        accelerationLabel = findViewById(R.id.currentAcceleration)
        activityExerciseLabel = findViewById(R.id.activityExercise)

            // Initialize the detector
            runningWalkingDetector = RunningWalkingDetector()

        // Start a loop to update UI every second with the current speed and step count
        handler.post(object : Runnable {
            override fun run() {
                stepCounterLabel.text = "Steps: ${runningWalkingDetector.getStepCount()}"
                accelerationLabel.text = "Speed: %.2f m/s".format(runningWalkingDetector.getCurrentSpeed())
                activityExerciseLabel.text = "Activity: ${runningWalkingDetector.getCurrentActivity()}"
                handler.postDelayed(this, 1000)
            }
        })
    }

    // start timer and rest time
    private fun resetAndStartTimer() {
        elapsedTime = 0
        startTime = System.currentTimeMillis()
        isTimerRunning = true
        handler.post(updateTimerRunnable)
    }

    // stop timer
    private fun stopTimer() {
        isTimerRunning = false
        handler.removeCallbacks(updateTimerRunnable)
    }

    // start accelerometer tracking
    override fun onResume() {
        super.onResume()
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(runningWalkingDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }


    // stop accelerometer tracking
    override fun onPause() {
        super.onPause()
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(runningWalkingDetector)
    }
}