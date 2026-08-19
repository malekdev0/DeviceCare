package com.malek.devicecare.ui.screens

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.malek.devicecare.DeviceCareApplication
import com.malek.devicecare.data.DeviceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CpuUiState(
    val cores: Int = 0,
    val architecture: String = "",
    val hardware: String = "",
    val model: String = "",
    val temperature: Double = 0.0,
    val frequencyRatio: Float = 0.5f // Simulated load/freq ratio
)

class CpuRoomViewModel(private val repository: DeviceRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CpuUiState())
    val uiState: StateFlow<CpuUiState> = _uiState.asStateFlow()

    init {
        loadStaticInfo()
        startLiveUpdates()
    }

    private fun loadStaticInfo() {
        val cores = Runtime.getRuntime().availableProcessors()
        val arch = System.getProperty("os.arch") ?: "Unknown"
        val hardware = Build.HARDWARE
        val model = Build.MODEL
        
        _uiState.value = _uiState.value.copy(
            cores = cores,
            architecture = arch,
            hardware = hardware,
            model = model
        )
    }

    private fun startLiveUpdates() {
        viewModelScope.launch {
            while (true) {
                val batteryInfo = repository.getBatteryInfo()
                // Using battery temp as a proxy for system temp
                _uiState.value = _uiState.value.copy(
                    temperature = batteryInfo.temperature,
                    frequencyRatio = (0.3f + (0.7f * Math.random())).toFloat() // Randomly pulsing for visual effect
                )
                delay(2000)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DeviceCareApplication)
                CpuRoomViewModel(application.deviceRepository)
            }
        }
    }
}
