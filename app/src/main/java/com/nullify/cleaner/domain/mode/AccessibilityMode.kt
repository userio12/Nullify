package com.nullify.cleaner.domain.mode

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.nullify.cleaner.domain.model.CleanProgress
import com.nullify.cleaner.domain.model.CleanupPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AccessibilityMode(private val context: Context) : ExecutionMode {

    override val name: String = "Accessibility"
    override val level: ModeLevel = ModeLevel.ACCESSIBILITY
    override val isAvailable: Boolean = isAccessibilityServiceEnabled()

    override suspend fun analyze(config: CleanConfig): Flow<CleanProgress> = flow {
        emit(CleanProgress(currentItem = "analyzing", currentProgress = 0, totalProgress = 100, isComplete = false))
        emit(CleanProgress(currentItem = "done", currentProgress = 100, totalProgress = 100, isComplete = true))
    }

    override suspend fun execute(plan: CleanupPlan): Flow<CleanProgress> = flow {
        plan.items.forEachIndexed { index, item ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${item.groupId}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            emit(CleanProgress(
                currentItem = item.path,
                currentProgress = index + 1,
                totalProgress = plan.items.size,
                isComplete = index == plan.items.size - 1
            ))
        }
    }

    override suspend fun shell(command: String): Result<String> =
        Result.failure(UnsupportedOperationException("Accessibility mode cannot execute shell commands"))

    override suspend fun deletePath(path: String, isDirectory: Boolean): Boolean = false

    override suspend fun listDirectory(path: String): Result<List<String>> =
        Result.failure(UnsupportedOperationException("Accessibility mode cannot list directories"))

    override suspend fun fileExists(path: String): Boolean = false

    private fun isAccessibilityServiceEnabled(): Boolean = runCatching {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return@runCatching false
        enabledServices.contains(context.packageName + "/.service.accessibility.CleanerAccessibilityService")
    }.getOrDefault(false)
}
