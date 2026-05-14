package com.nullify.cleaner.ui.exclusion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullify.cleaner.data.local.dao.ExclusionRuleDao
import com.nullify.cleaner.data.local.entity.ExclusionRuleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExclusionRuleUiState(
    val selectedToolFilter: String = "all",
    val showAddDialog: Boolean = false,
    val newPattern: String = "",
    val newRuleType: String = "path_glob",
    val newToolType: String = "all",
    val error: String? = null
)

class ExclusionRuleViewModel(
    private val exclusionRuleDao: ExclusionRuleDao
) : ViewModel() {

    val rules = exclusionRuleDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(ExclusionRuleUiState())
    val uiState: StateFlow<ExclusionRuleUiState> = _uiState.asStateFlow()

    fun setToolFilter(tool: String) {
        _uiState.value = _uiState.value.copy(selectedToolFilter = tool)
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true, newPattern = "", error = null)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, error = null)
    }

    fun updateNewPattern(pattern: String) {
        _uiState.value = _uiState.value.copy(newPattern = pattern)
    }

    fun updateNewRuleType(type: String) {
        _uiState.value = _uiState.value.copy(newRuleType = type)
    }

    fun updateNewToolType(toolType: String) {
        _uiState.value = _uiState.value.copy(newToolType = toolType)
    }

    fun addRule() {
        val pattern = _uiState.value.newPattern.trim()
        if (pattern.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Pattern cannot be empty")
            return
        }
        viewModelScope.launch {
            exclusionRuleDao.insert(ExclusionRuleEntity(
                pattern = pattern,
                ruleType = _uiState.value.newRuleType,
                toolType = _uiState.value.newToolType
            ))
            _uiState.value = _uiState.value.copy(showAddDialog = false, newPattern = "", error = null)
        }
    }

    fun deleteRule(rule: ExclusionRuleEntity) {
        viewModelScope.launch { exclusionRuleDao.delete(rule) }
    }

    fun toggleRule(rule: ExclusionRuleEntity) {
        viewModelScope.launch {
            exclusionRuleDao.update(rule.copy(isEnabled = !rule.isEnabled))
        }
    }
}
