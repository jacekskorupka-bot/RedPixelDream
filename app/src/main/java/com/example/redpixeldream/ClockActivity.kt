package com.example.redpixeldream

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.location.Geocoder
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.TextClock
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class ClockActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dreamContainer: View
    private var clockColor: Int = Color.RED

    private val moveRunnable = object : Runnable {
        override fun run() {
            moveContentForBurnInProtection()
            handler.postDelayed(this, 60000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupWakeFlags()
        setContentView(R.layout.dream_layout)
        dreamContainer = findViewById(R.id.dream_container)

        val prefs = getSharedPreferences("dream_prefs", Context.MODE_PRIVATE)
        clockColor = prefs.getInt("clock_color", Color.RED)
        val brightnessToApply = getEffectiveBrightness(prefs)

        // Jasność
        val params = window.attributes
        params.screenBrightness = brightnessToApply
        window.attributes = params

        applyColors()
        updateAllInfo()
        handler.post(moveRunnable)

        // Zamknij po dotknięciu (jak wygaszacz)
        dreamContainer.setOnClickListener { finish() }
    }

    private fun getEffectiveBrightness(prefs: android.content.SharedPreferences): Float {
        val autoNight = prefs.getBoolean("auto_night", true)
        val userBrightness = prefs.getFloat("brightness", 0.03f)
        if (autoNight) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (hour >= 22 || hour < 6) {
                return 0.01f
            }
        }
        return userBrightness
    }

    private fun setupWakeFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
    }

    private fun applyColors() {
        findViewById<TextClock>(R.id.digital_clock)?.apply {
            setTextColor(clockColor)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeCap = Paint.Cap.ROUND
        }
        findViewById<TextClock>(R.id.day_of_week)?.apply {
            setTextColor(clockColor)
            paint.style = Paint.Style.FILL
        }
        findViewById<TextView>(R.id.events_text)?.setTextColor(clockColor)
        findViewById<TextView>(R.id.month_grid)?.setTextColor(clockColor)
        findViewById<TextView>(R.id.weather_info)?.setTextColor(clockColor)
    }

    private fun updateAllInfo() {
        updateBatteryInfo()
        updateAlarmInfo()
        updateWeatherInfo()
        findViewById<TextView>(R.id.month_grid)?.text = generateMonthGrid()
        findViewById<TextView>(R.id.events_text)?.text = getNextEvents()
    }

    private fun generateMonthGrid(): CharSequence {
        val cal = Calendar.getInstance()
        val today = cal[Calendar.DAY_OF_MONTH]
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())?.uppercase()
        val firstDayOfWeek = cal[Calendar.DAY_OF_WEEK]
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val sb = StringBuilder("$monthName\nPN WT ŚR CZ PT SO ND\n")
        repeat(offset) { sb.append("   ") }
        val fullText = StringBuilder(sb.toString())
        var todayStart = -1
        var todayEnd = -1
        for (i in offset until offset + daysInMonth) {
            val currentDay = i - offset + 1
            val dayStr = currentDay.toString().padStart(2)
            if (currentDay == today) {
                todayStart = fullText.length
                fullText.append(dayStr)
                todayEnd = fullText.length
            } else {
                fullText.append(dayStr)
            }
            if ((i + 1) % 7 == 0) fullText.append("\n") else fullText.append(" ")
        }
        val spannable = SpannableString(fullText)
        if (todayStart != -1) {
            spannable.setSpan(BackgroundColorSpan(clockColor), todayStart, todayEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(ForegroundColorSpan(Color.BLACK), todayStart, todayEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Typeface.BOLD), todayStart, todayEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return spannable
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
        if (cursor != null && cursor.moveToFirst()) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
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
                if (result.isNotEmpty() && !result.endsWith(":\n")) result.append("\n" + "─".repeat(20) + "\n")
                result.append("${sdf.format(Date(begin))} - $title")
            } while (cursor.moveToNext())
            cursor.close()
        }
        return if (result.isEmpty()) getString(R.string.no_events_today_tomorrow) else result.toString()
    }

    private fun updateBatteryInfo() {
        val batteryStatus: Intent? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val batteryTextView = findViewById<TextView>(R.id.status_info)
        if (level >= 80) {
            batteryTextView?.setTextColor(Color.YELLOW)
            batteryTextView?.text = getString(R.string.battery_limit_format, level)
        } else {
            batteryTextView?.setTextColor(Color.GREEN)
            batteryTextView?.text = getString(R.string.battery_format, level)
        }
    }

    private fun updateAlarmInfo() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextAlarm = alarmManager.nextAlarmClock
        val alarmTextView = findViewById<TextView>(R.id.alarm_info)
        if (nextAlarm != null) {
            val sdf = SimpleDateFormat("EEE HH:mm", Locale.getDefault())
            alarmTextView?.text = getString(R.string.alarm_format, sdf.format(Date(nextAlarm.triggerTime)))
        } else {
            alarmTextView?.text = getString(R.string.alarm_none)
        }
    }

    private fun updateWeatherInfo() {
        val weatherTextView = findViewById<TextView>(R.id.weather_info) ?: return
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        thread {
            try {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val providers = locationManager.getProviders(true)
                var bestLocation: android.location.Location? = null
                for (provider in providers) {
                    val l = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                        bestLocation = l
                    }
                }

                bestLocation?.let {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val cityName = geocoder.getFromLocation(it.latitude, it.longitude, 1)?.firstOrNull()?.locality ?: "Nieznana"
                    val url = URL("https://api.open-meteo.com/v1/forecast?latitude=${it.latitude}&longitude=${it.longitude}&current_weather=true")
                    val response = url.openConnection().getInputStream().bufferedReader().use { r -> r.readText() }
                    val json = JSONObject(response)
                    val currentWeather = json.getJSONObject("current_weather")
                    val temp = currentWeather.getDouble("temperature")
                    handler.post { weatherTextView.text = getString(R.string.weather_format, cityName, temp) }
                }
            } catch (e: Exception) { }
        }
    }

    private fun moveContentForBurnInProtection() {
        val random = Random()
        val padding = 30
        dreamContainer.setPadding(random.nextInt(padding), random.nextInt(padding), random.nextInt(padding), random.nextInt(padding))
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(moveRunnable)
    }
}
