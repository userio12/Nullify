package com.nullify.cleaner.domain.model

data class StorageInfo(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val appBytes: Long,
    val mediaBytes: Long,
    val cacheBytes: Long,
    val miscBytes: Long
) {
    val usedPercent: Float
        get() = if (totalBytes > 0) usedBytes.toFloat() / totalBytes else 0f

    val freePercent: Float
        get() = if (totalBytes > 0) freeBytes.toFloat() / totalBytes else 0f
}
