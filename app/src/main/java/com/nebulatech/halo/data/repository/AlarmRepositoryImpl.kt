package com.nebulatech.halo.data.repository

import com.nebulatech.halo.data.local.AlarmDao
import com.nebulatech.halo.data.local.AlarmEntity
import com.nebulatech.halo.data.local.AlarmHistoryEntity
import com.nebulatech.halo.domain.model.Alarm
import com.nebulatech.halo.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlarmRepositoryImpl(
    private val dao: AlarmDao,
    private val historyDao: com.nebulatech.halo.data.local.AlarmHistoryDao
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<Alarm>> {
        return dao.getAllAlarms().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveAlarms(): Flow<List<Alarm>> {
        return dao.getActiveAlarms().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAlarmById(id: Long): Alarm? {
        return dao.getAlarmById(id)?.toDomain()
    }

    override suspend fun insertAlarm(alarm: Alarm): Long {
        return dao.insertAlarm(alarm.toEntity())
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        dao.updateAlarm(alarm.toEntity())
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        dao.deleteAlarm(alarm.toEntity())
    }

    override fun getAlarmHistory(): Flow<List<com.nebulatech.halo.domain.model.AlarmHistory>> {
        return historyDao.getAllHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertAlarmHistory(history: com.nebulatech.halo.domain.model.AlarmHistory) {
        historyDao.insertHistory(history.toEntity())
    }

    override suspend fun clearAlarmHistory() {
        historyDao.clearHistory()
    }

    private fun AlarmEntity.toDomain(): Alarm {
        return Alarm(
            id = id,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            name = name,
            isEnabled = isEnabled,
            createdDate = createdDate,
            soundUri = soundUri,
            soundTitle = soundTitle,
            category = category,
            daysOfWeek = if (daysOfWeek.isBlank()) emptyList() else daysOfWeek.split(",").map { it.toInt() },
            startTimeHour = startTimeHour,
            startTimeMinute = startTimeMinute,
            endTimeHour = endTimeHour,
            endTimeMinute = endTimeMinute,
            triggerType = triggerType,
            dwellTimeMinutes = dwellTimeMinutes
        )
    }

    private fun Alarm.toEntity(): AlarmEntity {
        return AlarmEntity(
            id = id,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            name = name,
            isEnabled = isEnabled,
            createdDate = createdDate,
            soundUri = soundUri,
            soundTitle = soundTitle,
            category = category,
            daysOfWeek = daysOfWeek.joinToString(","),
            startTimeHour = startTimeHour,
            startTimeMinute = startTimeMinute,
            endTimeHour = endTimeHour,
            endTimeMinute = endTimeMinute,
            triggerType = triggerType,
            dwellTimeMinutes = dwellTimeMinutes
        )
    }

    private fun AlarmHistoryEntity.toDomain(): com.nebulatech.halo.domain.model.AlarmHistory {
        return com.nebulatech.halo.domain.model.AlarmHistory(
            id = id,
            alarmId = alarmId,
            alarmName = alarmName,
            triggerTime = triggerTime,
            latitude = latitude,
            longitude = longitude
        )
    }

    private fun com.nebulatech.halo.domain.model.AlarmHistory.toEntity(): AlarmHistoryEntity {
        return AlarmHistoryEntity(
            id = id,
            alarmId = alarmId,
            alarmName = alarmName,
            triggerTime = triggerTime,
            latitude = latitude,
            longitude = longitude
        )
    }
}
