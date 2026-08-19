package com.malek.devicecare.domain.model

enum class JunkType {
    CACHE, TEMP, LOGS, LARGE_FILES, DUPLICATES, EMPTY_FOLDERS
}

data class JunkItem(
    val id: String,
    val name: String,
    val path: String,
    val size: Long,
    val type: JunkType,
    var isSelected: Boolean = true
)
