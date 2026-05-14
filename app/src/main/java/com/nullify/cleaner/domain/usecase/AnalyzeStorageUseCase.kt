package com.nullify.cleaner.domain.usecase

import com.nullify.cleaner.domain.cleaner.StorageAnalyzer
import com.nullify.cleaner.domain.model.FileItem

class AnalyzeStorageUseCase(
    private val storageAnalyzer: StorageAnalyzer
) {
    fun getVolumes(): List<StorageAnalyzer.VolumeInfo> = storageAnalyzer.getVolumes()

    fun getTopFiles(path: String, limit: Int = 100): List<FileItem> = storageAnalyzer.getTopFiles(path, limit)

    fun buildTree(path: String, maxDepth: Int = 4): StorageAnalyzer.StorageNode? =
        storageAnalyzer.buildTree(path, maxDepth)
}
