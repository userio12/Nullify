package com.nullify.cleaner.domain.model

data class CleanupPlan(
    val toolType: String,
    val items: List<CleanupItem>,
    val totalBytes: Long
)

data class CleanupItem(
    val path: String,
    val size: Long,
    val description: String,
    val groupId: String? = null
)

data class CleanProgress(
    val currentItem: String,
    val currentProgress: Int,
    val totalProgress: Int,
    val bytesFound: Long = 0L,
    val bytesCleaned: Long = 0L,
    val isComplete: Boolean = false,
    val error: String? = null,
    val items: List<CleanupItem> = emptyList(),
    val duplicateGroups: List<DuplicateGroup> = emptyList()
)
