package com.nullify.cleaner.data.repository

import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.content.pm.ApplicationInfo
import com.nullify.cleaner.domain.mode.ExecutionMode
import com.nullify.cleaner.domain.model.AppInfo
import com.nullify.cleaner.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

class AppRepositoryImpl(
    private val packageManager: PackageManager,
    private val executionMode: ExecutionMode
) : AppRepository {

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())

    override suspend fun getInstalledApps(includeSystem: Boolean): List<AppInfo> =
        withContext(Dispatchers.IO) {
            val apps = packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(
                PackageManager.GET_META_DATA.toLong()
            )).map { it.toAppInfo() }

            val filtered = if (!includeSystem) apps.filter { !it.isSystemApp } else apps
            _apps.value = filtered
            filtered
        }

    override suspend fun getAppInfo(packageName: String): AppInfo? =
        withContext(Dispatchers.IO) {
            try {
                val pkgInfo = packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                pkgInfo.toAppInfo()
            } catch (_: Exception) { null }
        }

    override suspend fun getOrphanedDirs(): List<String> =
        withContext(Dispatchers.IO) {
            val installed = getInstalledApps(true).map { it.packageName }.toSet()
            val dataDirs = listOf("/data/data", "/data/user/0")
            val orphaned = mutableListOf<String>()

            for (baseDir in dataDirs) {
                val dir = File(baseDir)
                if (dir.exists()) {
                    dir.listFiles()?.forEach { subDir ->
                        if (subDir.name !in installed) {
                            orphaned.add(subDir.absolutePath)
                        }
                    }
                }
            }
            orphaned
        }

    override suspend fun getPackageSize(packageName: String): Triple<Long, Long, Long> =
        withContext(Dispatchers.IO) {
            try {
                val info = packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                val appSize = info.applicationInfo?.let { File(it.sourceDir).length() } ?: 0L
                val dataDir = info.applicationInfo?.let { File(it.dataDir) }
                val dataSize = dataDir?.let { if (it.exists()) it.walkTopDown().filter { it.isFile }.sumOf { it.length() } else 0L } ?: 0L
                val cacheDir = info.applicationInfo?.let { File(it.dataDir, "cache") }
                val cacheSize = cacheDir?.let { if (it.exists()) it.walkTopDown().filter { it.isFile }.sumOf { it.length() } else 0L } ?: 0L
                Triple(appSize, dataSize, cacheSize)
            } catch (_: Exception) { Triple(0, 0, 0) }
        }

    override suspend fun forceStopPackage(packageName: String): Boolean =
        executionMode.shell("am force-stop $packageName").isSuccess

    override suspend fun clearAppCache(packageName: String): Boolean =
        executionMode.shell("pm clear --cache-only $packageName").isSuccess

    override suspend fun clearAppData(packageName: String): Boolean =
        executionMode.shell("pm clear $packageName").isSuccess

    override suspend fun uninstallPackage(packageName: String): Boolean =
        executionMode.shell("pm uninstall $packageName").isSuccess

    override suspend fun disablePackage(packageName: String): Boolean =
        executionMode.shell("pm disable $packageName").isSuccess

    override suspend fun enablePackage(packageName: String): Boolean =
        executionMode.shell("pm enable $packageName").isSuccess

    override fun observeInstalledApps(): Flow<List<AppInfo>> = _apps.asStateFlow()

    private fun PackageInfo.toAppInfo(): AppInfo = AppInfo(
        packageName = packageName,
        appName = applicationInfo?.loadLabel(packageManager)?.toString() ?: packageName,
        versionName = versionName ?: "",
        versionCode = longVersionCode,
        isSystemApp = (applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0,
        isEnabled = applicationInfo?.enabled ?: true,
        installedAt = firstInstallTime,
        dataDir = applicationInfo?.dataDir ?: "",
        sourceDir = applicationInfo?.sourceDir ?: ""
    )
}
