package com.nullify.cleaner.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullify.cleaner.data.local.dao.CleanupLogDao
import com.nullify.cleaner.domain.model.StorageInfo
import com.nullify.cleaner.domain.usecase.GetStorageInfoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DashboardUiState(
    val storageInfo: StorageInfo? = null,
    val lastRefreshTime: Long = 0L,
    val isLoading: Boolean = true,
    val error: String? = null
)

class DashboardViewModel(
    private val getStorageInfoUseCase: GetStorageInfoUseCase,
    private val cleanupLogDao: CleanupLogDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val totalCleanups = cleanupLogDao.observeTotalCleanups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalBytesFreedFlow = cleanupLogDao.observeTotalBytesFreed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val recentCleanups = cleanupLogDao.observeRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var refreshJob: Job? = null

    init { refresh(); startAutoRefresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val info = withContext(Dispatchers.IO) { getStorageInfoUseCase() }
                _uiState.value = _uiState.value.copy(storageInfo = info, isLoading = false, lastRefreshTime = System.currentTimeMillis())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load storage info")
            }
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(30_000L)
                try { _uiState.value = _uiState.value.copy(storageInfo = withContext(Dispatchers.IO) { getStorageInfoUseCase() }, lastRefreshTime = System.currentTimeMillis()) }
                catch (_: Exception) {}
            }
        }
    }
}
