package com.malek.devicecare

import android.app.Application
import com.malek.devicecare.data.DeviceRepository
import com.malek.devicecare.data.ScanHistoryRepository

class DeviceCareApplication : Application() {
    val deviceRepository by lazy { DeviceRepository(this) }
    val scanHistoryRepository by lazy { ScanHistoryRepository(this) }
}
