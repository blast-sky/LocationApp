package com.example.locationtestapp.domain

import android.location.Location
import com.example.locationtestapp.data.LocationFlowResult
import kotlinx.coroutines.flow.Flow


interface LocationProvider {

    val locationFlow: Flow<LocationFlowResult>

    suspend fun getLastLocation(): Location?

    suspend fun getCurrentLocation(): Location?
}