package com.nullify.cleaner.domain.repository

import com.nullify.cleaner.domain.model.FileItem
import com.nullify.cleaner.domain.model.StorageInfo
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    suspend fun getStorageInfo(): StorageInfo
    suspend fun getVolumes(): List<String>
    suspend fun walkDirectory(path: String): Flow<FileItem>
    suspend fun getTotalFiles(): Int
    fun observeStorageInfo(): Flow<StorageInfo>
}
