package com.nullify.cleaner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exclusion_rules")
data class ExclusionRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pattern: String,
    val ruleType: String,
    val toolType: String,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
