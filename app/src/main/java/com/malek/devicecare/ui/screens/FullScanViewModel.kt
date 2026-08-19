package com.malek.devicecare.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.malek.devicecare.DeviceCareApplication
import com.malek.devicecare.data.DeviceRepository
import com.malek.devicecare.data.ScanHistoryRepository
import com.malek.devicecare.domain.model.HealthScanRecord
import com.malek.devicecare.domain.model.HealthStatus
import com.malek.devicecare.domain.model.SecurityInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScanState {
    object Idle : ScanState()
    object ScanningBattery : ScanState()
    object ScanningStorage : ScanState()
    object ScanningMemory : ScanState()
    object ScanningNetwork : ScanState()
    object ScanningSecurity : ScanState()
    object ScanningLargeFiles : ScanState()
    object ScanningEmptyFolders : ScanState()
    data class Completed(
        val batteryStatus: HealthStatus,
        val storageStatus: HealthStatus,
        val memoryStatus: HealthStatus,
        val networkStatus: HealthStatus,
        val securityStatus: HealthStatus,
        val largeFileStatus: HealthStatus,
        val emptyFolderStatus: HealthStatus,
        val score: Int,
        val issues: List<String> = emptyList()
    ) : ScanState()
}

class FullScanViewModel(
    private val repository: DeviceRepository,
    private val historyRepository: ScanHistoryRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ScanState>(ScanState.Idle)
    val state: StateFlow<ScanState> = _state.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    fun startScan() {
        viewModelScope.launch {
            _progress.value = 0f
            val issues = mutableListOf<String>()
            
            _state.value = ScanState.ScanningBattery
            delay(1000)
            _progress.value = 0.2f
            val battery = repository.getBatteryInfo()
            val batteryStatus = if (battery.percentage < 20) {
                issues.add("Battery level is very low (${battery.percentage}%).")
                HealthStatus.WARNING
            } else HealthStatus.GOOD
            
            _state.value = ScanState.ScanningStorage
            delay(1000)
            _progress.value = 0.4f
            val storage = repository.getStorageInfo()
            val storageStatus = if (storage.usagePercentage > 90) {
                issues.add("Storage is almost full (${storage.usagePercentage}%).")
                HealthStatus.CRITICAL
            } else if (storage.usagePercentage > 75) {
                issues.add("Storage usage is high (${storage.usagePercentage}%).")
                HealthStatus.WARNING
            } else HealthStatus.GOOD
            
            _state.value = ScanState.ScanningMemory
            delay(1000)
            _progress.value = 0.6f
            val memory = repository.getMemoryInfo()
            val memoryStatus = if (memory.lowMemory) {
                issues.add("System is running low on memory.")
                HealthStatus.CRITICAL
            } else HealthStatus.GOOD
            
            _state.value = ScanState.ScanningNetwork
            delay(1000)
            _progress.value = 0.8f
            val network = repository.getNetworkInfo()
            val networkStatus = if (network.isConnected) HealthStatus.GOOD else {
                issues.add("Device is not connected to the internet.")
                HealthStatus.WARNING
            }

            _state.value = ScanState.ScanningSecurity
            delay(1000)
            _progress.value = 1f
            val security = repository.scanSecurity()
            val securityStatus = when {
                security.overallScore < 50 -> HealthStatus.CRITICAL
                security.overallScore < 85 -> HealthStatus.WARNING
                else -> HealthStatus.GOOD
            }
            security.risks.forEach { issues.add(it.title + ": " + it.description) }

            _state.value = ScanState.ScanningLargeFiles
            delay(1000)
            _progress.value = 1f
            val largeFileStatus: HealthStatus
            if (repository.hasStoragePermission()) {
                val largeFiles = repository.scanForLargeFiles(100) // Look for files > 100MB
                largeFileStatus = if (largeFiles.isNotEmpty()) {
                    issues.add("Found ${largeFiles.size} large files occupying space.")
                    HealthStatus.WARNING
                } else HealthStatus.GOOD
            } else {
                largeFileStatus = HealthStatus.UNKNOWN
                issues.add("Storage permission required to scan for large files.")
            }

            _state.value = ScanState.ScanningEmptyFolders
            delay(1000)
            _progress.value = 1f
            val emptyFolderStatus: HealthStatus
            if (repository.hasStoragePermission()) {
                val emptyFolders = repository.scanForEmptyFolders()
                emptyFolderStatus = if (emptyFolders.isNotEmpty()) {
                    issues.add("Found ${emptyFolders.size} empty folders that can be removed.")
                    HealthStatus.WARNING
                } else HealthStatus.GOOD
            } else {
                emptyFolderStatus = HealthStatus.UNKNOWN
            }

            val score = calculateScore(batteryStatus, storageStatus, memoryStatus, networkStatus, securityStatus)
            val completed = ScanState.Completed(
                batteryStatus = batteryStatus,
                storageStatus = storageStatus,
                memoryStatus = memoryStatus,
                networkStatus = networkStatus,
                securityStatus = securityStatus,
                largeFileStatus = largeFileStatus,
                emptyFolderStatus = emptyFolderStatus,
                score = score,
                issues = issues
            )
            historyRepository.add(
                HealthScanRecord(
                    timestamp = System.currentTimeMillis(),
                    score = score,
                    batteryStatus = batteryStatus,
                    storageStatus = storageStatus,
                    memoryStatus = memoryStatus,
                    networkStatus = networkStatus,
                    securityStatus = securityStatus
                )
            )
            _state.value = completed
        }
    }

    private fun calculateScore(vararg statuses: HealthStatus): Int {
        var points = 100
        statuses.forEach {
            when (it) {
                HealthStatus.WARNING -> points -= 15
                HealthStatus.CRITICAL -> points -= 30
                else -> {}
            }
        }
        return points.coerceAtLeast(0)
    }

    fun reset() {
        _state.value = ScanState.Idle
        _progress.value = 0f
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DeviceCareApplication)
                FullScanViewModel(application.deviceRepository, application.scanHistoryRepository)
            }
        }
    }
}
