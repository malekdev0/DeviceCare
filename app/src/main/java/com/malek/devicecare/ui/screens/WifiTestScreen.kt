package com.malek.devicecare.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malek.devicecare.domain.model.WifiTestResult
import com.malek.devicecare.ui.components.GlassCard
import com.malek.devicecare.ui.components.GradientButton
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiTestScreen(
    viewModel: WifiTestViewModel = viewModel(factory = WifiTestViewModel.Factory),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val currentSpeed by viewModel.currentSpeed.collectAsState()
    
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
                title = {
                    Text(
                        "Speed Test",
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                        )
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (val currentState = state) {
                    is WifiTestState.Idle -> {
                        TestIdleView(onStart = { viewModel.startTest() }, isDark = isDark)
                    }
                    is WifiTestState.Pinging, 
                    is WifiTestState.Downloading, 
                    is WifiTestState.Uploading -> {
                        TestActiveView(
                            state = currentState,
                            speed = currentSpeed,
                            progress = progress,
                            isDark = isDark
                        )
                    }
                    is WifiTestState.Finished -> {
                        TestFinishedView(
                            result = currentState.result,
                            onRestart = { viewModel.reset(); viewModel.startTest() },
                            isDark = isDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TestIdleView(onStart: () -> Unit, isDark: Boolean) {
    Spacer(modifier = Modifier.height(60.dp))
    Icon(
        imageVector = Icons.Default.Wifi,
        contentDescription = null,
        modifier = Modifier.size(120.dp),
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    )
    Spacer(modifier = Modifier.height(32.dp))
    Text(
        "Accurate Speed Test",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    )
    Text(
        "Test your download, upload speeds and ping with high accuracy.",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
    )
    Spacer(modifier = Modifier.height(48.dp))
    GradientButton(
        text = "BEGIN TEST",
        onClick = onStart,
        modifier = Modifier.width(200.dp)
    )
}

@Composable
fun TestActiveView(
    state: WifiTestState,
    speed: Double,
    progress: Float,
    isDark: Boolean
) {
    val statusText = when (state) {
        is WifiTestState.Pinging -> "Testing Ping..."
        is WifiTestState.Downloading -> "Testing Download..."
        is WifiTestState.Uploading -> "Testing Upload..."
        else -> ""
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    Spacer(modifier = Modifier.height(40.dp))
    
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
        // Gauge Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Gauge Foreground with smooth reset behavior
        val animatedSpeed by animateFloatAsState(
            targetValue = speed.toFloat(),
            animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
        )
        
        // Gauge scale: 0 to 1000 Mbps
        val sweepAngle = (animatedSpeed / 1000f * 270f).coerceAtMost(270f)
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                brush = Brush.sweepGradient(
                    0f to primaryColor,
                    0.5f to secondaryColor,
                    1f to primaryColor
                ),
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                String.format("%.1f", speed),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Mbps",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    
    Spacer(modifier = Modifier.height(40.dp))
    
    Text(
        statusText,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .padding(horizontal = 32.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
fun TestFinishedView(
    result: WifiTestResult,
    onRestart: () -> Unit,
    isDark: Boolean
) {
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        "Test Complete",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(24.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ResultCard(
            title = "Download",
            value = String.format("%.1f", result.downloadSpeedMbps),
            unit = "Mbps",
            icon = Icons.Default.Download,
            modifier = Modifier.weight(1f),
            color = Color(0xFF00E676),
            isDark = isDark
        )
        ResultCard(
            title = "Upload",
            value = String.format("%.1f", result.uploadSpeedMbps),
            unit = "Mbps",
            icon = Icons.Default.Upload,
            modifier = Modifier.weight(1f),
            color = Color(0xFF2979FF),
            isDark = isDark
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ResultCard(
            title = "Ping",
            value = result.pingMs.toString(),
            unit = "ms",
            icon = Icons.Default.Timer,
            modifier = Modifier.weight(1f),
            color = Color(0xFFFFAB40),
            isDark = isDark
        )
        ResultCard(
            title = "Network",
            value = if (result.isWifi) "Wi-Fi" else "Cellular",
            unit = result.ssid ?: "Mobile",
            icon = if (result.isWifi) Icons.Default.Wifi else Icons.Default.SignalCellularAlt,
            modifier = Modifier.weight(1f),
            color = Color(0xFFE91E63),
            isDark = isDark
        )
    }
    
    Spacer(modifier = Modifier.height(40.dp))
        GradientButton(
        text = "TEST AGAIN",
        onClick = onRestart,
        modifier = Modifier.fillMaxWidth(0.7f)
    )
}

@Composable
fun ResultCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color,
    isDark: Boolean
) {
    GlassCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Text(
                unit,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
