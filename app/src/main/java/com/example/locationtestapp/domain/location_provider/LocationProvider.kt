package com.example.locationtestapp.domain.location_provider

import android.location.Location
import kotlinx.coroutines.flow.Flow


interface LocationProvider {

    val locationFlow: Flow<LocationFlowResult>

    suspend fun getLastLocation(): Location?

    suspend fun getCurrentLocation(): Location?
}