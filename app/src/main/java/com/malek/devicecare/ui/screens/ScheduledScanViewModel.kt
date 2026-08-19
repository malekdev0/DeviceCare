package com.malek.devicecare.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.malek.devicecare.DeviceCareApplication
import com.malek.devicecare.schedule.ScheduledScanManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScheduledScanViewModel(private val context: Context) : ViewModel() {
    private val _enabled = MutableStateFlow(ScheduledScanManager.isEnabled(context))
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    fun setEnabled(enabled: Boolean) {
        ScheduledScanManager.setEnabled(context, enabled)
        _enabled.value = enabled
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DeviceCareApplication
                ScheduledScanViewModel(application)
            }
        }
    }
}
