package com.example.locationtestapp.domain.location_provider

import com.example.locationtestapp.domain.model.LocationWithDate


sealed class LocationFlowResult {
    class Result(val location: LocationWithDate?) : LocationFlowResult()
    class Availability(val availability: Boolean) : LocationFlowResult()
}
