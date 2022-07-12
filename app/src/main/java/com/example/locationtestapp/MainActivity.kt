package com.example.locationtestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.locationtestapp.presentation.MainScreen
import com.example.locationtestapp.presentation.MainScreenViewModel
import com.example.locationtestapp.ui.theme.LocationTestAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MainScreenViewModel by viewModels()

        setContent {
            LocationTestAppTheme {
                MainScreen(viewModel)
            }
        }
    }
}