package com.example.halo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val radius: Double, // in meters
    val name: String,
    val isEnabled: Boolean,
    val createdDate: Long = System.currentTimeMillis(),
    val soundUri: String? = null,
    val soundTitle: String? = null,
    val category: String = "General",
    val daysOfWeek: String = "", // Comma-separated list of integers
    val startTimeHour: Int? = null,
    val startTimeMinute: Int? = null,
    val endTimeHour: Int? = null,
    val endTimeMinute: Int? = null,
    val triggerType: String = "ENTER",
    val dwellTimeMinutes: Int? = null
)
