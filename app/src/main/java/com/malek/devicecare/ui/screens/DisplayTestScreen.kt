package com.malek.devicecare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DisplayTestScreen(onBack: () -> Unit) {
    val rows = 6
    val cols = 4
    val totalSections = rows * cols
    // Using a list of booleans to track which sections have been tapped
    val sectionStates = remember { mutableStateListOf(*Array(totalSections) { false }) }
    
    val allSelected = sectionStates.all { it }
    
    LaunchedEffect(allSelected) {
        if (allSelected) {
            // Give a tiny delay so the user sees the last box turn green
            kotlinx.coroutines.delay(200)
            onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (r in 0 until rows) {
                Row(modifier = Modifier.weight(1f)) {
                    for (c in 0 until cols) {
                        val index = r * cols + c
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (sectionStates[index]) Color(0xFF4CAF50) else Color(
                                    0xFFCD0303
                                )
                                )
                                .border(1.dp, Color.Black.copy(alpha = 0.15f))
                                .clickable {
                                    sectionStates[index] = true
                                }
                        )
                    }
                }
            }
        }

        // Instruction overlay that stays until at least half are done or just at the start
        if (sectionStates.count { !it } > 23) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.7f), shape = MaterialTheme.shapes.medium)
                    .padding(24.dp)
            ) {
                Text(
                    text = "DISPLAY & TOUCH TEST\n\nTap all red sections to turn them green.\nChecks for dead zones and color consistency.",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
