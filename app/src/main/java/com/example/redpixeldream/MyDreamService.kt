package com.example.redpixeldream

import android.app.AlarmManager
import android.content.Context
import android.service.dreams.DreamService
import android.widget.TextView
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.view.View
import android.view.WindowManager
import java.text.SimpleDateFormat
import java.util.*

class MyDreamService : DreamService() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dreamContainer: View
    private val moveRunnable = object : Runnable {
        override fun run() {
            moveContentForBurnInProtection()
            handler.postDelayed(this, 60000) // Co minutę
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        isInteractive = false
        isFullscreen = true

        setContentView(R.layout.dream_layout)
        dreamContainer = findViewById(R.id.dream_container)

        // Zmniejszenie jasności ekranu (0.0 do 1.0)
        val params = window.attributes
        params.screenBrightness = 0.03f // Bardzo niska jasność na noc
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        window.attributes = params

        // Aktualizacja baterii i alarmu
        updateBatteryInfo()
        updateAlarmInfo()

        // AKTUALIZACJA KALENDARZA
        val eventsTextView = findViewById<TextView>(R.id.events_text)
        eventsTextView?.text = getNextEvents()

        // Uruchomienie ochrony przed wypaleniem
        handler.post(moveRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(moveRunnable)
    }

    private fun moveContentForBurnInProtection() {
        val random = Random()
        val padding = 30 // Maksymalne przesunięcie w pikselach
        val padLeft = random.nextInt(padding)
        val padTop = random.nextInt(padding)
        val padRight = random.nextInt(padding)
        val padBottom = random.nextInt(padding)
        
        dreamContainer.setPadding(padLeft, padTop, padRight, padBottom)
    }

    private fun getNextEvents(): String {
        val result = StringBuilder()
        val now = Calendar.getInstance()
        val startMillis = now.timeInMillis
        
        // Koniec jutrzejszego dnia
        val endOfTomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN
        )

        val cursor = CalendarContract.Instances.query(
            contentResolver, projection, startMillis, endOfTomorrow
        )

        if (cursor != null && cursor.moveToFirst()) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val separator = "\n" + "─".repeat(20) + "\n"
            
            val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            var showedTomorrowHeader = false
            
            do {
                val title = cursor.getString(0)
                val begin = cursor.getLong(1)
                val eventTime = Calendar.getInstance().apply { timeInMillis = begin }
                val eventDay = eventTime.get(Calendar.DAY_OF_YEAR)

                if (eventDay != today && !showedTomorrowHeader) {
                    if (result.isNotEmpty()) result.append("\n")
                    result.append(getString(R.string.tomorrow_header))
                    showedTomorrowHeader = true
                } else if (result.isEmpty() && eventDay == today) {
                    result.append(getString(R.string.today_header))
                }

                if (result.isNotEmpty() && !result.endsWith(":\n")) {
                    result.append(separator)
                }
                
                result.append("${sdf.format(Date(begin))} - $title")
            } while (cursor.moveToNext())
            cursor.close()
        }

        return if (result.isEmpty()) getString(R.string.no_events_today_tomorrow) else result.toString()
    }

    private fun updateBatteryInfo() {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            registerReceiver(null, ifilter)
        }
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1

        val batteryTextView = findViewById<TextView>(R.id.status_info)
        batteryTextView?.text = getString(R.string.battery_format, level)
    }

    private fun updateAlarmInfo() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextAlarm = alarmManager.nextAlarmClock
        
        val alarmTextView = findViewById<TextView>(R.id.alarm_info)
        if (nextAlarm != null) {
            val sdf = SimpleDateFormat("EEE HH:mm", Locale.getDefault())
            val alarmTimeString = sdf.format(Date(nextAlarm.triggerTime))
            alarmTextView?.text = getString(R.string.alarm_format, alarmTimeString)
        } else {
            alarmTextView?.text = getString(R.string.alarm_none)
        }
    }
}
