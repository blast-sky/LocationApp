package com.example.locationtestapp.data.service

import android.os.Binder

class LocationBinder(val locationService: LocationService) : Binder()