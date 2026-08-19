package com.malek.devicecare.domain.model

data class StorageInfo(
    val totalSpace: Long,
    val usedSpace: Long,
    val availableSpace: Long,
    val usagePercentage: Int
)
