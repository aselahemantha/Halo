package com.nebulatech.halo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nebulatech.halo.domain.model.AlarmHistory
import com.nebulatech.halo.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmHistoryViewModel @Inject constructor(
    private val repository: AlarmRepository
) : ViewModel() {

    val historyEntries: StateFlow<List<AlarmHistory>> = repository.getAlarmHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAlarmHistory()
        }
    }
}
