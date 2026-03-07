package com.example.halo.ui.screens.home.widgets

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.halo.domain.model.Alarm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmItem(
    alarm: Alarm,
    currentLocation: com.google.android.gms.maps.model.LatLng?,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showShareDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    showShareDialog = true
                    false // Return false so it snaps back
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                else -> Color.Transparent
            }
            
            val iconVec = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Share
                else -> Icons.Default.Delete
            }

            val iconTint = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                else -> Color.Transparent
            }
            
            val alignment = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.Center
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (direction != SwipeToDismissBoxValue.Settled) {
                    Icon(
                        imageVector = iconVec,
                        contentDescription = null,
                        tint = iconTint
                    )
                }
            }
        },
        content = {
            val icon = when (alarm.category) {
                "Home" -> Icons.Default.Home
                "Work" -> Icons.Default.Work
                "Store" -> Icons.Default.ShoppingCart
                else -> Icons.Default.MyLocation
            }
            
            val iconColor = when (alarm.category) {
                "Home" -> Color(0xFFE8F5E9) // Light Green
                "Work" -> Color(0xFFE3F2FD) // Light Blue
                else -> Color(0xFFFFF3E0) // Light Orange
            }
            
            val iconTint = when (alarm.category) {
                "Home" -> Color(0xFF4CAF50)
                "Work" -> Color(0xFF2196F3)
                else -> Color(0xFFFF9800)
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat look from design
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(iconColor, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = iconTint)
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = alarm.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.Surface(
                                    color = iconColor.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = alarm.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = iconTint,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = if (alarm.isEnabled) "Arriving within ${alarm.radius.toInt()}m" else "Disabled",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        // Removed Share icon button from here, it's now accessible via swipe right

                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Alarm",
                                tint = Color.Gray
                            )
                        }

                        Switch(
                            checked = alarm.isEnabled,
                            onCheckedChange = onToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray.copy(alpha = 0.4f),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    // ETA Row below the main row
                    if (currentLocation != null) {
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(
                            currentLocation.latitude, currentLocation.longitude,
                            alarm.latitude, alarm.longitude,
                            results
                        )
                        val distanceMeters = results[0]
                        val distanceStr = if (distanceMeters > 1000) {
                            String.format(java.util.Locale.US, "%.1f km", distanceMeters / 1000)
                        } else {
                            "${distanceMeters.toInt()} m"
                        }
                        
                        // Rough walking ETA (5 km/h = ~83 m/min)
                        val etaMinutes = (distanceMeters / 83.33f).toInt()
                        val etaStr = if (etaMinutes > 60) {
                            "${etaMinutes / 60}h ${etaMinutes % 60}m"
                        } else if (etaMinutes > 0) {
                            "${etaMinutes}m"
                        } else {
                            "< 1m"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            Text(
                                text = "$distanceStr • ~$etaStr walk",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (showShareDialog) {
                // halo://alarm?name=MyAlarm&lat=1.23&lng=4.56&radius=500&category=Home
                val shareLink = "halo://alarm?name=${android.net.Uri.encode(alarm.name)}&lat=${alarm.latitude}&lng=${alarm.longitude}&radius=${alarm.radius}&category=${android.net.Uri.encode(alarm.category)}"
                val qrBitmap = remember(shareLink) {
                    com.example.halo.utils.QRCodeGenerator.generate(shareLink, 800)
                }

                AlertDialog(
                    onDismissRequest = { showShareDialog = false },
                    title = {
                        Text(
                            text = "Share Alarm",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Scan this QR code with another device to share the alarm location details.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap,
                                    contentDescription = "QR Code",
                                    modifier = Modifier
                                        .size(200.dp)
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(shareLink))
                                android.widget.Toast.makeText(context, "Link copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copy Link", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showShareDialog = false }) {
                            Text("Close", color = Color.Gray)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
