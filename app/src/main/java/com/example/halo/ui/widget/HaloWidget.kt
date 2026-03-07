package com.example.halo.ui.widget

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
import com.example.halo.MainActivity
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
import androidx.compose.ui.graphics.Color
import com.example.halo.data.local.AlarmDatabase
import com.example.halo.R
import kotlinx.coroutines.flow.firstOrNull
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.halo.data.local.AlarmDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class HaloWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun alarmDao(): AlarmDao
    }
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val scope = rememberCoroutineScope()
            val activeAlarmCount = remember { mutableStateOf<Int?>(null) }
            
            LaunchedEffect(Unit) {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
                    val activeAlarms = entryPoint.alarmDao().getActiveAlarms().firstOrNull() ?: emptyList()
                    activeAlarmCount.value = activeAlarms.size
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
                        color = ColorProvider(Color(0xFF1976D2)) // Darker blue typography
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                if (activeAlarmCount.value != null) {
                    Text(
                        text = context.getString(R.string.widget_active_alarms, activeAlarmCount.value),
                        style = TextStyle(
                            color = ColorProvider(Color.Black)
                        )
                    )
                } else {
                    Text(
                        text = context.getString(R.string.widget_loading),
                        style = TextStyle(
                            color = ColorProvider(Color.Gray)
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(16.dp))

                Button(
                    text = context.getString(R.string.widget_add_alarm),
                    onClick = actionStartActivity(launchIntent)
                )
            }
        }
    }
}
