package com.nullify.cleaner.data.repository

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nullify.cleaner.data.local.dao.CleanupLogDao
import com.nullify.cleaner.data.local.dao.ScheduleDao
import com.nullify.cleaner.data.local.entity.ScheduleEntity
import com.nullify.cleaner.domain.model.Schedule
import com.nullify.cleaner.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class ScheduleRepositoryImpl(
    private val scheduleDao: ScheduleDao,
    private val cleanupLogDao: CleanupLogDao,
    private val workManager: WorkManager
) : ScheduleRepository {

    override fun observeSchedules(): Flow<List<Schedule>> =
        scheduleDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun createSchedule(schedule: Schedule): Long {
        val id = scheduleDao.insert(schedule.toEntity())
        if (schedule.isEnabled) scheduleWork(id, schedule)
        return id
    }

    override suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.update(schedule.toEntity())
        if (schedule.isEnabled) {
            scheduleWork(schedule.id, schedule)
        } else {
            cancelWork(schedule.id)
        }
    }

    override suspend fun deleteSchedule(schedule: Schedule) {
        scheduleDao.delete(schedule.toEntity())
        cancelWork(schedule.id)
    }

    override suspend fun markRunCompleted(scheduleId: Long) {
        scheduleDao.updateLastRun(scheduleId, System.currentTimeMillis())
    }

    override suspend fun getEnabledSchedules(): List<Schedule> {
        return scheduleDao.observeAll().map { list -> list.map { it.toDomain() } }.first()
    }

    private fun scheduleWork(id: Long, schedule: Schedule) {
        val workRequest = PeriodicWorkRequestBuilder<com.nullify.cleaner.service.CleanupWorker>(
            schedule.intervalHours.toLong(), TimeUnit.HOURS
        ).setConstraints(
            androidx.work.Constraints.Builder().apply {
                if (schedule.onlyOnWifi) setRequiredNetworkType(androidx.work.NetworkType.UNMETERED)
                if (schedule.onlyOnCharging) setRequiresCharging(true)
            }.build()
        ).addTag("cleanup_$id").build()

        workManager.enqueueUniquePeriodicWork(
            "cleanup_schedule_$id",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun cancelWork(id: Long) {
        workManager.cancelUniqueWork("cleanup_schedule_$id")
    }

    private fun ScheduleEntity.toDomain() = Schedule(
        id = id,
        name = name,
        intervalHours = intervalHours,
        toolTypes = toolTypes.split(",").map { it.trim() },
        isEnabled = isEnabled,
        onlyOnWifi = onlyOnWifi,
        onlyOnCharging = onlyOnCharging,
        lastRunAt = lastRunAt,
        createdAt = createdAt
    )

    private fun Schedule.toEntity() = ScheduleEntity(
        id = id,
        name = name,
        intervalHours = intervalHours,
        toolTypes = toolTypes.joinToString(","),
        isEnabled = isEnabled,
        onlyOnWifi = onlyOnWifi,
        onlyOnCharging = onlyOnCharging,
        lastRunAt = lastRunAt,
        createdAt = createdAt
    )
}
