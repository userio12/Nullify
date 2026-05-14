package com.nullify.cleaner.domain.repository

import com.nullify.cleaner.domain.model.DuplicateGroup
import com.nullify.cleaner.domain.model.FileItem
import kotlinx.coroutines.flow.Flow

interface DuplicateRepository {
    suspend fun findDuplicates(files: Flow<FileItem>): Flow<DuplicateGroup>
    suspend fun computeHash(filePath: String): String?
    suspend fun computePartialHash(filePath: String): String?
}
