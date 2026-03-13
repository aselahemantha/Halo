package com.exoticstech.halo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.exoticstech.halo.services.LocationForegroundService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e("GeofenceReceiver", "GeofencingEvent is null")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "Geofencing error: ${geofencingEvent.errorCode}")
            // Handle error...
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            if (triggeringGeofences != null && triggeringGeofences.isNotEmpty()) {
                val alarmId = triggeringGeofences[0].requestId
                Log.d("GeofenceReceiver", "Geofence triggered for alarm ID: $alarmId")
                
                // Start Foreground Service to handle the alarm (sound, notification, UI)
                val serviceIntent = Intent(context, LocationForegroundService::class.java).apply {
                    action = LocationForegroundService.ACTION_TRIGGER_ALARM
                    putExtra(LocationForegroundService.EXTRA_ALARM_ID, alarmId)
                }
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
