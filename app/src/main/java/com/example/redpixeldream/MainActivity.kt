package com.example.redpixeldream

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.graphics.toColorInt

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
        
        // Wybudzanie ekranu i działanie nad blokadą
        setupWakeFlags()
        
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

        val prefs = getSharedPreferences("dream_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("proximity_enabled", true) && isDeviceCharging()) {
            android.util.Log.d("MainActivity", "Starting ProximityService")
            startForegroundService(Intent(this, ProximityService::class.java))
        }

        checkOverlayPermission()
        initSettings()
    }

    private fun isDeviceCharging(): Boolean {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun checkOverlayPermission() {
        if (android.provider.Settings.canDrawOverlays(this)) return
        Toast.makeText(this, "Włącz 'Wyświetlanie nad innymi aplikacjami', aby wybudzanie działało", Toast.LENGTH_LONG).show()
        val intent = Intent(
            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:$packageName".toUri(),
        )
        startActivity(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        setupWakeFlags()
    }

    private fun setupWakeFlags() {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun initSettings() {
        val prefs = getSharedPreferences("dream_prefs", MODE_PRIVATE)
        val brightnessSeekBar = findViewById<SeekBar>(R.id.brightness_seekbar)
        val autoNightSwitch = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_auto_night)
        val proximitySwitch = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_proximity)
        val colorGroup = findViewById<RadioGroup>(R.id.color_group)
        val saveButton = findViewById<Button>(R.id.btn_save)

        // Load current values
        brightnessSeekBar.progress = (prefs.getFloat("brightness", 0.03f) * 100).toInt()
        autoNightSwitch.isChecked = prefs.getBoolean("auto_night", true)
        proximitySwitch.isChecked = prefs.getBoolean("proximity_enabled", true)
        
        when (prefs.getInt("clock_color", android.graphics.Color.RED)) {
            android.graphics.Color.RED -> colorGroup.check(R.id.radio_red)
            "#FFBF00".toColorInt() -> colorGroup.check(R.id.radio_amber)
            android.graphics.Color.GREEN -> colorGroup.check(R.id.radio_green)
        }

        saveButton.setOnClickListener {
            val brightness = brightnessSeekBar.progress / 100f
            val isAutoNight = autoNightSwitch.isChecked
            val isProximityEnabled = proximitySwitch.isChecked
            
            val selectedColor = when (colorGroup.checkedRadioButtonId) {
                R.id.radio_amber -> "#FFBF00".toColorInt()
                R.id.radio_green -> android.graphics.Color.GREEN
                else -> android.graphics.Color.RED
            }

            prefs.edit().apply {
                putFloat("brightness", brightness)
                putBoolean("auto_night", isAutoNight)
                putBoolean("proximity_enabled", isProximityEnabled)
                putInt("clock_color", selectedColor)
                apply()
            }

            // Start or stop the service based on setting
            if (isProximityEnabled) {
                startForegroundService(Intent(this, ProximityService::class.java))
            } else {
                stopService(Intent(this, ProximityService::class.java))
            }

            Toast.makeText(this, "Ustawienia zapisane", Toast.LENGTH_SHORT).show()
        }
    }
}
