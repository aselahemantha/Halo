package com.exoticstech.halo.domain.repository

import com.exoticstech.halo.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAllAlarms(): Flow<List<Alarm>>
    fun getActiveAlarms(): Flow<List<Alarm>>
    suspend fun getAlarmById(id: Long): Alarm?
    suspend fun insertAlarm(alarm: Alarm): Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
    
    fun getAlarmHistory(): Flow<List<com.exoticstech.halo.domain.model.AlarmHistory>>
    suspend fun insertAlarmHistory(history: com.exoticstech.halo.domain.model.AlarmHistory)
    suspend fun clearAlarmHistory()
}
