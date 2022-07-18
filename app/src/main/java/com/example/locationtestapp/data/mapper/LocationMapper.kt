package com.example.locationtestapp.data.mapper

import android.location.Location
import com.example.locationtestapp.domain.model.LocationWithDate
import java.util.*

fun Location.toLocationWithDate() = LocationWithDate(
    location = toDomainLocation(),
    date = Calendar.getInstance().time
)

fun Location.toDomainLocation() = com.example.locationtestapp.domain.model.Location(
    latitude = latitude,
    longitude = longitude
)