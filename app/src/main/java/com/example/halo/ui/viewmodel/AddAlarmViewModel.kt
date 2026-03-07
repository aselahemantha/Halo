package com.example.halo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halo.domain.GeofenceManager
import com.example.halo.domain.model.Alarm
import com.example.halo.domain.repository.AlarmRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAlarmViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val geofenceManager: GeofenceManager,
    private val userPreferencesRepository: com.example.halo.data.repository.UserPreferencesRepository,
    private val fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    private val placesClient: com.google.android.libraries.places.api.net.PlacesClient,
    savedStateHandle: androidx.lifecycle.SavedStateHandle,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation.asStateFlow()

    private val _radius = MutableStateFlow(800.0) // Default 800m
    val radius: StateFlow<Double> = _radius.asStateFlow()

    private val _locationName = MutableStateFlow("")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private val _editingAlarmId = MutableStateFlow<Long?>(null)
    val editingAlarmId: StateFlow<Long?> = _editingAlarmId.asStateFlow()

    private val _category = MutableStateFlow("General")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Default to "Default" until loaded
    private val _alertSound = MutableStateFlow("Default")
    val alertSound: StateFlow<String> = _alertSound.asStateFlow()

    private val _alertSoundUri = MutableStateFlow<String?>(null)
    val alertSoundUri: StateFlow<String?> = _alertSoundUri.asStateFlow()
    
    private val _searchSuggestions = MutableStateFlow<List<com.google.android.libraries.places.api.model.AutocompletePrediction>>(emptyList())
    val searchSuggestions: StateFlow<List<com.google.android.libraries.places.api.model.AutocompletePrediction>> = _searchSuggestions.asStateFlow()

    private val _availableRingtones = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val availableRingtones: StateFlow<List<Pair<String, String>>> = _availableRingtones.asStateFlow()

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
            
            _availableRingtones.value = list
        }
    }

    init {
        val alarmId = savedStateHandle.get<Long>("alarmId")
        if (alarmId != null && alarmId != -1L) {
            _editingAlarmId.value = alarmId
            loadAlarmData(alarmId)
        } else {
            viewModelScope.launch {
                userPreferencesRepository.alarmSound.collect { (uri, title) ->
                    if (_alertSoundUri.value == null) { // Only set if not already set (e.g. rotation)
                        _alertSound.value = title
                        _alertSoundUri.value = uri
                    }
                }
            }
        }
    }

    private fun loadAlarmData(alarmId: Long) {
        viewModelScope.launch {
            val alarm = repository.getAlarmById(alarmId)
            if (alarm != null) {
                _selectedLocation.value = LatLng(alarm.latitude, alarm.longitude)
                _radius.value = alarm.radius
                _locationName.value = alarm.name
                _alertSound.value = alarm.soundTitle ?: "Default"
                _alertSoundUri.value = alarm.soundUri
                _category.value = alarm.category
            }
        }
    }

    fun updateCategory(newCategory: String) {
        _category.value = newCategory
    }

    fun updateLocation(latLng: LatLng) {
        _selectedLocation.value = latLng
        _searchSuggestions.value = emptyList() // Clear suggestions on selection
    }

    fun updateRadius(newRadius: Double) {
        _radius.value = newRadius.coerceIn(100.0, 5000.0)
    }

    fun updateName(newName: String) {
        _locationName.value = newName
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        fetchPredictions(query)
    }
    
    private fun fetchPredictions(query: String) {
        if (query.isBlank()) {
            _searchSuggestions.value = emptyList()
            return
        }
        
        val token = com.google.android.libraries.places.api.model.AutocompleteSessionToken.newInstance()
        val request = com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                _searchSuggestions.value = response.autocompletePredictions
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                _searchSuggestions.value = emptyList()
            }
    }
    
    fun onSuggestionSelected(placeId: String, primaryText: String) {
         val placeFields = listOf(com.google.android.libraries.places.api.model.Place.Field.LAT_LNG, com.google.android.libraries.places.api.model.Place.Field.NAME)
         val request = com.google.android.libraries.places.api.net.FetchPlaceRequest.newInstance(placeId, placeFields)
         
         placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                place.latLng?.let { latLng ->
                    updateLocation(latLng)
                    // If manually typing, we might want to update the name field too
                    if (_locationName.value.isBlank()) {
                        _locationName.value = place.name ?: primaryText
                    }
                    _searchQuery.value = place.name ?: primaryText
                    _searchSuggestions.value = emptyList()
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }
    
    fun performSearch(query: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            if (query.isBlank()) return@launch
            try {
                val geocoder = android.location.Geocoder(context)
                val addresses = geocoder.getFromLocationName(query, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val latLng = LatLng(address.latitude, address.longitude)
                    _selectedLocation.value = latLng
                    
                    // Update name if empty
                    if (_locationName.value.isBlank()) {
                        _locationName.value = address.featureName ?: address.locality ?: query
                    }
                    _searchSuggestions.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    _selectedLocation.value = LatLng(location.latitude, location.longitude)
                    _searchSuggestions.value = emptyList()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    fun updateAlertSound(uri: String, title: String) {
        _alertSound.value = title
        _alertSoundUri.value = uri
    }

    fun saveAlarm(onSuccess: () -> Unit) {
        val location = _selectedLocation.value ?: return
        val name = _locationName.value.ifBlank { "Location Alarm" }
        
        viewModelScope.launch {
            val isEdit = _editingAlarmId.value != null
            val alarmId = _editingAlarmId.value ?: 0L
            val alarm = Alarm(
                id = alarmId,
                latitude = location.latitude,
                longitude = location.longitude,
                radius = _radius.value,
                name = name,
                isEnabled = true,
                soundUri = _alertSoundUri.value,
                soundTitle = _alertSound.value,
                category = _category.value
            )
            
            if (isEdit) {
                repository.updateAlarm(alarm)
                geofenceManager.addGeofence(alarm)
            } else {
                val newId = repository.insertAlarm(alarm)
                geofenceManager.addGeofence(alarm.copy(id = newId))
            }

            onSuccess()
        }
    }
}
