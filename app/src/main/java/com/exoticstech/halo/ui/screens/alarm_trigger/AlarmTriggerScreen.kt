package com.exoticstech.halo.ui.screens.alarm_trigger

import com.exoticstech.halo.ui.screens.alarm_trigger.widgets.PulsatingButton
import com.exoticstech.halo.ui.screens.alarm_trigger.widgets.StatusIndicator

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
    
    var isSoundOn by remember { mutableStateOf(true) }
    var isVibrationOn by remember { mutableStateOf(true) }

    LaunchedEffect(alarmId) {
        viewModel.loadAlarm(alarmId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title
            Text(
                text = "You've reached\nyour destination",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Location Chip
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Navigation, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alarm?.name ?: "Unknown Location",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
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
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Status Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                StatusIndicator(
                    icon = if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    label = if (isSoundOn) "SOUND ON" else "SOUND OFF",
                    isActive = isSoundOn,
                    onClick = {
                        isSoundOn = !isSoundOn
                        val intent = Intent(context, LocationForegroundService::class.java).apply {
                            action = LocationForegroundService.ACTION_TOGGLE_SOUND
                        }
                        context.startService(intent)
                    }
                )
                Spacer(modifier = Modifier.width(32.dp))
                StatusIndicator(
                    icon = Icons.Default.Vibration, 
                    label = if (isVibrationOn) "VIBRATION ON" else "VIBRATION OFF",
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
            
            Spacer(modifier = Modifier.weight(1f))
            
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
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    "Snooze (5 min)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Secondary Action
            TextButton(onClick = { /* Remind later */ }) {
                Text("Remind me in 500m", color = MaterialTheme.colorScheme.secondary)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

