package com.exoticstech.halo.ui.screens.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exoticstech.halo.utils.PermissionUtils

@Composable
fun PermissionRequestScreen(
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    
    var showSettingsRationale by remember { mutableStateOf(false) }

    // Sequential permission states
    var foregroundLocationGranted by remember { 
        mutableStateOf(PermissionUtils.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) 
    }
    var backgroundLocationGranted by remember { 
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PermissionUtils.isPermissionGranted(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else true
        ) 
    }
    var notificationsGranted by remember { 
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionUtils.isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS)
            } else true
        ) 
    }

    // Launchers
    val foregroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        foregroundLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (!foregroundLocationGranted) {
            showSettingsRationale = true
        }
    }

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        backgroundLocationGranted = isGranted
        if (!isGranted) {
            showSettingsRationale = true
        }
    }

    val notificationsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationsGranted = isGranted
    }

    // Check if all are granted to navigate away
    LaunchedEffect(foregroundLocationGranted, backgroundLocationGranted, notificationsGranted) {
        if (foregroundLocationGranted && backgroundLocationGranted && notificationsGranted) {
            onAllPermissionsGranted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Security,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Halo requires these permissions to provide reliable location-based alerts.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        PermissionItem(
            icon = Icons.Rounded.LocationOn,
            title = "Precise Location",
            description = "Needed to detect when you reach your destination.",
            isGranted = foregroundLocationGranted
        )
        
        PermissionItem(
            icon = Icons.Rounded.LocationOn,
            title = "Background Tracking",
            description = "Allows the app to monitor your location even when closed.",
            isGranted = backgroundLocationGranted
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionItem(
                icon = Icons.Rounded.Notifications,
                title = "Alert Notifications",
                description = "Required to sound the alarm when you arrive.",
                isGranted = notificationsGranted
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                when {
                    !foregroundLocationGranted -> {
                        foregroundLocationLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                    !backgroundLocationGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                    !notificationsGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                        notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    showSettingsRationale -> {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(
                text = if (showSettingsRationale) "Open Settings" else "Grant Permissions",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (showSettingsRationale) {
            TextButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("App Settings")
            }
        }
    }
}

@Composable
fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (isGranted) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Granted",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
