package com.example.locationtestapp.domain

import android.location.Location
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow


interface LocationProvider {

    val locations: SharedFlow<Location?>

    val isLocationAvailable: StateFlow<Boolean>

    fun startObserveLocation()

    fun stopObserveLocation()

    suspend fun getLastLocation(): Location?

    suspend fun getCurrentLocation(): Location?
}