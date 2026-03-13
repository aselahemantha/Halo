package com.exoticstech.halo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmHistoryDao {
    @Query("SELECT * FROM alarm_history ORDER BY triggerTime DESC")
    fun getAllHistory(): Flow<List<AlarmHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: AlarmHistoryEntity): Long

    @Query("DELETE FROM alarm_history")
    suspend fun clearHistory()
}
