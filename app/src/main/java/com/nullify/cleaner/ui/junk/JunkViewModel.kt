package com.nullify.cleaner.ui.junk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullify.cleaner.domain.model.CleanupItem
import com.nullify.cleaner.domain.usecase.ExecuteCleanupUseCase
import com.nullify.cleaner.domain.usecase.ScanJunkUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JunkCategory(
    val name: String,
    val items: List<CleanupItem>,
    val totalBytes: Long,
    val isSelected: Boolean = true
)

data class JunkUiState(
    val categories: List<JunkCategory> = emptyList(),
    val allItems: List<CleanupItem> = emptyList(),
    val isScanning: Boolean = false,
    val scannedBytes: Long = 0L,
    val isCleaning: Boolean = false,
    val cleaningProgress: Int = 0,
    val cleaningTotal: Int = 0,
    val cleaningBytes: Long = 0L,
    val error: String? = null
)

class JunkViewModel(
    private val scanJunkUseCase: ScanJunkUseCase,
    private val executeCleanupUseCase: ExecuteCleanupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JunkUiState())
    val uiState: StateFlow<JunkUiState> = _uiState.asStateFlow()

    fun startScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, error = null)
            try {
                scanJunkUseCase.scan().collect { progress ->
                    val categories = progress.items.groupBy { it.groupId ?: "system" }
                        .map { (key, items) ->
                            JunkCategory(
                                name = key,
                                items = items,
                                totalBytes = items.sumOf { it.size },
                                isSelected = true
                            )
                        }
                    _uiState.value = _uiState.value.copy(
                        allItems = progress.items,
                        categories = categories,
                        isScanning = !progress.isComplete,
                        scannedBytes = progress.bytesFound
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isScanning = false, error = e.message)
            }
        }
    }

    fun cleanSelected() {
        viewModelScope.launch {
            val selected = _uiState.value.categories
                .filter { it.isSelected }
                .flatMap { it.items }

            if (selected.isEmpty()) return@launch
            val plan = scanJunkUseCase.toPlan(selected)
            _uiState.value = _uiState.value.copy(isCleaning = true)
            try {
                executeCleanupUseCase(plan).collect { progress ->
                    _uiState.value = _uiState.value.copy(
                        cleaningProgress = progress.currentProgress,
                        cleaningTotal = progress.totalProgress,
                        cleaningBytes = progress.bytesCleaned
                    )
                }
                _uiState.value = _uiState.value.copy(
                    isCleaning = false,
                    allItems = emptyList(),
                    categories = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCleaning = false, error = e.message)
            }
        }
    }

    fun toggleCategory(index: Int) {
        val cats = _uiState.value.categories.toMutableList()
        cats[index] = cats[index].copy(isSelected = !cats[index].isSelected)
        _uiState.value = _uiState.value.copy(categories = cats)
    }
}
