package com.malek.devicecare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.malek.devicecare.ui.components.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareTestsScreen(
    viewModel: HardwareTestsViewModel = viewModel(factory = HardwareTestsViewModel.Factory),
    onNavigateToDisplayTest: () -> Unit,
    onNavigateToMultiTouchTest: () -> Unit,
    onBack: () -> Unit
) {
    val vibrationStatus by viewModel.vibrationStatus.collectAsState()
    val flashlightStatus by viewModel.flashlightStatus.collectAsState()
    val speakerStatus by viewModel.speakerStatus.collectAsState()
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
                title = { Text("Hardware Tests") },
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ModernTestCard(
                        title = "Vibration Motor",
                        description = "Tests the device's haptic feedback mechanism.",
                        status = vibrationStatus,
                        onTest = { viewModel.testVibration() }
                    )
                }
                
                item {
                    ModernTestCard(
                        title = "Flashlight",
                        description = "Tests the LED flash on the back of the device.",
                        status = flashlightStatus,
                        onTest = { viewModel.testFlashlight() }
                    )
                }

                item {
                    ModernTestCard(
                        title = "Speaker Test",
                        description = "Plays a test frequency to check the main speakers.",
                        status = speakerStatus,
                        onTest = { viewModel.testSpeaker() }
                    )
                }

                item {
                    ModernTestCard(
                        title = "Display Test",
                        description = "Checks for dead pixels by cycling through solid colors.",
                        status = "Ready",
                        onTest = onNavigateToDisplayTest
                    )
                }

                item {
                    ModernTestCard(
                        title = "Multi-touch Test",
                        description = "Verify that the screen detects multiple fingers simultaneously.",
                        status = "Ready",
                        onTest = onNavigateToMultiTouchTest
                    )
                }
            }
        }
    }
}

@Composable
fun ModernTestCard(
    title: String,
    description: String,
    status: String,
    onTest: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.red + MaterialTheme.colorScheme.surface.green + MaterialTheme.colorScheme.surface.blue < 1.5f

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = title, 
                style = MaterialTheme.typography.titleLarge, 
                color = if (isDark) Color.White else MaterialTheme.colorScheme.primary
            )
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Status: $status", style = MaterialTheme.typography.labelLarge)
                Button(
                    onClick = onTest,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text("Start")
                }
            }
        }
    }
}
