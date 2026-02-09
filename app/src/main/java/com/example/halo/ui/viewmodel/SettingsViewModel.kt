package com.example.halo.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.halo.data.repository.UserPreferencesRepository
import com.example.halo.data.repository.AppTheme

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = userPreferencesRepository.appTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppTheme.SYSTEM
        )

    val alarmSound: StateFlow<Pair<String, String>> = userPreferencesRepository.alarmSound
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "" to "Default"
        )

    private val _availableRingtones = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val availableRingtones: StateFlow<List<Pair<String, String>>> = _availableRingtones.asStateFlow()

    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted.asStateFlow()

    private val _notificationPermissionGranted = MutableStateFlow(false)
    val notificationPermissionGranted: StateFlow<Boolean> = _notificationPermissionGranted.asStateFlow()
    
    private val _backgroundPermissionGranted = MutableStateFlow(false)
    val backgroundPermissionGranted: StateFlow<Boolean> = _backgroundPermissionGranted.asStateFlow()
    
    // Settings State
    private val _backgroundLocationEnabled = MutableStateFlow(true)
    val backgroundLocationEnabled: StateFlow<Boolean> = _backgroundLocationEnabled.asStateFlow()

    private val _defaultRadius = MutableStateFlow(500f)
    val defaultRadius: StateFlow<Float> = _defaultRadius.asStateFlow()

    private val _batteryOptimizationIgnored = MutableStateFlow(false)
    val batteryOptimizationIgnored: StateFlow<Boolean> = _batteryOptimizationIgnored.asStateFlow()

    fun checkPermissions() {
        val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        _locationPermissionGranted.value = fineLocation || coarseLocation

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            _backgroundPermissionGranted.value = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            _backgroundPermissionGranted.value = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            _notificationPermissionGranted.value = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            _notificationPermissionGranted.value = true
        }
        
        checkBatteryOptimization()
    }

    private fun checkBatteryOptimization() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        _batteryOptimizationIgnored.value = powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun toggleBackgroundLocation(enabled: Boolean) {
        _backgroundLocationEnabled.value = enabled
        // In real app, might need to disable/enable background processing
    }

    fun updateDefaultRadius(radius: Float) {
        _defaultRadius.value = radius
    }

    fun toggleBatteryOptimization(ignored: Boolean) {
        // The actual state change happens when the user accepts the system dialog.
        // We just verify the state.
        checkBatteryOptimization()
    }

    fun setAlarmSound(uri: String, title: String) {
        viewModelScope.launch {
            userPreferencesRepository.setAlarmSound(uri, title)
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.setTheme(theme)
        }
    }

    fun fetchRingtones() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val ringtoneManager = android.media.RingtoneManager(context)
            ringtoneManager.setType(android.media.RingtoneManager.TYPE_ALARM)
            val cursor = ringtoneManager.cursor
            val list = mutableListOf<Pair<String, String>>()
            
            // Add Default and Silent options manually if desired, or rely on system
            list.add("" to "Silent")
            
            while (cursor.moveToNext()) {
                val title = cursor.getString(android.media.RingtoneManager.TITLE_COLUMN_INDEX)
                val uriPrefix = cursor.getString(android.media.RingtoneManager.URI_COLUMN_INDEX)
                val id = cursor.getString(android.media.RingtoneManager.ID_COLUMN_INDEX)
                val uri = "$uriPrefix/$id"
                list.add(uri to title)
            }
            // cursor is managed by RingtoneManager? No, we should probably close it if we obtained it directly, 
            // but RingtoneManager.getCursor() caches it. 
            // Actually, safer to just iterate and let manager handle it or just use it.
            // But for a simple list, we can extract.
            
            _availableRingtones.value = list
        }
    }
}
