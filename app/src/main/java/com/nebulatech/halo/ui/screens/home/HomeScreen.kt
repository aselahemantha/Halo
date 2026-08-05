package com.nebulatech.halo.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.nebulatech.halo.R
import com.nebulatech.halo.domain.model.Alarm
import com.nebulatech.halo.ui.components.MapPreviewCard
import com.nebulatech.halo.ui.components.BottomNavBar
import androidx.navigation.NavController
import com.nebulatech.halo.ui.screens.home.widgets.AlarmItem
import com.nebulatech.halo.ui.screens.home.widgets.CurrentStatusCard
import com.nebulatech.halo.ui.screens.home.widgets.StatsCard
import com.nebulatech.halo.ui.viewmodel.AlarmFilter
import com.nebulatech.halo.ui.viewmodel.HomeViewModel
import com.nebulatech.halo.ui.viewmodel.UpdateInfo
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddAlarm: () -> Unit,
    onNavigateToEditAlarm: (Long) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val alarms by viewModel.alarms.collectAsState()
    val activeCount = alarms.count { it.isEnabled }
    val currentLocation by viewModel.currentLocation.collectAsState()
    val currentAddress by viewModel.currentAddress.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    // Permission Handling
    var showPermissionDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Only check foreground permissions here. Background location is handled by
    // the dedicated PermissionRequestScreen with a proper 2-step flow (Android 11+ requirement).
    val foregroundPermissions =
        buildList {
            add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()

    val permissionLauncher =
        androidx.activity.compose.rememberLauncherForActivityResult(
            contract =
                androidx.activity.result.contract.ActivityResultContracts
                    .RequestMultiplePermissions(),
            onResult = { result ->
                val locationGranted =
                    result[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        result[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
                if (locationGranted) {
                    viewModel.refreshLocation()
                }
                showPermissionDialog = false
            },
        )

    LaunchedEffect(Unit) {
        val hasPermissions =
            foregroundPermissions.all {
                androidx.core.content.ContextCompat
                    .checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }

        if (!hasPermissions) {
            showPermissionDialog = true
        }
    }

    if (showPermissionDialog) {
        com.nebulatech.halo.ui.components.PermissionRequestDialog(
            onDismiss = { showPermissionDialog = false },
            onConfirm = {
                permissionLauncher.launch(foregroundPermissions)
            },
        )
    }

    val checkPermissions = {
        val hasPermissions =
            foregroundPermissions.all {
                androidx.core.content.ContextCompat
                    .checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        if (!hasPermissions) {
            showPermissionDialog = true
            false
        } else {
            true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name_geo_alarm),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                },
                actions = {
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (checkPermissions()) {
                        onNavigateToAddAlarm()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.AddAlarm,
                    contentDescription = stringResource(R.string.cd_add_alarm),
                    modifier = Modifier.size(32.dp),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // 1. Current Status Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.current_status),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                CurrentStatusCard(
                    currentLocation = currentLocation,
                    currentAddress = currentAddress,
                    onLocationClick = {
                        if (checkPermissions()) {
                            // Logic to focus location could be added here if needed,
                            // but for now we just want to ensure permissions are checked.
                            // The card itself handles camera updates when location changes.
                        }
                    },
                )
            }

            // 2. Active Alarms Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.active_alarms),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape,
                    ) {
                        Text(
                            text = stringResource(R.string.active_count, activeCount),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            val showSearchAndFilter =
                alarms.isNotEmpty() || searchQuery.isNotEmpty() || selectedFilter != com.nebulatech.halo.ui.viewmodel.AlarmFilter.ALL

            if (showSearchAndFilter) {
                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text(stringResource(R.string.search_alarms)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.cd_search_icon))
                        },
                    )
                }

                // Filter Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == AlarmFilter.ALL,
                                onClick = { viewModel.updateSelectedFilter(AlarmFilter.ALL) },
                                label = { Text(stringResource(R.string.filter_all)) },
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == AlarmFilter.ACTIVE,
                                onClick = { viewModel.updateSelectedFilter(AlarmFilter.ACTIVE) },
                                label = { Text(stringResource(R.string.filter_active)) },
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == AlarmFilter.INACTIVE,
                                onClick = { viewModel.updateSelectedFilter(AlarmFilter.INACTIVE) },
                                label = { Text(stringResource(R.string.filter_inactive)) },
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == AlarmFilter.PROXIMITY,
                                onClick = { viewModel.updateSelectedFilter(AlarmFilter.PROXIMITY) },
                                label = { Text(stringResource(R.string.filter_nearest)) },
                            )
                        }
                    }
                }
            }

            // List of Alarms
            items(alarms) { alarm ->
                AlarmItem(
                    alarm = alarm,
                    currentLocation = currentLocation,
                    onToggle = { isEnabled -> viewModel.toggleAlarm(alarm, isEnabled) },
                    onEdit = { onNavigateToEditAlarm(alarm.id) },
                    onDelete = { viewModel.deleteAlarm(alarm) },
                )
            }
            if (alarms.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.no_alarms_created),
                        modifier = Modifier.padding(vertical = 24.dp),
                        color = Color.Gray,
                    )
                }
            }

            // 3. Stats Section
            item {
                val alarmsThisWeek by viewModel.alarmsThisWeek.collectAsState()
                val totalAlarmsCount by viewModel.totalAlarmsCount.collectAsState()
                val batteryImpact by viewModel.batteryImpact.collectAsState()
                val updateInfo by viewModel.updateInfo.collectAsState()

                if (updateInfo?.isUpdateAvailable == true) {
                    UpdatePromptDialog(
                        updateInfo = updateInfo!!,
                        onDismiss = { viewModel.onUpdatePromptDismissed() },
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.SsidChart, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.alarms_this_week),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        stringResource(R.string.created_alarms),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                    )
                                    Text(
                                        totalAlarmsCount.toString(),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Spacer(modifier = Modifier.width(24.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        stringResource(R.string.triggered_this_week),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                    )
                                    Text(
                                        alarmsThisWeek.toString(),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }

                    StatsCard(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.BatteryStd,
                        label = stringResource(R.string.battery_impact),
                        value = batteryImpact,
                        description = stringResource(R.string.battery_detail_format, activeCount),
                        color = if (batteryImpact == "High") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun UpdatePromptDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
            )
        },
        title = {
            Text(
                text = "Update Available",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Text(
                text = "A new version of Halo is available. Update now to get the latest features and improvements.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.updateUrl))
                    context.startActivity(intent)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text("Update Now")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Later",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
            ),
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
    )
}
