package com.exoticstech.halo.ui.screens.alarm_trigger.widgets

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exoticstech.halo.ui.theme.*

@Composable
fun StatusIndicator(
    icon: ImageVector, 
    label: String, 
    isActive: Boolean,
    onClick: () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val backgroundColor = if (darkTheme) TriggerSurfaceDark else TriggerSurfaceLight
    val onSurfaceColor = if (darkTheme) TriggerOnSurfaceDark else TriggerOnSurfaceLight
    val onSurfaceVariantColor = if (darkTheme) TriggerOnSurfaceVariantDark else TriggerOnSurfaceVariantLight
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = if (isActive) onSurfaceColor else onSurfaceVariantColor.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isActive) onSurfaceColor else onSurfaceVariantColor.copy(alpha = 0.5f),
                letterSpacing = 0.5.sp
            )
        }
    }
}
