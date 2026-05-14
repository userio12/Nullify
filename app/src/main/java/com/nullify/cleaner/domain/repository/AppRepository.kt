package com.nullify.cleaner.domain.repository

import com.nullify.cleaner.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    suspend fun getInstalledApps(includeSystem: Boolean = false): List<AppInfo>
    suspend fun getAppInfo(packageName: String): AppInfo?
    suspend fun getOrphanedDirs(): List<String>
    suspend fun getPackageSize(packageName: String): Triple<Long, Long, Long>
    suspend fun forceStopPackage(packageName: String): Boolean
    suspend fun clearAppCache(packageName: String): Boolean
    suspend fun clearAppData(packageName: String): Boolean
    suspend fun uninstallPackage(packageName: String): Boolean
    suspend fun disablePackage(packageName: String): Boolean
    suspend fun enablePackage(packageName: String): Boolean
    fun observeInstalledApps(): Flow<List<AppInfo>>
}
