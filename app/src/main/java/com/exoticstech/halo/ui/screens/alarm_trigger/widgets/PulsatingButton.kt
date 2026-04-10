package com.exoticstech.halo.ui.screens.alarm_trigger.widgets

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exoticstech.halo.ui.theme.*

@Composable
fun PulsatingButton(onClick: () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val primaryColor = if (darkTheme) TriggerPrimaryDark else TriggerPrimaryLight
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer Rings (Pulsing)
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(scale)
                .background(primaryColor.copy(alpha = alpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(scale * 0.9f)
                .background(primaryColor.copy(alpha = alpha * 1.5f), CircleShape)
        )
        
        // Main Button
        Surface(
            onClick = onClick,
            modifier = Modifier.size(210.dp),
            shape = CircleShape,
            color = primaryColor,
            shadowElevation = 8.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "STOP",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Text(
                    "TAP TO SILENCE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
