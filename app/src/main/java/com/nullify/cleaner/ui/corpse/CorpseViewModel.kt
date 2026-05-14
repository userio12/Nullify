package com.nullify.cleaner.ui.corpse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullify.cleaner.domain.model.CleanupItem
import com.nullify.cleaner.domain.usecase.ExecuteCleanupUseCase
import com.nullify.cleaner.domain.usecase.ScanCorpsesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CorpseUiState(
    val items: List<CleanupItem> = emptyList(),
    val selectedItems: Set<String> = emptySet(),
    val isScanning: Boolean = false,
    val scannedBytes: Long = 0L,
    val isCleaning: Boolean = false,
    val cleaningProgress: Int = 0,
    val cleaningTotal: Int = 0,
    val cleaningBytes: Long = 0L,
    val cleanedBytes: Long = 0L,
    val error: String? = null
)

class CorpseViewModel(
    private val scanCorpsesUseCase: ScanCorpsesUseCase,
    private val executeCleanupUseCase: ExecuteCleanupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CorpseUiState())
    val uiState: StateFlow<CorpseUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    fun startScan() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, error = null, items = emptyList(), cleanedBytes = 0L)
            try {
                scanCorpsesUseCase.scan().collect { progress ->
                    if (progress.isComplete) {
                        _uiState.value = _uiState.value.copy(
                            items = progress.items,
                            isScanning = false,
                            scannedBytes = progress.bytesFound
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            items = progress.items,
                            scannedBytes = progress.bytesFound
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isScanning = false, error = e.message)
            }
        }
    }

    fun toggleItem(path: String) {
        val current = _uiState.value.selectedItems.toMutableSet()
        if (path in current) current.remove(path) else current.add(path)
        _uiState.value = _uiState.value.copy(selectedItems = current)
    }

    fun selectAll() {
        _uiState.value = _uiState.value.copy(
            selectedItems = _uiState.value.items.map { it.path }.toSet()
        )
    }

    fun deselectAll() {
        _uiState.value = _uiState.value.copy(selectedItems = emptySet())
    }

    fun cleanSelected() {
        viewModelScope.launch {
            val selected = _uiState.value.items.filter { it.path in _uiState.value.selectedItems }
            if (selected.isEmpty()) return@launch

            val plan = scanCorpsesUseCase.toPlan(selected)
            _uiState.value = _uiState.value.copy(isCleaning = true, cleanedBytes = 0L)
            try {
                val cleanBytes = selected.sumOf { it.size }
                executeCleanupUseCase(plan).collect { progress ->
                    _uiState.value = _uiState.value.copy(
                        cleaningProgress = progress.currentProgress,
                        cleaningTotal = progress.totalProgress,
                        cleaningBytes = progress.bytesCleaned
                    )
                }
                _uiState.value = _uiState.value.copy(
                    items = emptyList(), selectedItems = emptySet(), isCleaning = false, cleanedBytes = cleanBytes
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCleaning = false, error = e.message, cleanedBytes = 0L)
            }
        }
    }
}
