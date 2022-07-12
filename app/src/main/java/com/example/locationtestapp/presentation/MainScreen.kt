package com.example.locationtestapp.presentation


import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainScreenViewModel) {
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            modifier = Modifier.animateContentSize(),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    multiplePermissionsState.allPermissionsGranted -> OnGranted(
                        viewModel
                    )
                    multiplePermissionsState.shouldShowRationale -> OnShowRationale(
                        multiplePermissionsState
                    )
                    else -> OnNotGranted(context)
                }
            }
        }
    }
}

@Composable
private fun LifecycleDispatcher(
    onResume: () -> Unit,
    onPause: () -> Unit,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(key1 = lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onResume.invoke()
            }
            if (event == Lifecycle.Event.ON_PAUSE) {
                onPause.invoke()
            }
        }

        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}

@Composable
private fun OnNotGranted(context: Context) {
    Text("Can`t work without permission")
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        context.startActivity(intent)
    }) {
        Text("Location permission accessible")
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun OnShowRationale(multiplePermissionsState: MultiplePermissionsState) {
    Text(text = "Permission must be required")
    Button(onClick = {
        multiplePermissionsState.launchMultiplePermissionRequest()
    }) {
        Text(text = "Get permission")
    }
}

@Composable
private fun OnGranted(
    viewModel: MainScreenViewModel
) {
    val locations = viewModel.locations
    var isStartCollect by remember { mutableStateOf(false) }
    var isGpsProvided by remember { mutableStateOf(viewModel.isGpsProviderEnabled) }

    Text(text = "Granted")
    Spacer(modifier = Modifier.height(16.dp))

    if (locations.isNotEmpty()) {
        LazyColumn {
            locations.forEach {
                item {
                    Row(modifier = Modifier) {
                        Text(text = "${it.latitude}, ${it.longitude}")
                    }
                }
            }
        }
    }

    if (isGpsProvided && !isStartCollect) {
        Button(onClick = {
            isStartCollect = true
            viewModel.startGetLocation()
        }) {
            Text("Start collect location")
        }
    } else if (!isGpsProvided) {
        Text("GPS needed")
        Button(onClick = {
            isGpsProvided = viewModel.isGpsProviderEnabled
        }) {
            Text(text = "Check")
        }
    }

    LifecycleDispatcher(
        onPause = {
            if (isStartCollect)
                viewModel.stopGetLocation()
        },
        onResume = {
            if (isStartCollect)
                viewModel.startGetLocation()
            isGpsProvided = viewModel.isGpsProviderEnabled
        }
    )
}