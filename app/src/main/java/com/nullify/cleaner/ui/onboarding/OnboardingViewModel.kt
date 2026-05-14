package com.nullify.cleaner.ui.onboarding

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullify.cleaner.data.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

data class OnboardingUiState(
    val currentStep: Int = 0,
    val isRootAvailable: Boolean = false,
    val isShizukuAvailable: Boolean = false,
    val isShizukuGranted: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val hasStoragePermission: Boolean = false,
    val isComplete: Boolean = false
)

class OnboardingViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val shizukuPermissionCode = 100

    fun checkCapabilities(context: Context) {
        viewModelScope.launch {
            val (isRoot, isShizuku, isAccessibility, hasStorage) = withContext(Dispatchers.IO) {
                val root = runCatching {
                    val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "echo alive"))
                    val result = process.inputStream.bufferedReader().readText().trim()
                    process.waitFor()
                    result == "alive"
                }.getOrDefault(false)

                val shizuku = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

                val accessibility = runCatching {
                    val services = Settings.Secure.getString(context.contentResolver,
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
                    services.contains(context.packageName + "/.service.accessibility.CleanerAccessibilityService")
                }.getOrDefault(false)

                val storage = if (Build.VERSION.SDK_INT >= 30) Environment.isExternalStorageManager() else true

                Quad(root, shizuku, accessibility, storage)
            }

            val shizukuGranted = if (isShizuku) {
                runCatching { Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED }.getOrDefault(false)
            } else false

            _uiState.value = _uiState.value.copy(
                isRootAvailable = isRoot,
                isShizukuAvailable = isShizuku,
                isShizukuGranted = shizukuGranted,
                isAccessibilityEnabled = isAccessibility,
                hasStoragePermission = hasStorage
            )
        }
    }

    fun requestStoragePermission(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun requestShizukuPermission() {
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED
            && !Shizuku.shouldShowRequestPermissionRationale()) {
            Shizuku.requestPermission(shizukuPermissionCode)
        }
    }

    fun handleShizukuPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(isShizukuGranted = granted)
    }

    fun openAccessibilitySettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun nextStep() {
        _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep + 1)
    }

    fun skip() {
        _uiState.value = _uiState.value.copy(currentStep = 3)
    }

    fun complete() {
        viewModelScope.launch {
            appPreferences.setFirstLaunchComplete()
            _uiState.value = _uiState.value.copy(isComplete = true)
        }
    }
}

private data class Quad<T1, T2, T3, T4>(val first: T1, val second: T2, val third: T3, val fourth: T4)
