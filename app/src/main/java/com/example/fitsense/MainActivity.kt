package com.example.fitsense

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var tracker: Accelerometer
    private lateinit var accelerationLabel: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        accelerationLabel = findViewById(R.id.currentAcceleration)

        tracker = Accelerometer(this) { acc ->
            this@MainActivity.runOnUiThread {
                accelerationLabel.text = "Current Speed: %.2f m/s²".format(acc)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        tracker.startTracking()
    }

    override fun onPause() {
        super.onPause()
        tracker.stopTracking()
    }

}