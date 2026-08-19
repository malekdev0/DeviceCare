package com.malek.devicecare.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.util.VelocityTracker

@Composable
fun MultiTouchTestScreen(onBack: () -> Unit) {
    var touches by remember { mutableStateOf(mapOf<PointerId, Offset>()) }
    
    val touchColors = listOf(
        Color.Red, Color.Green, Color.Blue, Color.Yellow, 
        Color.Cyan, Color.Magenta, Color.White, Color.DarkGray,
        Color(0xFFFF5722), Color(0xFF4CAF50)
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val newTouches = event.changes
                                .filter { it.pressed }
                                .associate { it.id to it.position }
                            touches = newTouches
                        }
                    }
                }
        ) {
            touches.values.forEachIndexed { index, offset ->
                val color = touchColors[index % touchColors.size]
                drawCircle(
                    color = color,
                    radius = 100f,
                    center = offset
                )
                drawCircle(
                    color = color.copy(alpha = 0.3f),
                    radius = 150f,
                    center = offset
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Multi-touch Test",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                "Taps detected: ${touches.size}",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
    }
}
