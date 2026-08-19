package com.malek.devicecare.domain.model

data class LargeFile(
    val id: String,
    val name: String,
    val path: String,
    val size: Long,
    val mimeType: String,
    var isSelected: Boolean = false
)
