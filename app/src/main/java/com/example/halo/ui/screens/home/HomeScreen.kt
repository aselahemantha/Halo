package com.example.halo.ui.screens.home

import com.example.halo.ui.screens.home.widgets.CurrentStatusCard
import com.example.halo.ui.screens.home.widgets.AlarmItem
import com.example.halo.ui.screens.home.widgets.StatsCard

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
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.lazy.LazyRow
import com.example.halo.ui.viewmodel.AlarmFilter
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.halo.domain.model.Alarm
import com.example.halo.ui.components.MapPreviewCard
import com.example.halo.ui.viewmodel.HomeViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddAlarm: () -> Unit,
    onNavigateToEditAlarm: (Long) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
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
    
    val permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { result ->
            val allGranted = result.values.all { it }
            if (allGranted) {
                showPermissionDialog = false
            } else {
                showPermissionDialog = false
            }
        }
    )
    
    LaunchedEffect(Unit) {
        val hasPermissions = permissions.all {
                androidx.core.content.ContextCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        
        if (!hasPermissions) {
            showPermissionDialog = true
        }
    }

    if (showPermissionDialog) {
        com.example.halo.ui.components.PermissionRequestDialog(
            onDismiss = { showPermissionDialog = false },
            onConfirm = {
                permissionLauncher.launch(permissions)
            }
        )
    }

    val checkPermissions = {
        val hasPermissions = permissions.all {
            androidx.core.content.ContextCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
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
                        "Halo",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {

                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Alarm History",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.SettingsSuggest,
                            contentDescription = "Settings",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
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
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddAlarm,
                    contentDescription = "Add Alarm",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // 1. Current Status Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "CURRENT STATUS",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
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
                    }
                )
            }

            // 2. Active Alarms Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ACTIVE ALARMS",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "$activeCount Active",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            val showSearchAndFilter = alarms.isNotEmpty() || searchQuery.isNotEmpty() || selectedFilter != com.example.halo.ui.viewmodel.AlarmFilter.ALL
            
            if (showSearchAndFilter) {
                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search alarms...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.List, contentDescription = "Search Icon") // Replace with Search icon if you have one, using List for now
                        }
                    )
                }

                // Filter Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == AlarmFilter.ALL,
                                onClick = { viewModel.updateSelectedFilter(AlarmFilter.ALL) },
                                label = { Text("All") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == AlarmFilter.ACTIVE,
                                onClick = { viewModel.updateSelectedFilter(AlarmFilter.ACTIVE) },
                                label = { Text("Active") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == AlarmFilter.INACTIVE,
                                onClick = { viewModel.updateSelectedFilter(AlarmFilter.INACTIVE) },
                                label = { Text("Inactive") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == AlarmFilter.PROXIMITY,
                                onClick = { viewModel.updateSelectedFilter(AlarmFilter.PROXIMITY) },
                                label = { Text("Nearest") }
                            )
                        }
                    }
                }
            }

            // List of Alarms
            items(alarms) { alarm ->
                AlarmItem(
                    alarm = alarm,
                    onToggle = { isEnabled -> viewModel.toggleAlarm(alarm, isEnabled) },
                    onEdit = { onNavigateToEditAlarm(alarm.id) },
                    onDelete = { viewModel.deleteAlarm(alarm) }
                )
            }
            if (alarms.isEmpty()) {
                item {
                    Text(
                        "No alarms created yet.",
                        modifier = Modifier.padding(vertical = 24.dp),
                        color = Color.Gray
                    )
                }
            }

            // 3. Stats Section
            item {
                val alarmsThisWeek by viewModel.alarmsThisWeek.collectAsState()
                val batteryImpact by viewModel.batteryImpact.collectAsState()

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp), // Padding for FAB
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.SsidChart,
                        label = "Alarms this week",
                        value = alarmsThisWeek.toString(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.BatteryStd,
                        label = "Battery impact",
                        value = batteryImpact,
                        color = if (batteryImpact == "High") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

