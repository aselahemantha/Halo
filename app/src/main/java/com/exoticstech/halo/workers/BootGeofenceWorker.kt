package com.exoticstech.halo.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.exoticstech.halo.domain.repository.AlarmRepository
import com.exoticstech.halo.domain.GeofenceManager
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class BootGeofenceWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: AlarmRepository,
    private val geofenceManager: GeofenceManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val alarms = repository.getActiveAlarms().firstOrNull() ?: emptyList()
            for (alarm in alarms) {
                geofenceManager.addGeofence(alarm)
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // If it fails for something temporary retrying makes sense, but typically boot operations either work or don't.
            Result.retry()
        }
    }
}
