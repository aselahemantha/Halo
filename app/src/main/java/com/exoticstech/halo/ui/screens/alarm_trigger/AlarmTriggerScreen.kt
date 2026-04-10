package com.exoticstech.halo.ui.screens.alarm_trigger

import com.exoticstech.halo.ui.screens.alarm_trigger.widgets.PulsatingButton
import com.exoticstech.halo.ui.screens.alarm_trigger.widgets.StatusIndicator
import com.exoticstech.halo.ui.screens.alarm_trigger.widgets.ArrivalAlertBadge
import com.exoticstech.halo.ui.screens.alarm_trigger.widgets.TargetLocationCard
import com.exoticstech.halo.ui.theme.*
import androidx.compose.foundation.isSystemInDarkTheme

import android.content.Intent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exoticstech.halo.services.LocationForegroundService
import com.exoticstech.halo.ui.viewmodel.AlarmTriggerViewModel

@Composable
fun AlarmTriggerScreen(
    alarmId: String,
    onNavigateBack: () -> Unit,
    viewModel: AlarmTriggerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val alarm by viewModel.alarm.collectAsState()
    val darkTheme = isSystemInDarkTheme()
    
    val backgroundColor = if (darkTheme) TriggerBackgroundDark else TriggerBackgroundLight
    val onBackground = if (darkTheme) Color.White else Color.Black
    val onSurfaceVariant = if (darkTheme) TriggerOnSurfaceVariantDark else TriggerOnSurfaceVariantLight
    val snoozeBg = if (darkTheme) TriggerSnoozeBgDark else Color(0xFFF0F4F8)

    var isSoundOn by remember { mutableStateOf(true) }
    var isVibrationOn by remember { mutableStateOf(true) }

    LaunchedEffect(alarmId) {
        viewModel.loadAlarm(alarmId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Badge
            ArrivalAlertBadge()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = "Destination Reached",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtitle
            Text(
                text = "You have arrived within the set perimeter.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Target Location Card
            TargetLocationCard(locationName = alarm?.name ?: "Headrogen | Sri Lanka")
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Pulsating Stop Button
            PulsatingButton(
                onClick = {
                    val stopIntent = Intent(context, LocationForegroundService::class.java).apply {
                        action = LocationForegroundService.ACTION_STOP_ALARM
                    }
                    context.startService(stopIntent)
                    onNavigateBack()
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Status Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    StatusIndicator(
                        icon = if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        label = if (isSoundOn) "Sound On" else "Sound Off",
                        isActive = isSoundOn,
                        onClick = {
                            isSoundOn = !isSoundOn
                            val intent = Intent(context, LocationForegroundService::class.java).apply {
                                action = LocationForegroundService.ACTION_TOGGLE_SOUND
                            }
                            context.startService(intent)
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    StatusIndicator(
                        icon = Icons.Default.Vibration, 
                        label = if (isVibrationOn) "Vibration On" else "Vibration Off",
                        isActive = isVibrationOn,
                        onClick = {
                            isVibrationOn = !isVibrationOn
                            val intent = Intent(context, LocationForegroundService::class.java).apply {
                                action = LocationForegroundService.ACTION_TOGGLE_VIBRATION
                            }
                            context.startService(intent)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Snooze Button
            Button(
                onClick = {
                    val intent = Intent(context, LocationForegroundService::class.java).apply {
                        action = LocationForegroundService.ACTION_SNOOZE
                        putExtra(LocationForegroundService.EXTRA_ALARM_ID, alarmId)
                    }
                    context.startService(intent)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = snoozeBg,
                    contentColor = onBackground
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    "Snooze",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Secondary Action
            TextButton(
                onClick = { /* Remind later */ },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Remind me in 500m", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.width(40.dp).height(2.dp).background(onSurfaceVariant.copy(alpha = 0.3f)))
                }
            }
        }
    }
}

