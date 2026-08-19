package com.malek.devicecare.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malek.devicecare.domain.model.LargeFile
import com.malek.devicecare.ui.components.GlassCard
import com.malek.devicecare.ui.components.GradientButton
import com.malek.devicecare.ui.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeFileScreen(
    viewModel: LargeFileViewModel = viewModel(factory = LargeFileViewModel.Factory),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val surface = MaterialTheme.colorScheme.surface
    val isDark = surface.red + surface.green + surface.blue < 1.5f

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.startScan()
    }

    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0A0E21), Color(0xFF1A1F38)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFFFFFFF)))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Large File Finder") },
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
                is LargeFileState.Idle -> LargeFileIdleView(onStart = { viewModel.startScan() }, isDark)
                is LargeFileState.PermissionRequired -> LargeFilePermissionView(
                    onGrant = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    },
                    isDark = isDark
                )
                is LargeFileState.Scanning -> LargeFileScanningView(isDark)
                is LargeFileState.Results -> LargeFileResultsView(
                    files = currentState.files,
                    selectedFiles = selectedFiles,
                    onToggleSelect = { viewModel.toggleFileSelection(it) },
                    onDelete = { viewModel.deleteSelectedFiles() },
                    isDark = isDark
                )
                is LargeFileState.Deleting -> LargeFileDeletingView(isDark)
                is LargeFileState.Finished -> LargeFileFinishedView(onBack = onBack, isDark)
            }
        }
    }
}

@Composable
fun LargeFileIdleView(onStart: () -> Unit, isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SdStorage,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = if (isDark) Color.White else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Find Large Files",
            style = MaterialTheme.typography.headlineMedium,
            color = if (isDark) Color.White else Color.Black
        )
        Text(
            "Locate and remove space-consuming files from your device storage.",
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
fun LargeFilePermissionView(onGrant: () -> Unit, isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SdStorage,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFFFFD600)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            color = if (isDark) Color.White else Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "To find and delete large files, DeviceCare needs permission to access your storage.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
        )
        Spacer(modifier = Modifier.height(48.dp))
        GradientButton(
            text = "GRANT PERMISSION",
            onClick = onGrant,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LargeFileScanningView(isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = if (isDark) Color.White else MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Scanning Storage...", style = MaterialTheme.typography.headlineSmall, color = if (isDark) Color.White else Color.Black)
    }
}

@Composable
fun LargeFileResultsView(
    files: List<LargeFile>,
    selectedFiles: Set<String>,
    onToggleSelect: (String) -> Unit,
    onDelete: () -> Unit,
    isDark: Boolean
) {
    val totalSelectedSize = files.filter { selectedFiles.contains(it.id) }.sumOf { it.size }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (files.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No large files found.", color = if (isDark) Color.White else Color.Black)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(files) { file ->
                    val isSelected = selectedFiles.contains(file.id)
                    LargeFileItem(file, isSelected, { onToggleSelect(file.id) }, isDark)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (selectedFiles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                GradientButton(
                    text = "DELETE SELECTED (${FormatUtils.formatBytes(totalSelectedSize)})",
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun LargeFileItem(file: LargeFile, isSelected: Boolean, onToggle: () -> Unit, isDark: Boolean) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray
                )
            )
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = if (isDark) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isDark) Color.White else Color.Black
                )
                Text(
                    file.path,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray
                )
            }
            Text(
                FormatUtils.formatBytes(file.size),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun LargeFileDeletingView(isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = if (isDark) Color.White else MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Deleting Selected Files...", style = MaterialTheme.typography.headlineSmall, color = if (isDark) Color.White else Color.Black)
    }
}

@Composable
fun LargeFileFinishedView(onBack: () -> Unit, isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Delete,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFF00FF85)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Files Deleted Successfully!", style = MaterialTheme.typography.headlineMedium, color = if (isDark) Color.White else Color.Black)
        Spacer(modifier = Modifier.height(48.dp))
        GradientButton(text = "DONE", onClick = onBack, modifier = Modifier.fillMaxWidth())
    }
}
