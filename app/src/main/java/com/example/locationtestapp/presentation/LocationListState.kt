package com.example.locationtestapp.presentation

import com.example.locationtestapp.domain.model.LocationWithDate

data class LocationListState(
    val locationPoints: List<LocationWithDate> = emptyList(),
    val isRecording: Boolean = false,
    val isGpsAvailability: Boolean = true,
)