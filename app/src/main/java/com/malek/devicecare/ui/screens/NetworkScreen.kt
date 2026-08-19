package com.malek.devicecare.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.malek.devicecare.domain.model.NetworkHistoryItem
import com.malek.devicecare.ui.components.GlassCard
import com.malek.devicecare.ui.components.GradientButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel = viewModel(factory = NetworkViewModel.Factory),
    onNavigateToWifiTest: () -> Unit,
    onBack: () -> Unit
) {
    val networkInfo by viewModel.networkInfo.collectAsState()
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
                title = { Text("Network Status") },
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
                networkInfo?.let { info ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = info.type,
                                style = MaterialTheme.typography.displayMedium,
                                color = if (info.isConnected) (if (isDark) Color.White else MaterialTheme.colorScheme.primary) else Color(0xFFFF4B4B)
                            )
                            Text(
                                text = if (info.isConnected) "Connected" else "Disconnected",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            NetworkDetailRow("Wi-Fi", if (info.isWifi) "Available" else "N/A")
                            NetworkDetailRow("Cellular", if (info.isCellular) "Available" else "N/A")
                            if (info.isConnected) {
                                NetworkDetailRow("Link Speed", "${info.downloadSpeed} Mbps")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Connection History",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history.reversed()) { item ->
                            HistoryItemRow(item, isDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GradientButton(
                        text = "PERFORM SPEED TEST",
                        onClick = onNavigateToWifiTest,
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
fun HistoryItemRow(item: NetworkHistoryItem, isDark: Boolean) {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeString = sdf.format(Date(item.timestamp))
    
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = item.type, style = MaterialTheme.typography.bodyLarge)
                Row {
                    Text(
                        text = timeString, 
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray
                    )
                    item.speedMbps?.let {
                        if (it > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• $it Mbps",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Text(
                text = if (item.isConnected) "Connected" else "Disconnected",
                color = if (item.isConnected) Color(0xFF00FF85) else Color(0xFFFF4B4B),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun NetworkDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
    }
}
