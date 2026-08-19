package com.malek.devicecare.ui.screens

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malek.devicecare.domain.model.JunkItem
import com.malek.devicecare.domain.model.JunkType
import com.malek.devicecare.ui.components.GlassCard
import com.malek.devicecare.ui.components.GradientButton
import com.malek.devicecare.ui.utils.FormatUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageCleanerScreen(
    viewModel: StorageCleanerViewModel = viewModel(factory = StorageCleanerViewModel.Factory),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val progress by viewModel.scanningProgress.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val estimatedTime by viewModel.estimatedTimeRemaining.collectAsState()
    
    val context = LocalContext.current
    val surface = MaterialTheme.colorScheme.surface
    val isDark = surface.red + surface.green + surface.blue < 1.5f

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.startScan()
    }

    // Automatically re-scan when returning from settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0A0E21), Color(0xFF1A1F38)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFFFFFFF)))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Cleaner") },
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
                is CleanerState.Idle -> IdleView(onStartScan = { viewModel.startScan() }, isDark)
                is CleanerState.PermissionRequired -> PermissionView(
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
                is CleanerState.Scanning -> ScanningView(progress, currentPath, estimatedTime, isDark)
                is CleanerState.Results -> ResultsView(
                    currentState.items, 
                    onClean = { viewModel.cleanJunk(it) },
                    onToggle = { viewModel.toggleItemSelection(it) },
                    onOpenFile = { path ->
                        try {
                            val file = File(path)
                            if (file.exists()) {
                                val extension = MimeTypeMap.getFileExtensionFromUrl(path)
                                var type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
                                
                                if (type == null) {
                                    type = "*/*"
                                }

                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, type)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                
                                val chooser = Intent.createChooser(intent, "Open with")
                                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(chooser)
                            }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Cannot open file", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    isDark
                )
                is CleanerState.Cleaning -> CleaningView(isDark)
                is CleanerState.Finished -> FinishedView(onBack = onBack, isDark)
            }
        }
    }
}

@Composable
fun IdleView(onStartScan: () -> Unit, isDark: Boolean) {
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
            "Deep Storage Scan",
            style = MaterialTheme.typography.headlineMedium,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Find and remove unnecessary junk files",
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
        )
        Spacer(modifier = Modifier.height(48.dp))
        GradientButton(
            text = "ANALYZE STORAGE",
            onClick = onStartScan,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PermissionView(onGrant: () -> Unit, isDark: Boolean) {
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
            "To perform a deep scan and find large files, DeviceCare needs permission to access your storage.",
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
fun ScanningView(progress: Float, path: String, estimatedTime: String, isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(160.dp),
                strokeWidth = 8.dp,
                color = if (isDark) Color.White else MaterialTheme.colorScheme.primary
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                )
                Text(
                    estimatedTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Scanning Storage...",
            style = MaterialTheme.typography.headlineSmall,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            path,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Gray
        )
    }
}

@Composable
fun ResultsView(
    items: List<JunkItem>, 
    onClean: (List<JunkItem>) -> Unit, 
    onToggle: (String) -> Unit,
    onOpenFile: (String) -> Unit,
    isDark: Boolean
) {
    val totalSize = items.filter { it.isSelected }.sumOf { it.size }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    FormatUtils.formatBytes(totalSize),
                    style = MaterialTheme.typography.displayMedium,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                )
                Text("Selected for cleaning", style = MaterialTheme.typography.titleMedium)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items, key = { it.id }) { item ->
                JunkItemRow(item, onToggle, onOpenFile, isDark)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        GradientButton(
            text = "CLEAN SELECTED",
            onClick = { onClean(items) },
            modifier = Modifier.fillMaxWidth(),
            enabled = items.any { it.isSelected }
        )
    }
}

@Composable
fun JunkItemRow(
    item: JunkItem, 
    onToggle: (String) -> Unit,
    onOpenFile: (String) -> Unit,
    isDark: Boolean
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox for selection - explicit interaction
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = { onToggle(item.id) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            
            // Middle section for opening file - separate click target
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenFile(item.path) }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Text(
                    item.name, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (item.type == JunkType.LARGE_FILES) Icons.Default.Description else Icons.Default.SdStorage,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        item.type.name, 
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Size text
            Text(
                FormatUtils.formatBytes(item.size),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 12.dp),
                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CleaningView(isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = if (isDark) Color.White else MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Cleaning Junk...", style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun FinishedView(onBack: () -> Unit, isDark: Boolean) {
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
        Text("System Cleaned!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(48.dp))
        GradientButton(text = "DONE", onClick = onBack, modifier = Modifier.fillMaxWidth())
    }
}
