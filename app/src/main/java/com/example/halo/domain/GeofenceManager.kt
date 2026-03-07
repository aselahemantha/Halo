package com.example.halo.domain

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.halo.domain.model.Alarm
import com.example.halo.receivers.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: GeofencingClient
) {

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that valid PendingIntent is retrieved
        // FLAG_MUTABLE might be needed for S+ depending on usage, but usually suggested for geofence.
        PendingIntent.getBroadcast(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission") // Permissions are handled in UI
    suspend fun addGeofence(alarm: Alarm) {
        val transitionType = when (alarm.triggerType) {
            "EXIT" -> Geofence.GEOFENCE_TRANSITION_EXIT
            "DWELL" -> Geofence.GEOFENCE_TRANSITION_DWELL
            else -> Geofence.GEOFENCE_TRANSITION_ENTER
        }

        val geofenceBuilder = Geofence.Builder()
            .setRequestId(alarm.id.toString())
            .setCircularRegion(alarm.latitude, alarm.longitude, alarm.radius.toFloat())
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(transitionType)
            
        if (transitionType == Geofence.GEOFENCE_TRANSITION_DWELL) {
            val minutes = alarm.dwellTimeMinutes ?: 5
            geofenceBuilder.setLoiteringDelay(minutes * 60 * 1000)
        }

        val geofence = geofenceBuilder.build()

        val initialTrigger = when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_EXIT -> GeofencingRequest.INITIAL_TRIGGER_EXIT
            Geofence.GEOFENCE_TRANSITION_DWELL -> GeofencingRequest.INITIAL_TRIGGER_DWELL
            else -> GeofencingRequest.INITIAL_TRIGGER_ENTER
        }

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(initialTrigger)
            .addGeofence(geofence)
            .build()

        client.addGeofences(request, geofencePendingIntent).await()
    }

    suspend fun removeGeofence(alarmId: Long) {
        client.removeGeofences(listOf(alarmId.toString())).await()
    }
    
    suspend fun removeGeofences(alarmIds: List<Long>) {
        if (alarmIds.isNotEmpty()) {
            client.removeGeofences(alarmIds.map { it.toString() }).await()
        }
    }
}
