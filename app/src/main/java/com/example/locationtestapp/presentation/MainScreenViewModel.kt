package com.example.locationtestapp.presentation

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtestapp.data.DefaultLocationProvider
import com.example.locationtestapp.domain.LocationProvider
import kotlinx.coroutines.launch

class MainScreenViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val manager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val locationProvider: LocationProvider = DefaultLocationProvider(application)

    val locations = mutableStateListOf<Location>()

    init {
        viewModelScope.launch {
            locationProvider.locations.collect {
                it?.let { locations.add(it) }
            }
        }
    }

    val isGpsProviderEnabled get() = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    fun startGetLocation() = locationProvider.startObserveLocation()


    fun stopGetLocation() = locationProvider.stopObserveLocation()

}