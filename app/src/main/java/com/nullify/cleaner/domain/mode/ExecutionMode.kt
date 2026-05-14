package com.nullify.cleaner.domain.mode

import com.nullify.cleaner.domain.model.CleanProgress
import com.nullify.cleaner.domain.model.CleanupPlan
import kotlinx.coroutines.flow.Flow

enum class ModeLevel {
    BASIC, ACCESSIBILITY, SHIZUKU, ROOT
}

interface ExecutionMode {
    val name: String
    val level: ModeLevel
    val isAvailable: Boolean

    suspend fun analyze(config: CleanConfig): Flow<CleanProgress>
    suspend fun execute(plan: CleanupPlan): Flow<CleanProgress>
    suspend fun shell(command: String): Result<String>
    suspend fun deletePath(path: String, isDirectory: Boolean): Boolean
    suspend fun listDirectory(path: String): Result<List<String>>
    suspend fun fileExists(path: String): Boolean
}

data class CleanConfig(
    val toolType: String,
    val paths: List<String> = emptyList(),
    val packageNames: List<String> = emptyList(),
    val excludePatterns: List<String> = emptyList(),
    val minFileSize: Long = 0L,
    val maxFileSize: Long = Long.MAX_VALUE,
    val onlyMedia: Boolean = false,
    val dryRun: Boolean = true
)
