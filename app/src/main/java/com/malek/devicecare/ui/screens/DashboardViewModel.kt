package com.malek.devicecare.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.malek.devicecare.DeviceCareApplication
import com.malek.devicecare.data.DeviceRepository
import com.malek.devicecare.data.ScanHistoryRepository
import com.malek.devicecare.domain.model.DashboardState
import com.malek.devicecare.domain.model.HealthStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: DeviceRepository,
    private val historyRepository: ScanHistoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                refreshStatus()
                delay(2000) // Update every 2 seconds
            }
        }
    }
    
    fun refreshStatus() {
        val batteryInfo = repository.getBatteryInfo()
        val storageInfo = repository.getStorageInfo()
        val memoryInfo = repository.getMemoryInfo()
        val networkInfo = repository.getNetworkInfo()
        val lastScanTimestamp = historyRepository.getRecords().firstOrNull()?.timestamp
        
        // Dynamic health status calculation
        val overallStatus = when {
            batteryInfo.percentage < 15 || memoryInfo.lowMemory || storageInfo.usagePercentage > 95 -> HealthStatus.CRITICAL
            batteryInfo.percentage < 30 || storageInfo.usagePercentage > 85 -> HealthStatus.WARNING
            else -> HealthStatus.GOOD
        }
        
        _uiState.value = DashboardState(
            overallStatus = overallStatus,
            batteryPercentage = batteryInfo.percentage,
            storageUsagePercentage = storageInfo.usagePercentage,
            memoryStatus = if (memoryInfo.lowMemory) "Low" else "Normal",
            temperatureStatus = "${batteryInfo.temperature}°C",
            networkStatus = if (networkInfo.isConnected) networkInfo.type else "Disconnected",
            lastScanTimestamp = lastScanTimestamp
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DeviceCareApplication)
                DashboardViewModel(application.deviceRepository, application.scanHistoryRepository)
            }
        }
    }
}
