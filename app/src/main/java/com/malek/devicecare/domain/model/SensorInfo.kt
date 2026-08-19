package com.malek.devicecare.domain.model

data class SensorInfo(
    val name: String,
    val vendor: String,
    val version: Int,
    val type: Int,
    val power: Float,
    val resolution: Float,
    val maximumRange: Float
)
