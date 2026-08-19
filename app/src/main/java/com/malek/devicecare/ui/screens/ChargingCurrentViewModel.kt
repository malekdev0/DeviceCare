package com.malek.devicecare.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.malek.devicecare.DeviceCareApplication
import com.malek.devicecare.data.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ChargingCurrentState {
    object Idle : ChargingCurrentState()
    object Measuring : ChargingCurrentState()
    object NotCharging : ChargingCurrentState()
    object Unavailable : ChargingCurrentState()
    data class Result(val milliamps: Long) : ChargingCurrentState()
}

class ChargingCurrentViewModel(private val repository: DeviceRepository) : ViewModel() {
    private val _state = MutableStateFlow<ChargingCurrentState>(ChargingCurrentState.Idle)
    val state: StateFlow<ChargingCurrentState> = _state.asStateFlow()

    init {
        startScanning()
    }

    private fun startScanning() {
        viewModelScope.launch {
            while (isActive) {
                _state.value = ChargingCurrentState.Measuring
                val result = withContext(Dispatchers.IO) {
                    if (!repository.getBatteryInfo().isCharging) {
                        ChargingCurrentState.NotCharging
                    } else {
                        repository.getChargingCurrentMa()?.let(ChargingCurrentState::Result)
                            ?: ChargingCurrentState.Unavailable
                    }
                }
                _state.value = result
                delay(1_000)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DeviceCareApplication
                ChargingCurrentViewModel(application.deviceRepository)
            }
        }
    }
}
