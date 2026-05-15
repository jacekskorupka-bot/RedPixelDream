package com.example.redpixeldream

import android.app.*
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.content.IntentFilter
import android.util.Log
import androidx.core.app.NotificationCompat

class ProximityService : Service(), SensorEventListener {
    private val tag = "ProximityService"
    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, createNotification())
        }
    }

    private fun createNotification(): Notification {
        val channelId = "proximity_channel"
        val channel = NotificationChannel(channelId, "Obecność", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Wykrywanie zbliżenia aktywne")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val distance = event?.values?.get(0) ?: 1f
        val maxRange = proximitySensor?.maximumRange ?: 0f
        
        Log.d(tag, "Sensor value: $distance, Max range: $maxRange")
        
        // Jeśli coś jest blisko (zazwyczaj 0.0)
        if (distance < maxRange) {
            // Sprawdzenie czy telefon się ładuje przed wybudzeniem
            if (!isDeviceCharging()) {
                Log.d(tag, "Object detected but device is NOT charging. Skipping wake up.")
                return
            }

            Log.d(tag, "Object detected and device is charging! Attempting to wake up ClockActivity...")
            val intent = Intent(this, ClockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(tag, "Failed to start activity: ${e.message}")
            }
        }
    }

    private fun isDeviceCharging(): Boolean {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || 
               status == BatteryManager.BATTERY_STATUS_FULL
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}
