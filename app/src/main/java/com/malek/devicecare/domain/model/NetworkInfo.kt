package com.malek.devicecare.domain.model

data class NetworkInfo(
    val isConnected: Boolean,
    val type: String,
    val isWifi: Boolean,
    val isCellular: Boolean,
    val downloadSpeed: Int = 0, // Mbps
    val uploadSpeed: Int = 0    // Mbps
)
