package com.nullify.cleaner.domain.repository

import com.nullify.cleaner.domain.model.CleanProgress
import kotlinx.coroutines.flow.Flow

interface CleanupRepository {
    suspend fun executeShellCommand(command: String): Result<String>
    suspend fun executeShellCommands(commands: List<String>): Flow<String>
    suspend fun fileExists(path: String): Boolean
    suspend fun deleteFile(path: String): Boolean
    suspend fun deleteDirectory(path: String): Boolean
    suspend fun getFileSize(path: String): Long
    suspend fun listDirectory(path: String): List<String>
}
