package com.nullify.cleaner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_files")
data class DeletedFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalPath: String,
    val backupPath: String,
    val fileSize: Long,
    val toolType: String,
    val deletedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86_400_000L
)
