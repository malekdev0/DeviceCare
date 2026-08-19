package com.malek.devicecare.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.malek.devicecare.DeviceCareApplication
import com.malek.devicecare.data.DeviceRepository
import com.malek.devicecare.domain.model.NetworkHistoryItem
import com.malek.devicecare.domain.model.NetworkInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NetworkViewModel(private val repository: DeviceRepository) : ViewModel() {
    private val _networkInfo = MutableStateFlow<NetworkInfo?>(null)
    val networkInfo: StateFlow<NetworkInfo?> = _networkInfo.asStateFlow()

    private val _history = MutableStateFlow<List<NetworkHistoryItem>>(emptyList())
    val history: StateFlow<List<NetworkHistoryItem>> = _history.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                refresh()
                delay(5000) // History points every 5s is enough
            }
        }
    }

    fun refresh() {
        val info = repository.getNetworkInfo()
        _networkInfo.value = info
        
        val newItem = NetworkHistoryItem(
            timestamp = System.currentTimeMillis(),
            type = info.type,
            isConnected = info.isConnected,
            speedMbps = info.downloadSpeed
        )
        
        val currentHistory = _history.value.toMutableList()
        // Only add if something changed or every minute
        if (currentHistory.isEmpty() || currentHistory.last().type != info.type || currentHistory.last().isConnected != info.isConnected) {
            currentHistory.add(newItem)
            if (currentHistory.size > 20) currentHistory.removeAt(0)
            _history.value = currentHistory
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DeviceCareApplication)
                NetworkViewModel(application.deviceRepository)
            }
        }
    }
}
