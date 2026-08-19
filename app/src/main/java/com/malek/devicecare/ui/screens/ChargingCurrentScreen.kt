package com.malek.devicecare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malek.devicecare.ui.components.GlassCard
import com.malek.devicecare.ui.components.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargingCurrentScreen(
    viewModel: ChargingCurrentViewModel = viewModel(factory = ChargingCurrentViewModel.Factory),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isDark = MaterialTheme.colorScheme.surface.let { it.red + it.green + it.blue < 1.5f }
    val background = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0A0E21), Color(0xFF1A1F38)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color.White))
    }
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Charging current") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(Modifier.fillMaxSize().background(background).padding(padding)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        when (val currentState = state) {
                            ChargingCurrentState.Measuring -> CircularProgressIndicator()
                            is ChargingCurrentState.Result -> {
                                Text("${currentState.milliamps} mA", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("Current charging rate", color = textColor)
                            }
                            ChargingCurrentState.NotCharging -> Text("Plug in your charger before scanning.", textAlign = TextAlign.Center, color = textColor)
                            ChargingCurrentState.Unavailable -> Text("This device does not provide a charging-current reading.", textAlign = TextAlign.Center, color = textColor)
                            ChargingCurrentState.Idle -> Text("Starting live current scan...", textAlign = TextAlign.Center, color = textColor)
                        }
                        Spacer(Modifier.height(24.dp))
                        GradientButton(
                            text = "END SCAN",
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Live readings update every second and stop when you leave this screen.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
