package com.example.locationtestapp.presentation


import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.text.format.DateFormat.format
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationtestapp.R
import com.example.locationtestapp.domain.model.LocationWithDate
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = viewModel.permissions
    )

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            modifier = Modifier
                .animateContentSize()
                .padding(6.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    multiplePermissionsState.allPermissionsGranted -> LocationList(
                        state = viewModel.state,
                        startRecordingLocation = viewModel::startRecordLocation,
                        stopRecordingLocation = viewModel::stopRecordLocation,
                    )
                    multiplePermissionsState.shouldShowRationale -> Rationale(
                        multiplePermissionsState
                    )
                    else -> RequirePermissionFromSettings(context)
                }
            }
        }
    }
}

@Composable
private fun RequirePermissionFromSettings(context: Context) {
    Text(text = stringResource(R.string.cant_work_without_permission))
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        context.startActivity(intent)
    }) {
        Text(text = stringResource(R.string.location_permission_accessible))
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun Rationale(multiplePermissionsState: MultiplePermissionsState) {
    Text(text = stringResource(R.string.permission_must_be_required))
    Button(onClick = {
        multiplePermissionsState.launchMultiplePermissionRequest()
    }) {
        Text(text = stringResource(R.string.get_permission))
    }
}

@Composable
private fun LocationList(
    state: LocationListState,
    startRecordingLocation: () -> Unit,
    stopRecordingLocation: () -> Unit,
) {
    val locationPoints = state.locationPoints
    val isRecording = state.isRecording
    val isLocationProvided = state.isGpsAvailability

    val currentStartRecordingLocation = rememberUpdatedState(startRecordingLocation)
    val currentStopRecordingLocation = rememberUpdatedState(stopRecordingLocation)

    val lazyListState = rememberLazyListState()

    Text(text = stringResource(R.string.granted))

    if (locationPoints.isNotEmpty()) Divider(modifier = Modifier.padding(6.dp))

    LazyColumn(
        modifier = Modifier.heightIn(0.dp, 300.dp),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        contentPadding = PaddingValues(vertical = 10.dp),

        ) {
        items(locationPoints) { locationWithDate ->
            LocationItem(locationWithDate = locationWithDate)
        }
    }

    if (!isLocationProvided) {
        Text(text = "Turn on GPS")
    }

    if (locationPoints.isNotEmpty()) Divider(modifier = Modifier.padding(6.dp))

    if (!isRecording) {
        Button(onClick = currentStartRecordingLocation.value) {
            Text(text = stringResource(R.string.start_collect_location))
        }
    } else {
        Button(onClick = currentStopRecordingLocation.value) {
            Text(text = stringResource(R.string.stop_collect_location))
        }
    }
    LaunchedEffect(key1 = locationPoints) {
        lazyListState.animateScrollToItem(locationPoints.size)
    }
}

private fun isLocationProvided(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    return isGpsEnabled && isNetworkEnabled
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationItem(locationWithDate: LocationWithDate) {
    val lat = locationWithDate.location.latitude
    val long = locationWithDate.location.longitude
    val time = format("HH:mm:ss", locationWithDate.date)
    Card(
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSecondary),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SelectionContainer {
                Text(text = "$lat, $long")
            }
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp),
                color = MaterialTheme.colorScheme.onSecondary
            )
            Text(text = stringResource(R.string.time, time), overflow = TextOverflow.Ellipsis)
        }
    }
}