package com.example.locationtestapp.data.mapper

import android.location.Location
import com.example.locationtestapp.domain.model.LocationWithDate
import java.util.*

fun Location.toLocationWithDate() = LocationWithDate(
    location = com.example.locationtestapp.domain.model.Location(latitude, longitude),
    date = Calendar.getInstance().time
)