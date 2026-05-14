package com.nullify.cleaner.ui.analyzer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullify.cleaner.domain.cleaner.StorageAnalyzer
import com.nullify.cleaner.domain.model.FileItem
import com.nullify.cleaner.domain.usecase.AnalyzeStorageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AnalyzerUiState(
    val roots: List<StorageAnalyzer.StorageNode> = emptyList(),
    val volumes: List<StorageAnalyzer.VolumeInfo> = emptyList(),
    val topFiles: List<FileItem> = emptyList(),
    val currentNode: StorageAnalyzer.StorageNode? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class AnalyzerViewModel(
    private val analyzeStorageUseCase: AnalyzeStorageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyzerUiState())
    val uiState: StateFlow<AnalyzerUiState> = _uiState.asStateFlow()

    init { loadAnalysis() }

    fun loadAnalysis() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val (volumes, tree, topFiles) = withContext(Dispatchers.IO) {
                    val v = analyzeStorageUseCase.getVolumes()
                    val t = analyzeStorageUseCase.buildTree(v.firstOrNull()?.path ?: "/storage/emulated/0")
                    val f = analyzeStorageUseCase.getTopFiles(v.firstOrNull()?.path ?: "/storage/emulated/0", 100)
                    Triple(v, t, f)
                }
                _uiState.value = _uiState.value.copy(volumes = volumes, currentNode = tree, roots = if (tree != null) listOf(tree) else emptyList(), topFiles = topFiles, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to analyze storage")
            }
        }
    }

    fun navigateTo(path: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val (tree, topFiles) = withContext(Dispatchers.IO) {
                    val t = analyzeStorageUseCase.buildTree(path)
                    val f = analyzeStorageUseCase.getTopFiles(path, 100)
                    Pair(t, f)
                }
                _uiState.value = _uiState.value.copy(currentNode = tree, topFiles = topFiles, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to navigate")
            }
        }
    }
}
