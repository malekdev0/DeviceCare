package com.malek.devicecare.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.malek.devicecare.DeviceCareApplication
import com.malek.devicecare.data.DeviceRepository
import com.malek.devicecare.domain.model.BatteryInfo
import com.malek.devicecare.domain.model.UsagePoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BatteryViewModel(private val repository: DeviceRepository) : ViewModel() {
    private val _batteryInfo = MutableStateFlow<BatteryInfo?>(null)
    val batteryInfo: StateFlow<BatteryInfo?> = _batteryInfo.asStateFlow()

    private val _history = MutableStateFlow<List<UsagePoint>>(emptyList())
    val history: StateFlow<List<UsagePoint>> = _history.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                refresh()
                delay(3000) // Update every 3 seconds
            }
        }
    }

    fun refresh() {
        val info = repository.getBatteryInfo()
        _batteryInfo.value = info
        
        val newPoint = UsagePoint(System.currentTimeMillis(), info.percentage.toFloat())
        
        val currentHistory = _history.value.toMutableList()
        // Only add if percentage changed or every 5 mins (but here we just add for demo)
        if (currentHistory.isEmpty() || currentHistory.last().value != info.percentage.toFloat()) {
            currentHistory.add(newPoint)
            if (currentHistory.size > 20) currentHistory.removeAt(0)
            _history.value = currentHistory
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DeviceCareApplication)
                BatteryViewModel(application.deviceRepository)
            }
        }
    }
}
