package com.exoticstech.halo.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.text.Text
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.Alignment
import androidx.glance.Button
import androidx.glance.appwidget.action.actionStartActivity
import android.content.Intent
import com.exoticstech.halo.MainActivity
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import androidx.glance.background
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Row
import androidx.glance.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.exoticstech.halo.data.local.AlarmDatabase
import com.exoticstech.halo.R
import kotlinx.coroutines.flow.firstOrNull
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.exoticstech.halo.data.local.AlarmDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class HaloWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun alarmDao(): com.exoticstech.halo.data.local.AlarmDao
        fun fusedLocationClient(): com.google.android.gms.location.FusedLocationProviderClient
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val activeAlarmCount = remember { mutableStateOf<Int?>(null) }
            val currentAddress = remember { mutableStateOf<String?>(null) }
            
            LaunchedEffect(Unit) {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
                    
                    // Fetch Alarm Count
                    val activeAlarms = entryPoint.alarmDao().getActiveAlarms().firstOrNull() ?: emptyList()
                    activeAlarmCount.value = activeAlarms.size

                    // Fetch Location
                    val locationClient = entryPoint.fusedLocationClient()
                    try {
                        locationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                // Simple reverse geocoding (blocking, but in LaunchedEffect it's okay for widget update)
                                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                                try {
                                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                    if (!addresses.isNullOrEmpty()) {
                                        currentAddress.value = addresses[0].getAddressLine(0)
                                    } else {
                                        currentAddress.value = "Unknown Location"
                                    }
                                } catch (e: Exception) {
                                    currentAddress.value = "Error fetching address"
                                }
                            } else {
                                currentAddress.value = "Location not available"
                            }
                        }
                    } catch (e: SecurityException) {
                        currentAddress.value = "Permission required"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    activeAlarmCount.value = 0
                }
            }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = Intent.ACTION_VIEW
                data = android.net.Uri.parse("halo://alarm_shortcut")
            }

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFFE3F2FD)) // Light Blue
                    .padding(16.dp)
                    .cornerRadius(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = context.getString(R.string.widget_title),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color(0xFF1976D2)),
                        fontSize = 18.sp
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                // Location Display
                Text(
                    text = currentAddress.value ?: context.getString(R.string.widget_loading),
                    style = TextStyle(
                        color = ColorProvider(Color.Black),
                        fontSize = 14.sp
                    ),
                    maxLines = 1
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                if (activeAlarmCount.value != null) {
                    Text(
                        text = context.getString(R.string.widget_active_alarms, activeAlarmCount.value),
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF1976D2)),
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                Button(
                    text = context.getString(R.string.widget_add_alarm),
                    onClick = actionStartActivity(launchIntent)
                )
            }
        }
    }
}
