package com.exoticstech.halo.ui.screens.settings

import com.exoticstech.halo.ui.screens.settings.widgets.*

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import com.exoticstech.halo.data.repository.AppTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exoticstech.halo.ui.viewmodel.SettingsViewModel
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import com.exoticstech.halo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWalkthrough: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val locationGranted by viewModel.locationPermissionGranted.collectAsState()
    val notificationGranted by viewModel.notificationPermissionGranted.collectAsState()
    val backgroundEnabled by viewModel.backgroundLocationEnabled.collectAsState()
    val defaultRadius by viewModel.defaultRadius.collectAsState()
    val batteryOptimized by viewModel.batteryOptimizationIgnored.collectAsState()
    val availableRingtones by viewModel.availableRingtones.collectAsState()

    var showHelpDialog by remember { mutableStateOf(false) }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.developer_support))

    val context = LocalContext.current

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchRingtones()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.settings_title), 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.cd_back), tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) }, // Center title trick
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background // Use theme background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. PERMISSIONS
            SectionHeader(stringResource(R.string.section_permissions))
            SettingsCard {
                Column {
                    // Location Access
                    SettingsItem(
                        icon = Icons.Default.LocationOn,
                        title = stringResource(R.string.perm_location),
                        subtitle = stringResource(R.string.perm_location_desc),
                        trailing = {
                            StatusBadge(active = locationGranted)
                        },
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                    SettingsDivider()
                    
                    // Background Data
                    SettingsItem(
                        icon = Icons.Default.Sync,
                        title = stringResource(R.string.perm_background),
                        subtitle = stringResource(R.string.perm_background_desc),
                        trailing = {
                            val backgroundPermissionGranted by viewModel.backgroundPermissionGranted.collectAsState()
                            
                            Switch(
                                checked = backgroundEnabled && backgroundPermissionGranted,
                                onCheckedChange = { 
                                    if (it && !backgroundPermissionGranted) {
                                        // Open App Settings because we need "Allow all the time"
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                        // Provide feedback?
                                        android.widget.Toast.makeText(context, context.getString(R.string.toast_background_allow_all), android.widget.Toast.LENGTH_LONG).show()
                                    } else {
                                        viewModel.toggleBackgroundLocation(it) 
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    uncheckedBorderColor = Color.Transparent
                                )
                            )
                        }
                    )
                    SettingsDivider()
                    
                    // Alert Notifications
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.perm_notifications),
                        subtitle = stringResource(R.string.perm_notifications_desc),
                        trailing = {
                            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        onClick = {
                             val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // 2. GENERAL
            SectionHeader(stringResource(R.string.section_general))
            SettingsCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                         Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(stringResource(R.string.default_radius_label), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(stringResource(R.string.radius_format_m, defaultRadius.toInt()), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Slider(
                        value = defaultRadius,
                        onValueChange = { viewModel.updateDefaultRadius(it) },
                        valueRange = 100f..2000f
                    )
                     Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.radius_100m), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.radius_1km), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.radius_2km), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                SettingsDivider()
                
                SettingsItem(
                    icon = Icons.Default.BatteryStd,
                    title = stringResource(R.string.battery_opt_label),
                    subtitle = stringResource(R.string.battery_opt_desc),
                    trailing = {
                         Switch(
                            checked = batteryOptimized,
                            onCheckedChange = { 
                                if (!batteryOptimized) {
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                } else {
                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    context.startActivity(intent)
                                }
                            },
                             colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                )

                SettingsDivider()

                val appTheme by viewModel.appTheme.collectAsState()
                var showThemeDialog by remember { mutableStateOf(false) }

                SettingsItem(
                    icon = Icons.Default.DarkMode, 
                    title = stringResource(R.string.app_theme_label),
                    subtitle = when(appTheme) {
                        AppTheme.SYSTEM -> stringResource(R.string.theme_system)
                        AppTheme.LIGHT -> stringResource(R.string.theme_light)
                        AppTheme.DARK -> stringResource(R.string.theme_dark)
                    },
                    trailing = {
                         Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    onClick = { showThemeDialog = true }
                )

                if (showThemeDialog) {
                    CustomThemeDialog(
                        currentTheme = appTheme,
                        onThemeSelected = {
                            viewModel.setTheme(it)
                            showThemeDialog = false
                        },
                        onDismiss = { showThemeDialog = false }
                    )
                }
            }

            // 3. ALERT SOUNDS
            SectionHeader(stringResource(R.string.section_alert_sounds))
            SettingsCard {
                 val currentSound by viewModel.alarmSound.collectAsState()
                 var showSoundDialog by remember { mutableStateOf(false) }


                 SettingsItem(
                    icon = Icons.Default.MusicNote,
                    title = stringResource(R.string.alarm_sound_label),
                    subtitle = stringResource(R.string.alarm_sound_current, currentSound.second),
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.modify_btn), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = { showSoundDialog = true }
                )
                
                if (showSoundDialog) {
                    CustomSoundPickerDialog(
                        availableRingtones = availableRingtones,
                        currentUri = currentSound.first,
                        onSoundSelected = { uri, title ->
                            viewModel.setAlarmSound(uri, title)
                            showSoundDialog = false
                        },
                        onDismiss = { showSoundDialog = false }
                    )
                }
            }

            // 4. DATA MANAGEMENT
            SectionHeader(stringResource(R.string.section_data_management))
            SettingsCard {
                Column {
                    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
                    ) { uri ->
                        if (uri != null) {
                            viewModel.exportAlarms(context, uri)
                        }
                    }

                    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
                    ) { uri ->
                        if (uri != null) {
                            viewModel.importAlarms(context, uri)
                        }
                    }

                    SettingsItem(
                        icon = Icons.Default.Save,
                        title = stringResource(R.string.export_alarms_label),
                        subtitle = stringResource(R.string.export_alarms_desc),
                        trailing = {
                            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                        iconColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            exportLauncher.launch("halo_alarms_backup.json")
                        }
                    )
                    
                    SettingsDivider()

                    SettingsItem(
                        icon = Icons.Default.FileOpen,
                        title = stringResource(R.string.import_alarms_label),
                        subtitle = stringResource(R.string.import_alarms_desc),
                        trailing = {
                            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                        iconColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            importLauncher.launch(arrayOf("application/json", "*/*"))
                        }
                    )
                }
            }

            // 5. SUPPORT
            SectionHeader(stringResource(R.string.section_support))
            SettingsCard {
                Column {
                    SettingsItem(
                        icon = Icons.Default.QuestionAnswer, 
                        title = stringResource(R.string.help_center_label),
                        subtitle = null,
                        iconBgColor = MaterialTheme.colorScheme.surfaceVariant,
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        trailing = {
                            Icon(Icons.Default.OpenInNew, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        },
                        onClick = { showHelpDialog = true }
                    )
                    
                    if (showHelpDialog) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { showHelpDialog = false },
                            title = {
                                Text(
                                    text = stringResource(R.string.help_center_dialog_title),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.help_center_dialog_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = { showHelpDialog = false }
                                ) {
                                    Text(stringResource(R.string.got_it_btn), fontWeight = FontWeight.Bold)
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    SettingsDivider()
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.about_app_label),
                        subtitle = null,
                        iconBgColor = MaterialTheme.colorScheme.surfaceVariant,
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        trailing = {
                            Text(stringResource(R.string.app_version), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        onClick = onNavigateToWalkthrough 
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    modifier = Modifier.size(100.dp),
                    iterations = com.airbnb.lottie.compose.LottieConstants.IterateForever
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.reset_all_settings),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { /* Reset Logic */ }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.made_in_sri_lanka),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

