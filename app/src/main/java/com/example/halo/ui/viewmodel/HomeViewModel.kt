package com.example.halo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halo.domain.GeofenceManager
import com.example.halo.domain.model.Alarm
import com.example.halo.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val _currentAddress = kotlinx.coroutines.flow.MutableStateFlow("Locating...")
    val currentAddress: StateFlow<String> = _currentAddress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Locating..."
    )

    val alarms: StateFlow<List<Alarm>> = repository.getAllAlarms()
        .stateIn(
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
            activeCount < 3 -> "Low"
            activeCount < 6 -> "Medium"
            else -> "High"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Low"
    )

    init {
        getCurrentLocation()
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

    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = com.google.android.gms.maps.model.LatLng(location.latitude, location.longitude)
                    _currentLocation.value = latLng
                    reverseGeocode(latLng)
                } else {
                    _currentAddress.value = "Location not found"
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            _currentAddress.value = "Permission denied"
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
                            _currentAddress.value = address.getAddressLine(0) ?: "Unknown Address"
                        } else {
                            _currentAddress.value = "Unknown Address"
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                         _currentAddress.value = addresses[0].getAddressLine(0) ?: "Unknown Address"
                    } else {
                        _currentAddress.value = "Unknown Address"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _currentAddress.value = "Error fetching address"
            }
        }
    }
}
