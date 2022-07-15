package com.example.locationtestapp.di

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.locationtestapp.MainActivity
import com.example.locationtestapp.R
import com.example.locationtestapp.data.DefaultLocationProvider
import com.example.locationtestapp.data.service.LocationService
import com.example.locationtestapp.domain.LocationProvider
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
class ServiceModule {

    @Provides
    fun provideFusedClient(@ApplicationContext app: Context) =
        LocationServices.getFusedLocationProviderClient(app)

    @ServiceScoped
    @Provides
    fun provideLocationProvider(defaultLocationProvider: DefaultLocationProvider): LocationProvider =
        defaultLocationProvider

    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(@ApplicationContext app: Context): PendingIntent =
        Intent(app, MainActivity::class.java).let { intent ->
            PendingIntent.getActivity(app, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext app: Context,
        myActivityPendingIntent: PendingIntent
    ) = Notification.Builder(
        app,
        LocationService.CHANNEL_ID
    )
        .setContentTitle(app.packageName)
        .setContentText("Content text")
        .setOngoing(true)
        .setContentIntent(myActivityPendingIntent)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setAutoCancel(false)
        .setCategory(Notification.CATEGORY_SERVICE)
}