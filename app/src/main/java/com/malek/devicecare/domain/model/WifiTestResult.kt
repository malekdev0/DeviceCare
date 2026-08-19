package com.malek.devicecare.domain.model

data class WifiTestResult(
    val downloadSpeedMbps: Double = 0.0,
    val uploadSpeedMbps: Double = 0.0,
    val pingMs: Long = 0,
    val isWifi: Boolean = false,
    val ssid: String? = null
)
