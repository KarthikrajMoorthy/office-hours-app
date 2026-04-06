package com.example.officehours

import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var startX = 0f
    private var endX = 0f
    private var startTime: Long = 0
    private var isWorking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val swipeView = findViewById<TextView>(R.id.swipeView)

        swipeView.setOnTouchListener { _, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                }

                MotionEvent.ACTION_UP -> {
                    endX = event.x
                    val diff = endX - startX

                    // 👉 Swipe Right = Start
                    if (diff > 150 && !isWorking) {
                        startTime = System.currentTimeMillis()
                        isWorking = true
                        statusText.text = "Office Started ✅"
                        swipeView.text = "👈 Swipe Left to Stop"
                    }

                    // 👉 Swipe Left = Stop
                    if (diff < -150 && isWorking) {
                        val endTime = System.currentTimeMillis()
                        val duration = (endTime - startTime) / 1000
                        isWorking = false
                        statusText.text = "Worked: $duration sec ⏱️"
                        swipeView.text = "👉 Swipe Right to Start"
                    }
                }
            }
            true
        }
    }
}
