package com.exoticstech.halo.domain.model

data class AlarmHistory(
    val id: Long = 0,
    val alarmId: Long,
    val alarmName: String,
    val triggerTime: Long,
    val latitude: Double,
    val longitude: Double
)
