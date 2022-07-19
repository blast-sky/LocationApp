package com.example.locationtestapp.data

import android.location.Location


sealed class LocationFlowResult {
    class Result(val location: Location?) : LocationFlowResult()
    class Availability(val availability: Boolean) : LocationFlowResult()
}
