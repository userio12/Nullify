package com.nullify.cleaner.domain.mode

import com.nullify.cleaner.domain.model.CleanProgress
import com.nullify.cleaner.domain.model.CleanupPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class BasicMode : ExecutionMode {

    override val name: String = "Basic"
    override val level: ModeLevel = ModeLevel.BASIC
    override val isAvailable: Boolean = true

    override suspend fun analyze(config: CleanConfig): Flow<CleanProgress> = flow {
        emit(CleanProgress(currentItem = "analyzing", currentProgress = 0, totalProgress = 100, isComplete = false))
        emit(CleanProgress(currentItem = "done", currentProgress = 100, totalProgress = 100, isComplete = true))
    }

    override suspend fun execute(plan: CleanupPlan): Flow<CleanProgress> = flow {
        var cleaned = 0L
        plan.items.forEachIndexed { index, item ->
            val success = deletePath(item.path, item.path.isDirectory())
            if (success) cleaned += item.size
            emit(CleanProgress(
                currentItem = item.path,
                currentProgress = index + 1,
                totalProgress = plan.items.size,
                bytesCleaned = cleaned,
                isComplete = index == plan.items.size - 1
            ))
        }
    }

    override suspend fun shell(command: String): Result<String> =
        Result.failure(UnsupportedOperationException("Basic mode cannot execute shell commands"))

    override suspend fun deletePath(path: String, isDirectory: Boolean): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(path)
            if (isDirectory) file.deleteRecursively() else file.delete()
        }.getOrDefault(false)
    }

    override suspend fun listDirectory(path: String): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            File(path).list()?.toList() ?: emptyList()
        }
    }

    override suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        runCatching { File(path).exists() }.getOrDefault(false)
    }

    private fun String.isDirectory(): Boolean = runCatching { File(this).isDirectory }.getOrDefault(false)
}
