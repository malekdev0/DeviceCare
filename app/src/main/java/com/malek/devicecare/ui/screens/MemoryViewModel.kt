package com.malek.devicecare.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.malek.devicecare.DeviceCareApplication
import com.malek.devicecare.data.DeviceRepository
import com.malek.devicecare.domain.model.MemoryInfo
import com.malek.devicecare.domain.model.UsagePoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MemoryViewModel(private val repository: DeviceRepository) : ViewModel() {
    private val _memoryInfo = MutableStateFlow<MemoryInfo?>(null)
    val memoryInfo: StateFlow<MemoryInfo?> = _memoryInfo.asStateFlow()

    private val _history = MutableStateFlow<List<UsagePoint>>(emptyList())
    val history: StateFlow<List<UsagePoint>> = _history.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                refresh()
                delay(1000) // Update every 1 second
            }
        }
    }

    fun refresh() {
        val info = repository.getMemoryInfo()
        _memoryInfo.value = info
        
        val usagePct = ((info.totalMemory - info.availableMemory).toFloat() / info.totalMemory.toFloat()) * 100f
        val newPoint = UsagePoint(System.currentTimeMillis(), usagePct)
        
        val currentHistory = _history.value.toMutableList()
        currentHistory.add(newPoint)
        if (currentHistory.size > 20) currentHistory.removeAt(0) // Keep last 20 points
        _history.value = currentHistory
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DeviceCareApplication)
                MemoryViewModel(application.deviceRepository)
            }
        }
    }
}
