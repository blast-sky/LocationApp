package com.example.locationtestapp.di

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.locationtestapp.MainActivity
import com.example.locationtestapp.R
import com.example.locationtestapp.data.DefaultLocationProvider
import com.example.locationtestapp.data.service.LocationService
import com.example.locationtestapp.domain.location_provider.LocationProvider
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

    @ServiceScoped
    @Provides
    fun provideFusedClient(@ApplicationContext app: Context) =
        LocationServices.getFusedLocationProviderClient(app)

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
    ) = NotificationCompat.Builder(
        app,
        LocationService.CHANNEL_ID
    )
        .setContentTitle(app.resources.getString(R.string.app_name))
        .setContentText(app.resources.getString(R.string.starting))
        .setOngoing(true)
        .setContentIntent(myActivityPendingIntent)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setAutoCancel(false)
        .setCategory(Notification.CATEGORY_SERVICE)
}