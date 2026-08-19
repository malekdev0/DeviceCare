package com.malek.devicecare.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.malek.devicecare.DeviceCareApplication
import com.malek.devicecare.data.DeviceRepository
import com.malek.devicecare.domain.model.LargeFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LargeFileState {
    object Idle : LargeFileState()
    object PermissionRequired : LargeFileState()
    object Scanning : LargeFileState()
    data class Results(val files: List<LargeFile>) : LargeFileState()
    object Deleting : LargeFileState()
    object Finished : LargeFileState()
}

class LargeFileViewModel(private val repository: DeviceRepository) : ViewModel() {
    private val _state = MutableStateFlow<LargeFileState>(LargeFileState.Idle)
    val state: StateFlow<LargeFileState> = _state.asStateFlow()

    private val _selectedFiles = MutableStateFlow<Set<String>>(emptySet())
    val selectedFiles: StateFlow<Set<String>> = _selectedFiles.asStateFlow()

    fun startScan() {
        if (!repository.hasStoragePermission()) {
            _state.value = LargeFileState.PermissionRequired
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _state.value = LargeFileState.Scanning
            // Scan can take time, so move to IO thread
            val files = repository.scanForLargeFiles()
            _state.value = LargeFileState.Results(files)
        }
    }

    fun toggleFileSelection(fileId: String) {
        val current = _selectedFiles.value.toMutableSet()
        if (current.contains(fileId)) {
            current.remove(fileId)
        } else {
            current.add(fileId)
        }
        _selectedFiles.value = current
    }

    fun deleteSelectedFiles() {
        val currentState = _state.value
        if (currentState is LargeFileState.Results) {
            viewModelScope.launch(Dispatchers.IO) {
                _state.value = LargeFileState.Deleting
                val toDelete = currentState.files.filter { _selectedFiles.value.contains(it.id) }
                repository.deleteFiles(toDelete)
                delay(1000) // UI feedback
                _state.value = LargeFileState.Finished
            }
        }
    }

    fun reset() {
        _state.value = LargeFileState.Idle
        _selectedFiles.value = emptySet()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DeviceCareApplication)
                LargeFileViewModel(application.deviceRepository)
            }
        }
    }
}
