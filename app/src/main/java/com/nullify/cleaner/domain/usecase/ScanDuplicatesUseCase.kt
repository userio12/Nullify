package com.nullify.cleaner.domain.usecase

import com.nullify.cleaner.domain.cleaner.DuplicateCleaner
import com.nullify.cleaner.domain.model.CleanProgress
import com.nullify.cleaner.domain.model.FileItem
import com.nullify.cleaner.domain.mode.ExecutionMode
import kotlinx.coroutines.flow.Flow

class ScanDuplicatesUseCase(
    private val duplicateCleaner: DuplicateCleaner,
    private val executionMode: ExecutionMode
) {
    suspend fun scan(paths: List<String>): Flow<CleanProgress> {
        val files = mutableListOf<FileItem>()
        for (path in paths) {
            if (executionMode.fileExists(path)) {
                val entries = executionMode.listDirectory(path).getOrDefault(emptyList())
                for (entry in entries) {
                    val fullPath = "$path/$entry"
                    val size = executionMode.shell("stat -c%s \"$fullPath\" 2>/dev/null")
                        .getOrDefault("0").toLongOrNull() ?: 0L
                    files.add(FileItem(
                        path = fullPath,
                        name = entry,
                        size = size,
                        isDirectory = false,
                        lastModified = 0L
                    ))
                }
            }
        }
        return duplicateCleaner.scan(files)
    }
}
