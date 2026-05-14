package com.nullify.cleaner.ui.recyclebin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullify.cleaner.data.local.dao.DeletedFileDao
import com.nullify.cleaner.data.local.entity.DeletedFileEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecycleBinViewModel(
    private val deletedFileDao: DeletedFileDao
) : ViewModel() {

    val deletedFiles = deletedFileDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restoreFile(file: DeletedFileEntity) {
        viewModelScope.launch {
            runCatching {
                val backup = java.io.File(file.backupPath)
                val original = java.io.File(file.originalPath)
                original.parentFile?.mkdirs()
                backup.copyTo(original, overwrite = true)
                backup.delete()
                deletedFileDao.deleteById(file.id)
            }
        }
    }

    fun permanentlyDelete(file: DeletedFileEntity) {
        viewModelScope.launch {
            runCatching {
                java.io.File(file.backupPath).delete()
                deletedFileDao.deleteById(file.id)
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            deletedFiles.value.forEach { file ->
                java.io.File(file.backupPath).delete()
                deletedFileDao.deleteById(file.id)
            }
        }
    }
}
