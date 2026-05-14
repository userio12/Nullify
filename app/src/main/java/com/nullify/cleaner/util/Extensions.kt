package com.nullify.cleaner.util

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.formatTimestamp(pattern: String = "MMM dd HH:mm"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))
}

fun File.safeSize(): Long = if (isFile) length() else try {
    walkTopDown().filter { it.isFile }.sumOf { it.length() }
} catch (_: Exception) { 0L }

fun File.isHiddenFile(): Boolean = name.startsWith(".")

fun String.isApkFile(): Boolean = lowercase().endsWith(".apk")

fun String.isImageFile(): Boolean = lowercase().let {
    it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") ||
    it.endsWith(".gif") || it.endsWith(".webp") || it.endsWith(".bmp")
}

fun String.isVideoFile(): Boolean = lowercase().let {
    it.endsWith(".mp4") || it.endsWith(".mkv") || it.endsWith(".avi") ||
    it.endsWith(".mov") || it.endsWith(".wmv") || it.endsWith(".webm")
}

fun String.isAudioFile(): Boolean = lowercase().let {
    it.endsWith(".mp3") || it.endsWith(".wav") || it.endsWith(".flac") ||
    it.endsWith(".aac") || it.endsWith(".ogg") || it.endsWith(".m4a")
}
