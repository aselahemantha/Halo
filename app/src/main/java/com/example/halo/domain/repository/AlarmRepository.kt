package com.example.halo.domain.repository

import com.example.halo.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAllAlarms(): Flow<List<Alarm>>
    fun getActiveAlarms(): Flow<List<Alarm>>
    suspend fun getAlarmById(id: Long): Alarm?
    suspend fun insertAlarm(alarm: Alarm): Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
    
    fun getAlarmHistory(): Flow<List<com.example.halo.domain.model.AlarmHistory>>
    suspend fun insertAlarmHistory(history: com.example.halo.domain.model.AlarmHistory)
    suspend fun clearAlarmHistory()
}
