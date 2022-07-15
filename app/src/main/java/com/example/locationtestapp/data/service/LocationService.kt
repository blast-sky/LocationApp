package com.example.locationtestapp.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.locationtestapp.data.mapper.toLocationWithDate
import com.example.locationtestapp.domain.LocationProvider
import com.example.locationtestapp.domain.model.LocationWithDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject


@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var baseNotification: Notification.Builder

    @Inject
    lateinit var locationProvider: LocationProvider

    private var job: Job? = null
    private val locationBinder = LocationBinder(this)

    val savedLocations = MutableStateFlow(mutableListOf<LocationWithDate>())
    val isRecording = MutableStateFlow(false)

    fun startRecordLocation() {
        val notification = baseNotification

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, notification.build())
        notificationManager.notify(NOTIFICATION_ID, notification.build())

        isRecording.value = true
        locationProvider.startObserveLocation()
        job = CoroutineScope(SupervisorJob()).launch {
            locationProvider.locations.collect { location ->
                location?.let { curLocation ->
                    savedLocations.value =
                        (savedLocations.value + mutableListOf(curLocation.toLocationWithDate())).toMutableList()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    fun stopRecordLocation() {
        isRecording.value = false
        locationProvider.stopObserveLocation()
        job?.cancel()
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d("Location Service", "Bind")
        return locationBinder
    }

    override fun onDestroy() {
        Log.d("Location Service", "Destroy")
        isRecording.value = false
        job?.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "CHANNEL_ID_LOCATION_SERVICE"
        const val NOTIFICATION_ID = 101

        const val NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_ID_LOCATION_SERVICE"
        const val NOTIFICATION_CHANNEL_NAME = "NOTIFICATION_CHANNEL_NAME_LOCATION_SERVICE"
    }
}