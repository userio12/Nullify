package com.nullify.cleaner.domain.cleaner

import com.nullify.cleaner.domain.model.CleanProgress
import com.nullify.cleaner.domain.model.CleanupItem
import com.nullify.cleaner.domain.model.CleanupPlan
import com.nullify.cleaner.domain.mode.ExecutionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CorpseFinder(private val executionMode: ExecutionMode) {

    private val knownDataPaths = listOf(
        "/data/data",
        "/data/user/0",
        "/data/user_de/0",
        "/sdcard/Android/data",
        "/sdcard/Android/obb"
    )

    private val systemCriticalPackages = setOf(
        "android", "com.android.systemui", "com.android.phone", "com.android.settings",
        "com.android.launcher3", "com.google.android.gms", "com.google.android.gsf",
        "com.android.providers.settings", "com.android.providers.media"
    )

    suspend fun scan(): Flow<CleanProgress> = flow {
        emit(CleanProgress(currentItem = "Scanning for leftover files", currentProgress = 0, totalProgress = 100, bytesFound = 0))

        val installedPackages = getInstalledPackages()
        val orphanedItems = mutableListOf<CleanupItem>()
        var processedDirs = 0

        for (basePath in knownDataPaths) {
            if (!executionMode.fileExists(basePath)) continue

            val dirs = executionMode.listDirectory(basePath).getOrElse { emptyList() }
            for (dir in dirs) {
                if (dir !in installedPackages && dir !in systemCriticalPackages) {
                    val fullPath = "$basePath/$dir"
                    val size = executionMode.shell("du -sb \"$fullPath\" 2>/dev/null | cut -f1")
                        .getOrDefault("0").toLongOrNull() ?: 0L

                    orphanedItems.add(CleanupItem(
                        path = fullPath,
                        size = size,
                        description = "Leftover data: $dir",
                        groupId = dir
                    ))
                }
                processedDirs++
                if (processedDirs % 10 == 0) {
                    emit(CleanProgress(
                        currentItem = "Checking: $dir",
                        currentProgress = processedDirs,
                        totalProgress = 100,
                        bytesFound = orphanedItems.sumOf { it.size },
                        items = orphanedItems.toList()
                    ))
                }
            }
        }

        emit(CleanProgress(
            currentItem = "Scan complete",
            currentProgress = 100,
            totalProgress = 100,
            bytesFound = orphanedItems.sumOf { it.size },
            items = orphanedItems.toList(),
            isComplete = true
        ))
    }

    fun toCleanupPlan(items: List<CleanupItem>): CleanupPlan {
        return CleanupPlan(
            toolType = "corpse_finder",
            items = items,
            totalBytes = items.sumOf { it.size }
        )
    }

    private suspend fun getInstalledPackages(): Set<String> {
        return executionMode.shell("pm list packages 2>/dev/null | sed 's/package://g'")
            .getOrDefault("").lines().filter { it.isNotBlank() }.toSet()
    }
}
