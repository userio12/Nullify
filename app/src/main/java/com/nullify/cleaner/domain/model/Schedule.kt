package com.nullify.cleaner.domain.model

data class Schedule(
    val id: Long = 0,
    val name: String,
    val intervalHours: Int,
    val toolTypes: List<String>,
    val isEnabled: Boolean = true,
    val onlyOnWifi: Boolean = false,
    val onlyOnCharging: Boolean = false,
    val lastRunAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
