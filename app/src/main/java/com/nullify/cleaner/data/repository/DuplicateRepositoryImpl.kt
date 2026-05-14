package com.nullify.cleaner.data.repository

import com.nullify.cleaner.domain.cleaner.DuplicateCleaner
import com.nullify.cleaner.domain.model.DuplicateGroup
import com.nullify.cleaner.domain.model.FileItem
import com.nullify.cleaner.domain.repository.DuplicateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class DuplicateRepositoryImpl : DuplicateRepository {

    private val cleaner = DuplicateCleaner()

    override suspend fun findDuplicates(files: Flow<FileItem>): Flow<DuplicateGroup> = flow {
        val fileList = mutableListOf<FileItem>()
        files.collect { fileList.add(it) }
        cleaner.scan(fileList).collect { progress ->
            if (progress.isComplete) {
                progress.duplicateGroups.forEach { emit(it) }
            }
        }
    }

    override suspend fun computeHash(filePath: String): String? = withContext(Dispatchers.IO) {
        cleaner.computeFullHash(filePath)
    }

    override suspend fun computePartialHash(filePath: String): String? = withContext(Dispatchers.IO) {
        cleaner.computePartialHash(filePath)
    }
}
