package com.malek.devicecare.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.malek.devicecare.DeviceCareApplication
import com.malek.devicecare.data.DeviceRepository
import com.malek.devicecare.domain.model.SecurityInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SecurityState {
    object Idle : SecurityState()
    object Scanning : SecurityState()
    data class Success(val info: SecurityInfo) : SecurityState()
}

class SecurityViewModel(private val repository: DeviceRepository) : ViewModel() {
    private val _state = MutableStateFlow<SecurityState>(SecurityState.Idle)
    val state: StateFlow<SecurityState> = _state.asStateFlow()

    fun startScan() {
        viewModelScope.launch {
            _state.value = SecurityState.Scanning
            delay(2000) // Simulate deep analysis
            _state.value = SecurityState.Success(repository.scanSecurity())
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DeviceCareApplication)
                SecurityViewModel(application.deviceRepository)
            }
        }
    }
}
