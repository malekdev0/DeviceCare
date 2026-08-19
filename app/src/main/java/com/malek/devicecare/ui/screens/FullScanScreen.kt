package com.malek.devicecare.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malek.devicecare.domain.model.HealthStatus
import com.malek.devicecare.ui.components.GlassCard
import com.malek.devicecare.ui.components.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScanScreen(
    viewModel: FullScanViewModel = viewModel(factory = FullScanViewModel.Factory),
    onNavigateToBattery: () -> Unit,
    onNavigateToStorage: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToLargeFiles: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val progress by viewModel.progress.collectAsState()
    
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
                title = { Text("System Scanner") },
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
            when (val scanState = state) {
                is ScanState.Idle -> ScanIdleView(onStart = { viewModel.startScan() }, isDark)
                is ScanState.ScanningBattery, 
                is ScanState.ScanningStorage, 
                is ScanState.ScanningMemory, 
                is ScanState.ScanningNetwork,
                is ScanState.ScanningSecurity,
                is ScanState.ScanningLargeFiles,
                is ScanState.ScanningEmptyFolders -> ScanningProgressView(scanState, progress, isDark)
                is ScanState.Completed -> ScanResultsView(
                    scanState, 
                    onDone = onBack, 
                    onItemClick = { label ->
                        when(label) {
                            "Battery" -> onNavigateToBattery()
                            "Storage" -> onNavigateToStorage()
                            "Memory" -> onNavigateToMemory()
                            "Network" -> onNavigateToNetwork()
                            "Security" -> onNavigateToSecurity()
                            "Large Files" -> onNavigateToLargeFiles()
                            "Empty Folders" -> onNavigateToStorage()
                        }
                    },
                    isDark
                )
            }
        }
    }
}

@Composable
fun ScanIdleView(onStart: () -> Unit, isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Ready for a full checkup?",
            style = MaterialTheme.typography.headlineMedium,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "We'll analyze your battery, storage, memory, and network to ensure your device is running at its best.",
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        GradientButton(
            text = "START SCAN",
            onClick = onStart,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ScanningProgressView(state: ScanState, progress: Float, isDark: Boolean) {
    val statusText = when (state) {
        ScanState.ScanningBattery -> "Checking Battery Health..."
        ScanState.ScanningStorage -> "Analyzing Storage Space..."
        ScanState.ScanningMemory -> "Monitoring RAM Performance..."
        ScanState.ScanningNetwork -> "Testing Network Stability..."
        ScanState.ScanningSecurity -> "Auditing System Security..."
        ScanState.ScanningLargeFiles -> "Searching for Large Files..."
        ScanState.ScanningEmptyFolders -> "Hunting for Empty Folders..."
        else -> "Scanning..."
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(150.dp),
            strokeWidth = 12.dp,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            statusText,
            style = MaterialTheme.typography.headlineSmall,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ScanResultsView(
    results: ScanState.Completed, 
    onDone: () -> Unit, 
    onItemClick: (String) -> Unit,
    isDark: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Device Score",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
                Text(
                    "${results.score}",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                    fontWeight = FontWeight.Bold,
                    color = when {
                        results.score > 80 -> Color(0xFF00FF85)
                        results.score > 50 -> Color(0xFFFFD600)
                        else -> Color(0xFFFF4B4B)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ResultItem("Battery", results.batteryStatus, isDark, onItemClick)
        Spacer(modifier = Modifier.height(12.dp))
        ResultItem("Storage", results.storageStatus, isDark, onItemClick)
        Spacer(modifier = Modifier.height(12.dp))
        ResultItem("Memory", results.memoryStatus, isDark, onItemClick)
        Spacer(modifier = Modifier.height(12.dp))
        ResultItem("Network", results.networkStatus, isDark, onItemClick)
        Spacer(modifier = Modifier.height(12.dp))
        ResultItem("Security", results.securityStatus, isDark, onItemClick)
        Spacer(modifier = Modifier.height(12.dp))
        ResultItem("Large Files", results.largeFileStatus, isDark, onItemClick)
        Spacer(modifier = Modifier.height(12.dp))
        ResultItem("Empty Folders", results.emptyFolderStatus, isDark, onItemClick)

        if (results.issues.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Issues Detected", 
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = if (isDark) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            results.issues.forEach { issue ->
                GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(issue, style = MaterialTheme.typography.bodySmall, color = if (isDark) Color.White else Color.Black)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        GradientButton(
            text = "FINISH",
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ResultItem(label: String, status: HealthStatus, isDark: Boolean, onClick: (String) -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick(label) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (isDark) Color.White else Color.Black
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = status.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (status) {
                        HealthStatus.GOOD -> Color(0xFF00FF85)
                        HealthStatus.WARNING -> Color(0xFFFFD600)
                        HealthStatus.CRITICAL -> Color(0xFFFF4B4B)
                        else -> Color.Gray
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = when (status) {
                        HealthStatus.GOOD -> Icons.Default.CheckCircle
                        HealthStatus.WARNING -> Icons.Default.Warning
                        else -> Icons.Default.Error
                    },
                    contentDescription = null,
                    tint = when (status) {
                        HealthStatus.GOOD -> Color(0xFF00FF85)
                        HealthStatus.WARNING -> Color(0xFFFFD600)
                        else -> Color(0xFFFF4B4B)
                    }
                )
            }
        }
    }
}
