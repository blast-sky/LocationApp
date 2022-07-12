package com.example.locationtestapp.domain

import android.location.Location
import kotlinx.coroutines.flow.SharedFlow


interface LocationProvider {

    val locations: SharedFlow<Location?>

    fun startObserveLocation()

    fun stopObserveLocation()

    suspend fun getLastLocation(): Location?

    suspend fun getCurrentLocation(): Location?
}