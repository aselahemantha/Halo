package com.exoticstech.halo.ui.screens.alarm_trigger.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exoticstech.halo.ui.theme.*

@Composable
fun ArrivalAlertBadge() {
    val darkTheme = isSystemInDarkTheme()
    val backgroundColor = if (darkTheme) TriggerBadgeBgDark else Color(0xFFE2E2E6)
    val dotColor = if (darkTheme) TriggerPrimaryDark else TriggerPrimaryLight
    val textColor = if (darkTheme) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.6f)

    Surface(
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ARRIVAL ALERT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = 1.sp
            )
        }
    }
}
