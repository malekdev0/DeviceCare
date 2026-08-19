package com.malek.devicecare.domain.model

data class MemoryInfo(
    val totalMemory: Long,
    val availableMemory: Long,
    val lowMemory: Boolean,
    val threshold: Long
)
