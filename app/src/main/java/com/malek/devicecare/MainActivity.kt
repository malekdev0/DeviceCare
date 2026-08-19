package com.malek.devicecare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malek.devicecare.ui.navigation.AppNavigation
import com.malek.devicecare.ui.theme.DeviceCareTheme
import com.malek.devicecare.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory)
            val themeMode by themeViewModel.themeMode.collectAsState()
            
            DeviceCareTheme(themeMode = themeMode) {
                AppNavigation(themeViewModel = themeViewModel)
            }
        }
    }
}
