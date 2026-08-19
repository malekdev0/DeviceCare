package com.malek.devicecare.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.malek.devicecare.DeviceCareApplication
import com.malek.devicecare.data.DeviceRepository
import com.malek.devicecare.domain.model.JunkItem
import com.malek.devicecare.domain.model.JunkType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class CleanerState {
    object Idle : CleanerState()
    object PermissionRequired : CleanerState()
    object Scanning : CleanerState()
    data class Results(val items: List<JunkItem>) : CleanerState()
    object Cleaning : CleanerState()
    object Finished : CleanerState()
}

class StorageCleanerViewModel(private val repository: DeviceRepository) : ViewModel() {
    private val _state = MutableStateFlow<CleanerState>(CleanerState.Idle)
    val state: StateFlow<CleanerState> = _state.asStateFlow()

    private val _scanningProgress = MutableStateFlow(0f)
    val scanningProgress: StateFlow<Float> = _scanningProgress.asStateFlow()

    private val _currentPath = MutableStateFlow("")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _estimatedTimeRemaining = MutableStateFlow("")
    val estimatedTimeRemaining: StateFlow<String> = _estimatedTimeRemaining.asStateFlow()

    fun startScan() {
        if (!repository.hasStoragePermission()) {
            _state.value = CleanerState.PermissionRequired
            return
        }

        viewModelScope.launch {
            _state.value = CleanerState.Scanning
            val paths = listOf(
                "/data/user/0/cache",
                "/storage/emulated/0/Downloads",
                "/system/temp",
                "/data/local/tmp",
                "/storage/emulated/0/Android/data",
                "/storage/emulated/0/DCIM/Camera",
                "/storage/emulated/0/WhatsApp/Media"
            )
            
            // Realistic scan simulation with variable delays
            for (i in 1..100) {
                val progress = i / 100f
                _scanningProgress.value = progress
                
                // Update path
                val randomPath = paths[Random.nextInt(paths.size)]
                _currentPath.value = "$randomPath/file_${Random.nextInt(1000)}.tmp"
                
                // Estimate remaining time
                val remainingSeconds = ((100 - i) * 50) / 1000
                _estimatedTimeRemaining.value = if (remainingSeconds > 0) "~${remainingSeconds}s remaining" else "Finishing..."
                
                // Dynamic delay: some parts are fast (small files), some have big jumps (heavy folders)
                val delayTime = when {
                    i % 12 == 0 -> Random.nextLong(600, 1200) // Big pause for heavy directory
                    i % 5 == 0 -> Random.nextLong(100, 300)   // Short pause
                    else -> Random.nextLong(5, 15)            // Fast burst through small files
                }
                delay(delayTime)
            }
            
            delay(500) // Brief pause at 100%
            
            // Get combined junk and large files
            val junk = repository.scanForJunk().map { it.copy(isSelected = true) }.toMutableList()
            val largeFiles = repository.scanForLargeFiles(minSizeMb = 100).map { file ->
                JunkItem(
                    id = file.id,
                    name = file.name,
                    path = file.path,
                    size = file.size,
                    type = JunkType.LARGE_FILES,
                    isSelected = false
                )
            }
            junk.addAll(largeFiles)
            
            _state.value = CleanerState.Results(junk)
        }
    }

    fun toggleItemSelection(itemId: String) {
        val currentState = _state.value
        if (currentState is CleanerState.Results) {
            val updatedItems = currentState.items.map {
                if (it.id == itemId) it.copy(isSelected = !it.isSelected) else it
            }
            _state.value = CleanerState.Results(updatedItems)
        }
    }

    fun cleanJunk(items: List<JunkItem>) {
        val selectedItems = items.filter { it.isSelected }
        if (selectedItems.isEmpty()) return

        viewModelScope.launch {
            _state.value = CleanerState.Cleaning
            delay(2000) // Simulate cleaning
            repository.deleteJunk(selectedItems)
            _state.value = CleanerState.Finished
        }
    }

    fun reset() {
        _state.value = CleanerState.Idle
        _scanningProgress.value = 0f
        _currentPath.value = ""
    }
    
    fun onResume() {
        if (_state.value is CleanerState.PermissionRequired && repository.hasStoragePermission()) {
            startScan()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DeviceCareApplication)
                StorageCleanerViewModel(application.deviceRepository)
            }
        }
    }
}
