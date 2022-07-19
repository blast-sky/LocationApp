package com.example.locationtestapp.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.locationtestapp.R
import com.example.locationtestapp.data.LocationFlowResult
import com.example.locationtestapp.data.mapper.toDomainLocation
import com.example.locationtestapp.data.mapper.toLocationWithDate
import com.example.locationtestapp.domain.LocationProvider
import com.example.locationtestapp.domain.model.Location
import com.example.locationtestapp.domain.model.LocationWithDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var baseNotification: NotificationCompat.Builder

    @Inject
    lateinit var locationProvider: LocationProvider

    private var job: Job? = null
    private val locationBinder = LocationBinder(this)

    val savedLocations = MutableStateFlow(mutableListOf<LocationWithDate>())
    val isGpsAvailable = MutableStateFlow(true)
    val isRecording = MutableStateFlow(false)

    fun startRecordLocation() {
        val notification = baseNotification
        val notificationManager = NotificationManagerCompat.from(this)
        val channel =
            NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL_ID, NOTIFICATION_IMPORTANCE)
                .setName(NOTIFICATION_CHANNEL_NAME)
                .build()

        notificationManager.createNotificationChannel(channel)
        startForeground(NOTIFICATION_ID, notification.build())
        notificationManager.notify(NOTIFICATION_ID, notification.build())

        isRecording.value = true
        job = observeLocationProvider(notificationManager)
    }

    private fun observeLocationProvider(notificationManager: NotificationManagerCompat) =
        CoroutineScope(Job()).launch {
            locationProvider.locationFlow.collect { flowResult ->
                when (flowResult) {
                    is LocationFlowResult.Result -> flowResult.location?.let { notNullLocation ->
                        val locationWithDate = notNullLocation.toLocationWithDate()
                        savedLocations.value =
                            (savedLocations.value + locationWithDate).toMutableList()
                    }.also {
                        notifyWithLastLocation(
                            flowResult.location?.toDomainLocation(),
                            notificationManager,
                            baseNotification
                        )
                    }
                    is LocationFlowResult.Availability ->
                        isGpsAvailable.value = flowResult.availability
                }
            }
        }

    private fun notifyWithLastLocation(
        curLocation: Location?,
        notificationManager: NotificationManagerCompat,
        baseNotification: NotificationCompat.Builder
    ) {
        val notification = baseNotification
        notification.setContentTitle(resources.getText(R.string.current_location))
        notification.setContentText(
            curLocation?.toString() ?: getString(R.string.location_cant_determined)
        )
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    fun stopRecordLocation() {
        isRecording.value = false
        job?.cancel()
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder = locationBinder

    override fun onDestroy() {
        isRecording.value = false
        job?.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "CHANNEL_ID_LOCATION_SERVICE"
        const val NOTIFICATION_ID = 101

        const val NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_ID_LOCATION_SERVICE"
        const val NOTIFICATION_CHANNEL_NAME = "NOTIFICATION_CHANNEL_NAME_LOCATION_SERVICE"
        const val NOTIFICATION_IMPORTANCE = NotificationManagerCompat.IMPORTANCE_DEFAULT
    }
}