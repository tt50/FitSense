package com.example.fitsense

import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    // Timer
    private lateinit var timerTextView: TextView
    private var isTimerRunning = false
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private val handler = Handler()

    // Sensors
    private lateinit var tracker: Accelerometer
    private lateinit var barometer: Barometer
    private lateinit var accelerationLabel: TextView
    private lateinit var pressureLabel: TextView
    private lateinit var jumpHeightLabel: TextView

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
        accelerationLabel = findViewById(R.id.currentAcceleration)
        pressureLabel = findViewById(R.id.txtJumpHeight)
        jumpHeightLabel = findViewById(R.id.txtJumpHeight)

        // Initialize sensors
        tracker = Accelerometer(this) { acc ->
            runOnUiThread {
                accelerationLabel.text = "Current Acceleration: %.2f m/s²".format(acc)
            }
        }

        barometer = Barometer(this) { pressure ->
            runOnUiThread {
                pressureLabel.text = "Pressure: %.2f hPa".format(pressure)
                // Update jump height display continuously
                jumpHeightLabel.text = "Jump Height: %.2f ft".format(barometer.calculateJumpHeight())
            }
        }

        // Button click listeners
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            if (isTimerRunning) {
                stopTimer()
            } else {
                resetAndStartTimer()
                barometer.resetJumpMeasurement() // Reset jump measurement when starting
            }
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopTimer()
            checkJumpHeight()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun resetAndStartTimer() {
        elapsedTime = 0
        startTime = System.currentTimeMillis()
        isTimerRunning = true
        handler.post(updateTimerRunnable)
    }

    private fun stopTimer() {
        isTimerRunning = false
        handler.removeCallbacks(updateTimerRunnable)
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
        tracker.startTracking()
        barometer.startTracking()
    }

    override fun onPause() {
        super.onPause()
        tracker.stopTracking()
        barometer.stopTracking()
    }
}