package com.example.locationtestapp.data

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.example.locationtestapp.domain.LocationProvider
import com.example.locationtestapp.util.suspend
import com.google.android.gms.location.*
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@ServiceScoped
class DefaultLocationProvider @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val locationRequest: LocationRequest,
    private val currentLocationRequest: CurrentLocationRequest
) : LocationProvider {

    private var job: Job? = null

    private val _isLocationAvailable = MutableStateFlow(true)
    override val isLocationAvailable = _isLocationAvailable.asStateFlow()

    private val _locations = MutableSharedFlow<Location>()
    override val locations = _locations.asSharedFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationAvailability(availability: LocationAvailability) {
            _isLocationAvailable.value = availability.isLocationAvailable
        }

        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let {
                job = CoroutineScope(SupervisorJob()).launch {
                    _locations.emit(it)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation() =
        fusedLocationProviderClient.lastLocation.suspend()

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation() = fusedLocationProviderClient
        .getCurrentLocation(currentLocationRequest, null).suspend()

    @SuppressLint("MissingPermission")
    override fun startObserveLocation() {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun stopObserveLocation() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        job?.cancel()
    }
}