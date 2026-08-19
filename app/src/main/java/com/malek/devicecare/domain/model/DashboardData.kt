package com.malek.devicecare.domain.model

enum class HealthStatus {
    GOOD,
    WARNING,
    CRITICAL,
    UNKNOWN
}

data class DashboardState(
    val overallStatus: HealthStatus = HealthStatus.UNKNOWN,
    val batteryPercentage: Int = 0,
    val storageUsagePercentage: Int = 0,
    val memoryStatus: String = "Normal",
    val temperatureStatus: String = "Normal",
    val networkStatus: String = "Good",
    val lastScanTimestamp: Long? = null
)
