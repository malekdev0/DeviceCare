package com.malek.devicecare.domain.model

data class UsagePoint(
    val timestamp: Long,
    val value: Float
)

data class NetworkHistoryItem(
    val timestamp: Long,
    val type: String,
    val isConnected: Boolean,
    val signalStrength: Int? = null, // 0-4 or similar
    val speedMbps: Int? = null
)

data class HealthScanRecord(
    val timestamp: Long,
    val score: Int,
    val batteryStatus: HealthStatus,
    val storageStatus: HealthStatus,
    val memoryStatus: HealthStatus,
    val networkStatus: HealthStatus,
    val securityStatus: HealthStatus
)
