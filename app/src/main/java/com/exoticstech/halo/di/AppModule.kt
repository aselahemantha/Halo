package com.exoticstech.halo.di

import android.app.Application
import androidx.room.Room
import com.exoticstech.halo.data.local.AlarmDao
import com.exoticstech.halo.data.local.AlarmDatabase
import com.exoticstech.halo.data.repository.AlarmRepositoryImpl
import com.exoticstech.halo.domain.repository.AlarmRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAlarmDatabase(app: Application): AlarmDatabase {
        return Room.databaseBuilder(
            app,
            AlarmDatabase::class.java,
            "alarm_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideAlarmDao(db: AlarmDatabase): AlarmDao {
        return db.alarmDao()
    }

    @Provides
    @Singleton
    fun provideAlarmHistoryDao(db: AlarmDatabase): com.exoticstech.halo.data.local.AlarmHistoryDao {
        return db.alarmHistoryDao()
    }

    @Provides
    @Singleton
    fun provideAlarmRepository(dao: AlarmDao, historyDao: com.exoticstech.halo.data.local.AlarmHistoryDao): AlarmRepository {
        return AlarmRepositoryImpl(dao, historyDao)
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(app: Application): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }

    @Provides
    @Singleton
    fun provideGeofencingClient(app: Application): GeofencingClient {
        return LocationServices.getGeofencingClient(app)
    }

    @Provides
    @Singleton
    fun providePlacesClient(app: Application): com.google.android.libraries.places.api.net.PlacesClient {
        return com.google.android.libraries.places.api.Places.createClient(app)
    }
}
