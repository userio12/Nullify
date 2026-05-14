package com.nullify.cleaner.domain.usecase

import com.nullify.cleaner.domain.model.CleanProgress
import com.nullify.cleaner.domain.model.CleanupPlan
import com.nullify.cleaner.domain.mode.ExecutionMode
import kotlinx.coroutines.flow.Flow

class ExecuteCleanupUseCase(
    private val executionMode: ExecutionMode
) {
    suspend operator fun invoke(plan: CleanupPlan): Flow<CleanProgress> {
        return executionMode.execute(plan)
    }
}
