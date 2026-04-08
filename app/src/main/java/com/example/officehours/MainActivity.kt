package com.example.officehours

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var officeLat = 12.991033
    private var officeLng = 80.245636

    private var startX = 0f
    private var isWorking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val swipeView = findViewById<TextView>(R.id.swipeView)
        val reportBtn = findViewById<Button>(R.id.reportBtn)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        requestPermissions()

        // 🔄 Restore session if app reopened
        val prefs = getSharedPreferences("office_data", MODE_PRIVATE)
        val savedStart = prefs.getLong("start_time", 0)

        if (savedStart > 0) {
            isWorking = true
            statusText.text = "Office Running..."
            swipeView.text = "👈 Swipe Left to Stop"
        }

        // 📊 Open report
        reportBtn.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        // 👉 Swipe logic
        swipeView.setOnTouchListener { _, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                }

                MotionEvent.ACTION_UP -> {
                    val diff = event.x - startX

                    // 👉 Swipe Right → START
                    if (diff > 150 && !isWorking) {
                        checkLocationAndStart(statusText, swipeView)
                    }

                    // 👉 Swipe Left → STOP
                    if (diff < -150 && isWorking) {
                        stopWork(statusText, swipeView)
                    }
                }
            }
            true
        }
    }

    // 🔐 Permissions
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            ),
            1
        )
    }

    // 📍 Check GPS and start
    private fun checkLocationAndStart(statusText: TextView, swipeView: TextView) {

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Location permission needed", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->

            if (location != null) {

                val distance = FloatArray(1)

                android.location.Location.distanceBetween(
                    location.latitude, location.longitude,
                    officeLat, officeLng,
                    distance
                )

                if (distance[0] < 200) {
                    val currentTime = System.currentTimeMillis()

                    val prefs = getSharedPreferences("office_data", MODE_PRIVATE)
                    prefs.edit().putLong("start_time", currentTime).apply()

                    isWorking = true
                    statusText.text = "Office Started ✅"
                    swipeView.text = "👈 Swipe Left to Stop"

                    startLeaveNotification()

                } else {
                    statusText.text = "Not in office location ❌"
                }

            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🛑 Stop work
    private fun stopWork(statusText: TextView, swipeView: TextView) {

        val prefs = getSharedPreferences("office_data", MODE_PRIVATE)
        val startTime = prefs.getLong("start_time", 0)

        if (startTime == 0L) return

        val endTime = System.currentTimeMillis()
        val duration = (endTime - startTime) / 1000

        isWorking = false

        statusText.text = "Worked: $duration sec ⏱️"
        swipeView.text = "👉 Swipe Right to Start"

        saveSession(duration)

        prefs.edit().remove("start_time").apply()
    }

    // 💾 Save session
    private fun saveSession(duration: Long) {
        val prefs = getSharedPreferences("office_data", MODE_PRIVATE)
        val existing = prefs.getString("sessions", "") ?: ""

        val newEntry = System.currentTimeMillis().toString() + "," + duration + ";"
        prefs.edit().putString("sessions", existing + newEntry).apply()
    }

    // 🔔 Notification after 8 hours
    private fun startLeaveNotification() {

        Handler(Looper.getMainLooper()).postDelayed({

            val channelId = "office_channel"

            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Office Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
                manager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Office Time Completed")
                .setContentText("It's time to leave office 🚶")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()

            manager.notify(1, notification)

        }, 8 * 60 * 60 * 1000) // 8 hours
    }
}
