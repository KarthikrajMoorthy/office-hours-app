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
                val date = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(java.util.Date(parts[0].toLong()))
                builder.append("Date: $date \nDuration: ${parts[1]} sec\n\n")          
            }
        }

        tv.text = builder.toString()
        setContentView(tv)

        // ✅ CALL FUNCTIONS INSIDE onCreate
        exportCSV(builder.toString())
        exportPDF(builder.toString())
        android.widget.Toast.makeText(this, "Report Generated ✅", android.widget.Toast.LENGTH_LONG).show()
    }
private fun exportCSV(data: String) {
    try {
        val file = java.io.File(getExternalFilesDir(null), "office_report.csv")
        file.writeText(data)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun exportPDF(text: String) {
    try {
        val file = java.io.File(getExternalFilesDir(null), "office_report.pdf")

        val writer = com.itextpdf.kernel.pdf.PdfWriter(file)
        val pdf = com.itextpdf.kernel.pdf.PdfDocument(writer)
        val doc = com.itextpdf.layout.Document(pdf)

        doc.add(com.itextpdf.layout.element.Paragraph(text))
        doc.close()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}
}
