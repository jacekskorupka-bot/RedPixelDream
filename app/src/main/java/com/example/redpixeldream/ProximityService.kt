package com.example.redpixeldream

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

class ProximityService : Service(), SensorEventListener {
    private val tag = "ProximityService"
    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null
    
    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                            status == BatteryManager.BATTERY_STATUS_FULL
            
            if (!isCharging) {
                Log.d(tag, "Power disconnected. Stopping service.")
                stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        registerReceiver(powerReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopSelf()
            return START_NOT_STICKY
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification(): Notification {
        val channelId = "proximity_channel"
        val channel = NotificationChannel(channelId, "Obecność", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val stopIntent = Intent(this, ProximityService::class.java).apply {
            action = "STOP_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Wykrywanie zbliżenia aktywne")
            .setContentText("Działa podczas ładowania.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "WYŁĄCZ", stopPendingIntent)
            .build()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val distance = event?.values?.get(0) ?: 1f
        val maxRange = proximitySensor?.maximumRange ?: 0f
        
        if (distance < maxRange) {
            val prefs = getSharedPreferences("dream_prefs", MODE_PRIVATE)

            // 1. Check charging
            if (!isDeviceCharging()) {
                Log.d(tag, "Not charging. Stopping.")
                stopSelf()
                return
            }

            // 2. Check "Night only" window
            if (prefs.getBoolean("night_only_enabled", false) && !isCurrentlyNight(prefs)) {
                Log.d(tag, "Outside night window. Skipping wake up.")
                return
            }

            Log.d(tag, "Wake up triggered by proximity.")
            val intent = Intent(this, ClockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(tag, "Error: ${e.message}")
            }
        }
    }

    private fun isDeviceCharging(): Boolean {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || 
               status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun isCurrentlyNight(prefs: android.content.SharedPreferences): Boolean {
        val startHour = prefs.getInt("night_start_hour", 22)
        val startMinute = prefs.getInt("night_start_minute", 0)
        val endHour = prefs.getInt("night_end_hour", 6)
        val endMinute = prefs.getInt("night_end_minute", 0)

        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)

        val currentTime = currentHour * 60 + currentMinute
        val startTime = startHour * 60 + startMinute
        val endTime = endHour * 60 + endMinute

        return if (startTime <= endTime) {
            currentTime in startTime..endTime
        } else {
            currentTime >= startTime || currentTime <= endTime
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        unregisterReceiver(powerReceiver)
        super.onDestroy()
    }
}
