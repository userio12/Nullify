package com.nullify.cleaner.domain.model

data class DuplicateGroup(
    val id: String,
    val fileHash: String,
    val totalBytes: Long,
    val files: List<FileItem>,
    val isExactDuplicate: Boolean = true,
    val similarity: Float = 1.0f
)
