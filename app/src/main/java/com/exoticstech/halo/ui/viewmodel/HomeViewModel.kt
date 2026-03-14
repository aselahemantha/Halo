package com.exoticstech.halo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoticstech.halo.domain.GeofenceManager
import com.exoticstech.halo.domain.model.Alarm
import com.exoticstech.halo.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.exoticstech.halo.R

enum class AlarmFilter {
    ALL, ACTIVE, INACTIVE, PROXIMITY
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val geofenceManager: GeofenceManager,
    private val fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _currentLocation = kotlinx.coroutines.flow.MutableStateFlow<com.google.android.gms.maps.model.LatLng?>(null)
    val currentLocation: StateFlow<com.google.android.gms.maps.model.LatLng?> = _currentLocation.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _currentAddress = kotlinx.coroutines.flow.MutableStateFlow(context.getString(R.string.locating_address))
    val currentAddress: StateFlow<String> = _currentAddress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = context.getString(R.string.locating_address)
    )

    private val _searchQuery = kotlinx.coroutines.flow.MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    private val _selectedFilter = kotlinx.coroutines.flow.MutableStateFlow(AlarmFilter.ALL)
    val selectedFilter: StateFlow<AlarmFilter> = _selectedFilter.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlarmFilter.ALL
    )

    val alarms: StateFlow<List<Alarm>> = combine(
        repository.getAllAlarms(),
        _searchQuery,
        _selectedFilter,
        _currentLocation
    ) { allAlarms, query, filter, location ->
        var filteredList = allAlarms

        // 1. Search filter
        if (query.isNotBlank()) {
            filteredList = filteredList.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        // 2. Type filter
        filteredList = when (filter) {
            AlarmFilter.ACTIVE -> filteredList.filter { it.isEnabled }
            AlarmFilter.INACTIVE -> filteredList.filter { !it.isEnabled }
            else -> filteredList
        }

        // 3. Proximity Sort
        if (filter == AlarmFilter.PROXIMITY && location != null) {
            filteredList = filteredList.sortedBy { alarm ->
                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    location.latitude, location.longitude,
                    alarm.latitude, alarm.longitude,
                    results
                )
                results[0]
            }
        } else {
            // Default sort by newest if no proximity is requested
            filteredList = filteredList.sortedByDescending { it.createdDate }
        }

        filteredList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val alarmsThisWeek: StateFlow<Int> = alarms.map { alarmList ->
        val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        alarmList.count { it.createdDate > oneWeekAgo }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val batteryImpact: StateFlow<String> = alarms.map { alarmList ->
        val activeCount = alarmList.count { it.isEnabled }
        when {
            activeCount < 3 -> context.getString(R.string.battery_impact_low)
            activeCount < 6 -> context.getString(R.string.battery_impact_medium)
            else -> context.getString(R.string.battery_impact_high)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = context.getString(R.string.battery_impact_low)
    )

    init {
        refreshLocation()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedFilter(filter: AlarmFilter) {
        _selectedFilter.value = filter
    }

    fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        viewModelScope.launch {
            val updatedAlarm = alarm.copy(isEnabled = isEnabled)
            repository.updateAlarm(updatedAlarm)

            if (isEnabled) {
                geofenceManager.addGeofence(updatedAlarm)
            } else {
                geofenceManager.removeGeofence(updatedAlarm.id)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
            geofenceManager.removeGeofence(alarm.id)
        }
    }

    fun refreshLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val latLng = com.google.android.gms.maps.model.LatLng(location.latitude, location.longitude)
                        _currentLocation.value = latLng
                        reverseGeocode(latLng)
                    } else {
                        // If last location is null, try to get a one-time fresh location
                        val hasFineLocation = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        
                        val hasCoarseLocation = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        val priority = when {
                            hasFineLocation -> com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
                            hasCoarseLocation -> com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY
                            else -> null
                        }

                        if (priority != null) {
                            fusedLocationClient.getCurrentLocation(priority, null)
                                .addOnSuccessListener { freshLocation ->
                                    if (freshLocation != null) {
                                        val latLng = com.google.android.gms.maps.model.LatLng(freshLocation.latitude, freshLocation.longitude)
                                        _currentLocation.value = latLng
                                        reverseGeocode(latLng)
                                    } else {
                                        _currentAddress.value = context.getString(R.string.location_not_found)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    e.printStackTrace()
                                    _currentAddress.value = context.getString(R.string.location_not_found)
                                }
                        } else {
                            _currentAddress.value = context.getString(R.string.permission_denied)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    _currentAddress.value = context.getString(R.string.error_fetching_address)
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
            _currentAddress.value = context.getString(R.string.permission_denied)
        }
    }

    private fun reverseGeocode(latLng: com.google.android.gms.maps.model.LatLng) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            _currentAddress.value = address.getAddressLine(0) ?: context.getString(R.string.unknown_address)
                        } else {
                            _currentAddress.value = context.getString(R.string.unknown_address)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                         _currentAddress.value = addresses[0].getAddressLine(0) ?: context.getString(R.string.unknown_address)
                    } else {
                        _currentAddress.value = context.getString(R.string.unknown_address)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _currentAddress.value = context.getString(R.string.error_fetching_address)
            }
        }
    }
}
