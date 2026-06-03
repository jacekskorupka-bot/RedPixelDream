package com.example.redpixeldream

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PowerConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("dream_prefs", Context.MODE_PRIVATE)
        val proximityEnabled = prefs.getBoolean("proximity_enabled", true)
        
        if (proximityEnabled) {
            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    context.startForegroundService(Intent(context, ProximityService::class.java))
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    context.stopService(Intent(context, ProximityService::class.java))
                }
            }
        }
    }
}
