package com.example.locationtestapp.di

import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideLocationRequest() = LocationRequest.create().apply {
        maxWaitTime = 1000
        fastestInterval = 1000
        interval = 1000
        priority = PRIORITY
    }

    @Singleton
    @Provides
    fun provideCurrentLocationRequest() = CurrentLocationRequest.Builder().apply {
        setPriority(PRIORITY)
    }.build()

    companion object {
        const val PRIORITY = Priority.PRIORITY_HIGH_ACCURACY
    }
}