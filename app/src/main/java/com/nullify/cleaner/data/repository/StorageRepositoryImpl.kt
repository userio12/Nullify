package com.nullify.cleaner.data.repository

import android.os.StatFs
import android.os.storage.StorageManager
import android.content.Context
import com.nullify.cleaner.domain.model.FileCategory
import com.nullify.cleaner.domain.model.FileItem
import com.nullify.cleaner.domain.model.StorageInfo
import com.nullify.cleaner.domain.repository.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

class StorageRepositoryImpl(
    private val storageManager: StorageManager,
    private val context: Context
) : StorageRepository {

    private val _storageInfo = MutableStateFlow(StorageInfo(0, 0, 0, 0, 0, 0, 0))

    override suspend fun getStorageInfo(): StorageInfo = withContext(Dispatchers.IO) {
        val defaultPath = "/storage/emulated/0"
        val stat = try { StatFs(defaultPath) } catch (_: Exception) { null }

        val total = stat?.totalBytes ?: 0L
        val free = stat?.availableBytes ?: 0L
        val used = total - free

        val appDir = context.filesDir.parentFile
        val appSize = appDir?.totalSpace ?: 0L
        val cacheSize = getCacheSize()

        StorageInfo(
            totalBytes = total,
            usedBytes = used,
            freeBytes = free,
            appBytes = appSize,
            mediaBytes = getMediaSize(),
            cacheBytes = cacheSize,
            miscBytes = (used - appSize - getMediaSize() - cacheSize).coerceAtLeast(0)
        ).also { _storageInfo.value = it }
    }

    override suspend fun getVolumes(): List<String> = withContext(Dispatchers.IO) {
        try {
            storageManager.storageVolumes!!.mapNotNull { it.getDirectory()?.absolutePath }
        } catch (_: Exception) {
            listOf("/storage/emulated/0")
        }
    }

    override suspend fun walkDirectory(path: String): Flow<FileItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<FileItem>()
        try {
            File(path).walkTopDown().forEach { file ->
                items.add(FileItem(
                    path = file.absolutePath,
                    name = file.name,
                    size = if (file.isFile) file.length() else 0L,
                    isDirectory = file.isDirectory,
                    lastModified = file.lastModified(),
                    fileType = categorizeFile(file.name)
                ))
            }
        } catch (_: Exception) {}
        kotlinx.coroutines.flow.flow {
            items.forEach { emit(it) }
        }
    }

    override suspend fun getTotalFiles(): Int = withContext(Dispatchers.IO) {
        try {
            File("/storage/emulated/0").walkTopDown().count { it.isFile }
        } catch (_: Exception) { 0 }
    }

    override fun observeStorageInfo(): Flow<StorageInfo> = _storageInfo.asStateFlow()

    private fun getCacheSize(): Long = try {
        val cacheDir = File("/storage/emulated/0/Android/data")
        if (cacheDir.exists()) cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        else 0L
    } catch (_: Exception) { 0L }

    private fun getMediaSize(): Long = try {
        val dcim = File("/storage/emulated/0/DCIM")
        val pictures = File("/storage/emulated/0/Pictures")
        val downloads = File("/storage/emulated/0/Download")
        (listOf(dcim, pictures, downloads)).sumOf { dir ->
            if (dir.exists()) dir.walkTopDown().filter { it.isFile }.sumOf { it.length() } else 0L
        }
    } catch (_: Exception) { 0L }

    private fun categorizeFile(fileName: String): FileCategory {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "heic" -> FileCategory.IMAGE
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm" -> FileCategory.VIDEO
            "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a" -> FileCategory.AUDIO
            "pdf", "doc", "docx", "xls", "xlsx", "txt", "csv" -> FileCategory.DOCUMENT
            "zip", "rar", "7z", "tar", "gz" -> FileCategory.ARCHIVE
            "apk" -> FileCategory.APK
            else -> FileCategory.OTHER
        }
    }
}
