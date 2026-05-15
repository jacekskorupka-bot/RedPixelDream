package com.example.redpixeldream

import android.app.AlarmManager
import android.content.Context
import android.graphics.Paint
import android.service.dreams.DreamService
import android.widget.TextView
import android.widget.TextClock
import android.graphics.Color
import android.widget.LinearLayout
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.provider.CalendarContract
import android.view.View
import android.view.WindowManager
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class MyDreamService : DreamService() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dreamContainer: View
    private var clockColor: Int = Color.RED

    private val moveRunnable = object : Runnable {
        override fun run() {
            moveContentForBurnInProtection()
            handler.postDelayed(this, 60000) // Co minutę
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Sprawdzenie czy telefon się ładuje - jeśli nie, wyłączamy wygaszacz natychmiast
        if (!isDeviceCharging()) {
            finish()
            return
        }

        isInteractive = false
        isFullscreen = true

        val prefs = getSharedPreferences("dream_prefs", MODE_PRIVATE)
        val clockColor = prefs.getInt("clock_color", Color.RED)
        val brightnessToApply = getEffectiveBrightness(prefs)

        setContentView(R.layout.dream_layout)
        dreamContainer = findViewById(R.id.dream_container)

        // Zastosowanie jasności
        val params = window.attributes
        params.screenBrightness = brightnessToApply
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        window.attributes = params

        this.clockColor = clockColor
        applyColors()
        
        // Aktualizacja baterii i alarmu
        updateBatteryInfo()
        updateAlarmInfo()

        // AKTUALIZACJA KALENDARZA
        val eventsTextView = findViewById<TextView>(R.id.events_text)
        eventsTextView?.text = getNextEvents()

        // AKTUALIZACJA SIATKI MIESIĄCA
        updateMonthGrid()

        // AKTUALIZACJA POGODY I LOKALIZACJI
        updateWeatherInfo()

        // Uruchomienie ochrony przed wypaleniem
        handler.post(moveRunnable)
    }

    private fun isDeviceCharging(): Boolean {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || 
               status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun applyColors() {
        // Efekt obramowania dla godziny i dnia tygodnia (Styl "Bold Outline")
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
            paint.strokeWidth = 0f
        }
        
        findViewById<TextView>(R.id.events_text)?.setTextColor(clockColor)
        findViewById<TextView>(R.id.month_grid)?.setTextColor(clockColor)
        findViewById<TextView>(R.id.weather_info)?.setTextColor(clockColor)
    }

    private fun updateMonthGrid() {
        val monthGridTextView = findViewById<TextView>(R.id.month_grid)
        monthGridTextView?.text = generateMonthGrid()
    }

    private fun generateMonthGrid(): CharSequence {
        val cal = Calendar.getInstance()
        val today = cal[Calendar.DAY_OF_MONTH]
        
        cal[Calendar.DAY_OF_MONTH] = 1
        val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())?.uppercase()
        val firstDayOfWeek = cal[Calendar.DAY_OF_WEEK] 
        
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val sb = StringBuilder()
        sb.append("$monthName\n")
        sb.append("PN WT ŚR CZ PT SO ND\n")
        
        repeat(offset) {
            sb.append("   ")
        }
        
        val fullText = StringBuilder(sb.toString())
        var todayStart = -1
        var todayEnd = -1

        for (i in offset until (offset + daysInMonth)) {
            val currentDay = (i - offset) + 1
            val dayStr = currentDay.toString().padStart(2)
            
            if (currentDay == today) {
                todayStart = fullText.length
                fullText.append(dayStr)
                todayEnd = fullText.length
            } else {
                fullText.append(dayStr)
            }
            
            if ((i + 1) % 7 == 0) {
                fullText.append("\n")
            } else {
                fullText.append(" ")
            }
        }

        val spannable = android.text.SpannableString(fullText)
        if (todayStart != -1) {
            // Tło: Kolor wybrany przez użytkownika
            spannable.setSpan(
                android.text.style.BackgroundColorSpan(clockColor),
                todayStart, todayEnd, 
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            // Tekst: Czarny (kontrast)
            spannable.setSpan(
                android.text.style.ForegroundColorSpan(Color.BLACK),
                todayStart, todayEnd, 
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            spannable.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                todayStart, todayEnd,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
        
        return spannable
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(moveRunnable)
    }

    private fun moveContentForBurnInProtection() {
        val random = Random()
        val padding = 30 
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
        val endOfTomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
        )

        val cursor = CalendarContract.Instances.query(
            contentResolver, projection, startMillis, endOfTomorrow
        )

        if ((cursor != null) && cursor.moveToFirst()) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val separator = "\n" + "─".repeat(20) + "\n"
            val today = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
            var showedTomorrowHeader = false
            
            do {
                val title = cursor.getString(0)
                val begin = cursor.getLong(1)
                val eventTime = Calendar.getInstance().apply { timeInMillis = begin }
                val eventDay = eventTime[Calendar.DAY_OF_YEAR]

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
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
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

    private fun getEffectiveBrightness(prefs: android.content.SharedPreferences): Float {
        val autoNight = prefs.getBoolean("auto_night", true)
        val userBrightness = prefs.getFloat("brightness", 0.03f)
        if (autoNight) {
            val hour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            if (hour in 22..23 || hour in 0..5) {
                return 0.01f
            }
        }
        return userBrightness
    }

    private fun updateWeatherInfo() {
        val weatherTextView = findViewById<TextView>(R.id.weather_info) ?: return
        
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            weatherTextView.text = getString(R.string.no_location_permission)
            return
        }

        thread {
            try {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val providers = locationManager.getProviders(true)
                var bestLocation: Location? = null
                for (provider in providers) {
                    val l = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                        bestLocation = l
                    }
                }

                bestLocation?.let { location ->
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val cityName = addresses?.firstOrNull()?.locality ?: "Nieznana lokalizacja"

                    val url = URL("https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}&current_weather=true")
                    val connection = url.openConnection() as HttpURLConnection
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    val currentWeather = json.getJSONObject("current_weather")
                    val temp = currentWeather.getDouble("temperature")
                    val code = currentWeather.getInt("weathercode")
                    val description = getWeatherDescription(code)

                    handler.post {
                        weatherTextView.text = getString(R.string.weather_format, cityName, description, temp)
                    }
                }
            } catch (ignored: Exception) {
                handler.post {
                    weatherTextView.text = getString(R.string.weather_error)
                }
            }
        }
    }

    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Jasno"
            1, 2, 3 -> "Zachmurzenie"
            45, 48 -> "Mgła"
            51, 53, 55 -> "Mżawka"
            61, 63, 65 -> "Deszcz"
            66, 67 -> "Marznący deszcz"
            71, 73, 75 -> "Śnieg"
            77 -> "Grad"
            80, 81, 82 -> "Ulewa"
            85, 86 -> "Zamieć śnieżna"
            95, 96, 99 -> "Burza"
            else -> "Nieznana"
        }
    }
}
