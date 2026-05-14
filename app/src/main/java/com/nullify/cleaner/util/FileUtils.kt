package com.nullify.cleaner.util

import java.io.File

fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
    bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    else -> "%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
}

fun getFileExtension(fileName: String): String =
    fileName.substringAfterLast('.', "").lowercase()

fun isHiddenFile(file: File): Boolean = file.name.startsWith(".")

fun safeWalkDir(path: String, action: (File) -> Unit) {
    try {
        val dir = File(path)
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.forEach { action(it) }
        }
    } catch (_: SecurityException) {}
}
