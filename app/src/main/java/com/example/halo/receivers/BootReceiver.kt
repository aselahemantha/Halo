package com.example.halo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.halo.workers.BootGeofenceWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            val workRequest = OneTimeWorkRequestBuilder<BootGeofenceWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
