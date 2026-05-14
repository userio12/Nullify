package com.nullify.cleaner.domain.cleaner

import com.nullify.cleaner.domain.model.CleanProgress
import com.nullify.cleaner.domain.model.CleanupItem
import com.nullify.cleaner.domain.model.CleanupPlan
import com.nullify.cleaner.domain.mode.ExecutionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class JunkCleaner(private val executionMode: ExecutionMode) {

    private val cacheDirPatterns = listOf(
        "cache", "code_cache", "app_webview", "app_cache",
        ".thumbnails", "thumbnails", "temp", "tmp"
    )

    private val systemCachePaths = listOf(
        "/data/local/tmp"
    )

    private val systemCacheDir = "/cache"

    private val systemCriticalPackages = setOf(
        "android", "com.android.systemui", "com.android.phone", "com.android.settings",
        "com.android.launcher3", "com.google.android.gms", "com.android.providers.media"
    )

    suspend fun scan(): Flow<CleanProgress> = flow {
        emit(CleanProgress(currentItem = "Scanning for junk files", currentProgress = 0, totalProgress = 100, bytesFound = 0))

        val junkItems = mutableListOf<CleanupItem>()
        val packages = executionMode.shell("pm list packages 2>/dev/null | sed 's/package://g'")
            .getOrDefault("").lines().filter { it.isNotBlank() }
        var processed = 0

        for (pkg in packages) {
            if (pkg in systemCriticalPackages) continue
            val dataDir = "/data/data/$pkg"
            if (!executionMode.fileExists(dataDir)) continue

            for (pattern in cacheDirPatterns) {
                val cachePath = "$dataDir/$pattern"
                if (executionMode.fileExists(cachePath)) {
                    val size = executionMode.shell("du -sb \"$cachePath\" 2>/dev/null | cut -f1")
                        .getOrDefault("0").toLongOrNull() ?: 0L
                    if (size > 0) {
                        junkItems.add(CleanupItem(
                            path = cachePath,
                            size = size,
                            description = "Cache: $pkg/$pattern",
                            groupId = pkg
                        ))
                    }
                }
            }

            processed++
            if (processed % 20 == 0) {
                emit(CleanProgress(
                    currentItem = "Checking: $pkg",
                    currentProgress = processed,
                    totalProgress = packages.size + systemCachePaths.size,
                    bytesFound = junkItems.sumOf { it.size },
                    items = junkItems.toList()
                ))
            }
        }

        val allSystemPaths = systemCachePaths + systemCacheDir
        for (sysPath in allSystemPaths) {
            if (executionMode.fileExists(sysPath)) {
                val size = executionMode.shell("du -sb \"$sysPath\" 2>/dev/null | cut -f1")
                    .getOrDefault("0").toLongOrNull() ?: 0L
                if (size > 0) {
                    junkItems.add(CleanupItem(
                        path = sysPath,
                        size = size,
                        description = "System cache: $sysPath"
                    ))
                }
            }
        }

        emit(CleanProgress(
            currentItem = "Scan complete",
            currentProgress = 100,
            totalProgress = 100,
            bytesFound = junkItems.sumOf { it.size },
            items = junkItems.toList(),
            isComplete = true
        ))
    }

    fun toCleanupPlan(items: List<CleanupItem>): CleanupPlan {
        return CleanupPlan(
            toolType = "junk_cleaner",
            items = items,
            totalBytes = items.sumOf { it.size }
        )
    }
}
