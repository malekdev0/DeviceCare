package com.malek.devicecare.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malek.devicecare.domain.model.RiskLevel
import com.malek.devicecare.domain.model.SecurityInfo
import com.malek.devicecare.domain.model.SecurityRisk
import com.malek.devicecare.ui.components.GlassCard
import com.malek.devicecare.ui.components.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    viewModel: SecurityViewModel = viewModel(factory = SecurityViewModel.Factory),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
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
                title = { Text("Privacy & Security") },
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
            when (val currentState = state) {
                is SecurityState.Idle -> SecurityIdleView(onStart = { viewModel.startScan() }, isDark)
                is SecurityState.Scanning -> SecurityScanningView(isDark)
                is SecurityState.Success -> SecurityResultsView(
                    currentState.info, 
                    isDark,
                    onAppClick = { packageName ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", packageName, null)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun SecurityIdleView(onStart: () -> Unit, isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Shield,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = if (isDark) Color.White else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Privacy Auditor",
            style = MaterialTheme.typography.headlineMedium,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Scan for root access, suspicious permissions, and system vulnerabilities.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
        )
        Spacer(modifier = Modifier.height(48.dp))
        GradientButton(
            text = "SCAN SYSTEM",
            onClick = onStart,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SecurityScanningView(isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = if (isDark) Color.White else MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Checking Integrity...", style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun SecurityResultsView(info: SecurityInfo, isDark: Boolean, onAppClick: (String) -> Unit) {
    val scoreColor = when {
        info.overallScore > 85 -> Color(0xFF00FF85)
        info.overallScore > 60 -> Color(0xFFFFD600)
        else -> Color(0xFFFF4B4B)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Security Score",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )
                    Text(
                        "${info.overallScore}",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
            }
        }

        item {
            Text("Vulnerabilities", style = MaterialTheme.typography.titleLarge)
        }

        if (info.risks.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00FF85))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("No major threats detected.")
                    }
                }
            }
        } else {
            items(info.risks) { risk ->
                RiskItem(risk, isDark, onAppClick)
            }
        }

        item {
            Text("System Status", style = MaterialTheme.typography.titleLarge, color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface)
        }

        item {
            StatusRow("Root Access", !info.isRooted, isDark)
        }
        item {
            StatusRow("USB Debugging", !info.isAdbEnabled, isDark)
        }
        item {
            StatusRow("Device Encryption", info.isDeviceEncrypted, isDark)
        }
    }
}

@Composable
fun RiskItem(risk: SecurityRisk, isDark: Boolean, onAppClick: (String) -> Unit) {
    val riskColor = when (risk.level) {
        RiskLevel.CRITICAL -> Color(0xFFFF4B4B)
        RiskLevel.HIGH -> Color(0xFFFF7B4B)
        RiskLevel.MEDIUM -> Color(0xFFFFD600)
        else -> Color.Gray
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = risk.affectedApp?.let { { onAppClick(it) } }
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = riskColor)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(risk.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(risk.description, style = MaterialTheme.typography.bodySmall, color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray)
                if (risk.affectedApp != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "App: ${risk.affectedApp}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun StatusRow(label: String, isSafe: Boolean, isDark: Boolean) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface)
            Icon(
                if (isSafe) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isSafe) Color(0xFF00FF85) else Color(0xFFFF4B4B)
            )
        }
    }
}
