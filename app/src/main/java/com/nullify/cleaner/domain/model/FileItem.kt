package com.nullify.cleaner.domain.model

data class FileItem(
    val path: String,
    val name: String,
    val size: Long,
    val isDirectory: Boolean,
    val lastModified: Long,
    val fileType: FileCategory = FileCategory.OTHER
)

enum class FileCategory {
    IMAGE, VIDEO, AUDIO, DOCUMENT, ARCHIVE, APK, CACHE, THUMBNAIL, OTHER
}
