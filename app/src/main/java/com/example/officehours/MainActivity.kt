package com.example.officehours

import android.Manifest
import android.app.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import android.content.Intent
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var officeLat = 13.000235 //12.991425
    private var officeLng = 80.119567 //80.245939
    private var isWorking = false
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val statusText = findViewById<TextView>(R.id.statusText)
        val swipeView = findViewById<TextView>(R.id.swipeView)
        val reportBtn = findViewById<Button>(R.id.reportBtn)

        reportBtn.setOnClickListener {
             startActivity(Intent(this, ReportActivity::class.java))
        }
        requestPermissions()

        swipeView.setOnTouchListener { _, event ->
            when (event.action) {

                android.view.MotionEvent.ACTION_DOWN -> {}
                android.view.MotionEvent.ACTION_UP -> {

                    val diff = event.x - 0

                    if (diff > 150 && !isWorking) {
                        checkLocationAndStart(statusText, swipeView)
                    }

                    if (diff < -150 && isWorking) {
                        stopWork(statusText, swipeView)
                    }
                }
            }
            true
        }
    }

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

    private fun checkLocationAndStart(statusText: TextView, swipeView: TextView) {

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {

                val distance = FloatArray(1)
                android.location.Location.distanceBetween(
                    location.latitude, location.longitude,
                    officeLat, officeLng,
                    distance
                )

                if (distance[0] < 200) { // within 200 meters
                    startTime = System.currentTimeMillis()
                    isWorking = true
                    statusText.text = "Office Started ✅"
                    swipeView.text = "👈 Swipe Left to Stop"

                    startLeaveNotification()
                } else {
                    statusText.text = "Not in office location ❌"
                }
            }
        }
    }

    private fun stopWork(statusText: TextView, swipeView: TextView) {
    val endTime = System.currentTimeMillis()
    val duration = (endTime - startTime) / 1000

    isWorking = false
    statusText.text = "Worked: $duration sec ⏱️"
    swipeView.text = "👉 Swipe Right to Start"

    saveSession(duration) // ✅ SAVE DATA
}
    private fun saveSession(duration: Long) {
    val prefs = getSharedPreferences("office_data", MODE_PRIVATE)
    val existing = prefs.getString("sessions", "") ?: ""

    val newEntry = System.currentTimeMillis().toString() + "," + duration + ";"
    prefs.edit().putString("sessions", existing + newEntry).apply()
}
    private fun startLeaveNotification() {

        val handler = Handler()

        handler.postDelayed({

            val channelId = "office_channel"
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Office Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Office Time Done")
                .setContentText("Time to leave office 🏃")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()

            notificationManager.notify(1, notification)

        }, 8 * 60 * 60 * 1000) // 8 hours
    }
}
