package com.example.locationtestapp.data

import android.annotation.SuppressLint
import android.os.Looper
import com.example.locationtestapp.domain.LocationProvider
import com.example.locationtestapp.util.suspend
import com.google.android.gms.location.*
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@ServiceScoped
class DefaultLocationProvider @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val locationRequest: LocationRequest,
    private val currentLocationRequest: CurrentLocationRequest
) : LocationProvider {

    @SuppressLint("MissingPermission")
    override val locationFlow = channelFlow {
        val locationCallback = createLocationCallback(
            onAvailability = { launch { send(LocationFlowResult.Availability(it.isLocationAvailable)) } },
            onLocationResult = { launch { send(LocationFlowResult.Result(it.lastLocation)) } }
        )

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private inline fun createLocationCallback(
        crossinline onAvailability: (LocationAvailability) -> Unit,
        crossinline onLocationResult: (LocationResult) -> Unit
    ) = object : LocationCallback() {
        override fun onLocationAvailability(availability: LocationAvailability) =
            onAvailability.invoke(availability)

        override fun onLocationResult(locationResult: LocationResult) =
            onLocationResult.invoke(locationResult)
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation() =
        fusedLocationProviderClient.lastLocation.suspend()

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation() = fusedLocationProviderClient
        .getCurrentLocation(currentLocationRequest, null).suspend()
}