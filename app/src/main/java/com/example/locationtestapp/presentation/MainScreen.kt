package com.example.locationtestapp.presentation


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel()
) {
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = viewModel.permissions
    )

    val context = LocalContext.current

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
                        viewModel
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
private fun Rationale(multiplePermissionsState: MultiplePermissionsState) {
    Text(text = "Permission must be required")
    Button(onClick = {
        multiplePermissionsState.launchMultiplePermissionRequest()
    }) {
        Text(text = "Get permission")
    }
}

@Composable
private fun LocationList(
    viewModel: MainScreenViewModel
) {
    val locationPoints by viewModel.locationPoints.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    var isGpsProvided by remember { mutableStateOf(true) }

    Text(text = "Granted")
    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn {
        if (locationPoints.isNotEmpty()) {
            items(locationPoints) {
                Row(modifier = Modifier) {
                    Text(text = "${it.location.latitude}, ${it.location.longitude} | Time = ${it.date.time}")
                }
            }
        }
        item {
            if (!isRecording) {
                Button(onClick = {
                    viewModel.startRecordLocation()
                }) {
                    Text("Start collect location")
                }
            } else {
                Button(onClick = {
                    viewModel.stopRecordLocation()
                }) {
                    Text("Stop collect location")
                }
            }
        }
    }

}