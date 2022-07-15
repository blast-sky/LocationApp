package com.example.locationtestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.locationtestapp.presentation.MainScreen
import com.example.locationtestapp.ui.theme.LocationTestAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LocationTestAppTheme {
                MainScreen()
            }
        }
    }
}