package com.malek.devicecare.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malek.devicecare.ui.components.GlassCard
import com.malek.devicecare.ui.components.UsageGraph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryScreen(
    viewModel: BatteryViewModel = viewModel(factory = BatteryViewModel.Factory),
    onNavigateToChargingCurrent: () -> Unit,
    onBack: () -> Unit
) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val history by viewModel.history.collectAsState()
    val surface = MaterialTheme.colorScheme.surface
    val isDark = surface.red + surface.green + surface.blue < 1.5f

    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0A0E21), Color(0xFF1A1F38)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFFFFFFF)))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battery Diagnostics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                batteryInfo?.let { info ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${info.percentage}%",
                                style = MaterialTheme.typography.displayLarge,
                                color = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (info.isCharging) "⚡ Charging" else "Discharging",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (info.isCharging) Color(0xFF278956) else MaterialTheme.colorScheme.secondary
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            UsageGraph(
                                points = history,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                color = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            BatteryDetailRow("Health", info.health)
                            BatteryDetailRow("Temperature", "${info.temperature}°C")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    com.malek.devicecare.ui.components.GradientButton(
                        text = "SCAN CHARGING CURRENT",
                        onClick = onNavigateToChargingCurrent,
                        modifier = Modifier.fillMaxWidth()
                    )
                } ?: run {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun BatteryDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
    }
}
