package com.nullify.cleaner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val intervalHours: Int,
    val toolTypes: String,
    val isEnabled: Boolean = true,
    val onlyOnWifi: Boolean = false,
    val onlyOnCharging: Boolean = false,
    val lastRunAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
