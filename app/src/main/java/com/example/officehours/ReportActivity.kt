package com.example.officehours

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)

        try {
            val prefs = getSharedPreferences("office_data", MODE_PRIVATE)
            val data = prefs.getString("sessions", "")

            val builder = StringBuilder()

            data?.split(";")?.forEach {
                if (it.contains(",")) {
                    val parts = it.split(",")

                    val date = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm")
                        .format(java.util.Date(parts[0].toLong()))

                    builder.append("Date: $date\nDuration: ${parts[1]} sec\n\n")
                }
            }

            tv.text = if (builder.isEmpty()) "No Data Available" else builder.toString()

        } catch (e: Exception) {
            tv.text = "Error loading report"
            e.printStackTrace()
        }

        setContentView(tv)
    }
}
