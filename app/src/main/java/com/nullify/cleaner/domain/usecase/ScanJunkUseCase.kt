package com.nullify.cleaner.domain.usecase

import com.nullify.cleaner.domain.cleaner.JunkCleaner
import com.nullify.cleaner.domain.model.CleanProgress
import com.nullify.cleaner.domain.model.CleanupItem
import com.nullify.cleaner.domain.model.CleanupPlan
import kotlinx.coroutines.flow.Flow

class ScanJunkUseCase(
    private val junkCleaner: JunkCleaner
) {
    suspend fun scan(): Flow<CleanProgress> = junkCleaner.scan()

    suspend fun toPlan(items: List<CleanupItem>): CleanupPlan {
        return junkCleaner.toCleanupPlan(items)
    }
}
