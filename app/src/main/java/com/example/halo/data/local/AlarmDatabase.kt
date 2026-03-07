package com.example.halo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AlarmEntity::class, AlarmHistoryEntity::class], version = 7, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun alarmHistoryDao(): AlarmHistoryDao
}
