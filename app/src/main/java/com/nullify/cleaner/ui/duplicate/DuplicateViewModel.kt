package com.nullify.cleaner.ui.duplicate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullify.cleaner.domain.model.DuplicateGroup
import com.nullify.cleaner.domain.usecase.ScanDuplicatesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DuplicateUiState(
    val groups: List<DuplicateGroup> = emptyList(),
    val selectedGroupIds: Set<String> = emptySet(),
    val isScanning: Boolean = false,
    val scannedBytes: Long = 0L,
    val error: String? = null
)

class DuplicateViewModel(
    private val scanDuplicatesUseCase: ScanDuplicatesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DuplicateUiState())
    val uiState: StateFlow<DuplicateUiState> = _uiState.asStateFlow()

    fun startScan(paths: List<String> = listOf(
        "/storage/emulated/0/DCIM",
        "/storage/emulated/0/Download",
        "/storage/emulated/0/Pictures"
    )) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, error = null, groups = emptyList())
            try {
                scanDuplicatesUseCase.scan(paths).collect { progress ->
                    _uiState.value = _uiState.value.copy(
                        scannedBytes = progress.bytesFound,
                        isScanning = !progress.isComplete,
                        groups = if (progress.isComplete) progress.duplicateGroups else _uiState.value.groups
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isScanning = false, error = e.message)
            }
        }
    }

    fun toggleGroup(groupId: String) {
        val current = _uiState.value.selectedGroupIds.toMutableSet()
        if (groupId in current) current.remove(groupId) else current.add(groupId)
        _uiState.value = _uiState.value.copy(selectedGroupIds = current)
    }
}
