package com.exoticstech.halo.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoticstech.halo.domain.model.Alarm
import com.exoticstech.halo.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmTriggerViewModel @Inject constructor(
    private val repository: AlarmRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _alarm = MutableStateFlow<Alarm?>(null)
    val alarm: StateFlow<Alarm?> = _alarm.asStateFlow()

    fun loadAlarm(alarmId: String) {
        viewModelScope.launch {
            val id = alarmId.toLongOrNull() ?: return@launch
            _alarm.value = repository.getAlarmById(id)
        }
    }
}
