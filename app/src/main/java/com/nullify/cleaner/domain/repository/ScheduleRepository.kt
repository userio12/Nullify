package com.nullify.cleaner.domain.repository

import com.nullify.cleaner.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun observeSchedules(): Flow<List<Schedule>>
    suspend fun createSchedule(schedule: Schedule): Long
    suspend fun updateSchedule(schedule: Schedule)
    suspend fun deleteSchedule(schedule: Schedule)
    suspend fun markRunCompleted(scheduleId: Long)
    suspend fun getEnabledSchedules(): List<Schedule>
}
