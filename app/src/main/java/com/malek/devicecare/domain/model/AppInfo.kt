package com.malek.devicecare.domain.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val installTime: Long,
    val updateTime: Long
)
