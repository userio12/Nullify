package com.nullify.cleaner.ui.appmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullify.cleaner.domain.model.AppInfo
import com.nullify.cleaner.domain.repository.AppRepository
import com.nullify.cleaner.domain.usecase.GetAppListUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SortOrder { NAME_ASC, NAME_DESC, SIZE_DESC, SIZE_ASC }

data class AppManagerUiState(
    val apps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.NAME_ASC,
    val isLoading: Boolean = true,
    val showSystemApps: Boolean = false,
    val actionMessage: String? = null,
    val error: String? = null
)

class AppManagerViewModel(
    private val getAppListUseCase: GetAppListUseCase,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppManagerUiState())
    val uiState: StateFlow<AppManagerUiState> = _uiState.asStateFlow()

    private var allApps: List<AppInfo> = emptyList()

    init { loadApps() }

    fun loadApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val rawApps = withContext(Dispatchers.IO) { getAppListUseCase.getAllApps(_uiState.value.showSystemApps) }
                allApps = withContext(Dispatchers.IO) {
                    rawApps.map { app ->
                        val sizes = appRepository.getPackageSize(app.packageName)
                        app.copy(appSize = sizes.first, dataSize = sizes.second, cacheSize = sizes.third,
                            totalSize = sizes.first + sizes.second + sizes.third)
                    }
                }
                applySortAndFilter()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load apps")
            }
        }
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = order)
        applySortAndFilter()
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applySortAndFilter()
    }

    fun toggleSystemApps() {
        _uiState.value = _uiState.value.copy(showSystemApps = !_uiState.value.showSystemApps)
        loadApps()
    }

    private fun applySortAndFilter() {
        val query = _uiState.value.searchQuery
        val order = _uiState.value.sortOrder
        val filtered = if (query.isBlank()) allApps else allApps.filter {
            it.appName.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true)
        }
        val sorted = when (order) {
            SortOrder.NAME_ASC -> filtered.sortedBy { it.appName.lowercase() }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.appName.lowercase() }
            SortOrder.SIZE_DESC -> filtered.sortedByDescending { it.totalSize }
            SortOrder.SIZE_ASC -> filtered.sortedBy { it.totalSize }
        }
        _uiState.value = _uiState.value.copy(apps = sorted, isLoading = false)
    }

    fun forceStop(pkg: String) { launchAction { appRepository.forceStopPackage(pkg) } }
    fun clearCache(pkg: String) { launchAction { appRepository.clearAppCache(pkg) } }
    fun clearData(pkg: String) { launchAction { appRepository.clearAppData(pkg) } }
    fun uninstall(pkg: String) { launchAction { appRepository.uninstallPackage(pkg) } }

    private fun launchAction(block: suspend () -> Boolean) {
        viewModelScope.launch {
            try { withContext(Dispatchers.IO) { block() } } catch (_: Exception) {}
            loadApps()
        }
    }
}
