package com.nullify.cleaner.domain.cleaner

import android.os.StatFs
import com.nullify.cleaner.domain.model.FileCategory
import com.nullify.cleaner.domain.model.FileItem
import java.io.File

class StorageAnalyzer {

    data class StorageNode(
        val name: String,
        val path: String,
        val size: Long,
        val isDirectory: Boolean,
        val fileCategory: FileCategory = FileCategory.OTHER,
        val children: List<StorageNode> = emptyList(),
        val percentage: Float = 0f
    )

    data class VolumeInfo(
        val path: String,
        val label: String,
        val totalBytes: Long,
        val usedBytes: Long,
        val freeBytes: Long,
        val isEmulated: Boolean
    )

    fun getVolumes(): List<VolumeInfo> {
        val volumes = mutableListOf<VolumeInfo>()
        try {
            val defaultPath = File("/storage/emulated/0")
            if (defaultPath.exists()) {
                val stat = StatFs(defaultPath.absolutePath)
                val total = stat.totalBytes
                val free = stat.availableBytes
                volumes.add(VolumeInfo(
                    path = defaultPath.absolutePath,
                    label = "Internal Storage",
                    totalBytes = total,
                    usedBytes = total - free,
                    freeBytes = free,
                    isEmulated = true
                ))
            }
        } catch (_: Exception) {}

        try {
            val extSdCard = File("/storage")
            extSdCard.listFiles()?.forEach { file ->
                if (file.isDirectory && file.name != "emulated" && file.name != "self") {
                    val stat = StatFs(file.absolutePath)
                    val total = stat.totalBytes
                    val free = stat.availableBytes
                    if (total > 0) {
                        volumes.add(VolumeInfo(
                            path = file.absolutePath,
                            label = file.name,
                            totalBytes = total,
                            usedBytes = total - free,
                            freeBytes = free,
                            isEmulated = false
                        ))
                    }
                }
            }
        } catch (_: Exception) {}
        return volumes
    }

    fun buildTree(rootPath: String, maxDepth: Int = 4): StorageNode? = runCatching {
        val root = File(rootPath)
        if (!root.exists()) return@runCatching null

        val children = if (maxDepth > 0) {
            val fileList = root.listFiles()
                ?.filter { !it.name.startsWith(".") }
                ?.mapNotNull { file ->
                    if (file.isDirectory) {
                        buildTree(file.absolutePath, maxDepth - 1)
                    } else {
                        StorageNode(
                            name = file.name,
                            path = file.absolutePath,
                            size = file.length(),
                            isDirectory = false,
                            fileCategory = categorizeFile(file.name)
                        )
                    }
                }
                ?.sortedByDescending { it.size } ?: emptyList()

            val totalSize = fileList.sumOf { it.size }.coerceAtLeast(1)
            fileList.map { node ->
                node.copy(percentage = node.size.toFloat() / totalSize.toFloat())
            }
        } else {
            emptyList()
        }

        val totalSize = children.sumOf { it.size }.coerceAtLeast(root.length())

        StorageNode(
            name = root.name,
            path = root.absolutePath,
            size = totalSize,
            isDirectory = true,
            children = children,
            percentage = 1.0f
        )
    }.getOrNull()

    fun getTopFiles(rootPath: String, limit: Int = 100): List<FileItem> {
        val files = mutableListOf<FileItem>()
        try {
            File(rootPath).walkTopDown()
                .filter { it.isFile && !it.name.startsWith(".") }
                .forEach { file ->
                    files.add(FileItem(
                        path = file.absolutePath,
                        name = file.name,
                        size = file.length(),
                        isDirectory = false,
                        lastModified = file.lastModified(),
                        fileType = categorizeFile(file.name)
                    ))
                }
        } catch (_: Exception) {}
        return files.sortedByDescending { it.size }.take(limit)
    }

    private fun categorizeFile(fileName: String): FileCategory {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "heic", "avif" -> FileCategory.IMAGE
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "3gp" -> FileCategory.VIDEO
            "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "opus" -> FileCategory.AUDIO
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv" -> FileCategory.DOCUMENT
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz" -> FileCategory.ARCHIVE
            "apk" -> FileCategory.APK
            else -> FileCategory.OTHER
        }
    }
}
