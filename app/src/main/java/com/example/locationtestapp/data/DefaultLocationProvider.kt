package com.example.locationtestapp.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.example.locationtestapp.domain.LocationProvider
import com.example.locationtestapp.util.suspend
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultLocationProvider(context: Context) : LocationProvider {

    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private val _isLocationAvailable = MutableStateFlow(false)
    val isLocationAvailable = _isLocationAvailable.asStateFlow()

    private val _locations = MutableSharedFlow<Location>()
    override val locations = _locations.asSharedFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationAvailability(availability: LocationAvailability) {
            _isLocationAvailable.value = availability.isLocationAvailable
        }

        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let {
                scope.launch { _locations.emit(it) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation() =
        fusedLocationProviderClient.lastLocation.suspend()

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation() = fusedLocationProviderClient
        .getCurrentLocation(
            CurrentLocationRequest.Builder().apply {
                setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            }.build(),
            null
        ).suspend()

    @SuppressLint("MissingPermission")
    override fun startObserveLocation() {
        val request = LocationRequest.create().apply {
            maxWaitTime = 2000
            fastestInterval = 500
            interval = 1000
        }

        fusedLocationProviderClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun stopObserveLocation() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}