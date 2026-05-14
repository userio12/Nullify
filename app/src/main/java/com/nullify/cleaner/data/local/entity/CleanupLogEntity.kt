package com.nullify.cleaner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cleanup_logs")
data class CleanupLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val toolType: String,
    val filesDeleted: Int,
    val bytesFreed: Long,
    val durationMs: Long,
    val modeUsed: String,
    val timestamp: Long = System.currentTimeMillis()
)
