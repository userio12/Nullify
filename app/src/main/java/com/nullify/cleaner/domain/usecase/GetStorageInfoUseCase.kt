package com.nullify.cleaner.domain.usecase

import com.nullify.cleaner.domain.model.StorageInfo
import com.nullify.cleaner.domain.repository.StorageRepository

class GetStorageInfoUseCase(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(): StorageInfo = storageRepository.getStorageInfo()
}
