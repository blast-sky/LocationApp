package com.example.locationtestapp.presentation

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtestapp.data.service.LocationBinder
import com.example.locationtestapp.data.service.LocationService
import com.example.locationtestapp.domain.model.LocationWithDate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    @ApplicationContext app: Context
) : AndroidViewModel(app as Application) {

    private var binderJob: Job? = null
    private var locationBinder: LocationBinder? = null

    private val serviceIntent = Intent(getApplication(), LocationService::class.java)
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            locationBinder = (binder as LocationBinder).apply {
                binderJob = viewModelScope.launch {
                    launch {
                        locationService.savedLocations.collect {
                            locationPoints.value = it
                        }
                    }
                    launch {
                        locationService.isRecording.collect {
                            isRecording.value = it
                        }
                    }
                }
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            locationBinder = null
            binderJob?.cancel()
        }
    }

    val locationPoints = MutableStateFlow(mutableListOf<LocationWithDate>())
    val isRecording = MutableStateFlow(false)

    val permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    init {
        getApplication<Application>().bindService(
            serviceIntent,
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun startRecordLocation() {
        locationBinder?.let {
            serviceConnection.onServiceConnected(null, it)
        } ?: getApplication<Application>().bindService(
            serviceIntent,
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        locationBinder?.locationService?.startRecordLocation()
    }

    fun stopRecordLocation() {
        locationBinder?.locationService?.stopRecordLocation()
        binderJob?.cancel()
    }
}