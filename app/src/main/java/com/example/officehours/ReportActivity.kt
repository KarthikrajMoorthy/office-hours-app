package com.example.officehours

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)

        val prefs = getSharedPreferences("office_data", MODE_PRIVATE)
        val data = prefs.getString("sessions", "")

        val builder = StringBuilder()

        data?.split(";")?.forEach {
            if (it.contains(",")) {
                val parts = it.split(",")
                builder.append("Date: ${parts[0]} \nDuration: ${parts[1]} sec\n\n")
            }
        }

        tv.text = builder.toString()
        setContentView(tv)
    }
}
