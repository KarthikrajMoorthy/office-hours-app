package com.example.officehours

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val startBtn = findViewById<Button>(R.id.startBtn)
        val stopBtn = findViewById<Button>(R.id.stopBtn)

        startBtn.setOnClickListener {
            startTime = System.currentTimeMillis()
            statusText.text = "Office Started"
        }

        stopBtn.setOnClickListener {
            val endTime = System.currentTimeMillis()
            val duration = (endTime - startTime) / 1000
            statusText.text = "Worked: $duration sec"
        }
    }
}
