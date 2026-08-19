package com.malek.devicecare.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malek.devicecare.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CpuRoomScreen(
    viewModel: CpuRoomViewModel = viewModel(factory = CpuRoomViewModel.Factory),
    onBack: () -> Unit
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
            TopAppBar(
                title = { Text("CPU Engine Room") },
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Visual Pulse Indicator
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CpuPulseIndicator(uiState.frequencyRatio)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${uiState.temperature}°C",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "SYSTEM TEMP",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CpuInfoRow("Hardware", uiState.hardware, isDark)
                        CpuInfoRow("Architecture", uiState.architecture, isDark)
                        CpuInfoRow("Cores", uiState.cores.toString(), isDark)
                        CpuInfoRow("Model", uiState.model, isDark)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "The CPU is the heart of your device. High temperatures can lead to thermal throttling, which reduces performance to protect hardware.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun CpuPulseIndicator(ratio: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val animatedRatio by animateFloatAsState(
        targetValue = ratio,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ratio"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val color = if (animatedRatio > 0.8f) Color(0xFFFF4B4B) else Color(0xFF00FF85)
        
        drawCircle(
            color = color.copy(alpha = 0.1f * pulseScale),
            radius = size.minDimension / 2 * pulseScale,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )

        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * animatedRatio,
            useCenter = false,
            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun CpuInfoRow(label: String, value: String, isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
        Text(value, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
    }
}
