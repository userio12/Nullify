package com.nullify.cleaner.domain.mode

import com.nullify.cleaner.domain.model.CleanProgress
import com.nullify.cleaner.domain.model.CleanupPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class RootMode : ExecutionMode {

    override val name: String = "Root"
    override val level: ModeLevel = ModeLevel.ROOT

    override val isAvailable: Boolean = runCatching {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "echo alive"))
        val result = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        result == "alive"
    }.getOrDefault(false)

    override suspend fun analyze(config: CleanConfig): Flow<CleanProgress> = flow {
        emit(CleanProgress(currentItem = "analyzing", currentProgress = 0, totalProgress = 100, isComplete = false))
        emit(CleanProgress(currentItem = "done", currentProgress = 100, totalProgress = 100, isComplete = true))
    }

    override suspend fun execute(plan: CleanupPlan): Flow<CleanProgress> = flow {
        var cleaned = 0L
        plan.items.forEachIndexed { index, item ->
            val success = deletePath(item.path, false)
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

    override suspend fun shell(command: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val output = process.inputStream.bufferedReader().readText().trim()
            val error = process.errorStream.bufferedReader().readText().trim()
            process.waitFor()
            if (error.isNotEmpty()) throw RuntimeException(error)
            output
        }
    }

    override suspend fun deletePath(path: String, isDirectory: Boolean): Boolean = runCatching {
        val cmd = if (isDirectory) "rm -rf \"$path\"" else "rm -f \"$path\""
        shell(cmd).isSuccess
    }.getOrDefault(false)

    override suspend fun listDirectory(path: String): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            shell("ls -1 \"$path\"").getOrThrow().lines().filter { it.isNotBlank() }
        }
    }

    override suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            shell("test -e \"$path\" && echo true || echo false").getOrThrow() == "true"
        }.getOrDefault(false)
    }
}
