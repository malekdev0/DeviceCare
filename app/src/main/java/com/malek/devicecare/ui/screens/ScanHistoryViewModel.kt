package com.malek.devicecare.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.malek.devicecare.DeviceCareApplication
import com.malek.devicecare.data.ScanHistoryRepository
import com.malek.devicecare.domain.model.HealthScanRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScanHistoryViewModel(private val repository: ScanHistoryRepository) : ViewModel() {
    private val _records = MutableStateFlow(repository.getRecords())
    val records: StateFlow<List<HealthScanRecord>> = _records.asStateFlow()

    fun refresh() {
        _records.value = repository.getRecords()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DeviceCareApplication
                ScanHistoryViewModel(application.scanHistoryRepository)
            }
        }
    }
}
