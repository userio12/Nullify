package com.nullify.cleaner.di

import com.nullify.cleaner.domain.cleaner.CorpseFinder
import com.nullify.cleaner.domain.cleaner.DuplicateCleaner
import com.nullify.cleaner.domain.cleaner.JunkCleaner
import com.nullify.cleaner.domain.cleaner.StorageAnalyzer
import com.nullify.cleaner.domain.mode.BasicMode
import com.nullify.cleaner.domain.mode.ExecutionMode
import com.nullify.cleaner.domain.mode.RootMode
import com.nullify.cleaner.domain.mode.ShizukuMode
import com.nullify.cleaner.domain.usecase.AnalyzeStorageUseCase
import com.nullify.cleaner.domain.usecase.ExecuteCleanupUseCase
import com.nullify.cleaner.domain.usecase.GetAppListUseCase
import com.nullify.cleaner.domain.usecase.GetStorageInfoUseCase
import com.nullify.cleaner.domain.usecase.ScanCorpsesUseCase
import com.nullify.cleaner.domain.usecase.ScanDuplicatesUseCase
import com.nullify.cleaner.domain.usecase.ScanJunkUseCase
import org.koin.dsl.module

val domainModule = module {
    single<ExecutionMode> {
        RootMode().takeIf { it.isAvailable }
            ?: ShizukuMode().takeIf { it.isAvailable }
            ?: BasicMode()
    }

    single { CorpseFinder(get()) }
    single { JunkCleaner(get()) }
    single { DuplicateCleaner() }
    single { StorageAnalyzer() }

    factory { GetStorageInfoUseCase(get()) }
    factory { ScanCorpsesUseCase(get()) }
    factory { ScanJunkUseCase(get()) }
    factory { ScanDuplicatesUseCase(get(), get()) }
    factory { AnalyzeStorageUseCase(get()) }
    factory { ExecuteCleanupUseCase(get()) }
    factory { GetAppListUseCase(get()) }
}
