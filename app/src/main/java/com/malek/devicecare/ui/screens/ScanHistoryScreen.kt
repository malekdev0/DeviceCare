package com.malek.devicecare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malek.devicecare.domain.model.HealthScanRecord
import com.malek.devicecare.ui.components.GlassCard
import com.malek.devicecare.ui.components.UsageGraph
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryScreen(
    viewModel: ScanHistoryViewModel = viewModel(factory = ScanHistoryViewModel.Factory),
    onBack: () -> Unit
) {
    val records by viewModel.records.collectAsState()
    val isDark = MaterialTheme.colorScheme.surface.let { it.red + it.green + it.blue < 1.5f }
    val background = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0A0E21), Color(0xFF1A1F38)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color.White))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan history") },
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
            if (records.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No scans yet", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Run a system scan or enable scheduled scans to start building your history.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { ScanTrendCard(records, isDark) }
                    items(records, key = { it.timestamp }) { record -> ScanHistoryItem(record, isDark) }
                }
            }
        }
    }
}

@Composable
private fun ScanTrendCard(records: List<HealthScanRecord>, isDark: Boolean) {
    val trend = if (records.size < 2) "Your first scan has been saved." else {
        val difference = records.first().score - records[1].score
        when {
            difference > 0 -> "Your score improved by $difference since the previous scan."
            difference < 0 -> "Your score changed by $difference since the previous scan."
            else -> "Your score is unchanged since the previous scan."
        }
    }
    GlassCard(Modifier.fillMaxWidth()) {
        Text("Health trend", style = MaterialTheme.typography.titleLarge, color = if (isDark) Color.White else MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(trend, color = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
        if (records.size > 1) {
            UsageGraph(
                points = records.asReversed().map { com.malek.devicecare.domain.model.UsagePoint(it.timestamp, it.score.toFloat()) },
                modifier = Modifier.fillMaxWidth().height(72.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ScanHistoryItem(record: HealthScanRecord, isDark: Boolean) {
    val date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(record.timestamp))
    GlassCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(date, style = MaterialTheme.typography.titleMedium, color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface)
                Text(
                    "Battery ${record.batteryStatus.name.lowercase()} - Storage ${record.storageStatus.name.lowercase()} - Security ${record.securityStatus.name.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                record.score.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (record.score >= 85) Color(0xFF00B86B) else Color(0xFFFF9800)
            )
        }
    }
}
