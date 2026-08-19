package com.malek.devicecare.domain.model

enum class RiskLevel {
    SAFE, LOW, MEDIUM, HIGH, CRITICAL
}

data class SecurityRisk(
    val id: String,
    val title: String,
    val description: String,
    val level: RiskLevel,
    val affectedApp: String? = null
)

data class SecurityInfo(
    val isRooted: Boolean,
    val isAdbEnabled: Boolean,
    val isDeviceEncrypted: Boolean,
    val risks: List<SecurityRisk>,
    val overallScore: Int
)
