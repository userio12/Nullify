package com.nullify.cleaner.domain.model

data class AppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val isEnabled: Boolean,
    val installedAt: Long,
    val dataDir: String,
    val sourceDir: String,
    val appSize: Long = 0L,
    val dataSize: Long = 0L,
    val cacheSize: Long = 0L,
    val totalSize: Long = 0L
)
