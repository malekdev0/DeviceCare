package com.malek.devicecare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malek.devicecare.ui.components.GlassCard
import com.malek.devicecare.ui.components.UsageGraph
import com.malek.devicecare.ui.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel = viewModel(factory = MemoryViewModel.Factory),
    onBack: () -> Unit
) {
    val memoryInfo by viewModel.memoryInfo.collectAsState()
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
                title = { Text("Performance") },
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
                memoryInfo?.let { info ->
                    val usedMem = info.totalMemory - info.availableMemory
                    val usagePercentage = if (info.totalMemory > 0) ((usedMem.toDouble() / info.totalMemory.toDouble()) * 100).toInt() else 0

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$usagePercentage%",
                                style = MaterialTheme.typography.displayLarge,
                                color = if (info.lowMemory) Color(0xFFFF4B4B) else (if (isDark) Color.White else MaterialTheme.colorScheme.primary)
                            )
                            Text("RAM Used", style = MaterialTheme.typography.titleMedium)
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            UsageGraph(
                                points = history,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                color = if (info.lowMemory) Color(0xFFFF4B4B) else (if (isDark) Color.White else MaterialTheme.colorScheme.primary)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            LinearProgressIndicator(
                                progress = { usagePercentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = if (info.lowMemory) Color(0xFFFF4B4B) else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            MemoryDetailRow("Total RAM", FormatUtils.formatBytes(info.totalMemory))
                            MemoryDetailRow("Available RAM", FormatUtils.formatBytes(info.availableMemory))
                            MemoryDetailRow("Used RAM", FormatUtils.formatBytes(usedMem))
                            MemoryDetailRow("Status", if (info.lowMemory) "Low Memory Warning" else "Normal")
                        }
                    }
                } ?: run {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun MemoryDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
    }
}
