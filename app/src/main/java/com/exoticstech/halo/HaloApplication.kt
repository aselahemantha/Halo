package com.exoticstech.halo

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HaloApplication :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(workerFactory)
                .build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.MAPS_API_KEY.isNotEmpty()) {
            com.google.android.libraries.places.api.Places
                .initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
    }
}
