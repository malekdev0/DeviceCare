package com.malek.devicecare.domain.model

data class BatteryInfo(
    val percentage: Int,
    val isCharging: Boolean,
    val temperature: Double,
    val health: String
)
