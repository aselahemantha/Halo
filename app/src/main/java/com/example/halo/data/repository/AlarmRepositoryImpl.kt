package com.example.halo.data.repository

import com.example.halo.data.local.AlarmDao
import com.example.halo.data.local.AlarmEntity
import com.example.halo.domain.model.Alarm
import com.example.halo.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlarmRepositoryImpl(
    private val dao: AlarmDao
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
            soundTitle = soundTitle
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
            soundTitle = soundTitle
        )
    }
}
