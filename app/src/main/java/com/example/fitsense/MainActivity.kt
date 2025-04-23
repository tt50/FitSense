package com.example.fitsense

import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Button
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

    // Accelerometer
    private lateinit var tracker: Accelerometer
    private lateinit var accelerationLabel: TextView

    // Barometer
    private lateinit var barometer: Barometer
    private lateinit var pressureLabel: TextView

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

        // set up Accelerometer
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        accelerationLabel = findViewById(R.id.currentAcceleration)

        // initialize accelerometer tracker
        tracker = Accelerometer(this) { acc ->
            this@MainActivity.runOnUiThread {
                accelerationLabel.text = "Current Speed: %.2f m/s²".format(acc)
            }
        }

        // setup barometer tracker
        pressureLabel = findViewById(R.id.txtJumpHeight)

        barometer = Barometer(this) { pressure ->
            this@MainActivity.runOnUiThread {
                pressureLabel.text = "Pressure: %.2f hPa".format(pressure)
            }
        }
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
        tracker.startTracking()
        barometer.startTracking()
    }

    // stop accelerometer tracking
    override fun onPause() {
        super.onPause()
        tracker.stopTracking()
        barometer.stopTracking()
    }
}

