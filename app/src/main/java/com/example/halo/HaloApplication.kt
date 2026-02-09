package com.example.halo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HaloApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.HAS_MAPS_API_KEY) {
            com.google.android.libraries.places.api.Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
    }
}
