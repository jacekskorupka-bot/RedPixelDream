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
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.provider.CalendarContract
import android.view.View
import android.view.WindowManager
import android.widget.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Sprawdzamy uprawnienia
        val permissions = mutableListOf(Manifest.permission.READ_CALENDAR)
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        if (permissions.isNotEmpty()) {
            requestPermissions(permissions.toTypedArray(), 100)
        }

        // Uruchomienie usługi wykrywania zbliżenia
        startForegroundService(Intent(this, ProximityService::class.java))

        initSettings()
    }

    private fun initSettings() {
        val prefs = getSharedPreferences("dream_prefs", MODE_PRIVATE)
        val brightnessSeekBar = findViewById<SeekBar>(R.id.brightness_seekbar)
        val colorGroup = findViewById<RadioGroup>(R.id.color_group)
        val saveButton = findViewById<Button>(R.id.btn_save)

        // Load current values
        brightnessSeekBar.progress = (prefs.getFloat("brightness", 0.03f) * 100).toInt()
        val currentColor = prefs.getInt("clock_color", android.graphics.Color.RED)
        when (currentColor) {
            android.graphics.Color.RED -> colorGroup.check(R.id.radio_red)
            android.graphics.Color.parseColor("#FFBF00") -> colorGroup.check(R.id.radio_amber)
            android.graphics.Color.GREEN -> colorGroup.check(R.id.radio_green)
        }

        saveButton.setOnClickListener {
            val brightness = brightnessSeekBar.progress / 100f
            val selectedColor = when (colorGroup.checkedRadioButtonId) {
                R.id.radio_amber -> android.graphics.Color.parseColor("#FFBF00")
                R.id.radio_green -> android.graphics.Color.GREEN
                else -> android.graphics.Color.RED
            }

            prefs.edit().apply {
                putFloat("brightness", brightness)
                putInt("clock_color", selectedColor)
                apply()
            }
            Toast.makeText(this, "Ustawienia zapisane", Toast.LENGTH_SHORT).show()
        }
    }
}
