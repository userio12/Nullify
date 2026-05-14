package com.nullify.cleaner.domain.usecase

import com.nullify.cleaner.domain.cleaner.CorpseFinder
import com.nullify.cleaner.domain.model.CleanProgress
import com.nullify.cleaner.domain.model.CleanupItem
import com.nullify.cleaner.domain.model.CleanupPlan
import kotlinx.coroutines.flow.Flow

class ScanCorpsesUseCase(
    private val corpseFinder: CorpseFinder
) {
    suspend fun scan(): Flow<CleanProgress> = corpseFinder.scan()

    fun toPlan(items: List<CleanupItem>): CleanupPlan {
        return corpseFinder.toCleanupPlan(items)
    }
}
