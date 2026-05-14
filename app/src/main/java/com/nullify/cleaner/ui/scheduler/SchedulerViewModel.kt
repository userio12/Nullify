package com.nullify.cleaner.ui.scheduler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullify.cleaner.domain.model.Schedule
import com.nullify.cleaner.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SchedulerUiState(
    val schedules: List<Schedule> = emptyList(),
    val showCreateDialog: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class SchedulerViewModel(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchedulerUiState())
    val uiState: StateFlow<SchedulerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            scheduleRepository.observeSchedules().collect { schedules ->
                _uiState.value = _uiState.value.copy(schedules = schedules, isLoading = false)
            }
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun createSchedule(name: String, intervalHours: Int, toolTypes: List<String>) {
        viewModelScope.launch {
            scheduleRepository.createSchedule(
                Schedule(name = name, intervalHours = intervalHours, toolTypes = toolTypes)
            )
            _uiState.value = _uiState.value.copy(showCreateDialog = false)
        }
    }

    fun toggleSchedule(schedule: Schedule) {
        viewModelScope.launch {
            scheduleRepository.updateSchedule(schedule.copy(isEnabled = !schedule.isEnabled))
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            scheduleRepository.deleteSchedule(schedule)
        }
    }
}
