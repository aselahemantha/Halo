package com.example.halo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.halo.services.LocationForegroundService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(LocationForegroundService.EXTRA_ALARM_ID) ?: return

        // Trigger the alarm again
        val serviceIntent = Intent(context, LocationForegroundService::class.java).apply {
            action = LocationForegroundService.ACTION_TRIGGER_ALARM
            putExtra(LocationForegroundService.EXTRA_ALARM_ID, alarmId)
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
