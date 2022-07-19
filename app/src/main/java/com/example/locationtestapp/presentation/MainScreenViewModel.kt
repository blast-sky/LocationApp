package com.example.locationtestapp.presentation

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtestapp.App
import com.example.locationtestapp.data.service.LocationBinder
import com.example.locationtestapp.data.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    @ApplicationContext app: Context
) : AndroidViewModel(app as App) {

    private var binderJob: Job? = null
    private var locationBinder: LocationBinder? = null

    private val serviceIntent = Intent(getApplication(), LocationService::class.java)
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            locationBinder = (binder as LocationBinder).also { locationBinder ->
                binderJob = observeService(locationBinder.locationService)
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            locationBinder = null
            binderJob?.cancel()
        }
    }

    var state by mutableStateOf(LocationListState())

    val permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    init {
        bindAndObserveService()
    }

    fun startRecordLocation() {
        locationBinder?.let { binder ->
            serviceConnection.onServiceConnected(null, binder)
        } ?: bindAndObserveService()
        locationBinder?.locationService?.startRecordLocation()
    }

    fun stopRecordLocation() {
        locationBinder?.locationService?.stopRecordLocation()
        binderJob?.cancel()
    }

    private fun bindAndObserveService() = getApplication<App>().bindService(
        serviceIntent,
        serviceConnection,
        Context.BIND_AUTO_CREATE
    )

    private fun observeService(locationService: LocationService): Job = with(locationService) {
        viewModelScope.launch {
            launch {
                savedLocations.collectLatest { locations ->
                    state = state.copy(locationPoints = locations)
                }
            }
            launch {
                isRecording.collectLatest { isRecording ->
                    state = state.copy(isRecording = isRecording)
                }
            }
            launch {
                isGpsAvailable.collectLatest { isGpsAvailability ->
                    state = state.copy(isGpsAvailability = isGpsAvailability)
                }
            }
        }
    }
}