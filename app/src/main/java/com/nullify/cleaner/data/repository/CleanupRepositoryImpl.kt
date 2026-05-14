package com.nullify.cleaner.data.repository

import com.nullify.cleaner.domain.repository.CleanupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class CleanupRepositoryImpl : CleanupRepository {

    override suspend fun executeShellCommand(command: String): Result<String> = runCatching {
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
        val output = process.inputStream.bufferedReader().readText().trim()
        val error = process.errorStream.bufferedReader().readText().trim()
        process.waitFor()
        if (error.isNotEmpty() && output.isEmpty()) throw RuntimeException(error)
        output
    }

    override suspend fun executeShellCommands(commands: List<String>): Flow<String> = flow {
        for (cmd in commands) {
            val result = executeShellCommand(cmd)
            emit(result.getOrElse { "Error: $it" })
        }
    }

    override suspend fun fileExists(path: String): Boolean = File(path).exists()

    override suspend fun deleteFile(path: String): Boolean = runCatching {
        File(path).delete()
    }.getOrDefault(false)

    override suspend fun deleteDirectory(path: String): Boolean = runCatching {
        File(path).deleteRecursively()
    }.getOrDefault(false)

    override suspend fun getFileSize(path: String): Long = runCatching {
        val file = File(path)
        if (file.isFile) file.length() else if (file.isDirectory) file.walkTopDown().filter { it.isFile }.sumOf { it.length() } else 0L
    }.getOrDefault(0L)

    override suspend fun listDirectory(path: String): List<String> = runCatching {
        File(path).list()?.toList() ?: emptyList()
    }.getOrDefault(emptyList())
}
