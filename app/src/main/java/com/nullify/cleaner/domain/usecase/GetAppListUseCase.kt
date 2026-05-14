package com.nullify.cleaner.domain.usecase

import com.nullify.cleaner.domain.model.AppInfo
import com.nullify.cleaner.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow

class GetAppListUseCase(
    private val appRepository: AppRepository
) {
    suspend fun getAllApps(includeSystem: Boolean = false): List<AppInfo> {
        return appRepository.getInstalledApps(includeSystem)
    }

    suspend fun getAppDetails(packageName: String): AppInfo? {
        return appRepository.getAppInfo(packageName)
    }

    fun observeApps(): Flow<List<AppInfo>> = appRepository.observeInstalledApps()
}
