package com.example.redpixeldream

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.app.AlarmManager
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Paint
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.TextClock
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dreamContainer: View
    private val moveRunnable = object : Runnable {
        override fun run() {
            moveContentForBurnInProtection()
            handler.postDelayed(this, 60000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.dream_layout)
        dreamContainer = findViewById(R.id.dream_container)

        // Ukryj pasek stanu i nawigacji dla pełnego efektu
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Sprawdzamy uprawnienia
        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR), 100)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dream_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        updateAllInfo()
        handler.post(moveRunnable)
    }

    override fun onResume() {
        super.onResume()
        updateAllInfo()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(moveRunnable)
    }

    private fun updateAllInfo() {
        updateBatteryInfo()
        updateAlarmInfo()
        
        // Efekt obramowania dla godziny i dnia tygodnia
        findViewById<TextClock>(R.id.digital_clock)?.apply {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
        }
        findViewById<TextClock>(R.id.day_of_week)?.apply {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
        }

        findViewById<TextView>(R.id.events_text)?.text = getNextEvents()
    }

    // --- LOGIKA KOPIOWANA Z MYDREAMSERVICE ---

    private fun moveContentForBurnInProtection() {
        val random = Random()
        val padding = 30
        dreamContainer.setPadding(
            random.nextInt(padding),
            random.nextInt(padding),
            random.nextInt(padding),
            random.nextInt(padding),
        )
    }

    private fun getNextEvents(): String {
        val result = StringBuilder()
        val now = Calendar.getInstance()
        val startMillis = now.timeInMillis
        val endOfTomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }.timeInMillis

        val projection = arrayOf(CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN)
        val cursor = CalendarContract.Instances.query(contentResolver, projection, startMillis, endOfTomorrow)

        if ((cursor != null) && cursor.moveToFirst()) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val separator = "\n" + "─".repeat(20) + "\n"
            val today = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
            var showedTomorrowHeader = false
            
            do {
                val title = cursor.getString(0)
                val begin = cursor.getLong(1)
                val eventDay = Calendar.getInstance().apply { timeInMillis = begin }[Calendar.DAY_OF_YEAR]

                if (eventDay != today && !showedTomorrowHeader) {
                    if (result.isNotEmpty()) result.append("\n")
                    result.append(getString(R.string.tomorrow_header))
                    showedTomorrowHeader = true
                } else if (result.isEmpty() && eventDay == today) {
                    result.append(getString(R.string.today_header))
                }

                if (result.isNotEmpty() && !result.endsWith(":\n")) result.append(separator)
                result.append("${sdf.format(Date(begin))} - $title")
            } while (cursor.moveToNext())
            cursor.close()
        }
        return if (result.isEmpty()) getString(R.string.no_events_today_tomorrow) else result.toString()
    }

    private fun updateBatteryInfo() {
        val batteryStatus: Intent? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        findViewById<TextView>(R.id.status_info)?.text = getString(R.string.battery_format, level)
    }

    private fun updateAlarmInfo() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val nextAlarm = alarmManager.nextAlarmClock
        val alarmTextView = findViewById<TextView>(R.id.alarm_info)
        if (nextAlarm != null) {
            val sdf = SimpleDateFormat("EEE HH:mm", Locale.getDefault())
            alarmTextView?.text = getString(R.string.alarm_format, sdf.format(Date(nextAlarm.triggerTime)))
        } else {
            alarmTextView?.text = getString(R.string.alarm_none)
        }
    }
}