package com.malek.devicecare.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malek.devicecare.ui.components.GlassCard
import com.malek.devicecare.ui.theme.ThemeMode
import com.malek.devicecare.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel,
    onNavigateToInfo: () -> Unit,
    onBack: () -> Unit
) {
    val themeMode by themeViewModel.themeMode.collectAsState()
    val context = LocalContext.current
    val scheduledScanViewModel: ScheduledScanViewModel = viewModel(factory = ScheduledScanViewModel.Factory)
    val scheduledScansEnabled by scheduledScanViewModel.enabled.collectAsState()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        scheduledScanViewModel.setEnabled(granted)
    }
    val isDark = MaterialTheme.colorScheme.surface.red + MaterialTheme.colorScheme.surface.green + MaterialTheme.colorScheme.surface.blue < 1.5f

    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0A0E21), Color(0xFF1A1F38)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFFFFFFF)))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Appearance", 
                        style = MaterialTheme.typography.titleLarge, 
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ThemeOption("System Default", themeMode == ThemeMode.SYSTEM, isDark) {
                        themeViewModel.setThemeMode(ThemeMode.SYSTEM)
                    }
                    ThemeOption("Light Mode", themeMode == ThemeMode.LIGHT, isDark) {
                        themeViewModel.setThemeMode(ThemeMode.LIGHT)
                    }
                    ThemeOption("Dark Mode", themeMode == ThemeMode.DARK, isDark) {
                        themeViewModel.setThemeMode(ThemeMode.DARK)
                    }
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToInfo
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Device Information", 
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            Icons.Default.Info, 
                            contentDescription = null,
                            tint = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Scheduled scans",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Get a weekly notification with your device health summary.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = scheduledScansEnabled,
                            onCheckedChange = { enabled ->
                                val requiresPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                if (enabled && requiresPermission) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    scheduledScanViewModel.setEnabled(enabled)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeOption(
    label: String,
    isSelected: Boolean,
    isDark: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label, 
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        )
        RadioButton(
            selected = isSelected, 
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary,
                unselectedColor = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
