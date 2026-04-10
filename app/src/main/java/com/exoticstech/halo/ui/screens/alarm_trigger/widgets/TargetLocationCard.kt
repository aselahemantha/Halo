package com.exoticstech.halo.ui.screens.alarm_trigger.widgets

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
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

@Suppress("ktlint:standard:function-naming")
@Composable
fun TargetLocationCard(locationName: String) {
    val darkTheme = isSystemInDarkTheme()
    val backgroundColor = if (darkTheme) TriggerSurfaceDark else TriggerSurfaceLight
    val onSurfaceColor = if (darkTheme) TriggerOnSurfaceDark else TriggerOnSurfaceLight
    val labelColor = if (darkTheme) TriggerOnSurfaceVariantDark else TriggerOnSurfaceVariantLight

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (darkTheme) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (darkTheme) Color(0xFF001D36) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "TARGET LOCATION",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = labelColor,
                )

                Text(
                    text = locationName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor,
                )
            }
        }
    }
}
