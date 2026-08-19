package com.malek.devicecare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malek.devicecare.domain.model.HealthStatus
import com.malek.devicecare.ui.components.GlassCard
import com.malek.devicecare.ui.components.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
    onNavigateToInfo: () -> Unit,
    onNavigateToBattery: () -> Unit,
    onNavigateToStorage: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    onNavigateToSensors: () -> Unit,
    onNavigateToHardwareTests: () -> Unit,
    onNavigateToAppManager: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToCpuRoom: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val surface = MaterialTheme.colorScheme.surface
    val isDark = surface.red + surface.green + surface.blue < 1.5f

    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0A0E21), Color(0xFF1A1F38)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFFFFFFF)))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Device Care", style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "Scan history")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
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
                // Main Health Status Widget
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "OVERALL STATUS", 
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val statusColor = when (uiState.overallStatus) {
                                HealthStatus.GOOD -> Color(0xFF00FF85)
                                HealthStatus.WARNING -> Color(0xFFFFD600)
                                HealthStatus.CRITICAL -> Color(0xFFFF4B4B)
                                else -> Color.Gray
                            }
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.overallStatus.name,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        uiState.lastScanTimestamp?.let { timestamp ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Last scan: ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timestamp))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Grid of Widgets
                val items = listOf(
                    DashboardItem("Battery", "${uiState.batteryPercentage}%", Icons.Default.BatteryChargingFull, onNavigateToBattery),
                    DashboardItem("Storage", "${uiState.storageUsagePercentage}%", Icons.Default.Storage, onNavigateToStorage),
                    DashboardItem("Memory", uiState.memoryStatus, Icons.Default.Memory, onNavigateToMemory),
                    DashboardItem("Apps", "Manager", Icons.Default.Apps, onNavigateToAppManager),
                    DashboardItem("Security", "Privacy", Icons.Default.Shield, onNavigateToSecurity),
                    DashboardItem("Network", uiState.networkStatus, Icons.Default.Wifi, onNavigateToNetwork),
                    DashboardItem("CPU Room", "Engine", Icons.Default.Speed, onNavigateToCpuRoom),
                    DashboardItem("Sensors", "Hardware", Icons.Default.Sensors, onNavigateToSensors),
                    DashboardItem("Tests", "Hardware", Icons.Default.Build, onNavigateToHardwareTests)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items) { item ->
                        GlassCard(
                            onClick = item.onClick,
                            modifier = Modifier.height(120.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    item.icon, 
                                    contentDescription = null, 
                                    tint = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    item.title, 
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    item.value, 
                                    style = MaterialTheme.typography.bodyMedium, 
                                    color = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GradientButton(
                    text = "START SYSTEM SCAN",
                    onClick = onNavigateToScan,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

data class DashboardItem(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
