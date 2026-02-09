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
        val geofence = Geofence.Builder()
            .setRequestId(alarm.id.toString())
            .setCircularRegion(alarm.latitude, alarm.longitude, alarm.radius.toFloat())
            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time.
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
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
