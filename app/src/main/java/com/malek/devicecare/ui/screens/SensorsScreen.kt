package com.malek.devicecare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malek.devicecare.domain.model.SensorInfo
import com.malek.devicecare.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreen(
    viewModel: SensorsViewModel = viewModel(factory = SensorsViewModel.Factory),
    onBack: () -> Unit
) {
    val sensors by viewModel.sensors.collectAsState()
    val isDark = MaterialTheme.colorScheme.surface.red + MaterialTheme.colorScheme.surface.green + MaterialTheme.colorScheme.surface.blue < 1.5f

    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0A0E21), Color(0xFF1A1F38)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFFFFFFF)))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hardware Sensors") },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(sensors) { sensor ->
                    SensorGlassCard(sensor)
                }
            }
        }
    }
}

@Composable
fun SensorGlassCard(sensor: SensorInfo) {
    val isDark = MaterialTheme.colorScheme.surface.red + MaterialTheme.colorScheme.surface.green + MaterialTheme.colorScheme.surface.blue < 1.5f
    
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = sensor.name, 
                style = MaterialTheme.typography.titleMedium, 
                color = if (isDark) Color.White else MaterialTheme.colorScheme.primary
            )
            Text(text = "Vendor: ${sensor.vendor}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Power: ${sensor.power}mA", style = MaterialTheme.typography.labelSmall)
                Text(text = "Res: ${sensor.resolution}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
